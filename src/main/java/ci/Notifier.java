package ci;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Represents the notifier for build updates to GitHub users.
 */
public class Notifier {
	private final PushPayload payload;
	private final HttpClient client;

	/**
	 * Creates a notifier object with push payload-specific details
	 *
	 * @param payload The push payload
	 * @param client  The HTTP client
	 */
	public Notifier(PushPayload payload, HttpClient client) {
		this.payload = payload;
		this.client = client;
	}

	/**
	 * Function that generates a commit status request and sends it to the GitHub API.
	 *
	 * @param state        The state of the commit status (error, failure, pending or success)
	 * @param description  Description to give context regarding commit status state
	 * @param buildInfoUrl URL to build info regarding the commit
	 * @return true if HTTP response code is 201 (successfully updated commit status), false otherwise
	 * @throws IOException          If an I/O error occurs
	 * @throws InterruptedException If the operation is interrupted
	 */
	public boolean setCommitStatus(CommitStatuses state, String description, String buildInfoUrl) throws IOException, InterruptedException {
		PushPayload.Commit headCommit = payload.getCommits()[payload.getCommits().length - 1];
		String sha = headCommit.sha();
		// Generate GitHub commit status request
		String apiUrl = getApiUrl(sha);
		String jsonData = getJsonData(state, description, buildInfoUrl);

		String token = System.getenv("GITHUB_COMMIT_STATUS_TOKEN");

		// HTTP request skeleton from: https://openjdk.org/groups/net/httpclient/recipes.html
		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create(apiUrl))
			.header("Accept", "application/vnd.github+json")
			.header("Authorization", "Bearer " + token)
			.header("X-GitHub-Api-Version", "2022-11-28")
			.POST(HttpRequest.BodyPublishers.ofString(jsonData))
			.build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		// Verify if commit status update was successful
		if (response.statusCode() != 201) {
			System.err.println("ERROR: Commit status update was unsuccessful (status code: " + response.statusCode() + ")." +
				" For more information, see response body contents: " + response.body());
			return false;
		} else {
			System.out.println("Status of commit \"" + headCommit.sha() + "\" was successfully updated to: \"" + state + "\"!");
			return true;
		}
	}

	/**
	 * Function that generates API Url to interact with GitHub API to generate commit status.
	 *
	 * @param sha Commit hash (id)
	 * @return API Url
	 */
	public String getApiUrl(String sha) {
		return "https://api.github.com/repos/" + payload.getRepo() + "/statuses/" + sha;
	}

	/**
	 * Function that generates json data used in HTTP request body required by GitHub API to generate commit statuses.
	 *
	 * @param state        The state of the commit status (error, failure, pending or success)
	 * @param description  Description to give context regarding commit status state
	 * @param buildInfoUrl URL to build info regarding the commit
	 * @return json data
	 */
	public String getJsonData(CommitStatuses state, String description, String buildInfoUrl) {
		return "{\"state\": \"" + state + "\", \"description\": \"" + description + "\", \"target_url\": \"" + buildInfoUrl + "\"}";
	}

}
