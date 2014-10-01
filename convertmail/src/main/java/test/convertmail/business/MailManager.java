package test.convertmail.business;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import test.convertmail.authentication.CredentialLoader;

import com.aspose.email.LinkedResource;
import com.aspose.email.MailMessage;
import com.aspose.email.MailMessageSaveType;
import com.aspose.email.MediaTypeNames;
import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
import com.google.api.client.http.FileContent;
import com.google.api.client.util.Base64;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.MessagePartHeader;

public class MailManager {

	private static final String USER = "mimoun.chikhi@capgemini-sogeti.com";
	
	private static final String STRBASEFOLDER = "D:\\Temp\\";
	
	private static final String MCIFOLDER = "MCI Events";

	public static void convert() throws Exception {
		String userId = USER;
		
		Gmail service = CredentialLoader.getGmailService();

		List<Message> list = listMessagesMatchingQuery(service, USER,"is:unread");
		if (list.size() == 0) {
			System.err.println("No unread message in mailbox");
		} else {
			// TODO MCH difference between m and message
			Message m = list.get(0);
			Message gMailMessage = service.users().messages().get(userId, m.getId()).execute();

		    MimeMessage mimeMessage = getMimeMessage(service, USER, gMailMessage.getId());
		    InputStream isMessage = mimeMessage.getInputStream();
		    MailMessage asposeMessage = MailMessage.load(isMessage);
		    
	    	List<MessagePart> parts = gMailMessage.getPayload().getParts();
		    for (MessagePart part : parts) {
			      if (part.getFilename() != null && part.getFilename().length() > 0) {
			        String attId = part.getBody().getAttachmentId();
			        MessagePartBody attachPart = service.users().messages().attachments().
			            get(userId, gMailMessage.getId(), attId).execute();
			        byte[] fileByteArray = Base64.decodeBase64(attachPart.getData());
				    if (getHeaderValue(part.getHeaders(),"Content-Disposition").startsWith("attachment")) {
					        String filename = part.getFilename();
					        FileOutputStream fileOutFile = new FileOutputStream("D:\\Temp\\"+filename+".jpg");
					        fileOutFile.write(fileByteArray);
					        fileOutFile.close();
					        File pj = storeToDrive(filename, "attachment No"+part.getPartId(), "image/jpeg", "image/jpeg", "jpg");
					        asposeMessage.setHtmlBody(asposeMessage.getHtmlBody()+"<br><a href=\""+pj.getAlternateLink()+"\">"+filename+"</a>");					        
				    } else if ((getHeaderValue(part.getHeaders(),"Content-Disposition").startsWith("inline"))) {
				    		LinkedResource res = new LinkedResource( new ByteArrayInputStream(fileByteArray), MediaTypeNames.Image.JPEG);// TODO MCH : pay attention to type				    	
					    	res.setContentId(getHeaderValue(part.getHeaders(),"X-Attachment-Id"));
					    	asposeMessage.getLinkedResources().addItem(res);
				    }
			      }
			 }	
		    asposeMessage = constructOutputDoc(asposeMessage, mimeMessage);
		    asposeMessage.save(STRBASEFOLDER + "message.mhtml", MailMessageSaveType.getMHtmlFormat());	
		    Document doc = new Document(STRBASEFOLDER + "message.mhtml");
		    doc.save(STRBASEFOLDER +"eventNo1.doc", SaveFormat.DOC);
		}

	}
	
	private static MailMessage constructOutputDoc(MailMessage asposeMessage, MimeMessage mimeMessage) throws MessagingException {
		MailMessage toReturn = asposeMessage;
		
		StringBuffer messageHeader = new StringBuffer();
		messageHeader.append("<h1>1. Message Header</h1>");// TODO MCH iterate over To adresses
		messageHeader.append("<br>From Email : ").append(((InternetAddress)mimeMessage.getFrom()[0]).getAddress());
		messageHeader.append("<br>From  : ").append(((InternetAddress)mimeMessage.getFrom()[0]).getPersonal());
		messageHeader.append("<br>To : ").append(((InternetAddress)mimeMessage.getAllRecipients()[0]).getAddress()); 
		messageHeader.append("<br>Date : ").append(mimeMessage.getSentDate());
		messageHeader.append("<br>Subject : ").append(mimeMessage.getSubject());
		messageHeader.append("<br><br><br><br>");
		
		StringBuffer originalMail = new StringBuffer();
		originalMail.append("<h1>2. Original Mail</h1>");
		originalMail.append(asposeMessage.getHtmlBody());
		originalMail.append("<br><br><br><br>");
		
		StringBuffer workingNotes = new StringBuffer();
		workingNotes.append("<h1>3. Working Notes</h1>");
		workingNotes.append("<br><br><br><br>");
		
		StringBuffer responses = new StringBuffer();
		responses.append("<h1>4. Responses</h1>");
		responses.append("<br><h2>4.1. answer in progress</h2>");
		responses.append("<br>[Begin response]");
		responses.append("<br>[End response]");
		
		toReturn.setHtmlBody(messageHeader.append(originalMail).append(workingNotes).append(responses).toString());
		
		return toReturn;
	}

	private static void write() throws FileNotFoundException, IOException, GeneralSecurityException, URISyntaxException {
	    storeToDrive("eventNo1", "An email converted to a document","application/vnd.google-apps.document", "application/msword", "doc");
	}
	
	private static File storeToDrive(String name, String description, String mimetypeBody, String mimetypeFile, String extension) throws IOException, GeneralSecurityException, URISyntaxException {
		Drive service = CredentialLoader.getDriveService();
		File mciFolder = getMCIFolder(service);
		if (mciFolder==null) {
			mciFolder = createMCIFolder(service);
		}
		
	    //Insert a file  
	    File body = new File();
	    body.setTitle(name);
	    body.setDescription(description);
	    body.setMimeType(mimetypeBody);
	    
	    if (mciFolder.getId() != null && mciFolder.getId().length() > 0) {
	        body.setParents(
	            Arrays.asList(new ParentReference().setId(mciFolder.getId())));
	     }
	    
	    java.io.File fileContent = new java.io.File(STRBASEFOLDER + name.trim() + "." + extension);
	    FileContent mediaContent = new FileContent(mimetypeFile, fileContent);

	    File file = service.files().insert(body, mediaContent).execute();
		return file;		
		
	}

	public static void main(String args[]) {

		try {
			
			//convert();
			write();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	 private static File getMCIFolder(Drive service) throws IOException, GeneralSecurityException, URISyntaxException {
	        
		 	File toReturn = null;
		 	
		 	Drive.Files.List request;
	        request = service.files().list();
	        
			String rootId = service.about().get().execute().getRootFolderId();
	        
	        String query = "mimeType='application/vnd.google-apps.folder' AND trashed=false AND title='" + MCIFOLDER + "' AND '" + rootId + "' in parents";
	        request = request.setQ(query);
	        FileList files = request.execute();
	        
	        if (files.getItems().size() > 0){ toReturn = files.getItems().get(0); }
	        
	        return toReturn;
	    }
	
	private static File createMCIFolder(Drive service) throws IOException{
		String rootId = service.about().get().execute().getRootFolderId();
		File toReturn = new File();
		toReturn.setTitle(MCIFOLDER);
		toReturn.setMimeType("application/vnd.google-apps.folder");
		toReturn.setParents(Arrays.asList(new ParentReference().setId(rootId))); 
		toReturn = service.files().insert(toReturn).execute();
		
		return toReturn;
	}
	
	private static String getHeaderValue(List<MessagePartHeader> list, String name) {
		String toReturn = "";
		for (MessagePartHeader messagePartHeader : list) {
			if (name.equalsIgnoreCase(messagePartHeader.getName())) {
				toReturn = messagePartHeader.getValue();				
			}
		}		
		return toReturn;
	}

	private static List<Message> listMessagesMatchingQuery(Gmail service,
			String userId, String query) throws IOException {
		ListMessagesResponse response = service.users().messages().list(userId)
				.setQ(query).execute();

		List<Message> messages = new ArrayList<Message>();
		while (response.getMessages() != null) {
			messages.addAll(response.getMessages());
			if (response.getNextPageToken() != null) {
				String pageToken = response.getNextPageToken();
				response = service.users().messages().list(userId).setQ(query)
						.setPageToken(pageToken).execute();
			} else {
				break;
			}
		}

		for (Message message : messages) {
			System.out.println(message.toPrettyString());
		}

		return messages;
	}
	
	private static MimeMessage getMimeMessage(Gmail service, String userId, String messageId)
		      throws IOException, MessagingException {
	    Message message = service.users().messages().get(userId, messageId).setFormat("raw").execute();
	    		    
	    byte[] emailBytes = Base64.decodeBase64(message.getRaw());

	    Properties props = new Properties();
	    Session session = Session.getDefaultInstance(props, null);

	    MimeMessage email = new MimeMessage(session, new ByteArrayInputStream(emailBytes));

	    return email;
	}
}
