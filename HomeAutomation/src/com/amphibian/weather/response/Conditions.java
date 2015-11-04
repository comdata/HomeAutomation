package com.amphibian.weather.response;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement(name="current_observation")
@JsonIgnoreProperties(ignoreUnknown=true)
public class Conditions {

	@XmlTransient private Location displayLocation;
	@XmlTransient private Location observationLocation;
	
	
	@XmlTransient private String stationId;
	private String observationTime;
	private String observationTimeRfc822;
	@XmlTransient private Date localTime;
	@XmlTransient private String weather;
	@XmlTransient private String tempString;
	@XmlTransient private float tempF;
	@XmlTransient private float tempC;
	@XmlTransient private String relativeHumidity;
	@XmlTransient private String wind;
	@XmlTransient private String windDirection;
	@XmlTransient private int windDegrees;
	@XmlTransient private float windMph;
	@XmlTransient private float windGustMph;
	@XmlTransient private float windKph;
	@XmlTransient private float windGustKph;
	@XmlTransient private int pressureMb;
	@XmlTransient private float pressureIn;
	@XmlTransient private String pressureTrend;
	@XmlTransient private String dewpointString;
	@XmlTransient private String visibilityMi;
	@XmlTransient private String visibilityKm;
	@XmlTransient private int uv;
	private String image;
	private Object estimated;
	
	@XmlTransient private String icon;
	
	
	/**
	 * @return the displayLocation
	 */
	@XmlElement(name="display_location")
	public Location getDisplayLocation() {
		return displayLocation;
	}

	/**
	 * @param displayLocation the displayLocation to set
	 */
	public void setDisplayLocation(Location displayLocation) {
		this.displayLocation = displayLocation;
	}

	/**
	 * @return the observationLocation
	 */
	@XmlElement(name="observation_location")
	public Location getObservationLocation() {
		return observationLocation;
	}

	/**
	 * @param observationLocation the observationLocation to set
	 */
	public void setObservationLocation(Location observationLocation) {
		this.observationLocation = observationLocation;
	}

	/**
	 * @return the temp
	 */
	@XmlElement(name="temp_f")
	public float getTempF() {
		return tempF;
	}

	/**
	 * @param temp the temp to set
	 */
	public void setTempF(float temp) {
		this.tempF = temp;
	}

	/**
	 * @return the weather
	 */
	@XmlElement(name="weather")
	public String getWeather() {
		return weather;
	}

	/**
	 * @param weather the weather to set
	 */
	public void setWeather(String weather) {
		this.weather = weather;
	}

	/**
	 * @return the stationId
	 */
	@XmlElement(name="station_id")
	public String getStationId() {
		return stationId;
	}

	/**
	 * @param stationId the stationId to set
	 */
	public void setStationId(String stationId) {
		this.stationId = stationId;
	}

	/**
	 * @return the observationTime
	 */
	@XmlTransient
	public String getObservationTime() {
		return observationTime;
	}

	/**
	 * @param observationTime the observationTime to set
	 */
	@XmlElement(name="observation_time")
	public void setObservationTime(String observationTime) {
		this.observationTime = observationTime;
	}
	
	@XmlElement(name="observation_epoch")
	public void setObservationEpoch(long e) {
		//this.observationTime = new Date(e);
	}

	/**
	 * @return the localTime
	 */
	@XmlTransient
	public Date getLocalTime() {
		return localTime;
	}

	/**
	 * @param localTime the localTime to set
	 */
	public void setLocalTime(Date localTime) {
		this.localTime = localTime;
	}
	
	@XmlElement(name="local_epoch")
	public void setLocalEpoch(long e) {
		this.localTime = new Date(e);
	}

	/**
	 * @return the tempString
	 */
	@XmlElement(name="temperature_string")
	public String getTempString() {
		return tempString;
	}

	/**
	 * @param tempString the tempString to set
	 */
	public void setTempString(String tempString) {
		this.tempString = tempString;
	}

	/**
	 * @return the tempC
	 */
	@XmlElement(name="temp_c")
	public float getTempC() {
		return tempC;
	}

	/**
	 * @param tempC the tempC to set
	 */
	public void setTempC(float tempC) {
		this.tempC = tempC;
	}

	/**
	 * @return the relativeHumidity
	 */
	@XmlElement(name="relative_humidity")
	public String getRelativeHumidity() {
		return relativeHumidity;
	}

	/**
	 * @param relativeHumidity the relativeHumidity to set
	 */
	public void setRelativeHumidity(String relativeHumidity) {
		this.relativeHumidity = relativeHumidity;
	}

	/**
	 * @return the wind
	 */
	@XmlElement(name="wind_string")
	public String getWind() {
		return wind;
	}

	/**
	 * @param wind the wind to set
	 */
	public void setWind(String wind) {
		this.wind = wind;
	}

	/**
	 * @return the windDirection
	 */
	@XmlElement(name="wind_dir")
	public String getWindDirection() {
		return windDirection;
	}

	/**
	 * @param windDirection the windDirection to set
	 */
	public void setWindDirection(String windDirection) {
		this.windDirection = windDirection;
	}

	/**
	 * @return the windDegrees
	 */
	@XmlElement(name="wind_degrees")
	public int getWindDegrees() {
		return windDegrees;
	}

	/**
	 * @param windDegrees the windDegrees to set
	 */
	public void setWindDegrees(int windDegrees) {
		this.windDegrees = windDegrees;
	}

	/**
	 * @return the windMph
	 */
	@XmlElement(name="wind_mph")
	public float getWindMph() {
		return windMph;
	}

	/**
	 * @param windMph the windMph to set
	 */
	public void setWindMph(float windMph) {
		this.windMph = windMph;
	}

	/**
	 * @return the windGustMph
	 */
	@XmlElement(name="wind_gust_mph")
	public float getWindGustMph() {
		return windGustMph;
	}

	/**
	 * @param windGustMph the windGustMph to set
	 */
	public void setWindGustMph(float windGustMph) {
		this.windGustMph = windGustMph;
	}

	/**
	 * @return the windKph
	 */
	@XmlElement(name="wind_kph")
	public float getWindKph() {
		return windKph;
	}

	/**
	 * @param windKph the windKph to set
	 */
	public void setWindKph(float windKph) {
		this.windKph = windKph;
	}

	/**
	 * @return the windGustKph
	 */
	@XmlElement(name="wind_gust_kph")
	public float getWindGustKph() {
		return windGustKph;
	}

	/**
	 * @param windGustKph the windGustKph to set
	 */
	public void setWindGustKph(float windGustKph) {
		this.windGustKph = windGustKph;
	}

	/**
	 * @return the pressureMb
	 */
	@XmlElement(name="pressure_mb")
	public int getPressureMb() {
		return pressureMb;
	}

	/**
	 * @param pressureMb the pressureMb to set
	 */
	public void setPressureMb(int pressureMb) {
		this.pressureMb = pressureMb;
	}

	/**
	 * @return the pressureIn
	 */
	@XmlElement(name="pressure_in")
	public float getPressureIn() {
		return pressureIn;
	}

	/**
	 * @param pressureIn the pressureIn to set
	 */
	public void setPressureIn(float pressureIn) {
		this.pressureIn = pressureIn;
	}

	/**
	 * @return the pressureTrend
	 */
	@XmlElement(name="pressure_trend")
	public String getPressureTrend() {
		return pressureTrend;
	}

	/**
	 * @param pressureTrend the pressureTrend to set
	 */
	public void setPressureTrend(String pressureTrend) {
		this.pressureTrend = pressureTrend;
	}

	/**
	 * @return the dewpointString
	 */
	@XmlElement(name="dewpoint_string")
	public String getDewpointString() {
		return dewpointString;
	}

	/**
	 * @param dewpointString the dewpointString to set
	 */
	public void setDewpointString(String dewpointString) {
		this.dewpointString = dewpointString;
	}

	
	/**
	 * @return the visibilityMi
	 */
	@XmlElement(name="visibility_mi")
	public String getVisibilityMi() {
		return visibilityMi;
	}

	/**
	 * @param visibilityMi the visibilityMi to set
	 */
	public void setVisibilityMi(String visibilityMi) {
		this.visibilityMi = visibilityMi;
	}

	/**
	 * @return the visibilityKm
	 */
	@XmlElement(name="visibility_km")
	public String getVisibilityKm() {
		return visibilityKm;
	}

	/**
	 * @param visibilityKm the visibilityKm to set
	 */
	public void setVisibilityKm(String visibilityKm) {
		this.visibilityKm = visibilityKm;
	}

	/**
	 * @return the uv
	 */
	@XmlElement(name="UV")
	public int getUv() {
		return uv;
	}

	/**
	 * @param uv the uv to set
	 */
	public void setUv(int uv) {
		this.uv = uv;
	}

	/**
	 * @return the icon
	 */
	@XmlElement(name="icon")
	public String getIcon() {
		return icon;
	}

	/**
	 * @param icon the icon to set
	 */
	public void setIcon(String icon) {
		this.icon = icon;
	}

	@JsonIgnore
	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	@JsonIgnore
	public Object getEstimated() {
		return estimated;
	}

	public void setEstimated(Object estimated) {
		this.estimated = estimated;
	}

	public String getObservationTimeRfc822() {
		return observationTimeRfc822;
	}

	@XmlElement(name="observation_time_rfc822")
	public void setObservationTimeRfc822(String observationTimeRfc822) {
		this.observationTimeRfc822 = observationTimeRfc822;
	}
	
	
}
