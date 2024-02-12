package ci;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class HistoryDAOTest {

	private HistoryDAO historyDAO;
	private final String testDatabaseName = "UnitTests.db";

	@BeforeEach
	void testSetup() throws SQLException {
		historyDAO = new HistoryDAO(testDatabaseName);

		Connection connection = historyDAO.getConnection();
		Statement statement = connection.createStatement();

		statement
				.addBatch("INSERT INTO authors (name, username, email) VALUES ('John Doe', 'johndoe', 'john@doe.com')");
		statement.addBatch(
				"INSERT INTO authors (name, username, email) VALUES ('Mr Bean', 'mrbean', 'beanAndTeddy@funny.org')");

		statement.addBatch(
				"INSERT INTO commits (sha, message, authorId, url, modifiedFiles) VALUES ('sha1', 'Commit1', 1, 'commitUrl1', 'File1.txt, File2.txt')");
		statement.addBatch(
				"INSERT INTO commits (sha, message, authorId, url, modifiedFiles) VALUES ('sha2', 'Commit2', 2, 'commitUrl2', 'File1.txt, File2.txt')");
		statement.addBatch(
				"INSERT INTO commits (sha, message, authorId, url, modifiedFiles) VALUES ('sha3', 'Commit3', 1, 'commitUrl3', 'File2.txt')");

		statement.addBatch("INSERT INTO senders (login, url, avatarUrl) VALUES ('johndoe', 'johndoeUrl', 'johndoeAvatarUrl')");

		statement.addBatch("INSERT INTO history ('senderId', 'buildResult', 'buildLog', 'totalTests', 'numOfPassedTests', 'testLog') VALUES (1, 1, 'Build Log 1', 10, 10, 'All test passed')");
		statement.addBatch("INSERT INTO history ('senderId', 'buildResult', 'buildLog', 'totalTests', 'numOfPassedTests', 'testLog') VALUES (1, 0, 'Build Log 2', 10, 5, '5 test failed')");

		statement.addBatch("INSERT INTO historyCommits ('historyId', 'commitId') VALUES (1, 1)");
		statement.addBatch("INSERT INTO historyCommits ('historyId', 'commitId') VALUES (1, 2)");
		statement.addBatch("INSERT INTO historyCommits ('historyId', 'commitId') VALUES (2, 3)");


		statement.executeBatch();
	}

	@AfterEach
	void testCleanup() throws IOException, SQLException {
		historyDAO.closeConnection();
		FileUtils.forceDelete(new File(testDatabaseName));
	}

	@Test
	void HistoryDAO_Constructor_CreatesDatabaseWithTables() throws SQLException {
		Connection connection = historyDAO.getConnection();
		DatabaseMetaData metaData = connection.getMetaData();
		String[] types = { "TABLE" };
		ResultSet resultSet = metaData.getTables(null, null, "%", types);

		Assertions.assertThat(resultSet.next()).isTrue();
		Assertions.assertThat(resultSet.getString("TABLE_NAME")).isEqualTo("authors");
		Assertions.assertThat(resultSet.next()).isTrue();
		Assertions.assertThat(resultSet.getString("TABLE_NAME")).isEqualTo("commits");
		Assertions.assertThat(resultSet.next()).isTrue();
		Assertions.assertThat(resultSet.getString("TABLE_NAME")).isEqualTo("history");
		Assertions.assertThat(resultSet.next()).isTrue();
		Assertions.assertThat(resultSet.getString("TABLE_NAME")).isEqualTo("historyCommits");
		Assertions.assertThat(resultSet.next()).isTrue();
		Assertions.assertThat(resultSet.getString("TABLE_NAME")).isEqualTo("senders");
		Assertions.assertThat(resultSet.next()).isFalse();
	}

	@Test
	void getAuthor_AuthorExists_ReturnsAuthor() throws SQLException {
		ci.PushPayload.Author author = historyDAO.getAuthor(1);
		Assertions.assertThat(author).isNotNull();
		Assertions.assertThat(author.name()).isEqualTo("John Doe");
		Assertions.assertThat(author.userName()).isEqualTo("johndoe");
		Assertions.assertThat(author.email()).isEqualTo("john@doe.com");
	}

	@Test
	void getAuthor_AuthorDoesNotExists_ReturnsNull() throws SQLException {
		ci.PushPayload.Author author = historyDAO.getAuthor(13);
		Assertions.assertThat(author).isNull();
	}

	@Test
	void getSender_SenderExists_ReturnsSender() throws SQLException
	{
		ci.PushPayload.Sender sender = historyDAO.getSender(1);
		Assertions.assertThat(sender).isNotNull();
		Assertions.assertThat(sender.name()).isEqualTo("johndoe");
		Assertions.assertThat(sender.url()).isEqualTo("johndoeUrl");
		Assertions.assertThat(sender.avatarUrl()).isEqualTo("johndoeAvatarUrl");
	}

	@Test
	void getSender_SenderDoesNotExists_ReturnsNull() throws SQLException
	{
		ci.PushPayload.Sender sender = historyDAO.getSender(12);
		Assertions.assertThat(sender).isNull();
	}

	
}
