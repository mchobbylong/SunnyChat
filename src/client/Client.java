package client;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.JOptionPane;

import server.ServerIF;
import common.*;

public class Client extends UnicastRemoteObject implements ClientIF {
	private static final long serialVersionUID = 7468891722773409712L;

	ClientMainGUI chatGUI;
	User me;
	private ReadWriteLock guiLock = new ReentrantReadWriteLock();
	private static String hostName = "localhost";
	private static int registryPort = 1099;
	private static String serviceName = "SunnyChatService";
	protected ServerIF serverIF;
	protected boolean connectionProblem = false;

	public Client() throws Exception {
		super();
		serverIF = (ServerIF) Naming.lookup(String.format("rmi://%s:%d/%s", hostName, registryPort, serviceName));
	}

	public boolean login(String userName, String password) {
		guiLock.writeLock().lock();
		boolean success = false;
		try {
			me = serverIF.login(userName, password, this);
			chatGUI = new ClientMainGUI(this);
			success = true;
		} catch (ObjectNotFoundException e) {
			success = false;
		} catch (RemoteException e) {
			raiseFatalError(e);
		} finally {
			guiLock.writeLock().unlock();
		}
		return success;
	}

	public boolean register(String userName, String password) {
		try {
			serverIF.register(userName, password);
			return true;
		} catch (RemoteException e) {
			raiseFatalError(e);
		} catch (DuplicatedObjectException e) {
			return false;
		}
		return false;
	}

	public boolean joinGroup(int groupNumber) {
		try {
			serverIF.joinGroup(groupNumber, me);
			return true;
		} catch (RemoteException | InvalidSessionException e) {
			raiseFatalError(e);
		} catch (DuplicatedObjectException e) {
			return false;
		}
		return false;
	}

	public void sendMessage(int selectedChatRoomID, String message) {
		try {
			serverIF.sendMessage(me, selectedChatRoomID, message);
		} catch (RemoteException | InvalidSessionException e) {
			raiseFatalError(e);
		}
	}

	public String[] getChatRoomMembers(int cid) {
		try {
			return serverIF.getChatRoomMembers(me, cid);
		} catch (RemoteException | InvalidSessionException e) {
			raiseFatalError(e);
		}
		return new String[] {};
	}

	public ArrayList<User> getOnlineUsers() {
		try {
			return serverIF.getOnlineUsers(me);
		} catch (RemoteException | InvalidSessionException e) {
			raiseFatalError(e);
		}
		return new ArrayList<User>();
	}

	/**
	 * Send an invitation to a online user.
	 *
	 * @param user
	 * @return Status:
	 *         <P/>
	 *         - 0: Success
	 *         <P/>
	 *         - 1: The user is not online
	 *         <P/>
	 *         - 2: The user is already a friend
	 */
	public int sendFriendInvitation(User user) {
		try {
			serverIF.sendFriendInvitation(me, user.uid);
			return 0;
		} catch (RemoteException | InvalidSessionException e) {
			raiseFatalError(e);
		} catch (ObjectNotFoundException e) {
			return 1;
		} catch (DuplicatedObjectException e) {
			return 2;
		}
		return 1;
	}

	public void logout() {
		try {
			serverIF.logout(me);
		} catch (RemoteException e) {
			raiseFatalError(e);
		} catch (InvalidSessionException e) {
			// Do nothing, since the client is closing
		}
	}

	/**
	 * Receive a new ChatRoom from server
	 */
	@Override
	public void receiveChatRoom(ChatRoom room) throws RemoteException {
		guiLock.readLock().lock();
		try {
			chatGUI.addChatRoom(room);
		} finally {
			guiLock.readLock().unlock();
		}
	}

	@Override
	public void receiveMessage(ChatMessage message) throws RemoteException {
		guiLock.readLock().lock();
		try {
			chatGUI.addMessage(message);
		} finally {
			guiLock.readLock().unlock();
		}
	}

	@Override
	public void receiveFriendInvitation(User user) throws RemoteException {
		new Thread(new Runnable() {
			public void run() {
				int result = JOptionPane.showConfirmDialog(null,
						String.format("User %s wants to become your friend, accept or not?", user.userName), "Question",
						JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.NO_OPTION)
					return;
				try {
					serverIF.acceptFriendInvitation(me, user.uid);
				} catch (RemoteException | InvalidSessionException e) {
					raiseFatalError(e);
				}
			}
		}).start();
	}

	public void raiseFatalError(Exception e) {
		JOptionPane.showMessageDialog(null, e.getMessage(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}

	public static void main(String[] args) {
		try {
			Client client;
			if (args.length == 2) {
				hostName = args[0];
				registryPort = Integer.parseInt(args[1]);
				System.out.printf("Get arguments: hostName=%s, registryPort=%d\n", hostName, registryPort);
			}
			client = new Client();
			new ClientLoginGUI(client);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "The server is currently unavailable, please try again later!", "Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

}// end class
