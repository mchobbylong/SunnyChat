package server;

import java.sql.*;

public class Database {
	private Connection con;
	
	public Database() {
		try {
			Class.forName("org.mariadb.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mariadb://localhost:3306/chat?user=root");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ResultSet query(String sql) throws SQLException {
		Statement sm = con.createStatement();
		return sm.executeQuery(sql);
	}
}
