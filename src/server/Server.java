package server;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;

import client.ClientIF;
import server.model.*;
import common.*;

public class Server extends UnicastRemoteObject implements ServerIF {
	String line = "---------------------------------------------\n";
	private Vector<ChatClient> chatters;
	private static final long serialVersionUID = 1L;

	private Hashtable<Integer, ChatClient> onlineUsers;

	// asynchronized thread pool
	private ExecutorService threadPool = Executors.newFixedThreadPool(12);

	// Constructor
	public Server() throws RemoteException {
		super();
		chatters = new Vector<ChatClient>(10, 1);
		onlineUsers = new Hashtable<>();
	}

	// -----------------------------------------------------------
	/**
	 * LOCAL METHODS
	 */
	public static void main(String[] args) {
		startRMIRegistry();
		String hostName = "localhost";
		String serviceName = "GroupChatService";

		if (args.length == 2) {
			hostName = args[0];
			serviceName = args[1];
		}

		try {
			ServerIF server = new Server();
			Naming.rebind("rmi://" + hostName + "/" + serviceName, server);
			System.out.println("Group Chat RMI Server is running...");
		} catch (Exception e) {
			System.out.println("Server had problems starting");
		}
	}

	/**
	 * Start the RMI Registry
	 */
	public static void startRMIRegistry() {
		try {
			java.rmi.registry.LocateRegistry.createRegistry(1099);
			System.out.println("RMI Server ready");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Update all clients by remotely invoking their updateUserList RMI method
	 */
	private void updateUserList() {
		threadPool.submit(new Callable<Void>() {
			public Void call() throws Exception {
				String[] currentUsers = getUserList();
				for (ChatClient c : chatters) {
					// try {
					// c.getClient().updateUserList(currentUsers);
					// } catch (RemoteException e) {
					// System.out.println("Warning: failed to update user list of " + c.name);
					// }
				}
				return null;
			}
		});
	}

	/**
	 * generate a String array of current users
	 *
	 * @return
	 */
	private String[] getUserList() {
		// generate an array of current users
		String[] allUsers = new String[chatters.size()];
		for (int i = 0; i < allUsers.length; i++) {
			allUsers[i] = chatters.elementAt(i).getName();
		}
		return allUsers;
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

	/**
	 * Send a message to all users
	 *
	 * @param newMessage
	 */
	public void sendToAll(String newMessage) {
		threadPool.submit(new Callable<Void>() {
			public Void call() throws Exception {
				for (ChatClient c : chatters) {
					try {
						c.client.messageFromServer(newMessage);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
				return null;
			}
		});
	}

	// -----------------------------------------------------------
	/*
	 * REMOTE METHODS
	 */

	/**
	 * Send a string ( the latest post, mostly ) to all connected clients
	 */
	public void updateChat(String name, String nextPost) throws RemoteException {
		String message = name + " : " + nextPost + "\n";
		sendToAll(message);
	}

	/**
	 * remove a client from the list, notify everyone
	 */
	@Override
	public void leaveChat(String userName) throws RemoteException {

		for (ChatClient c : chatters) {
			if (c.getName().equals(userName)) {
				System.out.println(line + userName + " left the chat session");
				System.out.println(new Date(System.currentTimeMillis()));
				chatters.remove(c);
				break;
			}
		}
		if (!chatters.isEmpty()) {
			updateUserList();
		}
	}

	/**
	 * A method to send a private message to selected clients The integer array
	 * holds the indexes (from the chatters vector) of the clients to send the
	 * message to
	 */
	@Override
	public void sendPM(int[] privateGroup, String privateMessage) throws RemoteException {
		threadPool.submit(new Callable<Void>() {
			public Void call() throws Exception {
				ChatClient pc;
				for (int i : privateGroup) {
					pc = chatters.elementAt(i);
					try {
						pc.client.messageFromServer(privateMessage);
					} catch (RemoteException e) {
						System.out.println("Warning: failed to send message to " + pc.getName());
					}
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

		// Generate session for the user
		Session.createSession(user);
		return user;
	}

	@Override
	public int register(String userName, String password) throws RemoteException {
		UserModel user = new UserModel();
		user.userName = userName;
		user.password = password;
		try {
			user.create();
			return user.uid;
		} catch (DuplicatedObjectException e) {
			return 0;
		}
	}

	@Override
	public void joinGroup(int groupNumber, User user) throws RemoteException, DuplicatedObjectException {
		ChatRoomModel chatroom = ChatRoomModel.getGroupChat(groupNumber);
		chatroom.addUser(user);
		pushChatRoom(onlineUsers.get(user.uid), chatroom);

		// Announce the entered user to all members in the chatroom
		try {
			sendMessage(UserModel.SYSTEM_USER, chatroom.cid,
					String.format("User %s has entered the chat room.", user.userName));
		} catch (InvalidSessionException e) {
			System.out.println("Warning: Invalid session for SYSTEM_USER");
		}
	}

	@Override
	public void sendMessage(User user, int cid, String message) throws RemoteException, InvalidSessionException {
		ChatMessageModel messageModel = new ChatMessageModel(cid, user, message);
		pushMessage(messageModel);
	}
}
