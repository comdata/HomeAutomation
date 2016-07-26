package cm.homeautomation.transmission;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TransmissionStatusData {

	private Long uploadSpeed;
	private Long downloadSpeed;
	private int numberOfTorrents;
	private int numberOfDoneTorrents;

	public void setUploadSpeed(Long uploadSpeed) {
		this.uploadSpeed = uploadSpeed;
	}

	public void setDownloadSpeed(Long downloadSpeed) {
		this.downloadSpeed = downloadSpeed;
	}

	public void setTorrents(int numberOfTorrents) {
		this.numberOfTorrents = numberOfTorrents;
	}

	public Long getUploadSpeed() {
		return uploadSpeed;
	}

	public Long getDownloadSpeed() {
		return downloadSpeed;
	}

	public int getNumberOfTorrents() {
		return numberOfTorrents;
	}

	public void setDoneTorrents(int numberOfDoneTorrents) {
		this.numberOfDoneTorrents = numberOfDoneTorrents;
	}
	
	public int getNumberOfDoneTorrents() {
		return numberOfDoneTorrents;
	}

}
