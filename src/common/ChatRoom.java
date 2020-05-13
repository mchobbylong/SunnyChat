package common;

import java.io.Serializable;
import java.util.ArrayList;

public class ChatRoom implements Serializable {
	private static final long serialVersionUID = 1L;

	public int cid;
	public ArrayList<User> users;
}
