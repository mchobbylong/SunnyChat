package common;

import java.io.Serializable;

public class ChatMessage implements Serializable {
	private static final long serialVersionUID = -7166042503072155917L;

	public int cid;
	public Integer fromUID;
	public String fromName;
	public String time;
	public String message;

	public ChatMessage(int cid, Integer fromUID, String fromName, String message, String time) {
		this.cid = cid;
		this.fromUID = fromUID;
		this.fromName = fromName;
		this.message = message;
		this.time = time;
	}

	public String displayMessage() {
		return String.format("[%s]: %s\n", fromName, message);
	}
}
