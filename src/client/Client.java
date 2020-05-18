package client;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
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
	private String hostName = "localhost";
	private String serviceName = "GroupChatService";
	protected ServerIF serverIF;
	protected boolean connectionProblem = false;

	public Client() throws Exception {
		super();
		serverIF = (ServerIF) Naming.lookup("rmi://" + hostName + "/" + serviceName);
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
		} catch (RemoteException e) {
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

	public void updateChat(String message) {
		try {
			serverIF.updateChat(me.userName, message);
		} catch (RemoteException | InvalidSessionException e) {
			raiseFatalError(e);
		}
	}

	public void sendPM(int[] privateList, String message) {
		try {
			serverIF.sendPM(privateList, message);
		} catch (RemoteException | InvalidSessionException e) {
			raiseFatalError(e);
		}
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

	// =====================================================================
	/**
	 * Receive a string from the chat server this is the clients RMI method, which
	 * will be used by the server to send messages to us
	 */
	@Override
	public void messageFromServer(String message) throws RemoteException {
		// System.out.println(message);
		// guiLock.readLock().lock();
		// try {
		// chatGUI.textArea.append(message);
		// // make the gui display the last appended text, ie scroll to bottom
		// chatGUI.textArea.setCaretPosition(chatGUI.textArea.getDocument().getLength());
		// } finally {
		// guiLock.readLock().unlock();
		// }
	}

	// /**
	// * A method to update the display of users currently connected to the server
	// */
	// @Override
	// public void updateUserList(String[] currentUsers) throws RemoteException {
	// guiLock.readLock().lock();
	// try {
	// chatGUI.userPanel.remove(chatGUI.clientPanel);
	// chatGUI.setClientPanel(currentUsers);
	// chatGUI.clientPanel.repaint();
	// chatGUI.clientPanel.revalidate();
	// } finally {
	// guiLock.readLock(W).unlock();
	// }
	// }

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

	public void raiseFatalError(Exception e) {
		JOptionPane.showMessageDialog(chatGUI, e.getCause().getMessage(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}

	public static void main(String[] args) {
		try {
			Client client = new Client();
			new ClientLoginGUI(client);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "The server is currently unavailable, please try again later!", "Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

}// end class