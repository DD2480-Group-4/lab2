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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Builder that can run gradle actions on projects.
 * Create a new builder with {@link Builder#Builder(Path, OutputStream)}} and run
 * {@link Builder#runTasks(Consumer, ResultHandler)}.
 * For example:
 * try (var builder = new Builder()) {
 *      builder.runTasks(
 *          launcher -> launcher.forTasks("build"),
 *          new BlockingResultHandler<>(Void.class)
 *      );
 * } catch (IOException err) {
 *     throw new RuntimeException(err);
 * }
 */
public class Builder implements AutoCloseable {

	private final GradleConnector connector;
	private final Path projectDir;
	private final OutputStream output;

	/**
	 * Creates a new builder.
	 * @param projectDir The directory of the project to build.
	 */
	public Builder(Path projectDir, OutputStream output) {
		this.projectDir = projectDir;
		connector = GradleConnector.newConnector();
		connector.forProjectDirectory(projectDir.toFile());
		this.output = output;
	}

	/**
	 * Run gradle tasks on the project.
	 * Example invocation:
	 * builder.runTasks(
	 *     launcher -> launcher.forTasks("build"),
	 *     new BlockingResultHandler<>(Void.class)
	 * );
	 * @param executeTasks Allows you to run tasks on the {@link BuildLauncher}.
	 * @param handler The {@link ResultHandler}, allows you to get the result from the compilation.
	 */
	public void runTasks(Consumer<BuildLauncher> executeTasks, ResultHandler<? super Void> handler) {
		try (var connection = connector.connect()) {
			var taskRunner = connection.newBuild();
			taskRunner.setStandardOutput(output);
			taskRunner.setStandardError(output);
			executeTasks.accept(taskRunner);
			taskRunner.run(handler);
		}
	}

	/**
	 * Removes gradle-specific files.
	 * @throws IOException If it is unable to remove the files.
	 */
	@Override
	public void close() throws IOException {
		FileUtils.deleteDirectory(projectDir.resolve(".gradle").toFile());
		FileUtils.deleteDirectory(projectDir.resolve("build").toFile());
	}

	/**
	 * Try to compile the project and run tests.
	 * @return {@link CommitStatuses#failure} if the build fails,
	 *         {@link CommitStatuses#error} if the build succeeds, but the tests fail,
	 *         {@link CommitStatuses#success} otherwise.
	 */
	public CommitStatuses buildAndTest() {
		var handler = new BlockingResultHandler<>(Void.class);
		try {
			runTasks(launcher -> launcher.forTasks("assemble"), handler);
			handler.getResult();

			try {
				runTasks(launcher -> launcher.forTasks("test"), handler);
				handler.getResult();
				return CommitStatuses.success;
			} catch (BuildException ignored) {
				return CommitStatuses.error;
			}
		} catch (BuildException ignored) {
			return CommitStatuses.failure;
		}
	}

	private boolean deleteAfter = false;
	/**
	 * Clones repository using provided Url into temporary directory
	 * Checks out the provided branch
	 * Deletes the temporary directory at the end
	 *
	 * @param targetRepoUrl Url to repository to clone
	 * @param targetBranch Branch from repository to be checked out
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
}
