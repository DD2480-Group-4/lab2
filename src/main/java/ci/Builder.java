package ci;

import org.apache.commons.io.FileUtils;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ResultHandler;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

public class Builder implements AutoCloseable {

	private final GradleConnector connector;
	private final Path projectDir;

	public Builder(Path projectDir) {
		this.projectDir = projectDir;
		connector = GradleConnector.newConnector();
		connector.forProjectDirectory(projectDir.toFile());
	}

	public void runTasks(Consumer<BuildLauncher> executeTasks, ResultHandler<? super Void> handler) {
		try (var connection = connector.connect()) {
			var taskRunner = connection.newBuild();
			executeTasks.accept(taskRunner);
			taskRunner.run(handler);
		}
	}

	@Override
	public void close() throws IOException {
		FileUtils.deleteDirectory(projectDir.resolve(".gradle").toFile());
		FileUtils.deleteDirectory(projectDir.resolve("build").toFile());
	}
}
