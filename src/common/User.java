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

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof User) {
			Integer objUID = ((User) obj).uid;
			if (this.uid == null && uid == null)
				return true;
			if (this.uid != null)
				return this.uid.intValue() == objUID;
			return objUID.intValue() == this.uid;
		}
		return false;
	}
}
