package com.amphibian.weather.response;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name="moon_phase")
public class Astronomy {

	@XmlTransient private int moonPercentIlluminated;
	@XmlTransient private int moonAge;
	@XmlTransient private AstronomyTime currentTime;
	@XmlTransient private AstronomyTime sunrise;
	@XmlTransient private AstronomyTime sunset;

	/**
	 * @return the moonPercentIlluminated
	 */
	@XmlElement(name="percentIlluminated")
	public int getMoonPercentIlluminated() {
		return moonPercentIlluminated;
	}
	/**
	 * @param moonPercentIlluminated the moonPercentIlluminated to set
	 */
	public void setMoonPercentIlluminated(int moonPercentIlluminated) {
		this.moonPercentIlluminated = moonPercentIlluminated;
	}
	/**
	 * @return the moonAge
	 */
	@XmlElement(name="ageOfMoon")
	public int getMoonAge() {
		return moonAge;
	}
	/**
	 * @param moonAge the moonAge to set
	 */
	public void setMoonAge(int moonAge) {
		this.moonAge = moonAge;
	}
	/**
	 * @return the currentTime
	 */
	@XmlElement(name="current_time")
	public AstronomyTime getCurrentTime() {
		return currentTime;
	}
	/**
	 * @param currentTime the currentTime to set
	 */
	public void setCurrentTime(AstronomyTime currentTime) {
		this.currentTime = currentTime;
	}
	/**
	 * @return the sunrise
	 */
	@XmlElement(name="sunrise")
	public AstronomyTime getSunrise() {
		return sunrise;
	}
	/**
	 * @param sunrise the sunrise to set
	 */
	public void setSunrise(AstronomyTime sunrise) {
		this.sunrise = sunrise;
	}
	/**
	 * @return the sunset
	 */
	@XmlElement(name="sunset")
	public AstronomyTime getSunset() {
		return sunset;
	}
	/**
	 * @param sunset the sunset to set
	 */
	public void setSunset(AstronomyTime sunset) {
		this.sunset = sunset;
	}
	
}
