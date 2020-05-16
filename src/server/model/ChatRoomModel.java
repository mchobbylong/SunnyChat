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
	 * Create a new user. Must call method create() later.
	 */
	public ChatRoomModel() {
		// Do nothing
	}

	/**
	 * Get a room by roomid and insert the user.
	 * @throws ObjectNotFoundException, DuplicatedObjectException 
	 */
	public ChatRoomModel(int cid, int uid) throws ObjectNotFoundException, DuplicatedObjectException {
		String sql = String.format("select cid from chatroom where cid=%d", cid);
		HashMap<String, Object> row_chatroom = DatabaseHelper.queryFirst(sql);
		if (row_chatroom != null) {
			// Check duplication first
			sql = String.format("select uid from chatroom_user where cid=%d", cid);
			HashMap<String, Object> row_roomMembers = DatabaseHelper.queryFirst(sql);
			for (Object user : row_roomMembers.values()) {
				if (uid == Integer.parseInt(user.toString())) {
					throw new DuplicatedObjectException(String.format("User with id %d already exists.", uid));
				}
			}
			// Insert the user
			sql = String.format("insert into chatroom_user (cid, uid) values ('%d', '%d')", cid, uid);
			DatabaseHelper.execute(sql);
		} else {
			throw new ObjectNotFoundException(String.format("The user with uid of %d is not found.", uid));
		}
	}

	/**
	 * Create a room with properties already stored in this model.
	 *
	 * @throws DuplicatedObjectException
	 */
	public void create(int uid) throws DuplicatedObjectException {
		// Check duplication first
		String sql = String.format("select uid from chatroom where cid=%d", cid);
		HashMap<String, Object> row_chatroom = DatabaseHelper.queryFirst(sql);
		for (Object room : row_chatroom.values()) {
			if (cid == Integer.parseInt(room.toString())) {
				throw new DuplicatedObjectException(String.format("Chatroom with id %d already exists.", cid));
			}
		}
		
		// Form a new room
		sql = String.format("insert into chatroom cid values %d", cid);
		DatabaseHelper.execute(sql);
	}
}
