package ci;

import java.io.File;
import java.sql.*;

import ci.PushPayload.Commit;

public class HistoryDAO {

	private final String databaseName;

	public HistoryDAO(String databaseName) throws SQLException {
		this.databaseName = databaseName;

		//Build the database if it does not exist
		File databaseFile = new File(databaseName);
		if (!databaseFile.exists()) {

			Connection connection = getConnection();
			Statement statement = connection.createStatement();

			statement.addBatch(
					"CREATE TABLE \"authors\" (\"id\" INTEGER, \"name\" TEXT, \"username\" TEXT, \"email\" TEXT, PRIMARY KEY(\"id\" AUTOINCREMENT))");
			statement.addBatch(
					"CREATE TABLE \"commits\" (\"id\" INTEGER, \"sha\" TEXT NOT NULL, \"message\" NUMERIC, \"authorId\" INTEGER NOT NULL, \"url\" TEXT, \"modifiedFiles\" TEXT, FOREIGN KEY(\"authorId\") REFERENCES \"authors\"(\"id\"), PRIMARY KEY(\"id\" AUTOINCREMENT))");
			statement.addBatch(
					"CREATE TABLE \"senders\" (\"id\" INTEGER, \"login\" TEXT, \"url\" TEXT, \"avatar_url\" TEXT, PRIMARY KEY(\"id\" AUTOINCREMENT))");
			statement.addBatch(
					"CREATE TABLE \"history\" (\"id\" INTEGER, \"senderId\" INTEGER NOT NULL, \"buildResult\" INTEGER, \"buildLog\" TEXT, \"totalTests\" INTEGER, \"numOfPassedTests\" INTEGER, \"testLog\" TEXT, FOREIGN KEY(\"senderId\") REFERENCES \"senders\"(\"id\"), PRIMARY KEY(\"id\" AUTOINCREMENT))");
			statement.addBatch(
					"CREATE TABLE \"historyCommits\" (\"historyId\" INTEGER NOT NULL, \"commitId\" INTEGER NOT NULL, FOREIGN KEY(\"historyId\") REFERENCES \"history\"(\"id\"), FOREIGN KEY(\"commitId\") REFERENCES \"commits\"(\"id\"))");

			statement.executeBatch();
			connection.close();
		}

	}

	private Connection getConnection() throws SQLException {
		try {
			Class.forName("org.sqlite.JDBC");
			return DriverManager.getConnection("jdbc:sqlite:" + databaseName);

		} catch (Exception e) {
			throw new SQLException("Error while connecting to database", e);
		}
	}

	public int addAuthor(String name, String username, String email) throws SQLException {

		Connection connection = getConnection();

		//Check if the author already exists
		PreparedStatement getStatement = connection.prepareStatement("SELECT id FROM authors WHERE email = ?");
		getStatement.setString(1, email);
		ResultSet rs = getStatement.executeQuery();
		if (rs.next()) {
			connection.close();
			return rs.getInt("id");
		}

		//Insert the author
		PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO authors (name, username, email) VALUES (?, ?, ?)");
		insertStatement.setString(1, name);
		insertStatement.setString(2, username);
		insertStatement.setString(3, email);
		insertStatement.execute();

		rs = insertStatement.getGeneratedKeys();
		int id = rs.getInt(1);
		connection.close();
		return id;
	}

	public int addSender(String login, String url, String avatarUrl) throws SQLException {

		Connection connection = getConnection();

		//Check if the sender already exists
		PreparedStatement getStatement = connection.prepareStatement("SELECT id FROM senders WHERE login = ?");
		getStatement.setString(1, login);
		ResultSet rs = getStatement.executeQuery();
		if (rs.next()) {
			connection.close();
			return rs.getInt("id");
		}

		//Insert the sender
		PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO senders (login, url, avatar_url) VALUES (?, ?, ?)");
		insertStatement.setString(1, login);
		insertStatement.setString(2, url);
		insertStatement.setString(3, avatarUrl);
		insertStatement.execute();

		rs = insertStatement.getGeneratedKeys();
		int id = rs.getInt(1);
		connection.close();
		return id;
	}

	// public int addCommit(Commit commit) throws SQLException {

	// 	Connection connection = getConnection();

	// 	//Check if the commit already exists
	// 	PreparedStatement getStatement = connection.prepareStatement("SELECT id FROM commits WHERE sha = ?");
	// 	ResultSet rs = getStatement.executeQuery();


		
	// }
}
