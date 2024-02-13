package ci;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gradle.tooling.BuildException;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ResultHandler;
import org.gradle.tooling.internal.consumer.BlockingResultHandler;

import ci.BuildInfo.TestDetails;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builder that can run gradle actions on projects.
 * Create a new builder with {@link Builder#Builder(Path, OutputStream)}} and
 * run
 * {@link Builder#runTasks(Consumer, ResultHandler)}.
 * For example:
 * try (var builder = new Builder()) {
 * builder.runTasks(
 * launcher -> launcher.forTasks("build"),
 * new BlockingResultHandler<>(Void.class)
 * );
 * } catch (IOException err) {
 * throw new RuntimeException(err);
 * }
 */
public class Builder implements AutoCloseable {

	private final Pattern testResultPattern = Pattern.compile("<div class=\"counter\">[0-9]+</div>");

	private final GradleConnector connector;
	private final Path projectDir;
	private final OutputStream buildOutput;
	private final OutputStream testOutput;

	/**
	 * Creates a new builder.
	 * 
	 * @param projectDir The directory of the project to build.
	 * @param output     The output stream to output the logs to.
	 */
	public Builder(Path projectDir, OutputStream buildOutput, OutputStream testOutput) {
		this.projectDir = projectDir;
		connector = GradleConnector.newConnector();
		connector.forProjectDirectory(projectDir.toFile());
		this.buildOutput = buildOutput;
		this.testOutput = testOutput;
	}

	/**
	 * Run gradle tasks on the project.
	 * Example invocation:
	 * builder.runTasks(
	 * launcher -> launcher.forTasks("build"),
	 * new BlockingResultHandler<>(Void.class)
	 * );
	 * 
	 * @param executeTasks Allows you to run tasks on the {@link BuildLauncher}.
	 * @param handler      The {@link ResultHandler}, allows you to get the result
	 *                     from the compilation.
	 */
	public void runTasks(Consumer<BuildLauncher> executeTasks, ResultHandler<? super Void> handler,
			OutputStream outputStream) {
		try (var connection = connector.connect()) {
			var taskRunner = connection.newBuild();
			taskRunner.setStandardOutput(outputStream);
			taskRunner.setStandardError(outputStream);
			executeTasks.accept(taskRunner);
			taskRunner.run(handler);
		}
	}

	/**
	 * Removes gradle-specific files.
	 * 
	 * @throws IOException If it is unable to remove the files.
	 */
	@Override
	public void close() throws IOException {
		FileUtils.deleteDirectory(projectDir.resolve(".gradle").toFile());
		FileUtils.deleteDirectory(projectDir.resolve("build").toFile());
	}

	/**
	 * Try to compile the project and run tests.
	 * 
	 * @return {@link CommitStatuses#failure} if the build fails,
	 *         {@link CommitStatuses#error} if the build succeeds, but the tests
	 *         fail,
	 *         {@link CommitStatuses#success} otherwise.
	 */
	public BuildResults buildAndTest() {
		var handler = new BlockingResultHandler<>(Void.class);
		int totalTests = 0;
		int passedTests = 0;
		CommitStatuses status = null;
		try {
			runTasks(launcher -> launcher.forTasks("assemble"), handler, buildOutput);
			handler.getResult();

			try {
				runTasks(launcher -> launcher.forTasks("test"), handler, testOutput);

				String testResultFile = Files.readString(projectDir.resolve("build/reports/tests/test/index.html"));
				Matcher matcher = testResultPattern.matcher(testResultFile);

				matcher.find();
				totalTests = Integer.parseInt(matcher.group().split(">")[1].split("<")[0]);
				matcher.find();
				passedTests = totalTests - Integer.parseInt(matcher.group().split(">")[1].split("<")[0]);

				handler.getResult();
				status = CommitStatuses.success;
			} catch (BuildException ignored) {
				status = CommitStatuses.error;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (BuildException ignored) {
			status = CommitStatuses.failure;
		}

		return new BuildResults(status, totalTests, passedTests);
	}

	private boolean deleteAfter = false;

	/**
	 * Clones repository using provided Url into temporary directory
	 * Checks out the provided branch
	 * Deletes the temporary directory at the end
	 *
	 * @param targetRepoUrl Url to repository to clone
	 * @param targetBranch  Branch from repository to be checked out
	 */
	public void cloneTargetRepo(String targetRepoUrl, String targetBranch) throws GitAPIException {
		Git git;

		File dir = projectDir.toFile();
		// Deletes the temp directory if it already exists, then creates a new temp dir
		deleteDirectory(dir);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		try {
			// Clone
			System.out.println("Cloning " + targetRepoUrl + " into " + dir);
			git = Git.cloneRepository()
					.setURI(targetRepoUrl)
					.setDirectory(dir)
					.call();
			System.out.println("Completed Cloning");
		} catch (GitAPIException e) {
			System.out.println("Exception occurred while cloning repository");
			e.printStackTrace();
			return;
		}

		try {
			// Checkout branch
			System.out.println("Checking out branch " + targetBranch + " of repo " + targetRepoUrl);
			git.branchCreate()
					.setName(targetBranch)
					.setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
					.setStartPoint("origin/" + targetBranch)
					.call();
			git.checkout()
					.setName(targetBranch)
					.call();
			System.out.println("Completed Branch-Checkout");
		} catch (GitAPIException e) {
			System.out.println("Exception occurred while checking out branch");
			e.printStackTrace();
		} finally {
			// Close the Git repository to release resources
			if (git != null && git.getRepository() != null) {
				git.getRepository().close();
			}
		}

		// Delete the temp directory if needed
		if (deleteAfter) {
			deleteDirectory(dir);
		}
	}

	/**
	 * Deletes a given directory if it exists
	 *
	 * @param directory Directory to be deleted
	 */
	public static void deleteDirectory(File directory) {
		if (directory.exists()) {
			try {
				FileUtils.deleteDirectory(directory);
				System.out.println("Temp directory deleted");
			} catch (IOException e) {
				System.out.println("Exception occurred while deleting temp directory");
				e.printStackTrace();
			}
		}
	}

	public record BuildResults(CommitStatuses status, int totalTests, int passedTests) {
	}
}
