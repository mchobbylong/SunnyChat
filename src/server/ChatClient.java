package server;

import client.ClientIF;
import common.User;

public class ChatClient {

	public User user;
	public ClientIF client;

	// constructor
	public ChatClient(User user, ClientIF client) {
		this.user = user;
		this.client = client;
	}

	public String getName() {
		return user.userName;
	}
}
