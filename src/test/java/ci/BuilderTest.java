package ci;

import org.assertj.core.api.Assertions;
import org.gradle.tooling.BuildException;
import org.gradle.tooling.internal.consumer.BlockingResultHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public class BuilderTest {

	@Test
	@DisplayName("Self-build test")
	void buildProject() {
		var buildDir = Path.of("./src/test/resources/build_success");
		try (var builder = new Builder(buildDir)) {
			var handler = new BlockingResultHandler<>(Object.class);
			builder.runTasks(
				launcher -> launcher.forTasks("build"),
				handler
			);
			Assertions.assertThat(handler.getResult()).isNull();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Assertions.assertThat(buildDir.resolve(".gradle").toFile().exists()).isFalse();
		Assertions.assertThat(buildDir.resolve("build").toFile().exists()).isFalse();
	}

	@Test
	@DisplayName("Self-build failure")
	void buildFail() {
		var buildDir = Path.of("./src/test/resources/build_fail");
		try (var builder = new Builder(buildDir)) {
			var handler = new BlockingResultHandler<>(Object.class);
			builder.runTasks(
				launcher -> launcher.forTasks("build"),
				handler
			);
			boolean built = false;
			try {
				handler.getResult();
				built = true;
			} catch (BuildException ignored) {}
			Assertions.assertThat(built).isFalse();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Assertions.assertThat(buildDir.resolve(".gradle").toFile().exists()).isFalse();
		Assertions.assertThat(buildDir.resolve("build").toFile().exists()).isFalse();
	}

}
