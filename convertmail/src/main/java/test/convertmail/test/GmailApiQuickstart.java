package test.convertmail.test;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import com.aspose.email.Attachment;
import com.aspose.email.LinkedResource;
import com.aspose.email.MailMessage;
import com.aspose.email.MailMessageSaveType;
import com.aspose.email.MediaTypeNames;
import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.ListThreadsResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.api.services.gmail.model.Thread;

public class GmailApiQuickstart {

  // Check https://developers.google.com/gmail/api/auth/scopes for all available scopes
  private static final String SCOPE = "https://www.googleapis.com/auth/gmail.readonly";
  private static final String APP_NAME = "Email Converter";
  // Email address of the user, or "me" can be used to represent the currently authorized user.
  private static final String USER = "mimoun.chikhi@capgemini-sogeti.com";
  // Path to the client_secret.json file downloaded from the Developer Console
  private static final String CLIENT_SECRET_PATH = "client_secret.json";

  private static GoogleClientSecrets clientSecrets;

  public static void main (String [] args) throws Exception {
    HttpTransport httpTransport = new NetHttpTransport();
    JsonFactory jsonFactory = new JacksonFactory();

    clientSecrets = GoogleClientSecrets.load(jsonFactory,new FileReader("D:\\Actelion\\workspace\\convertmail\\src\\main\\resources\\"+CLIENT_SECRET_PATH));

    // Allow user to authorize via url.
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        httpTransport, jsonFactory, clientSecrets, Arrays.asList(SCOPE))
        .setAccessType("online")
        .setApprovalPrompt("auto").build();

    String url = flow.newAuthorizationUrl().setRedirectUri(GoogleOAuthConstants.OOB_REDIRECT_URI)
        .build();
    System.out.println("Please open the following URL in your browser then type"
                       + " the authorization code:\n" + url);

    // Read code entered by user.
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    String code = br.readLine();

    // Generate Credential using retrieved code.
    GoogleTokenResponse response = flow.newTokenRequest(code)
        .setRedirectUri(GoogleOAuthConstants.OOB_REDIRECT_URI).execute();
    GoogleCredential credential = new GoogleCredential()
        .setFromTokenResponse(response);

    // Create a new authorized Gmail API client
    Gmail service = new Gmail.Builder(httpTransport, jsonFactory, credential)
        .setApplicationName(APP_NAME).build();

    // Retrieve a page of Threads; max of 100 by default.
    ListThreadsResponse threadsResponse = service.users().threads().list(USER).execute();
    
    
    List<Thread> threads = threadsResponse.getThreads();

    // Print ID of each Thread.
//    for (Thread thread : threads) {
//    	List<Message> messages = thread.getMessages();
//    	System.out.println(thread.getId());
//    	if (messages!=null) {
//	    	for (Message message : messages) {
//				System.out.println(message.getRaw());
//				break;
//			}
//	    }
//  }
    	
   List<Message> list = listMessagesMatchingQuery(service, USER, "is:unread");
   System.out.println(list.size());
   Message m = list.get(0);
   Message message = service.users().messages().get(USER, m.getId()).execute();
   //System.out.println(message.getPayload());
   //System.out.println(message.getSizeEstimate());
   //MimeMessage mim = getMimeMessage(service, USER, m.getId());
   //InputStream is = mim.getRawInputStream();
   //convert(is);
   //downloadImage();
   getAttachments(service, USER, m.getId());

  }
    
  
  public static void getAttachments(Gmail service, String userId, String messageId)
	      throws IOException, Exception {
	    Message message = service.users().messages().get(userId, messageId).execute();
	    
	    
	    
	    String strBaseFolder = "D:\\Temp\\";
	    MimeMessage mim = getMimeMessage(service, USER, messageId);
	    InputStream is = mim.getInputStream();
	    MailMessage msg = MailMessage.load(is);
	    for (int i = 0; i < msg.getAttachments().size(); i++)
	    {
	    	Attachment att = (Attachment) msg.getAttachments().get_Item(i);
		    LinkedResource res = new LinkedResource(att.getName(), MediaTypeNames.Image.JPEG);
		    res.setContentId(att.getContentId());
		    msg.getLinkedResources().addItem(res);
	    }

	    
	    
	    List<MessagePart> parts = message.getPayload().getParts();
	    for (MessagePart part : parts) {
	      if (part.getFilename() != null && part.getFilename().length() > 0) {
	        String filename = part.getFilename();
	        String attId = part.getBody().getAttachmentId();
	        MessagePartBody attachPart = service.users().messages().attachments().
	            get(userId, messageId, attId).execute();
	        byte[] fileByteArray = Base64.decodeBase64(attachPart.getData());
	        FileOutputStream fileOutFile =
	            new FileOutputStream("D:\\Temp\\image.jpg");
		    LinkedResource res = new LinkedResource( new ByteArrayInputStream(fileByteArray), MediaTypeNames.Image.JPEG);
		    res.setContentId(part.getHeaders().get(3).getValue());
		    msg.getLinkedResources().addItem(res);
	        fileOutFile.write(fileByteArray);
	        fileOutFile.close();
	      }
	    }
	    msg.save(strBaseFolder + "message.mhtml", MailMessageSaveType.getMHtmlFormat());
	    Document doc = new Document(strBaseFolder + "message.mhtml");
	    doc.save(strBaseFolder +"output.doc", SaveFormat.DOC);
	  }

  public static List<Message> listMessagesMatchingQuery(Gmail service, String userId,
	      String query) throws IOException {
	    ListMessagesResponse response = service.users().messages().list(userId).setQ(query).execute();

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
  
  public static List<Message> listMessagesWithLabels(Gmail service, String userId,
	      List<String> labelIds) throws IOException {
	    ListMessagesResponse response = service.users().messages().list(userId)
	        .setLabelIds(labelIds).execute();

	    List<Message> messages = new ArrayList<Message>();
	    while (response.getMessages() != null) {
	      messages.addAll(response.getMessages());
	      if (response.getNextPageToken() != null) {
	        String pageToken = response.getNextPageToken();
	        response = service.users().messages().list(userId).setLabelIds(labelIds)
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
  
  public static MimeMessage getMimeMessage(Gmail service, String userId, String messageId)
	      throws IOException, MessagingException {
	    Message message = service.users().messages().get(userId, messageId).setFormat("raw").execute();
	    
//	    String html = "PGRpdiBkaXI9Imx0ciI-YTxkaXY-PGltZyBzcmM9ImNpZDppaV8xNDhhY2EzNWMxN2U2ZTFmIiBhbHQ9IklubGluZSBpbWFnZSAxIj48YnI-PGRpdj5iPC9kaXY-PC9kaXY-PC9kaXY-DQo=";
//	    String text = "YQ0KW2ltYWdlOiBJbmxpbmUgaW1hZ2UgMV0NCmINCg==";
//	    FileOutputStream fop = new FileOutputStream(new File("D:\\Temp\\essai.html"));
//	    fop.write(Base64.decodeBase64(html));
//	    fop.close();
//	    fop = new FileOutputStream(new File("D:\\Temp\\essai.txt"));
//	    fop.write(Base64.decodeBase64(text));
//	    fop.close();
	    
	    byte[] emailBytes = Base64.decodeBase64(message.getRaw());

	    Properties props = new Properties();
	    Session session = Session.getDefaultInstance(props, null);

	    MimeMessage email = new MimeMessage(session, new ByteArrayInputStream(emailBytes));

	    return email;
	  }
  
  public static void convert(InputStream inputStream) {
	  
		OutputStream outputStream = null;
	 
		try {
	 
			// write the inputStream to a FileOutputStream
			outputStream = new FileOutputStream(new File("D:\\Temp\\essai.doc"));
				 
			int read = 0;
			byte[] bytes = new byte[1024];
	 
			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
	 
			System.out.println("Done!");
	 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (outputStream != null) {
				try {
					// outputStream.flush();
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	 
			}
		}
	    }
  
  public static void downloadImage() throws Exception {
	  URL url = new URL("https://mail.google.com/mail/u/0/?ui=2&ik=ed1e3ae22b&view=att&th=148aca364573c308&attid=0.1&disp=emb&realattid=ii_148aca35c17e6e1f&zw&atsh=1");
	  url = new URL("http://upload.wikimedia.org/wikipedia/commons/9/9c/Image-Porkeri_001.jpg");
	  InputStream in = new BufferedInputStream(url.openStream());
	  ByteArrayOutputStream out = new ByteArrayOutputStream();
	  byte[] buf = new byte[1024];
	  int n = 0;
	  while (-1!=(n=in.read(buf)))
	  {
	     out.write(buf, 0, n);
	  }
	  out.close();
	  in.close();
	  byte[] response = out.toByteArray();
	  FileOutputStream fos = new FileOutputStream("D://Temp//image.jpg");
	  fos.write(response);
	  fos.close();
	  
  }
  

  
}