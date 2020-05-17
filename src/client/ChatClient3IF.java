package client;

import java.rmi.Remote;
import java.rmi.RemoteException;

import common.*;

public interface ChatClient3IF extends Remote {

	public void messageFromServer(String message) throws RemoteException;

	public void receiveChatRoom(ChatRoom room) throws RemoteException;
}
