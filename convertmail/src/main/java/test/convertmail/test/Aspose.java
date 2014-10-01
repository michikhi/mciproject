package test.convertmail.test;

import com.aspose.email.Attachment;
import com.aspose.email.LinkedResource;
import com.aspose.email.MailMessage;
import com.aspose.email.MailMessageLoadOptions;
import com.aspose.email.MailMessageSaveType;
import com.aspose.email.MediaTypeNames;
import com.aspose.email.MessageFormat;
import com.aspose.words.Document;
import com.aspose.words.SaveFormat;

public class Aspose {

	
	public static void main(String[] args) throws Exception
	{
	    // Base folder for reading and writing files
	    String strBaseFolder = "C:\\Temp\\";

	    // Initialize and Load an existing EML file by specifying the MessageFormat
	    MailMessage msg = MailMessage.load(strBaseFolder + "essai.eml", MailMessageLoadOptions.getDefaultEml());

	    // Save the Email message to disk by specifying the MSG and MHT MailMessageSaveType
//	    msg.save(strBaseFolder + "message.msg", MailMessageSaveType.getOutlookMessageFormat());
	    msg.save(strBaseFolder + "message.mhtml", MailMessageSaveType.getMHtmlFormat());
	    
	    for (int i = 0; i < msg.getAttachments().size(); i++)
	    {
	    	Attachment att = (Attachment) msg.getAttachments().get_Item(i);
		    LinkedResource res = new LinkedResource(att.getName(), MediaTypeNames.Image.JPEG);
		    res.setContentId(att.getContentId());
		    msg.getLinkedResources().addItem(res);
	    }
	    
//	    MailMessage message = MailMessage.load("test.eml",MessageFormat.getEml());
//	    System.out.println("Saving message in MHTML format....");
//	    message.save("temp.mhtml", MailMessageSaveType.getMHtmlFromat());
//	    System.out.println("Loading MHTML file in Aspose.Words for Java....");
	    Document doc = new Document(strBaseFolder + "message.mhtml");
	    doc.save(strBaseFolder +"output.doc", SaveFormat.DOC);
	    
	    
	}
}
