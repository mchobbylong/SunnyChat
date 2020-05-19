package server;

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

	// asynchronized thread pool
	private ExecutorService threadPool = Executors.newFixedThreadPool(12);

	// Constructor
	public Server() throws RemoteException {
		super();
		onlineUsers = new Hashtable<>();
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
			System.out.printf("Fatal Error: Failed to get the LAN IP.");
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

	private ArrayList<User> getChatRoomMembers(int cid) {
		return UserModel.getChatRoomMembers(cid);
	}

	private void pushAllChatRooms(UserModel userModel, ClientIF client) {
		threadPool.submit(new Callable<Void>() {
			public Void call() throws Exception {
				for (ChatRoomModel roomModel : ChatRoomModel.getUserRelatedRooms(userModel.uid)) {
					ChatRoom room = roomModel.getInstance();
					room.title = roomModel.getTitle(userModel.uid);
					for (ChatMessage message : ChatMessageModel.getRoomRelatedMessages(room.cid, userModel.lastOnline))
						room.addMessage(message);
					client.receiveChatRoom(room);
				}
				return null;
			}
		});
	}

	private void pushChatRoom(ChatClient c, ChatRoomModel roomModel) {
		if (c == null)
			return;
		threadPool.submit(new Callable<Void>() {
			public Void call() throws Exception {
				ChatRoom room = roomModel.getInstance();
				room.title = roomModel.getTitle(c.user.uid);
				c.client.receiveChatRoom(room);
				return null;
			}
		});
	}

	private void pushMessage(ChatMessageModel messageModel) {
		threadPool.submit(new Callable<Void>() {
			public Void call() throws Exception {
				ChatMessage message = messageModel.getInstance();
				for (User member : getChatRoomMembers(messageModel.cid)) {
					// Check if the member is online
					ChatClient c = onlineUsers.get(member.uid);
					if (c != null)
						c.client.receiveMessage(message);
				}
				return null;
			}
		});
	}

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

	@Override
	public void register(String userName, String password) throws RemoteException, DuplicatedObjectException {
		UserModel user = new UserModel();
		user.userName = userName;
		user.password = password;
		user.create();
		System.out.println(String.format("[%s] User %s is registered.", TimeUtil.getCurrentTime(), userName));
	}

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

	@Override
	public void sendMessage(User user, int cid, String message) throws RemoteException, InvalidSessionException {
		Session.validateSession(user);
		ChatMessageModel messageModel = new ChatMessageModel(cid, user, message);
		pushMessage(messageModel);
	}

	@Override
	public String[] getChatRoomMembers(User user, int cid) throws RemoteException, InvalidSessionException {
		Session.validateSession(user);
		ArrayList<String> names = new ArrayList<>();
		for (User member : getChatRoomMembers(cid))
			names.add(member.userName);
		return (String[]) names.toArray(new String[names.size()]);
	}

	@Override
	public ArrayList<User> getOnlineUsers(User user) throws RemoteException, InvalidSessionException {
		Session.validateSession(user);
		ArrayList<User> users = new ArrayList<>();
		for (ChatClient c : onlineUsers.values())
			if (user.uid.intValue() != c.user.uid)
				users.add(c.user);
		return users;
	}

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
			throw new ObjectNotFoundException(String.format("User with uid of %d is offline.", uid));
		}
	}

	@Override
	public void acceptFriendInvitation(User user, int uid) throws RemoteException, InvalidSessionException {
		Session.validateSession(user);
		ChatRoomModel roomModel = new ChatRoomModel(user.uid, uid);
		pushChatRoom(onlineUsers.get(uid), roomModel);
		sendMessage(UserModel.SERVER_USER, roomModel.cid, "Your friend has accepted the invitation. Chat now!");
		pushChatRoom(onlineUsers.get(user.uid), roomModel);
	}
}
