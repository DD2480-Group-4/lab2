package ci;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import ci.BuildInfo.BuildDetails;
import ci.BuildInfo.TestDetails;
import ci.PushPayload.Author;

public class WebHandlerTest {
	private final List<BuildInfo> history = List.of(
		new BuildInfo(
			1,
			new PushPayload.Sender("John Doe", "johndoe", "john@doe.com"),
			List.of(new PushPayload.Commit("sha1", "commit1", new Author("Mr Bean", "mrbean", "beanAndTeddy@funny.org"), "commitUrl1", new String[]{"file1", "file2"})),
			new BuildDetails(1, "Build Log 1"),
			new TestDetails(1, 1, "Test Log 1"),
			"2021-01-01T00:00:00"
		),
		new BuildInfo(2, // Assuming the ID is 2 for this example
			new PushPayload.Sender("Jane Doe", "janedoe", "jane@doe.com"),
			List.of(new PushPayload.Commit("sha2", "commit2", new Author("Jane Doe", "janedoe", "jane@doe.com"), "commitUrl2", new String[]{"file3", "file4"})),
			new BuildDetails(2, "Build Log 2"),
			new TestDetails(2, 2, "Test Log 2"),
			"2021-01-02T00:00:00")
		);

	/*
	 * BuildInfo to HTML String Test:
	 * Builds a string containing the build info for a given build id
	 * from the history list.
	 */
	@Test
	@DisplayName("buildInfoToHtmlString success")
	void testBuildInfoToString() {
		WebHandler webHandler = new WebHandler(history);
		String expectedBuildInfoString = "<strong>Build Info for build id: 2</strong><br/>" +
			"Sender: Jane Doe<br/>" +
			"Commits: <br/>" +
			"&emsp;Author: Jane Doe | Message: commit2<br/>" +
			"Build Details: <br/>" +
			"&emsp;Result: 2<br/>" +
			"&emsp;Log: Build Log 2<br/>" +
			"Test Details: <br/>" +
			"&emsp;Total: 2<br/>" +
			"&emsp;Passed: 2<br/>" +
			"&emsp;Log: Test Log 2<br/>";
		Assertions.assertThat(webHandler.buildInfoToHtmlString(2)).isEqualTo(expectedBuildInfoString);
	}

	/*
	 * BuildInfo to HTML String Test:
	 * Builds a string containing "Build not found" as build id
	 * cannot be found in the history list.
	 */
	@Test
	@DisplayName("buildInfoToHtmlString build not found")
	void testBuildInfoToStringBuildNotFound() {
		WebHandler webHandler = new WebHandler(history);
		Assertions.assertThat(webHandler.buildInfoToHtmlString(3)).isEqualTo("<strong>Build not found</strong>");
	}

}
