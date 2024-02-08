package ci;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.nio.file.Files;

import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;


import java.io.IOException;
import java.nio.file.Path;

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
	@DisplayName("Check file deletion")
	void testRepositoryCloning() throws GitAPIException, IOException {

		Builder builder = new Builder();

		String currentDir = System.getProperty("user.dir");
		String testRemotePath = currentDir.concat("/remotetest");
		String dirPath = currentDir.concat("/temp");

		File testRemote = new File(testRemotePath);
		File testDir = new File(dirPath);

		// Creates the remote dir
		testRemote.mkdirs();

		try {

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
			builder.deleteDirectory(testRemote);
			builder.deleteDirectory(testDir);
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

		Builder builder = new Builder();

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

		builder.deleteDirectory(testDir);

		assertFalse(testDir.exists());
		assertFalse(testFile.exists());
	}


	/**
	 * BuilderTest:
	 * Attempts to build a Gradle project.
	 * The project is expected to compile successfully.
	 */
	@Test
	@DisplayName("Self-build test")
	void buildProject() {
		var buildDir = Path.of("./src/test/resources/build_success");
		try (var builder = new Builder(buildDir)) {
			Assertions.assertThat(builder.build()).isEqualTo(CommitStatuses.success);
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
		try (var builder = new Builder(buildDir)) {
			Assertions.assertThat(builder.build()).isEqualTo(CommitStatuses.failure);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Assertions.assertThat(buildDir.resolve(".gradle").toFile().exists()).isFalse();
		Assertions.assertThat(buildDir.resolve("build").toFile().exists()).isFalse();
	}

}
