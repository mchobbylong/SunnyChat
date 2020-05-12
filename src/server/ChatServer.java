package server;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.Vector;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;

import client.ChatClient3IF;
import server.exception.ObjectNotFoundException;
import server.model.*;
import common.*;

public class ChatServer extends UnicastRemoteObject implements ChatServerIF {
	String line = "---------------------------------------------\n";
	private Vector<Chatter> chatters;
	private static final long serialVersionUID = 1L;

	private Hashtable<Integer, Chatter> onlineUsers;

	// asynchronized thread pool
	private ExecutorService threadPool = Executors.newFixedThreadPool(12);

	// Constructor
	public ChatServer() throws RemoteException {
		super();
		chatters = new Vector<Chatter>(10, 1);
		onlineUsers = new Hashtable<>();
	}

	// -----------------------------------------------------------
	/**
	 * LOCAL METHODS
	 */
	public static void main(String[] args) {
		DatabaseHelper.init();
		startRMIRegistry();
		String hostName = "localhost";
		String serviceName = "GroupChatService";

		if (args.length == 2) {
			hostName = args[0];
			serviceName = args[1];
		}

		try {
			ChatServerIF server = new ChatServer();
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
	 * Update all clients by remotely invoking their updateUserList RMI method
	 */
	private void updateUserList() {
		threadPool.submit(new Callable<Void>() {
			public Void call() throws Exception {
				String[] currentUsers = getUserList();
				for (Chatter c : chatters) {
					try {
						c.getClient().updateUserList(currentUsers);
					} catch (RemoteException e) {
						System.out.println("Warning: failed to update user list of " + c.name);
					}
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

	/**
	 * Send a message to all users
	 *
	 * @param newMessage
	 */
	public void sendToAll(String newMessage) {
		threadPool.submit(new Callable<Void>() {
			public Void call() throws Exception {
				for (Chatter c : chatters) {
					try {
						c.getClient().messageFromServer(newMessage);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
				return null;
			}
		});
	}

	/**
	 * remove a client from the list, notify everyone
	 */
	@Override
	public void leaveChat(String userName) throws RemoteException {

		for (Chatter c : chatters) {
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
				Chatter pc;
				for (int i : privateGroup) {
					pc = chatters.elementAt(i);
					try {
						pc.getClient().messageFromServer(privateMessage);
					} catch (RemoteException e) {
						System.out.println("Warning: failed to send message to " + pc.name);
					}
				}
				return null;
			}
		});
	}

	@Override
	public User login(String userName, String password, ChatClient3IF client) throws RemoteException {
		try {
			UserModel userModel = new UserModel(userName, password);

			// Add to online user list
			onlineUsers.put(userModel.uid, new Chatter(userName, client));
			chatters.add(new Chatter(userName, client));

			// announce to all chatters (asynchronized)
			updateUserList();
			sendToAll("[Server] : " + userName + " has joined the group.\n");

			User user = new User(userModel.uid, userName);
			Session.createSession(user);
			return user;
		} catch (ObjectNotFoundException e) {
			return new User(0, userName);
		}
	}

}// END CLASS
