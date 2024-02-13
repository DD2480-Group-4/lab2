package ci;

import ci.PushPayload.Sender;
import ci.PushPayload.Commit;

import java.security.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a BuildInfo object to map against database schema.
 */
public class BuildInfo {
	private int id;
	private Sender sender;
	private List<Commit> commitList;
	private BuildDetails buildDetails;
	private TestDetails testDetails;
	private String buildDate;
	private String branch;

	/**
	 * Creates a new BuildInfo object.
	 *
	 * @param id           Build id
	 * @param sender       Sender of push payload that triggered CI build
	 * @param commitList   List of commits associated with push payload
	 * @param buildDetails Details on build, including result and logs
	 * @param testDetails  Details on test, including total tests, number of tests passed and logs
	 * @param buildDate    Date of build as string
	 * @param branch       Branch of the build
	 */
	public BuildInfo(int id, Sender sender, List<Commit> commitList, BuildDetails buildDetails, TestDetails testDetails, String buildDate, String branch) {
		this.id = id;
		this.sender = sender;
		this.commitList = commitList;
		this.buildDetails = buildDetails;
		this.testDetails = testDetails;
		this.buildDate = buildDate;
		this.branch = branch;
	}

	/**
	 * Creates a new BuildInfo object (without id)
	 *
	 * @param sender       Sender of push payload that triggered CI build
	 * @param commitList   List of commits associated with push payload
	 * @param buildDetails Details on build, including result and logs
	 * @param testDetails  Details on test, including total tests, number of tests passed and logs
	 * @param buildDate    Date of build as LocalDateTime
	 */
	public BuildInfo(Sender sender, List<Commit> commitList, BuildDetails buildDetails, TestDetails testDetails, LocalDateTime buildDate, String branch) {
		this.sender = sender;
		this.commitList = commitList;
		this.buildDetails = buildDetails;
		this.testDetails = testDetails;
		this.buildDate = buildDate.toString();
		this.branch = branch;
	}

	/**
	 * Gets id of build
	 *
	 * @return Build id
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * Gets Sender
	 *
	 * @return Sender of push payload that triggered CI build
	 */
	public Sender getSender() {
		return this.sender;
	}

	/**
	 * Gets list of commits
	 *
	 * @return List of commits associated with push payload
	 */
	public List<Commit> getCommitList() {
		return this.commitList;
	}

	/**
	 * Gets build details
	 *
	 * @return Details on build, including result and logs
	 */
	public BuildDetails getBuildDetails() {
		return this.buildDetails;
	}

	/**
	 * Gets test details
	 *
	 * @return Details on test, including total tests, number of tests passed and logs
	 */
	public TestDetails getTestDetails() {
		return this.testDetails;
	}

	/**
	 * Gets build date
	 *
	 * @return Date of build as string
	 */
	public String getBuildDate() {
		return buildDate;
	}

	/**
	 * Gets branch
	 *
	 * @return Branch of the build
	 */
	public String getBranch() {
		return branch;
	}

	/**
	 * Sets id of build
	 *
	 * @param id Build id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Sets sender of build
	 *
	 * @param sender Sender of push payload that triggered CI build
	 */
	public void setSender(Sender sender) {
		this.sender = sender;
	}

	/**
	 * Sets list of commits
	 *
	 * @param commitList List of commits associated with push payload
	 */
	public void setCommitList(List<Commit> commitList) {
		this.commitList = commitList;
	}

	/**
	 * Sets build details
	 *
	 * @param buildDetails Details on build, including result and logs
	 */
	public void setBuildDetails(BuildDetails buildDetails) {
		this.buildDetails = buildDetails;
	}

	/**
	 * Sets test details
	 *
	 * @param testDetails Details on test, including total tests, number of tests passed and logs
	 */
	public void setTestDetails(TestDetails testDetails) {
		this.testDetails = testDetails;
	}

	/**
	 * Sets build date
	 *
	 * @param buildDate Date of build as string
	 */
	public void setBuildDate(String buildDate) {
		this.buildDate = buildDate;
	}

	/**
	 * Sets branch
	 *
	 * @param branch Branch of the build
	 */
	public void setBranch(String branch) {
		this.branch = branch;
	}

	/**
	 * Build details.
	 *
	 * @param buildResult Integer storing result of build (0 = fail, 1 = success)
	 * @param buildLog    Logs containing in-depth information regarding build
	 */
	public record BuildDetails(int buildResult, String buildLog) {
	}

	/**
	 * Test details.
	 *
	 * @param totalTests       Total tests run during build
	 * @param numOfPassedTests Number of tests passed during build
	 * @param testLog          Logs containing in-depth information regarding testing phase of build
	 */
	public record TestDetails(int totalTests, int numOfPassedTests, String testLog) {
	}

}
