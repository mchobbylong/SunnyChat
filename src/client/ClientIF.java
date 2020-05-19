package client;

import java.rmi.Remote;
import java.rmi.RemoteException;

import common.*;

public interface ClientIF extends Remote {
	public void receiveChatRoom(ChatRoom room) throws RemoteException;

	public void receiveMessage(ChatMessage message) throws RemoteException;

	public void receiveFriendInvitation(User user) throws RemoteException;

	public void receiveFile(String fileName, byte[] fileContent) throws RemoteException;
}
