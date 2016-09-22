package cm.homeautomation.services.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.ws.rs.Path;

import cm.homeautomation.services.base.BaseService;

@Path("mail")
public class MailStatistics extends BaseService {

	private static List<MailData> mailDataList=new ArrayList<MailData>();

	@Path("get")
	public List<MailData> getMailboxStatus() {
		return mailDataList;
	}

	private static MailData findOrCreateMailEntryInList(String account) {
		
		for (MailData mailData : mailDataList) {
			
			if (mailData.getAccount().equals(account)) {
				return mailData;
			}
			
		}
		
		MailData mailData = new MailData(account);
		mailDataList.add(mailData);
		return mailData;
	}
	
	public static void updateMailData(String[] args) {
		
		String account = args[0];
		MailData mailData = findOrCreateMailEntryInList(account);
		
		List<MailData> mailDataList=new ArrayList<MailData>();
		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		try {
		  Session session = Session.getDefaultInstance(props, null);
		  Store store = session.getStore("imaps");
		  store.connect("imap.gmail.com", account, args[1]);

		  
		  
		  Folder folder = store.getFolder("INBOX");
		  System.out.println(folder.getNewMessageCount() + " - "+folder.getUnreadMessageCount());
		  mailData.setNewMessages(folder.getNewMessageCount());
		  mailData.setUnreadMessages(folder.getUnreadMessageCount());
		} catch (NoSuchProviderException e) {
		  e.printStackTrace();
		  System.exit(1);
		} catch (MessagingException e) {
		  e.printStackTrace();
		  System.exit(2);
		}
		
	}
	
	public static void main(String[] args) {
		String[] myArgs={"2", "1"};
		updateMailData(myArgs);
		
		List<MailData> mailboxStatus = new MailStatistics().getMailboxStatus();
		
		for (MailData mailData : mailboxStatus) {
			System.out.println(mailData.getAccount()+" - "+mailData.getNewMessages()+" - "+mailData.getUnreadMessages());
		}
	}
}
