package ci;

import org.assertj.core.api.Assertions;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.*;

public class BuilderTest {

	/*
	 * Remote Directory Cloning Test:
	 * Creates a new remote directory containing a master file.
	 * Creates a new secondary branch containing a secondary file
	 * Runs the cloneTargetRepo function on the remote directory's secondary branch.
	 * Both files and are expected to exist in the local directory.
	 * Both directories are deleted after the test.
	 */
	@Test
	@DisplayName("Test Repository Cloning")
	void testRepositoryCloning() throws GitAPIException, IOException {

		String currentDir = System.getProperty("user.dir");
		String testRemotePath = currentDir.concat("/remotetest");
		String dirPath = currentDir.concat("/temp");

		File testRemote = new File(testRemotePath);
		File testDir = new File(dirPath);

		// Creates the remote dir
		testRemote.mkdirs();

		try (Builder builder = new Builder(Path.of(dirPath))) {

			Git git = Git.init().setDirectory(testRemote).call();
			// Create a file in the master branch of the repository
			File masterFippelFileRemote = new File(testRemote, "fippel_master.txt");
			Files.write(masterFippelFileRemote.toPath(), "Hello, master!".getBytes());
			git.add().addFilepattern(".").call();
			git.commit().setMessage("Initial commit").call();
			//git.push().call();

			// Create and checkout the secondary branch
			git.branchCreate().setName("secondary").call();
			git.checkout().setName("secondary").call();

			// Create a file in the secondary branch of the repository
			File secondaryFippelFileRemote = new File(testRemote, "fippel_secondary.txt");
			Files.write(secondaryFippelFileRemote.toPath(), "Hello, secondary!".getBytes());
			git.add().addFilepattern(".").call();
			git.commit().setMessage("Secondary commit").call();
			//git.push().call();

			// Checkout master
			git.checkout().setName("master").call();

			builder.cloneTargetRepo(testRemotePath, "secondary");

			File masterFippelFilePath = new File(dirPath.concat("/fippel_master.txt"));
			File secondaryFippelFilePath = new File(dirPath.concat("/fippel_secondary.txt"));

			String secondaryContent = new String(Files.readAllBytes(secondaryFippelFilePath.toPath()));

			assertTrue(masterFippelFilePath.exists());
			assertTrue(secondaryFippelFilePath.exists());

			assertEquals("Hello, secondary!", secondaryContent);

		} catch (GitAPIException | IOException e) {
			// handle the exception or fail the test
			e.printStackTrace();
			fail("Exception occurred during the test: " + e.getMessage());
		} finally {
			Builder.deleteDirectory(testRemote);
			Builder.deleteDirectory(testDir);
		}
	}

	/*
	 * Directory Deletion Test:
	 * Creates a new directory containing a file.
	 * Runs the deleteDirectory function on the directory.
	 * Both file and directory are expected to be deleted.
	 */
	@Test
	@DisplayName("Check file deletion")
	void testDeleteDirectory() {
		String currentDir = System.getProperty("user.dir");
		String testDirPath = currentDir.concat("/temptest");

		File testDir = new File(testDirPath);
		// Creates a new temp dir
		testDir.mkdirs();

		// Creates a file in the temporary test directory
		File testFile = new File(testDir, "fippel.txt");

		try {
			testFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		assertTrue(testDir.exists());
		assertTrue(testFile.exists());

		Builder.deleteDirectory(testDir);

		assertFalse(testDir.exists());
		assertFalse(testFile.exists());
	}


	/**
	 * BuilderTest:
	 * Attempts to build a Gradle project.
	 * The project is expected to compile successfully.
	 */
	@Test
	@DisplayName("Self-build success")
	void buildAndTestProject() {
		var buildDir = Path.of("./src/test/resources/build_success");
		try (var builder = new Builder(buildDir, System.out)) {
			Assertions.assertThat(builder.buildAndTest()).isEqualTo(CommitStatuses.success);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Assertions.assertThat(buildDir.resolve(".gradle").toFile().exists()).isFalse();
		Assertions.assertThat(buildDir.resolve("build").toFile().exists()).isFalse();
	}

	/**
	 * BuilderTest:
	 * Attempts to build a Gradle project.
	 * The project is expected to compile successfully.
	 */
	@Test
	@DisplayName("Self-build test error")
	void buildProjectAndFailTest() {
		var buildDir = Path.of("./src/test/resources/build_success_test_fail");
		try (var builder = new Builder(buildDir, System.out)) {
			Assertions.assertThat(builder.buildAndTest()).isEqualTo(CommitStatuses.error);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Assertions.assertThat(buildDir.resolve(".gradle").toFile().exists()).isFalse();
		Assertions.assertThat(buildDir.resolve("build").toFile().exists()).isFalse();
	}


	/**
	 * BuilderTest:
	 * Attempts to compile a Gradle project with a syntax error.
	 * The compilation is expected to fail.
	 */
	@Test
	@DisplayName("Self-build failure")
	void buildFail() {
		var buildDir = Path.of("./src/test/resources/build_fail");
		try (var builder = new Builder(buildDir, System.out)) {
			Assertions.assertThat(builder.buildAndTest()).isEqualTo(CommitStatuses.failure);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Assertions.assertThat(buildDir.resolve(".gradle").toFile().exists()).isFalse();
		Assertions.assertThat(buildDir.resolve("build").toFile().exists()).isFalse();
	}

	/**
	 * BuilderTest:
	 * Compiles the successful project and checks that the log seems to contain the information it should.
	 * The log should contain the string "BUILD SUCCESSFUL".
	 */
	@Test
	@DisplayName("Test Output Stream")
	void testOutputStream() {
		var buildDir = Path.of("./src/test/resources/build_success");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (var builder = new Builder(buildDir, new PrintStream(out))) {
			builder.buildAndTest();
			Assertions.assertThat(out.toString().contains("BUILD SUCCESSFUL")).isTrue();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
