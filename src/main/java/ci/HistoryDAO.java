package ci;

import java.io.File;
import java.sql.*;

import ci.PushPayload.Commit;
import ci.PushPayload.Author;
import ci.PushPayload.Sender;

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

	public int addAuthor(Author author) throws SQLException {

		Connection connection = getConnection();

		//Check if the author already exists
		PreparedStatement getStatement = connection.prepareStatement("SELECT id FROM authors WHERE email = ?");
		getStatement.setString(1, author.email());
		ResultSet resultSet = getStatement.executeQuery();
		if (resultSet.next()) {
			connection.close();
			return resultSet.getInt("id");
		}

		//Insert the author
		PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO authors (name, username, email) VALUES (?, ?, ?)");
		insertStatement.setString(1, author.name());
		insertStatement.setString(2, author.userName());
		insertStatement.setString(3, author.email());
		insertStatement.execute();

		resultSet = insertStatement.getGeneratedKeys();
		int id = resultSet.getInt(1);
		connection.close();
		return id;
	}

	public int addSender(Sender sender) throws SQLException {

		Connection connection = getConnection();

		//Check if the sender already exists
		PreparedStatement getStatement = connection.prepareStatement("SELECT id FROM senders WHERE login = ?");
		getStatement.setString(1, sender.name());
		ResultSet resultSet = getStatement.executeQuery();
		if (resultSet.next()) {
			connection.close();
			return resultSet.getInt("id");
		}

		//Insert the sender
		PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO senders (login, url, avatar_url) VALUES (?, ?, ?)");
		insertStatement.setString(1, sender.name());
		insertStatement.setString(2, sender.url());
		insertStatement.setString(3, sender.avatarUrl());
		insertStatement.execute();

		resultSet = insertStatement.getGeneratedKeys();
		int id = resultSet.getInt(1);
		connection.close();
		return id;
	}

	public int addCommit(Commit commit) throws SQLException {

		Connection connection = getConnection();

		//Check if the commit already exists
		PreparedStatement getStatement = connection.prepareStatement("SELECT id FROM commits WHERE sha = ?");
		getStatement.setString(1, commit.sha());
		ResultSet resultSet = getStatement.executeQuery();
		if (resultSet.next()) {
			connection.close();
			return resultSet.getInt("id");
		}

		//Insert the commit
		int authorId = addAuthor(commit.author());
		String modifiedFiles = String.join(",", commit.modifiedFiles());

		PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO commits (sha, message, authorId, url, modifiedFiles) VALUES (?, ?, ?, ?, ?)");
		insertStatement.setString(1, commit.sha());
		insertStatement.setString(2, commit.message());
		insertStatement.setInt(3, authorId);
		insertStatement.setString(4, commit.url());
		insertStatement.setString(5, modifiedFiles);
		insertStatement.execute();
		
		resultSet = insertStatement.getGeneratedKeys();
		int id = resultSet.getInt(1);
		connection.close();
		return id;
	}
}
