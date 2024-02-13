package ci;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import ci.PushPayload.Commit;
import ci.PushPayload.Author;
import ci.PushPayload.Sender;

/**
 * Data Access Object for build history data
 */
public class HistoryDAO {

	private final String databaseName;
	private Connection connection;

	/**
	 * Creates a new HistoryDAO object
	 *
	 * @param databaseName Name of the database file
	 */
	public HistoryDAO(String databaseName) throws SQLException {
		this.databaseName = databaseName;

		// Build the database if it does not exist
		File databaseFile = new File(databaseName);
		if (!databaseFile.exists()) {
			
			connection = getConnection();
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
		else
		{
			connection = getConnection();
		}

	}

	/**
	 * Gets a connection to the database
	 *
	 * @return Connection to the database
	 */
	public Connection getConnection() throws SQLException {

		if(connection != null && !connection.isClosed())
			return connection;

		try {
			Class.forName("org.sqlite.JDBC");
			return DriverManager.getConnection("jdbc:sqlite:" + databaseName);

		} catch (Exception e) {
			throw new SQLException("Error while connecting to database", e);
		}
	}

	/**
	 * Closes the connection to the database
	 */
	public void closeConnection() throws SQLException
	{
		connection.close();
	}

	/**
	 * Adds an author to the database
	 *
	 * @param author Author to add
	 * @return ID of the author
	 */
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

	/**
	 * Adds a sender to the database
	 *
	 * @param sender Sender to add
	 * @return ID of the sender
	 */
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

	/**
	 * Adds a commit to the database
	 *
	 * @param commit Commit to add
	 * @return ID of the commit
	 */
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

	/**
	 * Adds a history to the database
	 *
	 * @param buildInfo Build information to add
	 * @return ID of the history
	 */
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

		return historyId;
	}

	/**
	 * Gets an author from the database
	 *
	 * @param id ID of the author
	 * @return Author object
	 */
	public Author getAuthor(int id) throws SQLException {
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM authors WHERE id=?");
		preparedStatement.setInt(1, id);
		ResultSet resultSet = preparedStatement.executeQuery();

		if (resultSet.next()) {
			return new Author(resultSet.getString("name"),
					resultSet.getString("username"),
					resultSet.getString("email"));
		}

		return null;
	}

	/**
	 * Gets a list of commits associated with a history
	 *
	 * @param historyId ID of the history
	 * @return List of commits associated to the history
	 */
	public List<Commit> getCommitsForHistory(int historyId) throws SQLException {
		PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT id, sha, message, authorId, url, modifiedFiles FROM commits JOIN historyCommits ON id = commitId WHERE historyId = ?");
		preparedStatement.setInt(1, historyId);
		ResultSet resultSet = preparedStatement.executeQuery();

		List<Commit> commits = new ArrayList<Commit>();
		while (resultSet.next()) {
			Author author = getAuthor(resultSet.getInt("authorId"));
			String[] modifiedFiles = resultSet.getString("modifiedFiles").split(",");

			commits.add(new Commit(resultSet.getString("sha"),
					resultSet.getString("message"),
					author,
					resultSet.getString("url"),
					modifiedFiles));

		}

		return commits;
	}

	/**
	 * Gets a sender from the database
	 *
	 * @param id ID of the sender
	 * @return Sender object
	 */
	public Sender getSender(int id) throws SQLException {
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM senders WHERE id=?");
		preparedStatement.setInt(1, id);
		ResultSet resultSet = preparedStatement.executeQuery();

		if (resultSet.next()) {
			return new Sender(resultSet.getString("login"),
					resultSet.getString("url"),
					resultSet.getString("avatarUrl"));
		}

		return null;
	}

	/**
	 * Gets all history from the database
	 *
	 * @return List of BuildInfo objects
	 */
	public List<BuildInfo> getAllHistory() throws SQLException {

		List<BuildInfo> history = new ArrayList<>();

		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery(
				"SELECT * FROM history");

		while (resultSet.next()) {
			int historyId = resultSet.getInt("id");
			int senderId = resultSet.getInt("senderId");
			int buildResult = resultSet.getInt("buildResult");
			String buildLog = resultSet.getString("buildLog");
			int totalTests = resultSet.getInt("totalTests");
			int numOfPassedTests = resultSet.getInt("numOfPassedTests");
			String testLog = resultSet.getString("testLog");

			Sender sender = getSender(senderId);
			List<Commit> commits = getCommitsForHistory(historyId);

			history.add(new BuildInfo(historyId, sender, commits,
					new BuildInfo.BuildDetails(buildResult, buildLog),
					new BuildInfo.TestDetails(totalTests, numOfPassedTests, testLog)));

		}

		return history;
	}

	/**
	 * Gets history from the database
	 *
	 * @param id ID of the history
	 * @return BuildInfo object
	 */
	public BuildInfo getHistory(int id) throws SQLException {
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM history WHERE id=?");
		preparedStatement.setInt(1, id);
		ResultSet resultSet = preparedStatement.executeQuery();

		if (resultSet.next()) {
			int historyId = resultSet.getInt("id");
			int senderId = resultSet.getInt("senderId");
			int buildResult = resultSet.getInt("buildResult");
			String buildLog = resultSet.getString("buildLog");
			int totalTests = resultSet.getInt("totalTests");
			int numOfPassedTests = resultSet.getInt("numOfPassedTests");
			String testLog = resultSet.getString("testLog");

			Sender sender = getSender(senderId);
			List<Commit> commits = getCommitsForHistory(historyId);

			 return new BuildInfo(historyId, sender, commits,
					new BuildInfo.BuildDetails(buildResult, buildLog),
					new BuildInfo.TestDetails(totalTests, numOfPassedTests, testLog));
		}

		return null;
	}

}
