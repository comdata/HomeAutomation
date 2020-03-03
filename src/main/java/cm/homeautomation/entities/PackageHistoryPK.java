package cm.homeautomation.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Embeddable;

@Embeddable
public class PackageHistoryPK implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6167575880452398827L;
	private Date timestamp;
	
	private String carrier;

	private String trackingNumber;
	
	private String statusText;


	
	public PackageHistoryPK(String trackingNumber, String carrier, Date timestamp, String statusText) {
		this.trackingNumber = trackingNumber;
		this.carrier = carrier;
		this.timestamp=timestamp;
		this.statusText = statusText;
	}
	
	public void setStatusText(String statusText) {
		this.statusText = statusText;
	}

	public String getStatusText() {
		return statusText;
	}


	public PackageHistoryPK() {
	}
	
	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}


	public String getCarrier() {
		return carrier;
	}

	public void setCarrier(String carrier) {
		this.carrier = carrier;
	}

	public String getTrackingNumber() {
		return trackingNumber;
	}

	public void setTrackingNumber(String trackingNumber) {
		this.trackingNumber = trackingNumber;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj instanceof PackageHistoryPK) {
			PackageHistoryPK asPackagePK = (PackageHistoryPK) obj;
			return (this.trackingNumber != null && this.trackingNumber.equals(asPackagePK.getTrackingNumber()))
					&& (this.carrier != null && this.carrier.equals(asPackagePK.getCarrier())
					&& (this.timestamp != null && this.timestamp.equals(asPackagePK.getTimestamp())));
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.trackingNumber.hashCode();
		hash = hash * prime + this.carrier.hashCode();
		hash = hash * prime + this.timestamp.hashCode();
		return hash;
	}
}
