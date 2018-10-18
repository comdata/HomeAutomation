package com.amphibian.weather.response;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonIgnore;


@XmlRootElement
public class WeatherResponse {
	@JsonIgnore
	private String response;
	
	private Conditions current_observation;
	
	@JsonIgnore
	private List<String> forecast;
	
	public Conditions getCurrent_observation() {
		return current_observation;
	}
	public void setCurrent_observation(Conditions current_observation) {
		this.current_observation = current_observation;
	}
	public List<String> getForecast() {
		return forecast;
	}
	public void setForecast(List<String> forecast) {
		this.forecast = forecast;
	}
	
	@JsonIgnore
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	
}
