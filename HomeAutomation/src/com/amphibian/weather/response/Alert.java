package com.amphibian.weather.response;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name="alert")
public class Alert {

	@XmlTransient private String type;
	@XmlTransient private String description;
	@XmlTransient private String date;
	@XmlTransient private long dateEpoch;
	@XmlTransient private String expires;
	@XmlTransient private long expiresEpoch;
	@XmlTransient private String message;
	@XmlTransient private String phenomena;
	@XmlTransient private String significance;
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the date
	 */
	public String getDate() {
		return date;
	}
	/**
	 * @param date the date to set
	 */
	public void setDate(String date) {
		this.date = date;
	}
	/**
	 * @return the dateEpoch
	 */
	@XmlElement(name="date_epoch")
	public long getDateEpoch() {
		return dateEpoch;
	}
	/**
	 * @param dateEpoch the dateEpoch to set
	 */
	public void setDateEpoch(long dateEpoch) {
		this.dateEpoch = dateEpoch;
	}
	/**
	 * @return the expires
	 */
	public String getExpires() {
		return expires;
	}
	/**
	 * @param expires the expires to set
	 */
	public void setExpires(String expires) {
		this.expires = expires;
	}
	/**
	 * @return the expiresEpoch
	 */
	@XmlElement(name="expires_epoch")
	public long getExpiresEpoch() {
		return expiresEpoch;
	}
	/**
	 * @param expiresEpoch the expiresEpoch to set
	 */
	public void setExpiresEpoch(long expiresEpoch) {
		this.expiresEpoch = expiresEpoch;
	}
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	/**
	 * @return the phenomena
	 */
	public String getPhenomena() {
		return phenomena;
	}
	/**
	 * @param phenomena the phenomena to set
	 */
	public void setPhenomena(String phenomena) {
		this.phenomena = phenomena;
	}
	/**
	 * @return the significance
	 */
	public String getSignificance() {
		return significance;
	}
	/**
	 * @param significance the significance to set
	 */
	public void setSignificance(String significance) {
		this.significance = significance;
	}
	
}
