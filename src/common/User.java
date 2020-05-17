package common;

import java.io.Serializable;

public class User implements Serializable {
	private static final long serialVersionUID = -4128101032767815161L;

	public long sessionId;
	public Integer uid;
	public String userName;

	public User(Integer uid, String userName) {
		this.uid = uid;
		this.userName = userName;
	}
}
