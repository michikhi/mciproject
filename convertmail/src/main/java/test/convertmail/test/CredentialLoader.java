package test.convertmail.test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.DirectoryScopes;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;


public class CredentialLoader {

		private static DomainCredentials domainCredentials = DomainCredentialsDAO.loadDomainCredentials();
		
		private static final Collection<String> serviceAccountScopes = getScopes();
				
		/**
		 * Build and returns a Drive service object authorized with the service accounts
		 * that act on behalf of the given user.
		 *
		 * @param userEmail The email of the user.
		 * @return Drive service object that is ready to make requests.
		 */
		public static Directory getDirectoryService(){
		  HttpTransport httpTransport = new NetHttpTransport();
		  JacksonFactory jsonFactory = new JacksonFactory();
		  
		  GoogleCredential credential = null;
		try {
			credential = new GoogleCredential.Builder()
			      .setTransport(httpTransport)
			      .setJsonFactory(jsonFactory)
			      .setServiceAccountId(domainCredentials.getServiceAccountEmail())
			      .setServiceAccountScopes(serviceAccountScopes)
			      .setServiceAccountUser(domainCredentials.getUserEmailAddress())
			      .setServiceAccountPrivateKeyFromP12File(new File(CredentialLoader.class.getResource("/" + domainCredentials.getCertificatePath()).toURI()))
			      .build();
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  
		  Directory service = new Directory.Builder(httpTransport, jsonFactory, null)
		      .setHttpRequestInitializer(credential).setApplicationName("bcera_portal").build();
		  
		  return service;
		}
		
		
		/**
		 * Build and returns a Drive service object authorized with the service accounts
		 * that act on behalf of the given user.
		 *
		 * @param userEmail The email of the user.
		 * @return Drive service object that is ready to make requests.
		 */
		public static Gmail getGmailService(String userEmail) throws GeneralSecurityException,
		    IOException, URISyntaxException {
		  HttpTransport httpTransport = new NetHttpTransport();
		  JacksonFactory jsonFactory = new JacksonFactory();
		  GoogleCredential credential = new GoogleCredential.Builder()
		      .setTransport(httpTransport)
		      .setJsonFactory(jsonFactory)
		      .setServiceAccountId(domainCredentials.getServiceAccountEmail())
		      .setServiceAccountScopes(GmailScopes.all())
		      .setServiceAccountUser(userEmail)
		      .setServiceAccountPrivateKeyFromP12File(new File(CredentialLoader.class.getResource("/" + domainCredentials.getCertificatePath()).toURI()))
		      .build();
		  
		  Gmail service = new Gmail.Builder(httpTransport, jsonFactory, null).setHttpRequestInitializer(credential).build();
		  
		  return service;
		}
		
			
		/**
		 * 
		 * @return Google Drive API scopes required
		 */
		private static ArrayList<String> getScopes(){
			ArrayList<String> scopes = new ArrayList<String>();
			scopes.add(DirectoryScopes.ADMIN_DIRECTORY_USER_READONLY);
			scopes.add(DirectoryScopes.ADMIN_DIRECTORY_GROUP);
			scopes.add(DirectoryScopes.ADMIN_DIRECTORY_USER_READONLY);
			return scopes;
		}	
		
}