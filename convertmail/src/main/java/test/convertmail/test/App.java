package test.convertmail.test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.List;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListThreadsResponse;
import com.google.api.services.gmail.model.Thread;

/**
 * Hello world!
 *
 */
public class App {
	
	static String user = "mimoun.chikhi@capgemini-sogeti.com";

	public static void saveToDoc() {

	}

	private static Gmail getGmailService(String user) {
		Gmail gmail = null;

		try {
			gmail = CredentialLoader.getGmailService(user);
		} catch (GeneralSecurityException e) {
			gmail = null;
			System.err
					.println("Exception in GmailAPIService.getGmailService(): "
							+ e.getMessage());
		} catch (IOException e) {
			gmail = null;
			System.err
					.println("Exception in GmailAPIService.getGmailService(): "
							+ e.getMessage());
		} catch (URISyntaxException e) {
			gmail = null;
			System.err
					.println("Exception in GmailAPIService.getGmailService(): "
							+ e.getMessage());
		}

		return gmail;
	}

	public static void main(String args[]) throws IOException {
		Gmail service = getGmailService(user);
		
		List<Label> labels =  service.users().labels().list(user).execute().getLabels() ;
		
		
		ListThreadsResponse threadsResponse = service.users().threads()
				.list("mimoun.chikhi@capgemini-sogeti.com").execute();
		List<Thread> threads = threadsResponse.getThreads();

		// Print ID of each Thread.
		for (Thread thread : threads) {
			System.out.println("Thread ID: " + thread.getId());
		}
	}
}
