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
	public String buildInfoToHtmlString(int buildId) {
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

		sb.append("<strong>Build Info for build " + build.getId() + " on branch "+ build.getBranch() + "</strong><br/>");
		sb.append("Sender:<br/>");
		sb.append("&emsp;<img src=\"" + build.getSender().avatarUrl() + "\" alt=\"Avatar\" width=\"50\" height=\"50\"><br/>");
		sb.append("&emsp;Name: " + build.getSender().name() + "<br/>");
		sb.append("&emsp;URL: " + build.getSender().url() + "<br/>");

		sb.append("<br/>Commits: <br/>");
		for (Commit commit : build.getCommitList()) {
			sb.append("&emsp;Id: " + commit.sha() + "<br/>");
			sb.append("&emsp;Message: " + commit.message() + "<br/>");
			sb.append("&emsp;Author: " + commit.author().name() + "<br/>");
			sb.append("&emsp;&emsp;Name: " + commit.author().userName() + ".<br/>");
			sb.append("&emsp;&emsp;Email: " + commit.author().email() + ".<br/><br/>");
		}
		sb.append("Build Details: <br/>");
		sb.append("&emsp;Time: " + build.getBuildDate() + "<br/>");


		CommitStatuses status = CommitStatuses.values()[build.getBuildDetails().buildResult()];
		switch (status) {
			case success:
				sb.append("&emsp;Result: success <br/>");
				break;
			case error:
				sb.append("&emsp;Result: Test error <br/>");
				break;
			case failure:
				sb.append("&emsp;Result: Build failure <br/>");
				break;
			default:
				sb.append("&emsp;Result: " + status.toString() + " <br/>");

				break;
		}
		sb.append("&emsp;Log: " + build.getBuildDetails().buildLog() + "<br/>");
		sb.append("<br/>Test Details: <br/>");
		sb.append("&emsp;Total: " + build.getTestDetails().totalTests() + "<br/>");
		sb.append("&emsp;Passed: " + build.getTestDetails().numOfPassedTests() + "<br/>");
		sb.append("&emsp;Log: " + build.getTestDetails().testLog() + "<br/>");
		return sb.toString();
	}
}
