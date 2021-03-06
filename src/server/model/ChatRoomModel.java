package server.model;

import server.DatabaseHelper;

import java.util.ArrayList;
import java.util.HashMap;

import common.*;

/**
 * Database model of ChatRoom
 *
 * @see ChatRoom
 */
public class ChatRoomModel {
	public int cid;

	// Type of the ChatRoom
	// 0 - Private Chat
	// 1 - Group Chat
	public int type;

	// Group number (if it is a group ChatRoom, or null)
	public Integer groupNumber;

	/**
	 * Create a new chat room. Must call method create() later.
	 */
	public ChatRoomModel() {
		// Do nothing
	}

	/**
	 * Get a room by room id.
	 *
	 * @param cid Room id
	 */
	public ChatRoomModel(int cid) throws ObjectNotFoundException {
		// Check whether the room exists
		String sql = String.format("select cid, type, group_number from chatroom where cid=%d", cid);
		HashMap<String, Object> row = DatabaseHelper.queryFirst(sql);
		if (row == null) {
			throw new ObjectNotFoundException(String.format("The chatroom with cid of %d is not found.", cid));
		}
		this.cid = cid;
		this.type = (int) row.get("type");
		this.groupNumber = (Integer) row.get("group_number");
	}

	/**
	 * Get a new private chat room with two users in it.
	 *
	 * @param uid1
	 * @param uid2
	 */
	public ChatRoomModel(int uid1, int uid2) {
		String sql = "insert into chatroom (type) values (0)";
		cid = DatabaseHelper.insert(sql);
		type = 0;
		groupNumber = null;
		sql = String.format("insert into chatroom_user (cid, uid) values (%d, %d), (%d, %d)", cid, uid1, cid, uid2);
		DatabaseHelper.execute(sql);
	}

	/**
	 * Get a room by given group number. If the room does not exist, then create
	 * one.
	 *
	 * @param groupNumber A group number.
	 * @return A ChatRoomModel instance.
	 */
	public static ChatRoomModel getGroupChat(int groupNumber) {
		ChatRoomModel room = new ChatRoomModel();
		room.groupNumber = groupNumber;
		room.type = 1;
		// Check whether the room exists
		String sql = String.format("select cid from chatroom where group_number=%d", groupNumber);
		HashMap<String, Object> row = DatabaseHelper.queryFirst(sql);
		if (row == null) {
			// If not, then create the room
			sql = String.format("insert into chatroom (type, group_number) values (1, %d)", groupNumber);
			room.cid = DatabaseHelper.insert(sql);
		} else {
			room.cid = (int) row.get("cid");
		}
		return room;
	}

	/**
	 * Load all ChatRoomModels with given user in them.
	 *
	 * @param uid User id.
	 * @return A list of ChatRoomModels
	 */
	public static ArrayList<ChatRoomModel> getUserRelatedRooms(int uid) {
		ArrayList<ChatRoomModel> rooms = new ArrayList<>();
		String sql = String.format("select cid from chatroom_user where uid=%d", uid);
		ArrayList<HashMap<String, Object>> rows = DatabaseHelper.queryAll(sql);
		for (HashMap<String, Object> row : rows) {
			int cid = (int) row.get("cid");
			try {
				rooms.add(new ChatRoomModel(cid));
			} catch (ObjectNotFoundException e) {
				System.out.println(
						String.format("Warning: failed to retrieve chatroom %d in getUserRelatedRooms().", cid));
			}
		}
		return rooms;
	}

	/**
	 * Convert a ChatRoomModel to a serializable ChatRoom instance.
	 *
	 * @return A ChatRoom instance
	 */
	public ChatRoom getInstance() {
		return new ChatRoom(cid, type, groupNumber);
	}

	/**
	 * Get room title in the given user's identity according to the room's type and
	 * members.
	 *
	 * @return Title of this ChatRoom
	 */
	public String getTitle(int uid) {
		String sql;
		if (type == 0) {
			sql = String.format("select username from chatroom_user natural join user where cid=%d and uid<>%d", cid,
					uid);
			HashMap<String, Object> row = DatabaseHelper.queryFirst(sql);
			if (row != null)
				return (String) row.get("username");
			return "Invalid Private Chat";
		}
		return String.format("Room %d", groupNumber);
	}

	/**
	 * Add a user into the room.
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
	}

	/**
	 * Check if two users are in a same private chat room (or, whether they are
	 * "friends").
	 *
	 * @param uid1
	 * @param uid2
	 * @return A boolean that indicates whether the two users are in the same
	 *         private chat room.
	 */
	public static boolean isFriend(int uid1, int uid2) {
		String sql = String.format("select count(*) as count "
				+ "from (chatroom natural join chatroom_user as t1) inner join chatroom_user as t2 on t1.cid=t2.cid "
				+ "where t1.uid=%d and t2.uid=%d and chatroom.type=0", uid1, uid2);
		HashMap<String, Object> row = DatabaseHelper.queryFirst(sql);
		return (long) row.get("count") > 0;
	}
}
