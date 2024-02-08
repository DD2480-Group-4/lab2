package ci;

import org.assertj.core.api.Assertions;
import org.gradle.tooling.internal.consumer.BlockingResultHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class BuilderTest {

	@Test
	@DisplayName("Self-build test")
	void buildProject() {
		var builder = new Builder("../lab1");
		var handler = new BlockingResultHandler<>(Object.class);
		builder.runTasks(
			launcher -> launcher.forTasks("build"),
			handler
		);
		Assertions.assertThat(handler.getResult()).isNull();
	}

}
