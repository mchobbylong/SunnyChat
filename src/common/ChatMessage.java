package common;

import java.util.Date;
import java.text.SimpleDateFormat;

public class ChatMessage {
	public int cid;
	public int fromUID;
	public String fromName;
	public String time;
	public String message;

	public ChatMessage(int cid, int fromUID, String fromName, String message, String time) {
		this.cid = cid;
		this.fromUID = fromUID;
		this.fromName = fromName;
		this.message = message;
		this.time = time;
	}
	public ChatMessage(int cid, int fromUID, String fromName, String message) {
		this(cid, fromUID, fromName, message, getCurrentTime());
	}

	public static String getCurrentTime() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	}
}
