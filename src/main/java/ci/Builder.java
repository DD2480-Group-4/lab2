package ci;

import org.apache.commons.io.FileUtils;
import org.gradle.tooling.BuildException;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ResultHandler;
import org.gradle.tooling.internal.consumer.BlockingResultHandler;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Builder that can run gradle actions on projects.
 * Create a new builder with {@link Builder#Builder(Path)}} and run
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

	/**
	 * Creates a new builder.
	 * @param projectDir The directory of the project to build.
	 */
	public Builder(Path projectDir) {
		this.projectDir = projectDir;
		connector = GradleConnector.newConnector();
		connector.forProjectDirectory(projectDir.toFile());
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
	 * Try to compile the project.
	 * TODO Run tests also
	 * @return {@link CommitStatuses#failure} if the build fails, {@link CommitStatuses#success} otherwise.
	 */
	public CommitStatuses build() {
		var handler = new BlockingResultHandler<>(Void.class);
		try {
			runTasks(launcher -> launcher.forTasks("build"), handler);
			handler.getResult();
			return CommitStatuses.success;
		} catch (BuildException ignored) {
			return CommitStatuses.failure;
		}
	}
}
