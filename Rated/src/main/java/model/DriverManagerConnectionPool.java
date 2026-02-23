package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class DriverManagerConnectionPool {
	// Reso final perché inizializzato inline e mai riassegnato (solo modificato il contenuto)
	private static List<Connection> freeDbConnections = new LinkedList<Connection>();
	
	static {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		}
		catch (final ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private static Connection createDBConnection() throws SQLException {
		Connection newConnection = null;
		final String db = "RatedDB";
		final String username = "root";
		final String password = "root";

		newConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/"+db, username, password);
		
		newConnection.setAutoCommit(false);
		
		return newConnection;
	}
	
	public static synchronized Connection getConnection() throws SQLException {
		Connection connection;
		
		if(! freeDbConnections.isEmpty()) {
			connection = (Connection) freeDbConnections.get(0);
			DriverManagerConnectionPool.freeDbConnections.remove(0);
			try {
				if (connection.isClosed()) {
					connection = DriverManagerConnectionPool.getConnection();
				}
			} catch (final SQLException e) {
				connection = DriverManagerConnectionPool.getConnection();
			}
		}
		else connection = DriverManagerConnectionPool.createDBConnection();
		
		return connection;
	}
	
	public static synchronized void releaseConnection(final Connection connection) {
		DriverManagerConnectionPool.freeDbConnections.add(connection);
	}
}