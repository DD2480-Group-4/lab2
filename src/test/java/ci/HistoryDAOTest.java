package ci;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

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
	@DisplayName("HistoryDAO Constructor creates database with tables")
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
	@DisplayName("Get Author that exists")
	void getAuthor_AuthorExists_ReturnsAuthor() throws SQLException {
		ci.PushPayload.Author author = historyDAO.getAuthor(1);
		Assertions.assertThat(author).isNotNull();
		Assertions.assertThat(author.name()).isEqualTo("John Doe");
		Assertions.assertThat(author.userName()).isEqualTo("johndoe");
		Assertions.assertThat(author.email()).isEqualTo("john@doe.com");
	}

	@Test
	@DisplayName("Get Author that does not exists")
	void getAuthor_AuthorDoesNotExists_ReturnsNull() throws SQLException {
		ci.PushPayload.Author author = historyDAO.getAuthor(13);
		Assertions.assertThat(author).isNull();
	}

	@Test
	@DisplayName("Get Sender that exists")
	void getSender_SenderExists_ReturnsSender() throws SQLException
	{
		ci.PushPayload.Sender sender = historyDAO.getSender(1);
		Assertions.assertThat(sender).isNotNull();
		Assertions.assertThat(sender.name()).isEqualTo("johndoe");
		Assertions.assertThat(sender.url()).isEqualTo("johndoeUrl");
		Assertions.assertThat(sender.avatarUrl()).isEqualTo("johndoeAvatarUrl");
	}

	@Test
	@DisplayName("Get Sender that does not exists")
	void getSender_SenderDoesNotExists_ReturnsNull() throws SQLException
	{
		ci.PushPayload.Sender sender = historyDAO.getSender(12);
		Assertions.assertThat(sender).isNull();
	}

	@Test 
	@DisplayName("Get Commits for history that exists")
	void getCommitsForHistory_CommitsExists_ReturnsCommits() throws SQLException
	{
		List<ci.PushPayload.Commit> commits = historyDAO.getCommitsForHistory(1);
		Assertions.assertThat(commits).hasSize(2);

		Assertions.assertThat(commits.get(0).sha()).isEqualTo("sha1");
		Assertions.assertThat(commits.get(0).message()).isEqualTo("Commit1");
		Assertions.assertThat(commits.get(0).author().name()).isEqualTo("John Doe");
		Assertions.assertThat(commits.get(0).author().userName()).isEqualTo("johndoe");
		Assertions.assertThat(commits.get(0).author().email()).isEqualTo("john@doe.com");
		Assertions.assertThat(commits.get(0).url()).isEqualTo("commitUrl1");
		Assertions.assertThat(commits.get(0).modifiedFiles()).containsExactly("File1.txt", "File2.txt");

		Assertions.assertThat(commits.get(1).sha()).isEqualTo("sha2");
		Assertions.assertThat(commits.get(1).message()).isEqualTo("Commit2");
		Assertions.assertThat(commits.get(1).author().name()).isEqualTo("Mr Bean");
		Assertions.assertThat(commits.get(1).author().userName()).isEqualTo("mrbean");
		Assertions.assertThat(commits.get(1).author().email()).isEqualTo("beanAndTeddy@funny.org");
		Assertions.assertThat(commits.get(1).url()).isEqualTo("commitUrl2");
		Assertions.assertThat(commits.get(1).modifiedFiles()).containsExactly("File1.txt", "File2.txt");
	}

	@Test 
	@DisplayName("Get Commits for history that does not exists")
	void getCommitsForHistory_CommitsDoesNotExists_ReturnsEmptyList() throws SQLException
	{
		List<ci.PushPayload.Commit> commits = historyDAO.getCommitsForHistory(15);
		Assertions.assertThat(commits).hasSize(0);
	}

	@Test
	@DisplayName("Get History that exists")
	void getHistory_HistoryExists_ReturnsHistory() throws SQLException
	{
		BuildInfo buildInfo = historyDAO.getHistory(1);

		ci.PushPayload.Sender sender = buildInfo.getSender();
		Assertions.assertThat(sender).isNotNull();
		Assertions.assertThat(sender.name()).isEqualTo("johndoe");
		Assertions.assertThat(sender.url()).isEqualTo("johndoeUrl");
		Assertions.assertThat(sender.avatarUrl()).isEqualTo("johndoeAvatarUrl");

		ci.BuildInfo.BuildDetails buildDetails = buildInfo.getBuildDetails();
		Assertions.assertThat(buildDetails).isNotNull();
		Assertions.assertThat(buildDetails.buildResult()).isEqualTo(1);
		Assertions.assertThat(buildDetails.buildLog()).isEqualTo("Build Log 1");

		ci.BuildInfo.TestDetails testDetails = buildInfo.getTestDetails();
		Assertions.assertThat(testDetails).isNotNull();
		Assertions.assertThat(testDetails.totalTests()).isEqualTo(10);
		Assertions.assertThat(testDetails.numOfPassedTests()).isEqualTo(10);
		Assertions.assertThat(testDetails.testLog()).isEqualTo("All test passed");

		List<ci.PushPayload.Commit> commits = buildInfo.getCommitList();
		Assertions.assertThat(commits).hasSize(2);
		Assertions.assertThat(commits).hasSize(2);

		Assertions.assertThat(commits.get(0).sha()).isEqualTo("sha1");
		Assertions.assertThat(commits.get(0).message()).isEqualTo("Commit1");
		Assertions.assertThat(commits.get(0).author().name()).isEqualTo("John Doe");
		Assertions.assertThat(commits.get(0).author().userName()).isEqualTo("johndoe");
		Assertions.assertThat(commits.get(0).author().email()).isEqualTo("john@doe.com");
		Assertions.assertThat(commits.get(0).url()).isEqualTo("commitUrl1");
		Assertions.assertThat(commits.get(0).modifiedFiles()).containsExactly("File1.txt", "File2.txt");

		Assertions.assertThat(commits.get(1).sha()).isEqualTo("sha2");
		Assertions.assertThat(commits.get(1).message()).isEqualTo("Commit2");
		Assertions.assertThat(commits.get(1).author().name()).isEqualTo("Mr Bean");
		Assertions.assertThat(commits.get(1).author().userName()).isEqualTo("mrbean");
		Assertions.assertThat(commits.get(1).author().email()).isEqualTo("beanAndTeddy@funny.org");
		Assertions.assertThat(commits.get(1).url()).isEqualTo("commitUrl2");
		Assertions.assertThat(commits.get(1).modifiedFiles()).containsExactly("File1.txt", "File2.txt");
	}

	@Test
	@DisplayName("Get History that does not exists")
	void getHistory_HistoryDoesNotExists_ReturnsNull() throws SQLException
	{
		BuildInfo buildInfo = historyDAO.getHistory(15);
		Assertions.assertThat(buildInfo).isNull();
	}

	@Test
	@DisplayName("Get all History")
	void getAllHistory_ReturnsHistory() throws SQLException
	{
		List<BuildInfo> history = historyDAO.getAllHistory();
		Assertions.assertThat(history).hasSize(2);

		BuildInfo buildInfo = history.get(0);
		ci.PushPayload.Sender sender = buildInfo.getSender();
		Assertions.assertThat(sender).isNotNull();
		Assertions.assertThat(sender.name()).isEqualTo("johndoe");
		Assertions.assertThat(sender.url()).isEqualTo("johndoeUrl");
		Assertions.assertThat(sender.avatarUrl()).isEqualTo("johndoeAvatarUrl");

		ci.BuildInfo.BuildDetails buildDetails = buildInfo.getBuildDetails();
		Assertions.assertThat(buildDetails).isNotNull();
		Assertions.assertThat(buildDetails.buildResult()).isEqualTo(1);
		Assertions.assertThat(buildDetails.buildLog()).isEqualTo("Build Log 1");

		ci.BuildInfo.TestDetails testDetails = buildInfo.getTestDetails();
		Assertions.assertThat(testDetails).isNotNull();
		Assertions.assertThat(testDetails.totalTests()).isEqualTo(10);
		Assertions.assertThat(testDetails.numOfPassedTests()).isEqualTo(10);
		Assertions.assertThat(testDetails.testLog()).isEqualTo("All test passed");

		List<ci.PushPayload.Commit> commits = buildInfo.getCommitList();
		Assertions.assertThat(commits).hasSize(2);
		Assertions.assertThat(commits).hasSize(2);

		Assertions.assertThat(commits.get(0).sha()).isEqualTo("sha1");
		Assertions.assertThat(commits.get(0).message()).isEqualTo("Commit1");
		Assertions.assertThat(commits.get(0).author().name()).isEqualTo("John Doe");
		Assertions.assertThat(commits.get(0).author().userName()).isEqualTo("johndoe");
		Assertions.assertThat(commits.get(0).author().email()).isEqualTo("john@doe.com");
		Assertions.assertThat(commits.get(0).url()).isEqualTo("commitUrl1");
		Assertions.assertThat(commits.get(0).modifiedFiles()).containsExactly("File1.txt", "File2.txt");

		Assertions.assertThat(commits.get(1).sha()).isEqualTo("sha2");
		Assertions.assertThat(commits.get(1).message()).isEqualTo("Commit2");
		Assertions.assertThat(commits.get(1).author().name()).isEqualTo("Mr Bean");
		Assertions.assertThat(commits.get(1).author().userName()).isEqualTo("mrbean");
		Assertions.assertThat(commits.get(1).author().email()).isEqualTo("beanAndTeddy@funny.org");
		Assertions.assertThat(commits.get(1).url()).isEqualTo("commitUrl2");
		Assertions.assertThat(commits.get(1).modifiedFiles()).containsExactly("File1.txt", "File2.txt");

		buildInfo = history.get(1);

		sender = buildInfo.getSender();
		Assertions.assertThat(sender).isNotNull();
		Assertions.assertThat(sender.name()).isEqualTo("johndoe");
		Assertions.assertThat(sender.url()).isEqualTo("johndoeUrl");
		Assertions.assertThat(sender.avatarUrl()).isEqualTo("johndoeAvatarUrl");

		buildDetails = buildInfo.getBuildDetails();
		Assertions.assertThat(buildDetails).isNotNull();
		Assertions.assertThat(buildDetails.buildResult()).isEqualTo(0);
		Assertions.assertThat(buildDetails.buildLog()).isEqualTo("Build Log 2");

		testDetails = buildInfo.getTestDetails();
		Assertions.assertThat(testDetails).isNotNull();
		Assertions.assertThat(testDetails.totalTests()).isEqualTo(10);
		Assertions.assertThat(testDetails.numOfPassedTests()).isEqualTo(5);
		Assertions.assertThat(testDetails.testLog()).isEqualTo("5 test failed");

		commits = buildInfo.getCommitList();
		Assertions.assertThat(commits).hasSize(1);

		Assertions.assertThat(commits.get(0).sha()).isEqualTo("sha3");
		Assertions.assertThat(commits.get(0).message()).isEqualTo("Commit3");
		Assertions.assertThat(commits.get(0).author().name()).isEqualTo("John Doe");
		Assertions.assertThat(commits.get(0).author().userName()).isEqualTo("johndoe");
		Assertions.assertThat(commits.get(0).author().email()).isEqualTo("john@doe.com");
		Assertions.assertThat(commits.get(0).url()).isEqualTo("commitUrl3");
		Assertions.assertThat(commits.get(0).modifiedFiles()).containsExactly("File2.txt");
	}
}
