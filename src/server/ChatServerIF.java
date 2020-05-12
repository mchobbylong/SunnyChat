package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import common.InvalidSessionException;
import common.User;
import client.ChatClient3IF;

/**
 * Server RMI interface
 *
 * @author Daragh Walshe B00064428 RMI Assignment 2 April 2015
 *
 */
public interface ChatServerIF extends Remote {
	public User login(String userName, String password, ChatClient3IF client) throws RemoteException;

	public int register(String userName, String password) throws RemoteException;

	public void updateChat(String userName, String chatMessage) throws RemoteException, InvalidSessionException;

	public void leaveChat(String userName) throws RemoteException, InvalidSessionException;

	public void sendPM(int[] privateGroup, String privateMessage) throws RemoteException, InvalidSessionException;
}
