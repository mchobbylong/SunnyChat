package server;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

import org.mariadb.jdbc.MariaDbPoolDataSource;

/**
 * Helper to interact with database.
 */
public class DatabaseHelper {
	private static MariaDbPoolDataSource pool;

	// Database server connection configuration
	private static String serverAddress = "localhost";
	private static String serverPort = "3306";
	private static String userName = "root";
	private static String password = "";
	private static String databaseName = "chat";
	private static String connectionParam = "&minPoolSize=2&maxPoolSize=12";

	static {
		String url = String.format("jdbc:mariadb://%s:%s/%s?user=%s&password=%s%s", serverAddress, serverPort,
				databaseName, userName, password, connectionParam);
		pool = new MariaDbPoolDataSource(url);
	}

	/**
	 * Execute a query and return map of the first row.
	 *
	 * @param sql The SQL query statement.
	 * @return HashMap that contains properties of the first row, or null if the
	 *         query returns empty result.
	 */
	public static HashMap<String, Object> queryFirst(String sql) {
		HashMap<String, Object> result = null;
		try (Connection con = pool.getConnection()) {
			try (Statement sm = con.createStatement()) {
				try (ResultSet rs = sm.executeQuery(sql)) {
					if (rs.first()) {
						ResultSetMetaData meta = rs.getMetaData();
						int columnCount = meta.getColumnCount();
						result = new HashMap<>();
						for (int i = 1; i <= columnCount; ++i) {
							result.put(meta.getColumnName(i), rs.getObject(i));
						}
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Execute a query and return list of all rows.
	 *
	 * @param sql The SQL query statement.
	 * @return ArrayList that contains properties of all rows.
	 */
	public static ArrayList<HashMap<String, Object>> queryAll(String sql) {
		ArrayList<HashMap<String, Object>> result = new ArrayList<>();
		try (Connection con = pool.getConnection()) {
			try (Statement sm = con.createStatement()) {
				try (ResultSet rs = sm.executeQuery(sql)) {
					ResultSetMetaData meta = rs.getMetaData();
					int columnCount = meta.getColumnCount();
					while (rs.next()) {
						HashMap<String, Object> rowData = new HashMap<>();
						for (int i = 1; i <= columnCount; ++i) {
							rowData.put(meta.getColumnName(i), rs.getObject(i));
						}
						result.add(rowData);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Execute a query with no return value.
	 *
	 * @param sql The SQL query statement.
	 */
	public static void execute(String sql) {
		try (Connection con = pool.getConnection()) {
			try (Statement sm = con.createStatement()) {
				sm.execute(sql);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Execute an INSERT query and get the primary key.
	 *
	 * @param sql The SQL query statement.
	 * @return The primary key of the row just inserted.
	 */
	public static int insert(String sql) {
		try (Connection con = pool.getConnection()) {
			try (PreparedStatement sm = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
				sm.executeUpdate();
				try (ResultSet rs = sm.getGeneratedKeys()) {
					if (rs.next()) {
						return rs.getInt(1);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
