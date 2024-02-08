package ci;

import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ResultHandler;

import java.io.File;
import java.util.function.Consumer;

public class Builder {

	private final GradleConnector connector;

	public Builder(String projectDir) {
		connector = GradleConnector.newConnector();
		connector.forProjectDirectory(new File(projectDir));
	}

	public void runTasks(Consumer<BuildLauncher> executeTasks, ResultHandler<Object> handler) {
		try (var connection = connector.connect()) {
			var taskRunner = connection.newBuild();
			executeTasks.accept(taskRunner);
			taskRunner.run(handler);
		}
	}

}
