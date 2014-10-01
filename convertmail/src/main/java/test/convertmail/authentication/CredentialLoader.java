package test.convertmail.authentication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;

public class CredentialLoader {
	
		private static DomainCredentials domainCredentials = DomainCredentialsDAO.loadDomainCredentials();
		private static GoogleCredentialItem googleCredentialItem = generateGoogleCredentialItem(getScopes());

		public static Gmail getGmailService(){
			Gmail service = null;			
			if(googleCredentialItem != null){
				service = new Gmail.Builder(googleCredentialItem.getHttpTransport(), googleCredentialItem.getJsonFactory(), null)
			      .setHttpRequestInitializer(googleCredentialItem.getGoogleCredential()).setApplicationName("MCI").build();
			}				  
		    return service;
		}
		
		public static Drive getDriveService(){
			Drive service = null;			
			if(googleCredentialItem != null){
				service = new Drive.Builder(googleCredentialItem.getHttpTransport(), googleCredentialItem.getJsonFactory(), null)
			      .setHttpRequestInitializer(googleCredentialItem.getGoogleCredential()).setApplicationName("MCI").build();
			}				  
		    return service;
		}

		private static GoogleCredentialItem generateGoogleCredentialItem(ArrayList<String> scopes){
			  HttpTransport httpTransport = new NetHttpTransport();
			  JacksonFactory jsonFactory = new JacksonFactory();
			  System.out.println(domainCredentials.getCertificatePath());
			  System.out.println( CredentialLoader.class.getResource("/" + domainCredentials.getCertificatePath()));
			  //File f = getP12File(CredentialLoader.class.getResourceAsStream("/" + domainCredentials.getCertificatePath()));
			  
			  GoogleCredential googleCredential = null;
			  GoogleCredentialItem googleCredentialItem = null;
			try {
				googleCredential = new GoogleCredential.Builder()
				      .setTransport(httpTransport)
				      .setJsonFactory(jsonFactory)
				      .setServiceAccountId(domainCredentials.getServiceAccountEmail())
				      .setServiceAccountScopes(scopes)
				      .setServiceAccountUser(domainCredentials.getUserEmailAddress())
				      .setServiceAccountPrivateKeyFromP12File(new File(CredentialLoader.class.getResource("/" + domainCredentials.getCertificatePath()).toURI()))
				      .build();
				
				googleCredentialItem = new GoogleCredentialItem();
				googleCredentialItem.setGoogleCredential(googleCredential);
				
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
			
			return googleCredentialItem;
		}
		
		private static File getP12File(InputStream  is) {
	        try {	             
	            OutputStream os = new FileOutputStream("D:\\Temp\\is.p12");
	             
	            byte[] buffer = new byte[1024];
	            int bytesRead;
	            //read from is to buffer
	            while((bytesRead = is.read(buffer)) !=-1){
	                os.write(buffer, 0, bytesRead);
	            }
	            is.close();
	            //flush OutputStream to write any buffered data to file
	            os.flush();
	            os.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
			return new File("D:\\Temp\\is.p12");
		}
		
		
		  
		private static ArrayList<String> getScopes(){
			ArrayList<String> scopes = new ArrayList<String>();
			scopes.add(GmailScopes.GMAIL_READONLY); //"https://www.googleapis.com/auth/gmail.readonly"
			scopes.add(DriveScopes.DRIVE);		
			return scopes;
		}
		
}