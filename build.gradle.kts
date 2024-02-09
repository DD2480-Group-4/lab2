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

	testImplementation(platform("org.junit:junit-bom:5.9.1"))
	testImplementation("org.junit.jupiter:junit-jupiter")
	testImplementation("org.assertj:assertj-core:3.25.1")
	testImplementation("org.mockito:mockito-core:3.+")
}

tasks.test {
	useJUnitPlatform()
}
