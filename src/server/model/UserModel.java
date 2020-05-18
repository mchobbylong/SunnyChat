package server.model;

import server.DatabaseHelper;
import common.*;

import java.util.ArrayList;
import java.util.HashMap;

public class UserModel {
	public int uid;
	public String userName;
	public String password;
	public String lastOnline;

	public static User SYSTEM_USER = new User(null, "System");

	/**
	 * Create a new user. Must call method create() later.
	 */
	public UserModel() {
		// Do nothing
	}

	/**
	 * Get a user by uid.
	 */
	public UserModel(int uid) throws ObjectNotFoundException {
		String sql = String.format("select uid, username, password, last_online from user where uid=%d", uid);
		HashMap<String, Object> row = DatabaseHelper.queryFirst(sql);
		if (row == null) {
			throw new ObjectNotFoundException(String.format("The user with uid of %d is not found.", uid));
		}
		this.uid = uid;
		this.userName = (String) row.get("username");
		this.password = (String) row.get("password");
		this.lastOnline = (String) row.get("last_online");
	}

	/**
	 * Get a user by username and password.
	 *
	 * @param userName
	 * @param password
	 * @throws ObjectNotFoundException
	 */
	public UserModel(String userName, String password) throws ObjectNotFoundException {
		String sql = String.format(
				"select uid, username, password, last_online from user where username ='%s' and password ='%s'",
				userName, password);
		HashMap<String, Object> row = DatabaseHelper.queryFirst(sql);
		if (row == null) {
			throw new ObjectNotFoundException(String.format("The user with uid of %d is not found.", uid));
		}
		this.uid = (int) row.get("uid");
		this.userName = (String) row.get("username");
		this.password = (String) row.get("password");
		this.lastOnline = (String) row.get("last_online");
	}

	/**
	 * Create a user with properties already stored in this model.
	 *
	 * @throws DuplicatedObjectException
	 */
	public void create() throws DuplicatedObjectException {
		// Check duplication first
		String sql = String.format("select count(*) as count from user where username='%s'", userName);
		HashMap<String, Object> row = DatabaseHelper.queryFirst(sql);
		Number count = (Number) row.get("count");
		if (count.intValue() > 0) {
			throw new DuplicatedObjectException(String.format("User with name %s already exists.", userName));
		}

		// Then insert the new user, and get the new uid
		sql = String.format("insert into user (username, password) values ('%s', '%s')", userName, password);
		this.uid = DatabaseHelper.insert(sql);
	}

	public static ArrayList<User> getChatRoomMembers(int cid) {
		ArrayList<User> users = new ArrayList<>();
		String sql = String.format("select uid, username from user natural join chatroom_user where cid=%d", cid);
		ArrayList<HashMap<String, Object>> rows = DatabaseHelper.queryAll(sql);
		for (HashMap<String, Object> row : rows) {
			users.add(new User((int) row.get("uid"), (String) row.get("username")));
		}
		return users;
	}
}
