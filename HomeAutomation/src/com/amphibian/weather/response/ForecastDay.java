package com.amphibian.weather.response;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name="forecastday")
public class ForecastDay {

	@XmlElement(name="period") private int period;
	@XmlElement(name="conditions") private String conditions;
	@XmlElement(name="icon") private String icon;
	@XmlElement(name="icon_url") private String iconUrl;
	@XmlElement(name="skyicon") private String skyIcon;
	@XmlElement(name="pop") private String pop;
	@XmlElement(name="avehumidity") private int averageHumidity;
	@XmlElement(name="maxhumidity") private int maxHumidity;
	@XmlElement(name="minhumidity") private int minHumidity;
	
	/**
	 * @return the period
	 */
	@XmlTransient
	public int getPeriod() {
		return period;
	}
	/**
	 * @param period the period to set
	 */
	public void setPeriod(int period) {
		this.period = period;
	}
	/**
	 * @return the conditions
	 */
	@XmlTransient
	public String getConditions() {
		return conditions;
	}
	/**
	 * @param conditions the conditions to set
	 */
	public void setConditions(String conditions) {
		this.conditions = conditions;
	}
	/**
	 * @return the icon
	 */
	@XmlTransient
	public String getIcon() {
		return icon;
	}
	/**
	 * @param icon the icon to set
	 */
	public void setIcon(String icon) {
		this.icon = icon;
	}
	/**
	 * @return the iconUrl
	 */
	@XmlTransient
	public String getIconUrl() {
		return iconUrl;
	}
	/**
	 * @param iconUrl the iconUrl to set
	 */
	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}
	/**
	 * @return the skyIcon
	 */
	@XmlTransient
	public String getSkyIcon() {
		return skyIcon;
	}
	/**
	 * @param skyIcon the skyIcon to set
	 */
	public void setSkyIcon(String skyIcon) {
		this.skyIcon = skyIcon;
	}
	/**
	 * @return the pop
	 */
	@XmlTransient
	public String getPop() {
		return pop;
	}
	/**
	 * @param pop the pop to set
	 */
	public void setPop(String pop) {
		this.pop = pop;
	}
	/**
	 * @return the averageHunidity
	 */
	@XmlTransient
	public int getAverageHumidity() {
		return averageHumidity;
	}
	/**
	 * @param averageHunidity the averageHunidity to set
	 */
	public void setAverageHumidity(int averageHumidity) {
		this.averageHumidity = averageHumidity;
	}
	/**
	 * @return the maxHumidity
	 */
	@XmlTransient
	public int getMaxHumidity() {
		return maxHumidity;
	}
	/**
	 * @param maxHumidity the maxHumidity to set
	 */
	public void setMaxHumidity(int maxHumidity) {
		this.maxHumidity = maxHumidity;
	}
	/**
	 * @return the minHumidity
	 */
	@XmlTransient
	public int getMinHumidity() {
		return minHumidity;
	}
	/**
	 * @param minHumidity the minHumidity to set
	 */
	public void setMinHumidity(int minHumidity) {
		this.minHumidity = minHumidity;
	}
	
	
	
}
