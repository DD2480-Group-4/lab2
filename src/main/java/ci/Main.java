package ci;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import ci.PushPayload.Commit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Main executable for Continuous-Integration handler
 */
public class Main extends AbstractHandler
{

	@Override
	public void handle(
		String target,
		Request baseRequest,
		HttpServletRequest request,
		HttpServletResponse response
	) throws IOException, ServletException {
		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);

		PushPayload payload = new PushPayload(request.getReader().readLine());

		printPushPayload(payload);
		

		response.getWriter().println("CI job done");
	}

	/**
	 * Prints information about PushPayload object received from GitHub webhook to standard output
	 * @param payload Parsed payload object
	 */
	private void printPushPayload(PushPayload payload)
	{
		System.out.println("-------------------------");
		StringBuilder sb = new StringBuilder();
		sb.append("CI job started\n");
		sb.append("Repository: ").append(payload.getCloneUrl()).append("\n");
		sb.append("Branch: ").append(payload.getBranch()).append("\n");
		sb.append("Pushed at: ").append(payload.getPushedAt()).append("\n");
		sb.append("Sender: ").append(payload.getSender().name()).append("\n");
		sb.append("Commits: \n");
		for (Commit commit : payload.getCommits()) {
			sb.append("\t");
			sb.append("Author: ").append(commit.author().name());
			sb.append(" | Message: ").append(commit.message()).append("\n");
		}
		
		System.out.println(sb.toString());
	}
 
	/** Initialize CI server 
	 */
	public static void main(String[] args) throws Exception {
		Server server = new Server(8080);
		server.setHandler(new Main());
		server.start();
		server.join();
	}

}
