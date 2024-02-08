package ci;

import java.io.File;
import java.sql.*;

public class HistoryDAO {

	private final String databaseName;

	public HistoryDAO(String databaseName) throws SQLException {
		this.databaseName = databaseName;

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

	public Connection getConnection() throws SQLException {
		try {
			Class.forName("org.sqlite.JDBC");
			return DriverManager.getConnection("jdbc:sqlite:" + databaseName);

		} catch (Exception e) {
			throw new SQLException("Error while connecting to database", e);
		}
	}

}
