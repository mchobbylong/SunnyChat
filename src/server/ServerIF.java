package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import common.InvalidSessionException;
import common.ObjectNotFoundException;
import common.User;
import common.DuplicatedObjectException;
import client.ClientIF;

public interface ServerIF extends Remote {
	public User login(String userName, String password, ClientIF client)
			throws RemoteException, ObjectNotFoundException;

	public void register(String userName, String password) throws RemoteException, DuplicatedObjectException;

	public void logout(User user) throws RemoteException, InvalidSessionException;

	public void joinGroup(int groupNumber, User user)
			throws RemoteException, InvalidSessionException, DuplicatedObjectException;

	public void sendMessage(User user, int cid, String message) throws RemoteException, InvalidSessionException;
}
