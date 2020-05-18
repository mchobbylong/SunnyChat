package client;

import java.rmi.Remote;
import java.rmi.RemoteException;

import common.*;

public interface ClientIF extends Remote {
	public void receiveChatRoom(ChatRoom room) throws RemoteException;

	public void receiveMessage(ChatMessage message) throws RemoteException;
}
