package server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;

import client.ClientIF;
import server.model.*;
import common.*;

public class Server extends UnicastRemoteObject implements ServerIF {
	private static final long serialVersionUID = 1L;

	// Server configuration
	private static String hostName = "localhost";
	private static int registryPort = 1099;
	private static int dataPort = 0;
	private static String serviceName = "SunnyChatService";

	private Hashtable<Integer, ChatClient> onlineUsers;
	private Hashtable<Integer, String> roomFiles;

	// asynchronized thread pool
	private ExecutorService threadPool = Executors.newFixedThreadPool(12);

	// Constructor
	public Server() throws RemoteException {
		super();
		onlineUsers = new Hashtable<>();
		roomFiles = new Hashtable<>();
	}

	public static void main(String[] args) {
		// Get arguments
		if (args.length == 3) {
			hostName = args[0];
			registryPort = Integer.parseInt(args[1]);
			dataPort = Integer.parseInt(args[2]);
			System.out.printf("Get arguments: hostName=%s, registryPort=%d, dataPort=%d\n", hostName, registryPort,
					dataPort);
		}

		// Start RMI registry
		String lanHostIP = "";
		try {
			lanHostIP = InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {
			System.out.println("Fatal Error: Failed to get the LAN IP.");
			e.printStackTrace();
			System.exit(1);
		}
		System.setProperty("java.rmi.server.hostname", hostName);
		try {
			RMISocketFactory.setSocketFactory(new SMRMISocket(dataPort));
			java.rmi.registry.LocateRegistry.createRegistry(registryPort);
		} catch (Exception e) {
			System.out.println("Fatal Error: Failed to bind a port to RMI registry.");
			e.printStackTrace();
			System.exit(1);
		}

		try {
			ServerIF server = new Server();
			Naming.rebind(String.format("rmi://%s:%d/%s", lanHostIP, registryPort, serviceName), server);
			System.out.println("Sunny Chat RMI service is now running...");
		} catch (Exception e) {
			System.out.println("Fatal Error: Failed to rebind server remote object.");
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Get all members in the given chat room.
	 *
	 * @param cid A ChatRoom id
	 * @return A list of User instances
	 */
	private ArrayList<User> getChatRoomMembers(int cid) {
		return UserModel.getChatRoomMembers(cid);
	}

	/**
	 * Push all related ChatRooms to a client (just after his login).
	 *
	 * @param userModel The UserModel instance, represent the user
	 * @param client    The client interface
	 */
	private void pushAllChatRooms(UserModel userModel, ClientIF client) {
		threadPool.submit(new Callable<Void>() {
			public Void call() throws Exception {
				for (ChatRoomModel roomModel : ChatRoomModel.getUserRelatedRooms(userModel.uid)) {
					ChatRoom room = roomModel.getInstance();
					room.title = roomModel.getTitle(userModel.uid);
					for (ChatMessage message : ChatMessageModel.getRoomRelatedMessages(room.cid, userModel.lastOnline))
						room.addMessage(message);
					try {
						client.receiveChatRoom(room);
					} catch (RemoteException e) {
						forcedLogout(userModel.uid);
						return null;
					}
				}
				return null;
			}
		});
	}

	/**
	 * Push a single ChatRoom to a client (typically after his joining a group chat)
	 *
	 * @param c         A ChatClient instance
	 * @param roomModel A ChatRoomModel instance, represent the ChatRoom
	 */
	private void pushChatRoom(ChatClient c, ChatRoomModel roomModel) {
		if (c == null)
			return;
		threadPool.submit(new Callable<Void>() {
			public Void call() throws Exception {
				ChatRoom room = roomModel.getInstance();
				room.title = roomModel.getTitle(c.user.uid);
				try {
					c.client.receiveChatRoom(room);
				} catch (RemoteException e) {
					forcedLogout(c.user.uid);
				}
				return null;
			}
		});
	}

	/**
	 * Push a single ChatMessage to all the clients in the corresponding ChatRoom.
	 *
	 * @param messageModel The ChatMessageModel instance
	 */
	private void pushMessage(ChatMessageModel messageModel) {
		threadPool.submit(new Callable<Void>() {
			public Void call() throws Exception {
				ChatMessage message = messageModel.getInstance();
				for (User member : getChatRoomMembers(messageModel.cid)) {
					// Check if the member is online
					ChatClient c = onlineUsers.get(member.uid);
					if (c != null)
						try {
							c.client.receiveMessage(message);
						} catch (RemoteException e) {
							forcedLogout(c.user.uid);
						}
				}
				return null;
			}
		});
	}

	/**
	 * Push the recent uploaded file in a ChatRoom to a client.
	 *
	 * @param user The User instance
	 * @param cid  ChatRoom id
	 */
	private void pushRoomFile(User user, int cid) {
		String filePath = roomFiles.get(cid);
		ChatClient c = onlineUsers.get(user.uid);
		if (filePath != null && c != null) {
			File file = new File(filePath);
			try {
				byte[] rawFile = new byte[(int) file.length()];
				BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
				is.read(rawFile);
				is.close();
				c.client.receiveFile(file.getName(), rawFile);
			} catch (RemoteException e) {
				forcedLogout(user.uid);
			} catch (IOException e) {
				System.out.printf("Warning: Failed to push file %s to user.\n", filePath);
			}
		}
	}

	/**
	 * Force a user to logout (since the broken connection to his client)
	 *
	 * @param uid The User id
	 */
	private void forcedLogout(int uid) {
		ChatClient c = onlineUsers.remove(uid);
		if (c != null)
			System.out.printf("[%s] User %s is forced to log out (due to connection error).\n",
					TimeUtil.getCurrentTime(), c.user.userName);
	}

	/**
	 * Log into the server using given username and password.
	 *
	 * @param userName Username
	 * @param password Password
	 * @param client   The client interface
	 * @return A valid User instance (with session)
	 * @category RemoteMethod
	 */
	@Override
	public User login(String userName, String password, ClientIF client)
			throws RemoteException, ObjectNotFoundException {
		UserModel userModel = new UserModel(userName, password);

		User user = new User(userModel.uid, userName);
		ChatClient c = new ChatClient(user, client);

		// Push all ChatRooms with messages
		pushAllChatRooms(userModel, client);

		// Add to online user list
		onlineUsers.put(userModel.uid, c);

		// Print message
		System.out.println(String.format("[%s] User %s is logged in.", TimeUtil.getCurrentTime(), userName));

		// Generate session for the user
		Session.createSession(user);
		return user;
	}

	/**
	 * Log out the server.
	 *
	 * @param user The User instance (with session)
	 */
	@Override
	public void logout(User user) throws RemoteException, InvalidSessionException {
		Session.validateSession(user);
		Session.destroySession(user);
		onlineUsers.remove(user.uid);
		String currentTime = TimeUtil.getCurrentTime();
		try {
			UserModel userModel = new UserModel(user.uid);
			userModel.lastOnline = currentTime;
			userModel.update();
		} catch (ObjectNotFoundException e) {
			System.out.println(String.format("Warning: %s", e.getCause().getMessage()));
		}
		System.out.println(String.format("[%s] User %s is logged out.", currentTime, user.userName));
	}

	/**
	 * Register a new user using given username and password.
	 *
	 * @param userName Username
	 * @param password Password
	 */
	@Override
	public void register(String userName, String password) throws RemoteException, DuplicatedObjectException {
		UserModel user = new UserModel();
		user.userName = userName;
		user.password = password;
		user.create();
		System.out.println(String.format("[%s] User %s is registered.", TimeUtil.getCurrentTime(), userName));
	}

	/**
	 * Join a group chat.
	 *
	 * @param groupNumber Group number of the group chat
	 * @param user        The User instance (with session)
	 */
	@Override
	public void joinGroup(int groupNumber, User user)
			throws RemoteException, InvalidSessionException, DuplicatedObjectException {
		Session.validateSession(user);
		ChatRoomModel chatroom = ChatRoomModel.getGroupChat(groupNumber);
		chatroom.addUser(user);
		pushChatRoom(onlineUsers.get(user.uid), chatroom);

		// Announce the entered user to all members in the chatroom
		try {
			sendMessage(UserModel.SERVER_USER, chatroom.cid,
					String.format("User %s has entered the chat room.", user.userName));
		} catch (InvalidSessionException e) {
			System.out.println("Warning: Invalid session for SYSTEM_USER");
		}
	}

	/**
	 * Send a message to a ChatRoom.
	 *
	 * @param user    The User instance (with session)
	 * @param cid     ChatRoom id
	 * @param message A message string
	 */
	@Override
	public void sendMessage(User user, int cid, String message) throws RemoteException, InvalidSessionException {
		Session.validateSession(user);

		// Check if the user wants to download a file
		if (message.trim().equals("/getfile")) {
			pushRoomFile(user, cid);
			return;
		}
		ChatMessageModel messageModel = new ChatMessageModel(cid, user, message);
		pushMessage(messageModel);
	}

	/**
	 * Get all members' names in the ChatRoom (typically be used only with group
	 * chats).
	 *
	 * @param user The User instance (with session)
	 * @param cid  ChatRoom id
	 * @return A list of Strings, which are members' names
	 */
	@Override
	public String[] getChatRoomMembers(User user, int cid) throws RemoteException, InvalidSessionException {
		Session.validateSession(user);
		ArrayList<String> names = new ArrayList<>();
		for (User member : getChatRoomMembers(cid))
			names.add(member.userName);
		return (String[]) names.toArray(new String[names.size()]);
	}

	/**
	 * Get all online users (to send friend invitations).
	 *
	 * @param user The User instance (with session)
	 * @return A list of User instances
	 */
	@Override
	public ArrayList<User> getOnlineUsers(User user) throws RemoteException, InvalidSessionException {
		Session.validateSession(user);
		ArrayList<User> users = new ArrayList<>();
		for (ChatClient c : onlineUsers.values())
			if (user.uid.intValue() != c.user.uid)
				users.add(c.user);
		return users;
	}

	/**
	 * Send a friend invitation to an online user.
	 *
	 * @param user The inviter User instance (with session)
	 * @param uid  The invitee user id
	 */
	@Override
	public void sendFriendInvitation(User user, int uid)
			throws RemoteException, InvalidSessionException, DuplicatedObjectException, ObjectNotFoundException {
		Session.validateSession(user);
		// Check if this two users are already friends
		if (ChatRoomModel.isFriend(user.uid, uid))
			throw new DuplicatedObjectException(String.format("User %d and %d are already friends.", user.uid, uid));
		ChatClient invitee = onlineUsers.get(uid);
		if (invitee == null)
			throw new ObjectNotFoundException(String.format("User with uid of %d is offline.", uid));
		User sentUser = new User(user.uid, user.userName);
		try {
			invitee.client.receiveFriendInvitation(sentUser);
		} catch (RemoteException e) {
			forcedLogout(uid);
			throw new ObjectNotFoundException(String.format("User with uid of %d is offline.", uid));
		}
	}

	/**
	 * Accept a friend invitation, and create a private chat room.
	 *
	 * @param user The invitee User instance (with session)
	 * @param uid  The inviter user id
	 */
	@Override
	public void acceptFriendInvitation(User user, int uid) throws RemoteException, InvalidSessionException {
		Session.validateSession(user);
		ChatRoomModel roomModel = new ChatRoomModel(user.uid, uid);
		pushChatRoom(onlineUsers.get(uid), roomModel);
		sendMessage(UserModel.SERVER_USER, roomModel.cid, "Your friend has accepted the invitation. Chat now!");
		pushChatRoom(onlineUsers.get(user.uid), roomModel);
	}

	/**
	 * Upload a file to a ChatRoom.
	 *
	 * @param user        The User instance (with session)
	 * @param cid         ChatRoom id
	 * @param fileName    Name of the uploaded file
	 * @param fileContent Raw bytes of the uploaded file
	 */
	@Override
	public void uploadFile(User user, int cid, String fileName, byte[] fileContent)
			throws RemoteException, InvalidSessionException, IOException {
		Session.validateSession(user);

		// Create directory for this chatroom
		File roomDir = new File(String.format("./uploaded_files/%d", cid));
		if (!roomDir.exists())
			roomDir.mkdirs();

		String filePath = String.format("./uploaded_files/%d/%s", cid, fileName);
		File f = new File(filePath);
		try {
			if (!f.exists())
				f.createNewFile();
			BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(f));
			os.write(fileContent);
			os.close();

			// Store this file into corresponding chat room
			roomFiles.put(cid, filePath);

			// Broadcast an message to the chat room, to notify all users
			sendMessage(UserModel.SERVER_USER, cid, String
					.format("User %s has shared a file '%s', type '/getfile' to download.", user.userName, fileName));
		} catch (IOException e) {
			System.out.printf("Warning: Failed to write file from user %s\n", user.userName);
			e.printStackTrace();
			throw e;
		}
	}
}
