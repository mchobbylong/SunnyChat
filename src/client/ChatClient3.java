package client;
import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.swing.JOptionPane;

import server.ChatServerIF;

import common.*;

/**
 * 
 * @author Daragh Walshe 	B00064428
 * RMI Assignment 2		 	April 2015
 *
 */
public class ChatClient3  extends UnicastRemoteObject implements ChatClient3IF {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7468891722773409712L;
	ClientRMIGUI gui;
	private String hostName = "localhost";
	private String serviceName = "GroupChatService";
	private String clientServiceName;
	private String name;
	private User me;
	protected ChatServerIF serverIF;
	protected boolean connectionProblem = false;

	public ChatClient3() throws RemoteException {
		super();
		try {
			this.serverIF = (ChatServerIF) Naming.lookup("rmi://" + hostName + "/" + serviceName);
		}
		catch (ConnectException e) {
			JOptionPane.showMessageDialog(
				gui.frame,
				"The server is unavailable, please try it later",
				"Connection Error",
				JOptionPane.ERROR_MESSAGE
			);
			
			connectionProblem = true;
			e.printStackTrace();
		}
		catch (NotBoundException | MalformedURLException e) {
			connectionProblem = true;
			e.printStackTrace();
		}
	}
	/**
	 * class constructor,
	 * note may also use an overloaded constructor with 
	 * a port no passed in argument to super
	 * @throws RemoteException
	 */
	public ChatClient3(ClientRMIGUI aChatGUI, String userName) throws RemoteException {
		super();
		this.gui = aChatGUI;
		this.name = userName;
		this.clientServiceName = "ClientListenService_" + userName;
		try {
			this.serverIF = (ChatServerIF) Naming.lookup("rmi://" + hostName + "/" + serviceName);
		}
		catch (ConnectException e) {
			JOptionPane.showMessageDialog(
				gui.frame,
				"The server is unavailable, please try it later",
				"Connection Error",
				JOptionPane.ERROR_MESSAGE
			);
			
			connectionProblem = true;
			e.printStackTrace();
		}
		catch (NotBoundException | MalformedURLException e) {
			connectionProblem = true;
			e.printStackTrace();
		}
	}
	
	
	public boolean login(String userName, String password) {
		try {
			int uid = this.serverIF.login(userName, password);
			if (uid != 0) {
				me = new User(uid, userName);
				return true;
			}
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Register our own listening service/interface
	 * lookup the server RMI interface, then send our details
	 * @throws RemoteException
	 */
	public void startClient() throws RemoteException {		
		String[] details = {name, hostName, clientServiceName};	

		try {
			Naming.rebind("rmi://" + hostName + "/" + clientServiceName, this);
			serverIF = ( ChatServerIF )Naming.lookup("rmi://" + hostName + "/" + serviceName);	
		} 
		catch (ConnectException  e) {
			JOptionPane.showMessageDialog(
					gui.frame, "The server seems to be unavailable\nPlease try later",
					"Connection problem", JOptionPane.ERROR_MESSAGE);
			connectionProblem = true;
			e.printStackTrace();
		}
		catch(NotBoundException | MalformedURLException me){
			connectionProblem = true;
			me.printStackTrace();
		}
		if(!connectionProblem){
			registerWithServer(details);
		}	
		System.out.println("Client Listen RMI Server is running...\n");
	}


	/**
	 * pass our username, hostname and RMI service name to
	 * the server to register out interest in joining the chat
	 * @param details
	 */
	public void registerWithServer(String[] details) {		
		try{
			serverIF.passIDentity(this.ref);//now redundant ??
			serverIF.registerListener(details);			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	//=====================================================================
	/**
	 * Receive a string from the chat server
	 * this is the clients RMI method, which will be used by the server 
	 * to send messages to us
	 */
	@Override
	public void messageFromServer(String message) throws RemoteException {
		System.out.println( message );
		gui.textArea.append( message );
		//make the gui display the last appended text, ie scroll to bottom
		gui.textArea.setCaretPosition(gui.textArea.getDocument().getLength());
	}

	/**
	 * A method to update the display of users 
	 * currently connected to the server
	 */
	@Override
	public void updateUserList(String[] currentUsers) throws RemoteException {

		if(currentUsers.length < 2){
			gui.privateMsgButton.setEnabled(false);
		}
		gui.userPanel.remove(gui.clientPanel);
		gui.setClientPanel(currentUsers);
		gui.clientPanel.repaint();
		gui.clientPanel.revalidate();
	}

}//end class













