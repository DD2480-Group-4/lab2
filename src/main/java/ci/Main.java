package ci;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import ci.PushPayload.Commit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class Main extends AbstractHandler {

	@Override
	public void handle(
		String target,
		Request baseRequest,
		HttpServletRequest request,
		HttpServletResponse response
	) throws IOException, ServletException {
		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);

		if (request.getMethod() == "POST") {
			// Incoming webhook payload from GitHub
			PushPayload payload = new PushPayload(request.getReader().readLine());
			printPushPayload(payload);
		} else {
			// GET request from web interface
			try {
				// Fetch history of builds from database
				HistoryDAO dao = new HistoryDAO("builds.db");
				List<BuildInfo> history = dao.getAllHistory();
				WebHandler webHandler = new WebHandler(history);

				// Display links to all builds from history
				if (!target.startsWith("/build_")) {
					for (BuildInfo build : history) {
						response.getWriter().println("<a href=\"/build_" + build.getId() + "/\">Build Info " + build.getId() + "</a><br>");
					}
				}
				// Display build info for specific build if link is clicked
				if (target.startsWith("/build_")) {
					response.getWriter().println("<br><a href=\"/\">Home</a><br></br>");
					int buildId = Integer.parseInt(target.substring(7, target.length() - 1));
					response.getWriter().println(webHandler.buildInfoToString(buildId));
				}

			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Prints information about PushPayload object received from GitHub webhook to standard output
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
