package server;

import client.ChatClient3IF;
import common.User;

/**
 * A class used by the server program to keep details of connected clients
 * ordered
 *
 * @author Daragh Walshe B00064428 RMI Assignment 2 April 2015
 *
 */
public class ChatClient {

	public User user;
	public ChatClient3IF client;

	// constructor
	public ChatClient(User user, ChatClient3IF client) {
		this.user = user;
		this.client = client;
	}

	public String getName() {
		return user.userName;
	}
}
