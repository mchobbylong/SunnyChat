package server.model;

import server.DatabaseHelper;
import server.exception.*;

import java.util.ArrayList;
import java.util.HashMap;

import common.User;

public class ChatRoomModel {
	public int cid;
	public ArrayList<User> users;

	/**
	 * Create a new chat room. Must call method create() later.
	 */
	public ChatRoomModel() {
		// Do nothing
	}

	/**
	 * Get a room by room id. If the room does not exist, then create the room.
	 *
	 * @param cid Room id
	 */
	public ChatRoomModel(int cid) {
		// Check whether the room exists
		String sql = String.format("select cid from chatroom where cid=%d", cid);
		HashMap<String, Object> row_chatroom = DatabaseHelper.queryFirst(sql);
		if (row_chatroom == null) {
			// If not, then create the room
			sql = String.format("insert into chatroom (cid) values (%d)", cid);
			DatabaseHelper.execute(sql);
		}
		this.cid = cid;
		this.users = new ArrayList<>();
	}

	/**
	 * Add a user into the room
	 *
	 * @param user User instance
	 * @throws DuplicatedObjectException
	 */
	public void addUser(User user) throws DuplicatedObjectException {
		int uid = user.uid;
		// Check duplication first
		String sql = String.format("select count(*) as count from chatroom_user where cid=%d and uid=%d", cid, uid);
		HashMap<String, Object> row = DatabaseHelper.queryFirst(sql);
		long count = (long) row.get("count");
		if (count > 0) {
			throw new DuplicatedObjectException(String.format("Chatroom %d already has the member %d.", cid, uid));
		}

		// Insert the chatroom-user relationship
		sql = String.format("insert into chatroom_user (cid, uid) values (%d, %d)", cid, uid);
		DatabaseHelper.execute(sql);
		users.add(user);
	}
}
