package ci;

import ci.BuildInfo.BuildDetails;
import ci.BuildInfo.TestDetails;
import ci.PushPayload.Commit;

import org.apache.commons.io.output.TeeOutputStream;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.io.OutputStream;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Main executable for Continuous-Integration handler
 */
public class Main extends AbstractHandler {

	@Override
	public void handle(
			String target,
			Request baseRequest,
			HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);

		HistoryDAO historyDAO;
		try {
			historyDAO = createHistoryDAO("builds.db");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		if (request.getMethod().equals("POST")) {
			// Incoming webhook payload from GitHub
			final String accessUrl = request.getRequestURL().toString();

			PushPayload payload = new PushPayload(request.getReader().readLine());
			var notifier = createNotifier(payload);
			try {
				notifier.setCommitStatus(CommitStatuses.pending, "Working", accessUrl);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}

			var rootDir = Path.of(".");

			var buildPath = rootDir.resolve(UUID.randomUUID().toString());
			while (buildPath.toFile().exists()) {
				buildPath = rootDir.resolve(UUID.randomUUID().toString());
			}

			OutputStream buildOutput = new ByteArrayOutputStream();
			OutputStream testOutput = new ByteArrayOutputStream();
			TeeOutputStream buildAndStdOut = new TeeOutputStream(buildOutput, System.out);
			TeeOutputStream testAndStdOut = new TeeOutputStream(testOutput, System.out);

			try (var builder = createBuilder(buildPath, buildAndStdOut, testAndStdOut)) {
				builder.cloneTargetRepo(payload.getCloneUrl(), payload.getBranch());
				var result = builder.buildAndTest();
				var desc = switch (result.status()) {
					case error -> "Tests failed";
					case failure -> "Build failed";
					case pending -> "Wait what happened here????????????????";
					case success -> "Build successful!";
				};
				notifier.setCommitStatus(result.status(), desc, accessUrl);

				BuildDetails buildDetails = new BuildDetails(result.status().ordinal(), buildOutput.toString());

				TestDetails testDetails = new TestDetails(result.totalTests(), result.passedTests(), testOutput.toString());
				BuildInfo buildInfo = new BuildInfo(payload.getSender(),
						Arrays.asList(payload.getCommits()),
						buildDetails, testDetails, payload.getPushedAt());

				historyDAO.addHistory(buildInfo);
			} catch (GitAPIException err) {
				try {
					notifier.setCommitStatus(CommitStatuses.failure, err.getLocalizedMessage(), accessUrl);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			} finally {
				Builder.deleteDirectory(buildPath.toFile());
			}
		} else {
			// GET request from web interface
			try {
				// Fetch history of builds from database
				List<BuildInfo> history = historyDAO.getAllHistory();
				WebHandler webHandler = new WebHandler(history);

				if (history.isEmpty()) {
					response.getWriter().println("<strong>No builds found in database.</strong>");
				} else {
					// Display links to all builds from history
					if (!target.startsWith("/build_")) {
						for (BuildInfo build : history) {
							response.getWriter().println("<a href=\"/build_" + build.getId() + "/\">Build Info "
									+ build.getId() + "</a><br>");
						}
					}
					// Display build info for specific build if link is clicked
					if (target.startsWith("/build_")) {
						response.getWriter().println("<br><a href=\"/\">Home</a><br></br>");
						int buildId = Integer.parseInt(target.substring(7, target.length() - 1));
						response.getWriter().println(webHandler.buildInfoToHtmlString(buildId));
					}
				}

			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}

		try {
			historyDAO.closeConnection();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates a new notifier from a PushPayload.
	 * This function merely exists to allow mock-testing.
	 * 
	 * @param payload The payload to use in the notifier.
	 * @return The notifier.
	 */
	protected Notifier createNotifier(PushPayload payload) {
		return new Notifier(payload, HttpClient.newHttpClient());
	}

	/**
	 * Creates a new builder from a path and an outputstream.
	 * This function merely exists to allow mock-testing.
	 * 
	 * @param path   The file path to build the project at.
	 * @param output The output stream to print logs to.
	 * @return The builder.
	 */
	protected Builder createBuilder(Path path, OutputStream output, OutputStream testOutput) {
		return new Builder(path, output, testOutput);
	}

	/**
	 * Creates a new HistoryDAO object.
	 * This function merely exists to allow mock-testing.
	 * 
	 * @param dbPath The path to the database file.
	 * @return The HistoryDAO.
	 */
	protected HistoryDAO createHistoryDAO(String dbPath) throws SQLException {
		return new HistoryDAO(dbPath);
	}

	/**
	 * Prints information about PushPayload object received from GitHub webhook to
	 * standard output
	 *
	 * @param payload Parsed payload object
	 */
	private void printPushPayload(PushPayload payload) {
		System.out.println("-------------------------");
		StringBuilder sb = new StringBuilder();
		sb.append("CI job started\n");
		sb.append("Repository: ").append(payload.getCloneUrl()).append("\n");
		sb.append("Branch: ").append(payload.getBranch()).append("\n");
		sb.append("Pushed at: ").append(payload.getPushedAt()).append("\n");
		sb.append("Sender: ").append(payload.getSender().name()).append("\n");
		sb.append("Commits: \n");
		for (Commit commit : payload.getCommits()) {
			sb.append("\t");
			sb.append("Author: ").append(commit.author().name());
			sb.append(" | Message: ").append(commit.message()).append("\n");
		}

		System.out.println(sb.toString());
	}

	/**
	 * Initialize CI server
	 */
	public static void main(String[] args) throws Exception {
		Server server = new Server(8080);
		server.setHandler(new Main());
		server.start();
		server.join();
	}

}
