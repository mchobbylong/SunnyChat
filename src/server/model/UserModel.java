package server.model;

import server.DatabaseHelper;
import server.exception.*;

import java.util.HashMap;

public class UserModel {
	public int uid;
	public String userName;
	public String password;

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
		String sql = String.format("select uid, username, password from user where uid=%d", uid);
		HashMap<String, Object> row = DatabaseHelper.queryFirst(sql);
		if (row != null) {
			this.uid = uid;
			this.userName = (String) row.get("username");
			this.password = (String) row.get("password");
		} else {
			throw new ObjectNotFoundException(String.format("The user with uid of %d is not found.", uid));
		}
	}

	/**
	 * Get a user by username and password.
	 *
	 * @param userName
	 * @param password
	 * @throws ObjectNotFoundException
	 */
	public UserModel(String userName, String password) throws ObjectNotFoundException {
		String sql = String.format("select uid, username, password from user where username ='%s' and password ='%s'",
				userName, password);
		HashMap<String, Object> row = DatabaseHelper.queryFirst(sql);
		if (row != null) {
			this.uid = (int) row.get("uid");
			this.userName = (String) row.get("username");
			this.password = (String) row.get("password");
		} else {
			throw new ObjectNotFoundException(String.format("The user with uid of %d is not found.", uid));
		}
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
}
