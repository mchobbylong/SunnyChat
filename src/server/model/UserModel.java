package server.model;

import server.Database;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserModel {
	public int uid;
	public String userName;
	public String password;
	
	public UserModel(String userName, String password) {
		this.userName = userName;
		this.password = password;
	}
	public UserModel(int uid, String userName, String password) {
		this(userName, password);
		this.uid = uid;
	}
	
	public static UserModel getByNameAndPassword(Database db, String userName, String password) {
		String sql = "select uid, username, password from user where username='" + userName + "' and password='" + password + "'";
		try {
			ResultSet rs = db.query(sql);
			while (rs.next()) {
				return new UserModel(rs.getInt(1), rs.getString(2), rs.getString(3));
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
