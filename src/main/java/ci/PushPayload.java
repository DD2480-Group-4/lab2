package ci;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


/** Represent payload from GitHub webhook on push events
 * 
 */
public class PushPayload {

    private String cloneUrl;
    private String branch;
    private LocalDateTime pushedAt;
    private Commit[] commits;
    private Sender sender;

    /**
     * Creates a PushPayload object
     * @param requestJsonPayload Payload json from webhook HTTP request
     */
    public PushPayload(String requestJsonPayload) throws JsonProcessingException, IOException {
        JsonNode payloadNode = new ObjectMapper().readTree(requestJsonPayload);

        cloneUrl = payloadNode.get("repository").get("clone_url").asText();
        branch = payloadNode.get("ref").asText().replace("refs/heads/", "");

        long pushedAtUnixTimestamp = payloadNode.get("repository").get("pushed_at").asLong();
        pushedAt = LocalDateTime.ofInstant(Instant.ofEpochSecond(pushedAtUnixTimestamp), ZoneId.systemDefault());

        JsonNode commitsNode = payloadNode.get("commits");
        commits = new Commit[commitsNode.size()];
        int commitIndex = 0;
        for (JsonNode commitNode : commitsNode) {

            String message = commitNode.get("message").asText();
            String url = commitNode.get("url").asText();

            Author author = new Author(
                    commitNode.get("author").get("name").asText(),
                    commitNode.get("author").get("username").asText(),
                    commitNode.get("author").get("email").asText());

            List<String> modifiedFilesList = new ArrayList<String>();

            for (JsonNode modifiedFileNode : commitNode.get("modified")) {
                modifiedFilesList.add(modifiedFileNode.asText());
            }
            String[] modifiedFiles = modifiedFilesList.toArray(new String[0]);
            commits[commitIndex++] = new Commit(message, author, url, modifiedFiles);
        }

        sender = new Sender(
                payloadNode.get("sender").get("login").asText(),
                payloadNode.get("sender").get("url").asText(),
                payloadNode.get("sender").get("avatar_url").asText());
        
    }


    /** Gets branch
     * @return Name of git branch in payload
    */
    public String getBranch() {
        return branch;
    }

    /** Gets clone url
     * @return Url to clone repository
     */
    public String getCloneUrl() {
        return cloneUrl;
    }

    /** Gets time for git push
     * @return Local time for push event
     */
    public LocalDateTime getPushedAt() {
        return pushedAt;
    }

    /** Gets all commits in push
     * @return Array of all commit object in push
     */
    public Commit[] getCommits() {
        return commits;
    }

    /** Gets sender
     * @return Sender object for push event
     */
    public Sender getSender() {
        return sender;
    }

    /**
     * Commit author information
     * @param name Name of author
     * @param userName Username of author
     * @param email email of author
     */
    public record Author(String name, String userName, String email) {
    }

    /**
     * Git commit information
     * @param message Commit message
     * @param author Commit author
     * @param url Commit url
     * @param modifiedFiles Paths to modified files in commit
     */
    public record Commit(String message, Author author, String url, String[] modifiedFiles) {
    }

    /**
     * GitHub user triggering webhook event
     * @name Name of sender
     * @url GitHub user Url
     * @avatarUrl GitHub avatar of user
     */
    public record Sender(String name, String url, String avatarUrl) {
    }
}
