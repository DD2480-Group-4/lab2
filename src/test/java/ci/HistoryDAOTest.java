package ci;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

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
		// Retrieving the columns in the database
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
}
