package cm.homeautomation.services.mail;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MailData {
	private String account;
	private int unreadMessages;
	private int newMessages;
	
	public MailData(String account) {
		this.account = account;
	
	}

	public int getUnreadMessages() {
		return unreadMessages;
	}

	public void setUnreadMessages(int unreadMessages) {
		this.unreadMessages = unreadMessages;
	}

	public int getNewMessages() {
		return newMessages;
	}

	public void setNewMessages(int newMessages) {
		this.newMessages = newMessages;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}
}
