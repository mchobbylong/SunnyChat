package server.model;

import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import common.User;
import common.ChatMessage;
import server.DatabaseHelper;

public class ChatMessageModel {
	public int mid;
	public int cid;
	public Integer fromUID;
	public String fromName;
	public String message;
	public String time;

	public static User SYSTEM_USER = new User(null, "System");

	/**
	 * Create a new ChatMessage from given properties.
	 */
	public ChatMessageModel(int cid, User user, String message) {
		this.cid = cid;
		fromUID = user.uid;
		fromName = user.userName;
		this.message = message;
		this.time = getCurrentTime();
		String sql = String.format(
				"insert into chatmessage (cid, from_uid, from_name, message, time) values (%d, %d, '%s', '%s', '%s')",
				cid, fromUID, fromName, message, time);
		this.mid = DatabaseHelper.insert(sql);
	}

	public static String getCurrentTime() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	}

	public ChatMessage getInstance() {
		return new ChatMessage(cid, fromUID, fromName, message, time);
	}

	/**
	 * Get ChatRoom related ChatMessages that was sent after lastOnline. If
	 * lastOnline is null, then get all ChatMessages.
	 *
	 * @param cid        ChatRoom id
	 * @param lastOnline Time string
	 * @return ChatMessage instances that related to the given ChatRoom
	 */
	public static ArrayList<ChatMessage> getRoomRelatedMessages(int cid, String lastOnline) {
		ArrayList<ChatMessage> messages = new ArrayList<>();
		String sql = String.format("select from_uid, from_name, message, time from chatmessage where cid=%d", cid);
		if (lastOnline != null)
			sql += String.format(" and time >= '%s'", lastOnline);
		sql += " order by time";
		ArrayList<HashMap<String, Object>> rows = DatabaseHelper.queryAll(sql);
		for (HashMap<String, Object> row : rows) {
			Integer fromUID = (Integer) row.get("from_uid");
			String fromName = (String) row.get("from_name");
			String message = (String) row.get("message");
			String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Timestamp) row.get("time"));
			messages.add(new ChatMessage(cid, fromUID, fromName, message, time));
		}
		return messages;
	}
}
