package ci;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class NotifierTest {
	private final String requestJsonPayload = "{\"ref\":\"refs/heads/push_branch\",\"before\":\"6d6c532899920951a9616ef38a2055258fdd2761\",\"after\":\"17a4c2ec28144d4b195d2e7dee7e605f66ce65f8\",\"repository\":{\"id\":751736168,\"node_id\":\"R_kgDOLM6VaA\",\"name\":\"lab2\",\"full_name\":\"DD2480-Group-4/lab2\",\"private\":true,\"owner\":{\"name\":\"DD2480-Group-4\",\"email\":null,\"login\":\"DD2480-Group-4\",\"id\":157609974,\"node_id\":\"O_kgDOCWTv9g\",\"avatar_url\":\"https://avatars.githubusercontent.com/u/157609974?v=4\",\"gravatar_id\":\"\",\"url\":\"https://api.github.com/users/DD2480-Group-4\",\"html_url\":\"https://github.com/DD2480-Group-4\",\"followers_url\":\"https://api.github.com/users/DD2480-Group-4/followers\",\"following_url\":\"https://api.github.com/users/DD2480-Group-4/following{/other_user}\",\"gists_url\":\"https://api.github.com/users/DD2480-Group-4/gists{/gist_id}\",\"starred_url\":\"https://api.github.com/users/DD2480-Group-4/starred{/owner}{/repo}\",\"subscriptions_url\":\"https://api.github.com/users/DD2480-Group-4/subscriptions\",\"organizations_url\":\"https://api.github.com/users/DD2480-Group-4/orgs\",\"repos_url\":\"https://api.github.com/users/DD2480-Group-4/repos\",\"events_url\":\"https://api.github.com/users/DD2480-Group-4/events{/privacy}\",\"received_events_url\":\"https://api.github.com/users/DD2480-Group-4/received_events\",\"type\":\"Organization\",\"site_admin\":false},\"html_url\":\"https://github.com/DD2480-Group-4/lab2\",\"description\":\"Repo for lab 2 in DD2480\",\"fork\":false,\"url\":\"https://github.com/DD2480-Group-4/lab2\",\"forks_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/forks\",\"keys_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/keys{/key_id}\",\"collaborators_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/collaborators{/collaborator}\",\"teams_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/teams\",\"hooks_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/hooks\",\"issue_events_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/issues/events{/number}\",\"events_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/events\",\"assignees_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/assignees{/user}\",\"branches_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/branches{/branch}\",\"tags_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/tags\",\"blobs_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/git/blobs{/sha}\",\"git_tags_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/git/tags{/sha}\",\"git_refs_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/git/refs{/sha}\",\"trees_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/git/trees{/sha}\",\"statuses_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/statuses/{sha}\",\"languages_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/languages\",\"stargazers_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/stargazers\",\"contributors_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/contributors\",\"subscribers_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/subscribers\",\"subscription_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/subscription\",\"commits_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/commits{/sha}\",\"git_commits_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/git/commits{/sha}\",\"comments_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/comments{/number}\",\"issue_comment_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/issues/comments{/number}\",\"contents_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/contents/{+path}\",\"compare_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/compare/{base}...{head}\",\"merges_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/merges\",\"archive_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/{archive_format}{/ref}\",\"downloads_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/downloads\",\"issues_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/issues{/number}\",\"pulls_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/pulls{/number}\",\"milestones_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/milestones{/number}\",\"notifications_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/notifications{?since,all,participating}\",\"labels_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/labels{/name}\",\"releases_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/releases{/id}\",\"deployments_url\":\"https://api.github.com/repos/DD2480-Group-4/lab2/deployments\",\"created_at\":1706861042,\"updated_at\":\"2024-02-05T13:48:33Z\",\"pushed_at\":1707217745,\"git_url\":\"git://github.com/DD2480-Group-4/lab2.git\",\"ssh_url\":\"git@github.com:DD2480-Group-4/lab2.git\",\"clone_url\":\"https://github.com/DD2480-Group-4/lab2.git\",\"svn_url\":\"https://github.com/DD2480-Group-4/lab2\",\"homepage\":null,\"size\":86,\"stargazers_count\":0,\"watchers_count\":0,\"language\":\"Java\",\"has_issues\":true,\"has_projects\":true,\"has_downloads\":true,\"has_wiki\":false,\"has_pages\":false,\"has_discussions\":false,\"forks_count\":0,\"mirror_url\":null,\"archived\":false,\"disabled\":false,\"open_issues_count\":1,\"license\":null,\"allow_forking\":false,\"is_template\":false,\"web_commit_signoff_required\":false,\"topics\":[],\"visibility\":\"private\",\"forks\":0,\"open_issues\":1,\"watchers\":0,\"default_branch\":\"main\",\"stargazers\":0,\"master_branch\":\"main\",\"organization\":\"DD2480-Group-4\",\"custom_properties\":{}},\"pusher\":{\"name\":\"ractodev\",\"email\":\"erik.j.winbladh@gmail.com\"},\"organization\":{\"login\":\"DD2480-Group-4\",\"id\":157609974,\"node_id\":\"O_kgDOCWTv9g\",\"url\":\"https://api.github.com/orgs/DD2480-Group-4\",\"repos_url\":\"https://api.github.com/orgs/DD2480-Group-4/repos\",\"events_url\":\"https://api.github.com/orgs/DD2480-Group-4/events\",\"hooks_url\":\"https://api.github.com/orgs/DD2480-Group-4/hooks\",\"issues_url\":\"https://api.github.com/orgs/DD2480-Group-4/issues\",\"members_url\":\"https://api.github.com/orgs/DD2480-Group-4/members{/member}\",\"public_members_url\":\"https://api.github.com/orgs/DD2480-Group-4/public_members{/member}\",\"avatar_url\":\"https://avatars.githubusercontent.com/u/157609974?v=4\",\"description\":null},\"sender\":{\"login\":\"ractodev\",\"id\":78312752,\"node_id\":\"MDQ6VXNlcjc4MzEyNzUy\",\"avatar_url\":\"https://avatars.githubusercontent.com/u/78312752?v=4\",\"gravatar_id\":\"\",\"url\":\"https://api.github.com/users/ractodev\",\"html_url\":\"https://github.com/ractodev\",\"followers_url\":\"https://api.github.com/users/ractodev/followers\",\"following_url\":\"https://api.github.com/users/ractodev/following{/other_user}\",\"gists_url\":\"https://api.github.com/users/ractodev/gists{/gist_id}\",\"starred_url\":\"https://api.github.com/users/ractodev/starred{/owner}{/repo}\",\"subscriptions_url\":\"https://api.github.com/users/ractodev/subscriptions\",\"organizations_url\":\"https://api.github.com/users/ractodev/orgs\",\"repos_url\":\"https://api.github.com/users/ractodev/repos\",\"events_url\":\"https://api.github.com/users/ractodev/events{/privacy}\",\"received_events_url\":\"https://api.github.com/users/ractodev/received_events\",\"type\":\"User\",\"site_admin\":false},\"created\":false,\"deleted\":false,\"forced\":false,\"base_ref\":null,\"compare\":\"https://github.com/DD2480-Group-4/lab2/compare/6d6c53289992...17a4c2ec2814\",\"commits\":[{\"id\":\"7efa6befd4d726a4197de78595c750f1aef2cbe3\",\"tree_id\":\"00d5a5afd8ad576745c00b6e11308f895e3aee36\",\"distinct\":true,\"message\":\"moc push\",\"timestamp\":\"2024-02-06T12:08:13+01:00\",\"url\":\"https://github.com/DD2480-Group-4/lab2/commit/7efa6befd4d726a4197de78595c750f1aef2cbe3\",\"author\":{\"name\":\"ractodev\",\"email\":\"erik.j.winbladh@gmail.com\",\"username\":\"ractodev\"},\"committer\":{\"name\":\"ractodev\",\"email\":\"erik.j.winbladh@gmail.com\",\"username\":\"ractodev\"},\"added\":[],\"removed\":[],\"modified\":[\"src/main/java/ci/Builder.java\",\"src/main/java/ci/Main.java\",\"src/main/java/ci/Notifier.java\"]},{\"id\":\"17a4c2ec28144d4b195d2e7dee7e605f66ce65f8\",\"tree_id\":\"1f7d710666219a46769843d95f663d1bf6f77cbe\",\"distinct\":true,\"message\":\"moc push2\",\"timestamp\":\"2024-02-06T12:08:51+01:00\",\"url\":\"https://github.com/DD2480-Group-4/lab2/commit/17a4c2ec28144d4b195d2e7dee7e605f66ce65f8\",\"author\":{\"name\":\"ractodev\",\"email\":\"erik.j.winbladh@gmail.com\",\"username\":\"ractodev\"},\"committer\":{\"name\":\"ractodev\",\"email\":\"erik.j.winbladh@gmail.com\",\"username\":\"ractodev\"},\"added\":[],\"removed\":[],\"modified\":[\"src/test/java/ci/MainTest.java\"]}],\"head_commit\":{\"id\":\"17a4c2ec28144d4b195d2e7dee7e605f66ce65f8\",\"tree_id\":\"1f7d710666219a46769843d95f663d1bf6f77cbe\",\"distinct\":true,\"message\":\"moc push2\",\"timestamp\":\"2024-02-06T12:08:51+01:00\",\"url\":\"https://github.com/DD2480-Group-4/lab2/commit/17a4c2ec28144d4b195d2e7dee7e605f66ce65f8\",\"author\":{\"name\":\"ractodev\",\"email\":\"erik.j.winbladh@gmail.com\",\"username\":\"ractodev\"},\"committer\":{\"name\":\"ractodev\",\"email\":\"erik.j.winbladh@gmail.com\",\"username\":\"ractodev\"},\"added\":[],\"removed\":[],\"modified\":[\"src/test/java/ci/MainTest.java\"]}}";
	private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();
    private final PrintStream origOut = System.out;
    private final PrintStream origErr = System.err;

    @BeforeEach
    void setUpStream() {
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
    }

    @AfterEach
    void closeStream() {
        System.setOut(origOut);
        System.setErr(origErr);
    }

	/**
	 *	setCommitStatus valid Test:
	 *	Sends an HTTP request to mock HTTP client and receives success response.
 	 * 	Is expected to print the correct information, while only performing one
	 * 	HTTP call.
	 */
	@Test
	@DisplayName("setCommitStatus success response")
	void setCommitStatus_successResponseCode_returnsTrueAndPrintsInfo() throws IOException, InterruptedException {
		// Set up mock http client
		HttpClient mockClient = Mockito.mock(HttpClient.class);
		HttpResponse<Object> mockResponse = Mockito.mock(HttpResponse.class);

		// Verify 201 response status code
		Mockito.when(mockResponse.statusCode())
			.thenReturn(201);
		Mockito.when(mockClient.send(Mockito.any(), Mockito.any())).thenReturn(mockResponse);

		// Generate push payload and initialize notifier
		PushPayload payload = new PushPayload(requestJsonPayload);
		Notifier notifier = new Notifier(payload, mockClient);

		boolean httpStatus = notifier.setCommitStatus(CommitStatuses.success, "Test success", "");

		Assertions.assertThat(httpStatus).isTrue();

		String expectedSha = "17a4c2ec28144d4b195d2e7dee7e605f66ce65f8";
		String expectedState = String.valueOf(CommitStatuses.success);
		Assertions.assertThat(out.toString().stripTrailing()).isEqualTo("Status of commit \"" + expectedSha + "\" was successfully updated to: \"" + expectedState + "\"!");
		Mockito.verify(mockClient, Mockito.times(1)).send(Mockito.any(), Mockito.any());
		Mockito.verifyNoMoreInteractions(mockClient);
	}

	/**
	 *	setCommitStatus invalid Test:
	 *	Sends an HTTP request to mock HTTP client and receives error response.
 	 * 	Is expected to print error context, while only performing one
	 * 	HTTP call.
	 */
	@Test
	@DisplayName("setCommitStatus error response")
	void setCommitStatus_errorResponseCode_returnsFalseAndPrintsError() throws IOException, InterruptedException {
		// Set up mock http client
		HttpClient mockClient = Mockito.mock(HttpClient.class);
		HttpResponse<Object> mockResponse = Mockito.mock(HttpResponse.class);

		// Verify 201 response status code
		Mockito.when(mockResponse.statusCode())
			.thenReturn(401);
		Mockito.when(mockResponse.body()).thenReturn("{payload}");
		Mockito.when(mockClient.send(Mockito.any(), Mockito.any())).thenReturn(mockResponse);

		// Generate push payload and initialize notifier
		PushPayload payload = new PushPayload(requestJsonPayload);
		Notifier notifier = new Notifier(payload, mockClient);

		boolean httpStatus = notifier.setCommitStatus(CommitStatuses.success, "Test success", "");

		Assertions.assertThat(httpStatus).isFalse();

		Assertions.assertThat(out.toString().stripTrailing()).isEqualTo("");
        Assertions.assertThat(err.toString().stripTrailing()).isEqualTo(
                "ERROR: Commit status update was unsuccessful (status code: " + mockResponse.statusCode() + ")." +
				" For more information, see response body contents: " + mockResponse.body());
		Mockito.verify(mockClient, Mockito.times(1)).send(Mockito.any(), Mockito.any());
		Mockito.verifyNoMoreInteractions(mockClient);
	}

	/**
	 *	getApiUrl test:
	 *	Generates apiUrl given payload.
	 *  Is expected to generate correct apiUrl.
	 */
	@Test
	@DisplayName("getApiUrl successful")
	void getApiUrl_generatesCorrectApiUrl() throws IOException {
		PushPayload payload = new PushPayload(requestJsonPayload);
		Notifier notifier = new Notifier(payload, Mockito.mock(HttpClient.class));

		PushPayload.Commit headCommit = payload.getCommits()[payload.getCommits().length - 1];
		String sha = headCommit.sha();

		String apiUrl = notifier.getApiUrl(sha);
		String expectedApiUrl = "https://api.github.com/repos/" + payload.getRepo() + "/statuses/" + sha;
		Assertions.assertThat(apiUrl).isEqualTo(expectedApiUrl);
	}

	/**
	 *	getJsonData test:
	 *	Generates jsonData given payload.
	 *  Is expected to generate correct jsonData.
	 */
	@Test
	@DisplayName("getJsonData successful")
	void getJsonData_generatesCorrectJsonData() throws IOException {
		PushPayload payload = new PushPayload(requestJsonPayload);
		Notifier notifier = new Notifier(payload, Mockito.mock(HttpClient.class));

		String description = "Test";
		String buildInfoUrl = "http://example.com/user/repo/build/sha";

		String jsonData = notifier.getJsonData(CommitStatuses.success, description, buildInfoUrl);
		String expectedJsonData = "{\"state\": \"success\", \"description\": \"" + description + "\", \"target_url\": \"" + buildInfoUrl + "\"}";;

		Assertions.assertThat(jsonData).isEqualTo(expectedJsonData);
	}

}
