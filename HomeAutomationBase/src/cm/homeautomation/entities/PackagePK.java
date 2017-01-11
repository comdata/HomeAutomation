package cm.homeautomation.entities;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class PackagePK implements Serializable  {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1662142372114601670L;

	private String carrier;

	private String trackingNumber;

	public PackagePK() {
	}
	
	public PackagePK(String trackingNumber, String carrier) {
		this.trackingNumber = trackingNumber;
		this.carrier = carrier;

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

		if (obj instanceof PackagePK) {
			PackagePK asPackagePK = (PackagePK) obj;
			return (this.trackingNumber != null && this.trackingNumber.equals(asPackagePK.getTrackingNumber()))
					&& (this.carrier != null && this.carrier.equals(asPackagePK.getCarrier()));
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
		return hash;
	}
}
