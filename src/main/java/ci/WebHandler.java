package ci;

import ci.PushPayload.Commit;

import java.util.List;

/**
 * WebHandler class to handle web requests.
 */
public class WebHandler {
	private final List<BuildInfo> history;

	/**
	 * Creates a new WebHandler object.
	 *
	 * @param history List of build history
	 */
	public WebHandler(List<BuildInfo> history) {
		this.history = history;
	}

	/**
	 * Returns a string representation of the build info for a specific build id.
	 *
	 * @param buildId The id of the build
	 * @return A string representation of the build info
	 */
	public String buildInfoToString(int buildId) {
		StringBuilder sb = new StringBuilder();

		BuildInfo build = null;
		for (BuildInfo b : history) {
			if (b.getId() == buildId) {
				build = b;
				break;
			}
		}
		if (build == null) {
			return "<strong>Build not found</strong>";
		}

		sb.append("<strong>Build Info for build id: " + build.getId() + "</strong><br/>");
		sb.append("Sender: " + build.getSender().name() + "<br/>");
		sb.append("Commits: <br/>");
		for (Commit commit : build.getCommitList()) {
			sb.append("&emsp;");
			sb.append("Author: " + commit.author().name());
			sb.append(" | Message: " + commit.message() + "<br/>");
		}
		sb.append("Build Details: <br/>");
		sb.append("&emsp;Result: " + build.getBuildDetails().buildResult() + "<br/>");
		sb.append("&emsp;Log: " + build.getBuildDetails().buildLog() + "<br/>");
		sb.append("Test Details: <br/>");
		sb.append("&emsp;Total: " + build.getTestDetails().totalTests() + "<br/>");
		sb.append("&emsp;Passed: " + build.getTestDetails().numOfPassedTests() + "<br/>");
		sb.append("&emsp;Log: " + build.getTestDetails().testLog() + "<br/>");
		return sb.toString();
	}
}
