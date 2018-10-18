package com.amphibian.weather.response;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name="location")
public class Location {

	@XmlTransient private String full;
	@XmlTransient private String city;
	@XmlTransient private String state;
	@XmlTransient private String stateName;
	@XmlTransient private String country;
	@XmlTransient private String countryIso3166;
	@XmlTransient private String zip;
	@XmlTransient private double latitude;
	@XmlTransient private double longitude;
	@XmlTransient private String elevation;
	private String magic;
	private String wmo;
	/**
	 * @return the full
	 */
	@XmlElement(name="full")
	public String getFull() {
		return full;
	}
	/**
	 * @param full the full to set
	 */
	public void setFull(String full) {
		this.full = full;
	}
	/**
	 * @return the city
	 */
	@XmlElement(name="city")
	public String getCity() {
		return city;
	}
	/**
	 * @param city the city to set
	 */
	public void setCity(String city) {
		this.city = city;
	}
	/**
	 * @return the state
	 */
	@XmlElement(name="state")
	public String getState() {
		return state;
	}
	/**
	 * @param state the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}
	/**
	 * @return the stateName
	 */
	@XmlElement(name="state_name")
	public String getStateName() {
		return stateName;
	}
	/**
	 * @param stateName the stateName to set
	 */
	public void setStateName(String stateName) {
		this.stateName = stateName;
	}
	/**
	 * @return the country
	 */
	@XmlElement(name="country")
	public String getCountry() {
		return country;
	}
	/**
	 * @param country the country to set
	 */
	public void setCountry(String country) {
		this.country = country;
	}
	/**
	 * @return the countryIso3166
	 */
	@XmlElement(name="country_iso3166")
	public String getCountryIso3166() {
		return countryIso3166;
	}
	/**
	 * @param countryIso3166 the countryIso3166 to set
	 */
	public void setCountryIso3166(String countryIso3166) {
		this.countryIso3166 = countryIso3166;
	}
	/**
	 * @return the zip
	 */
	@XmlElement(name="zip")
	public String getZip() {
		return zip;
	}
	/**
	 * @param zip the zip to set
	 */
	public void setZip(String zip) {
		this.zip = zip;
	}
	/**
	 * @return the latitude
	 */
	@XmlElement(name="latitude")
	public double getLatitude() {
		return latitude;
	}
	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	/**
	 * @return the longitude
	 */
	@XmlElement(name="longitude")
	public double getLongitude() {
		return longitude;
	}
	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	/**
	 * @return the elevation
	 */
	@XmlElement(name="elevation")
	public String getElevation() {
		return elevation;
	}
	/**
	 * @param elevation the elevation to set
	 */
	public void setElevation(String elevation) {
		this.elevation = elevation;
	}
	public String getMagic() {
		return magic;
	}
	public void setMagic(String magic) {
		this.magic = magic;
	}
	public String getWmo() {
		return wmo;
	}
	public void setWmo(String wmo) {
		this.wmo = wmo;
	}
	
}
