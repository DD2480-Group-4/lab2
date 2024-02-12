plugins {
	id("java")
}

group = "group4"
version = "1.0-SNAPSHOT"

repositories {
	mavenCentral()
}

dependencies {
	// https://mvnrepository.com/artifact/javax.servlet/javax.servlet-api
	compileOnly("javax.servlet:javax.servlet-api:3.0.1")
	// https://mvnrepository.com/artifact/org.eclipse.jetty/jetty-servlet
	implementation("org.eclipse.jetty:jetty-servlet:7.0.2.v20100331")
	implementation("com.fasterxml.jackson.core:jackson-databind:2.8.9")

	implementation("org.gradle:gradle-tooling-api:7.3-20210825160000+0000")
	implementation("ch.qos.logback:logback-classic:1.4.14")

	// https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc
	implementation("org.xerial:sqlite-jdbc:3.45.1.0")
	implementation("org.slf4j:slf4j-api:1.7.25")

	testImplementation(platform("org.junit:junit-bom:5.9.1"))
	testImplementation("org.junit.jupiter:junit-jupiter")
	testImplementation("org.assertj:assertj-core:3.25.1")

	// https://mvnrepository.com/artifact/org.eclipse.jgit/org.eclipse.jgit
	implementation("org.eclipse.jgit:org.eclipse.jgit:2.2.0.201212191850-r")

	// https://mvnrepository.com/artifact/commons-io/commons-io
	implementation("commons-io:commons-io:2.6")

	testImplementation("org.mockito:mockito-core:3.+")
}

tasks.test {
	useJUnitPlatform()
}
