package server;

import client.ClientIF;
import common.User;

/**
 * Wrapper with a User instance and a client interface.
 */
public class ChatClient {

	public User user;
	public ClientIF client;

	// constructor
	public ChatClient(User user, ClientIF client) {
		this.user = user;
		this.client = client;
	}
}
