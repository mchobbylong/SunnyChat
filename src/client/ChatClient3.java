package client;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.JOptionPane;

import server.ChatServerIF;

import common.*;

public class ChatClient3 extends UnicastRemoteObject implements ChatClient3IF {
	private static final long serialVersionUID = 7468891722773409712L;

	ClientRMIGUI chatGUI;
	User me;
	private ReadWriteLock guiLock = new ReentrantReadWriteLock();
	private String hostName = "localhost";
	private String serviceName = "GroupChatService";
	protected ChatServerIF serverIF;
	protected boolean connectionProblem = false;

	public ChatClient3() throws Exception {
		super();
		serverIF = (ChatServerIF) Naming.lookup("rmi://" + hostName + "/" + serviceName);
	}

	public boolean login(String userName, String password) {
		guiLock.writeLock().lock();
		boolean success = false;
		try {
			me = serverIF.login(userName, password, this);
			if (me.uid > 0) {
				chatGUI = new ClientRMIGUI(this);
				success = true;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} finally {
			guiLock.writeLock().unlock();
		}
		return success;
	}

	public boolean register(String userName, String password) {
		try {
			int uid = serverIF.register(userName, password);
			if (uid != 0) {
				return true;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void updateChat(String message) {
		try {
			serverIF.updateChat(me.userName, message);
		} catch (RemoteException | InvalidSessionException e) {
			JOptionPane.showMessageDialog(chatGUI, e.getCause().getMessage(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

	public void sendPM(int[] privateList, String message) {
		try {
			serverIF.sendPM(privateList, message);
		} catch (RemoteException | InvalidSessionException e) {
			JOptionPane.showMessageDialog(chatGUI, e.getCause().getMessage(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

	public void logout() {
		try {
			serverIF.leaveChat(me.userName);
		} catch (RemoteException e) {
			JOptionPane.showMessageDialog(chatGUI, e.getCause().getMessage(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
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
		System.out.println(message);
		guiLock.readLock().lock();
		try {
			chatGUI.textArea.append(message);
			// make the gui display the last appended text, ie scroll to bottom
			chatGUI.textArea.setCaretPosition(chatGUI.textArea.getDocument().getLength());
		} finally {
			guiLock.readLock().unlock();
		}
	}

	/**
	 * A method to update the display of users currently connected to the server
	 */
	@Override
	public void updateUserList(String[] currentUsers) throws RemoteException {
		guiLock.readLock().lock();
		try {
			if (currentUsers.length < 2) {
				chatGUI.privateMsgButton.setEnabled(false);
			}
			chatGUI.userPanel.remove(chatGUI.clientPanel);
			chatGUI.setClientPanel(currentUsers);
			chatGUI.clientPanel.repaint();
			chatGUI.clientPanel.revalidate();
		} finally {
			guiLock.readLock().unlock();
		}
	}

	public static void main(String[] args) {
		try {
			ChatClient3 client = new ChatClient3();
			new ClientLoginGUI(client);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "The server is currently unavailable, please try again later!", "Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

}// end class
