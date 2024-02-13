# lab2: Continuous Integration

This is a continuous integration server for Java projects that are using [Gradle](https://gradle.org/). It can be set up as a webhook for public GitHub repositories where it will build and test the program, then both report the result as a commit-status in GitHub and save the result for later access.

## Build and run
The server is built with Java 21 using Gradle. Follow these steps to compile the project: 

### With IDE
1. Set Java version to Java 21.
2. Import into gradle-compatible IDE and run.

### In Terminal 
1. Set Java version to Java 21.
2. Run `./gradlew build` .
3. `cd <BUILD PATH>`
4. COMMAND TO RUN HERE

## How to use:
Build and run the server, then setup GitHub to send webhooks to it. The results from the build and tests will be set as the commit-status on GitHub of the last commit in the push. History of the builds can also be accessed in a web-browser at the servers URL. 

### GitHub Webhook setup: 
To setup the GitHub webhook go to your public repository, select settings and then Webhooks. Click Add webhook, enter the URL to where your server is hosted, select `application/json` as content type, let the Active box be checked and click Add Webhook. 
![GitHub Webhook settings](Assets/WebhookSetup.png)

### Web Interface 
TODO: Display web interface functions

# Statement of Contributions:
All contributions of features and fixes includes accompanying tests.

#### Shared amongst the team
* Server listens to and parses webhook requests

#### Douglas Fischer (DouglasFischer):
* Clone and cleanup repository.
* Feature integrations.

#### Erik Winbladh (ractodev):
* GitHub commit status notification (Lead-author).
* History database with data access object (Co-author).
* History web interface.

#### Johan Norlin (Acuadragon100):
* Server builds projects.
* Server runs project tests. 
* Feature integrations. 

#### Robin Claesson (RobinClaesson):
* GitHub commit status notification (Co-author).
* History database with data access object (Lead-author).
* README.md 

### Prideful Remark
The history is built with a SQLite database with multiple tables and an accompanying data access object (DAO). The database can handle the edge cases of a commit being part of two different pushes without duplicating the commit entry. 

![Database relations](Assets/database.png)
