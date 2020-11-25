package cm.homeautomation.services.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.apache.logging.log4j.LogManager;

import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.services.base.BaseService;
import io.vertx.core.eventbus.EventBus;
import lombok.NonNull;

@ApplicationScoped
@Path("mail/")
public class MailStatistics extends BaseService {

	@Inject
	EventBus bus;

	private static MailStatistics instance;

	private static List<MailData> mailDataList = new ArrayList<>();

	public MailStatistics() {
		instance = this;
	}

	@GET
	@Path("get")
	public List<MailData> getMailboxStatus() {
		return MailStatistics.mailDataList;
	}

	private static MailData findOrCreateMailEntryInList(@NonNull String account) {

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
		instance.updateMailDataInternal(args);
	}

	public void updateMailDataInternal(String[] args) {

		String account = args[0];
		MailData mailData = findOrCreateMailEntryInList(account);

		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		try {
			Session session = Session.getDefaultInstance(props, null);
			Store store = session.getStore("imaps");
			store.connect("imap.gmail.com", account, args[1]);

			Folder folder = store.getFolder("INBOX");
			LogManager.getLogger(MailStatistics.class).info("Account: " + account + ": " + folder.getNewMessageCount()
					+ " - " + folder.getUnreadMessageCount());
			mailData.setNewMessages(folder.getNewMessageCount());
			mailData.setUnreadMessages(folder.getUnreadMessageCount());
			EventObject eventObject = new EventObject(mailData);
			bus.publish("EventObject", eventObject);
		} catch (MessagingException e) {
			LogManager.getLogger(MailStatistics.class).error(e);
		}

	}

	public static void main(String[] args) {
		String[] myArgs = { "2", "1" };
		updateMailData(myArgs);

		List<MailData> mailboxStatus = new MailStatistics().getMailboxStatus();

		for (MailData mailData : mailboxStatus) {
			LogManager.getLogger(MailStatistics.class).info(
					mailData.getAccount() + " - " + mailData.getNewMessages() + " - " + mailData.getUnreadMessages());
		}
	}
}
