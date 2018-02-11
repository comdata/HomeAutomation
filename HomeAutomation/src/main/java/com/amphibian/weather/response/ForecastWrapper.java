package com.amphibian.weather.response;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name="forecast")
public class ForecastWrapper {

	@XmlTransient private Forecast textForecast;
	@XmlTransient private Forecast simpleForecast;
	/**
	 * @return the textForecast
	 */
	@XmlElement(name="txt_forecast")
	public Forecast getTextForecast() {
		return textForecast;
	}
	/**
	 * @param textForecast the textForecast to set
	 */
	public void setTextForecast(Forecast textForecast) {
		this.textForecast = textForecast;
	}
	/**
	 * @return the simpleForecast
	 */
	@XmlElement(name="simpleforecast")
	public Forecast getSimpleForecast() {
		return simpleForecast;
	}
	/**
	 * @param simpleForecast the simpleForecast to set
	 */
	public void setSimpleForecast(Forecast simpleForecast) {
		this.simpleForecast = simpleForecast;
	}

	
	
}
