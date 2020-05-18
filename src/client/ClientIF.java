package client;

import java.rmi.Remote;
import java.rmi.RemoteException;

import common.*;

public interface ClientIF extends Remote {

	public void messageFromServer(String message) throws RemoteException;

	public void receiveChatRoom(ChatRoom room) throws RemoteException;

	public void receiveMessage(ChatMessage message) throws RemoteException;
}
