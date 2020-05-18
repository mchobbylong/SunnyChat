package server;

import java.rmi.Naming;
import java.rmi.RemoteException;
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

	private Hashtable<Integer, ChatClient> onlineUsers;

	// asynchronized thread pool
	private ExecutorService threadPool = Executors.newFixedThreadPool(12);

	// Constructor
	public Server() throws RemoteException {
		super();
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
}
