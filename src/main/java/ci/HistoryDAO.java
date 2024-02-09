package ci;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import ci.PushPayload.Commit;
import ci.PushPayload.Author;
import ci.PushPayload.Sender;

public class HistoryDAO {

	private final String databaseName;
	private Connection connection;

	public HistoryDAO(String databaseName) throws SQLException {
		this.databaseName = databaseName;

		connection = getConnection();
		// Build the database if it does not exist
		File databaseFile = new File(databaseName);
		if (!databaseFile.exists()) {

			Statement statement = connection.createStatement();

			statement.addBatch(
					"CREATE TABLE \"authors\" (\"id\" INTEGER, \"name\" TEXT, \"username\" TEXT, \"email\" TEXT, PRIMARY KEY(\"id\" AUTOINCREMENT))");
			statement.addBatch(
					"CREATE TABLE \"commits\" (\"id\" INTEGER, \"sha\" TEXT NOT NULL, \"message\" NUMERIC, \"authorId\" INTEGER NOT NULL, \"url\" TEXT, \"modifiedFiles\" TEXT, FOREIGN KEY(\"authorId\") REFERENCES \"authors\"(\"id\"), PRIMARY KEY(\"id\" AUTOINCREMENT))");
			statement.addBatch(
					"CREATE TABLE \"senders\" (\"id\" INTEGER, \"login\" TEXT, \"url\" TEXT, \"avatarUrl\" TEXT, PRIMARY KEY(\"id\" AUTOINCREMENT))");
			statement.addBatch(
					"CREATE TABLE \"history\" (\"id\" INTEGER, \"senderId\" INTEGER NOT NULL, \"buildResult\" INTEGER, \"buildLog\" TEXT, \"totalTests\" INTEGER, \"numOfPassedTests\" INTEGER, \"testLog\" TEXT, FOREIGN KEY(\"senderId\") REFERENCES \"senders\"(\"id\"), PRIMARY KEY(\"id\" AUTOINCREMENT))");
			statement.addBatch(
					"CREATE TABLE \"historyCommits\" (\"historyId\" INTEGER NOT NULL, \"commitId\" INTEGER NOT NULL, FOREIGN KEY(\"historyId\") REFERENCES \"history\"(\"id\"), FOREIGN KEY(\"commitId\") REFERENCES \"commits\"(\"id\"))");

			statement.executeBatch();
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
		// Check if the author already exists
		PreparedStatement getStatement = connection.prepareStatement("SELECT id FROM authors WHERE email = ?");
		getStatement.setString(1, author.email());
		ResultSet resultSet = getStatement.executeQuery();
		if (resultSet.next()) {
			;
			return resultSet.getInt("id");
		}

		// Insert the author
		PreparedStatement insertStatement = connection
				.prepareStatement("INSERT INTO authors (name, username, email) VALUES (?, ?, ?)");
		insertStatement.setString(1, author.name());
		insertStatement.setString(2, author.userName());
		insertStatement.setString(3, author.email());
		insertStatement.execute();

		resultSet = insertStatement.getGeneratedKeys();
		int id = resultSet.getInt(1);
		;
		return id;
	}

	public int addSender(Sender sender) throws SQLException {

		

		// Check if the sender already exists
		PreparedStatement getStatement = connection.prepareStatement("SELECT id FROM senders WHERE login = ?");
		getStatement.setString(1, sender.name());
		ResultSet resultSet = getStatement.executeQuery();
		if (resultSet.next()) {
			;
			return resultSet.getInt("id");
		}

		// Insert the sender
		PreparedStatement insertStatement = connection
				.prepareStatement("INSERT INTO senders (login, url, avatarUrl) VALUES (?, ?, ?)");
		insertStatement.setString(1, sender.name());
		insertStatement.setString(2, sender.url());
		insertStatement.setString(3, sender.avatarUrl());
		insertStatement.execute();

		resultSet = insertStatement.getGeneratedKeys();
		int id = resultSet.getInt(1);
		;
		return id;
	}

	public int addCommit(Commit commit) throws SQLException {

		

		// Check if the commit already exists
		PreparedStatement getStatement = connection.prepareStatement("SELECT id FROM commits WHERE sha = ?");
		getStatement.setString(1, commit.sha());
		ResultSet resultSet = getStatement.executeQuery();
		if (resultSet.next()) {
			;
			return resultSet.getInt("id");
		}

		// Insert the commit
		int authorId = addAuthor(commit.author());
		String modifiedFiles = String.join(",", commit.modifiedFiles());

		PreparedStatement insertStatement = connection.prepareStatement(
				"INSERT INTO commits (sha, message, authorId, url, modifiedFiles) VALUES (?, ?, ?, ?, ?)");
		insertStatement.setString(1, commit.sha());
		insertStatement.setString(2, commit.message());
		insertStatement.setInt(3, authorId);
		insertStatement.setString(4, commit.url());
		insertStatement.setString(5, modifiedFiles);
		insertStatement.execute();

		resultSet = insertStatement.getGeneratedKeys();
		int id = resultSet.getInt(1);
		;
		return id;
	}

	public int addHistory(BuildInfo buildInfo) throws SQLException {

		

		// Add sender and commits to the database
		int senderId = addSender(buildInfo.getSender());
		List<Integer> commitIds = new ArrayList<>();
		for (Commit commit : buildInfo.getCommitList()) {
			commitIds.add(addCommit(commit));
		}

		// Insert the history
		PreparedStatement insertStatement = connection.prepareStatement(
				"INSERT INTO history (senderId, buildResult, buildLog, totalTests, numOfPassedTests, testLog) VALUES (?, ?, ?, ?, ?, ?)");
		insertStatement.setInt(1, senderId);
		insertStatement.setInt(2, buildInfo.getBuildDetails().buildResult());
		insertStatement.setString(3, buildInfo.getBuildDetails().buildLog());
		insertStatement.setInt(4, buildInfo.getTestDetails().totalTests());
		insertStatement.setInt(5, buildInfo.getTestDetails().numOfPassedTests());
		insertStatement.setString(6, buildInfo.getTestDetails().testLog());
		insertStatement.execute();

		ResultSet resultSet = insertStatement.getGeneratedKeys();
		int historyId = resultSet.getInt(1);

		// Connect the history and commits
		for (int commitId : commitIds) {
			PreparedStatement insertHistoryCommits = connection
					.prepareStatement("INSERT INTO historyCommits (historyId, commitId) VALUES (?, ?)");
			insertHistoryCommits.setInt(1, historyId);
			insertHistoryCommits.setInt(2, commitId);
			insertHistoryCommits.execute();
		}

		;
		return historyId;
	}

	public List<BuildInfo> getAllHistory() throws SQLException {

		
		List<BuildInfo> history = new ArrayList<>();

		Statement statement = connection.createStatement();
		ResultSet historyResultSet = statement.executeQuery(
				"SELECT * FROM history");

		while (historyResultSet.next()) {
			//Get info from history
			int historyId = historyResultSet.getInt("id");
			int senderId = historyResultSet.getInt("senderId");
			int buildResult = historyResultSet.getInt("buildResult");
			String buildLog = historyResultSet.getString("buildLog");
			int totalTests = historyResultSet.getInt("totalTests");
			int numOfPassedTests = historyResultSet.getInt("numOfPassedTests");
			String testLog = historyResultSet.getString("testLog");

			// Get sender
			PreparedStatement getSenderStatement = connection.prepareStatement("SELECT * FROM senders WHERE id = ?");
			getSenderStatement.setInt(1, senderId);
			ResultSet senderResultSet = getSenderStatement.executeQuery();
			Sender sender = new Sender(senderResultSet.getString("login"), senderResultSet.getString("url"), senderResultSet.getString("avatarUrl"));

			// Get commits
			PreparedStatement getCommitsStatement = connection.prepareStatement("SELECT id, sha, message, authorId, url, modifiedFiles FROM commits JOIN historyCommits ON id = commitId WHERE historyId = ?");
		}
		
		return history;
	}

}