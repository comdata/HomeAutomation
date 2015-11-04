package com.amphibian.weather.request;

public enum Feature {

	ALERTS("alerts"),
	ALMANAC("almanac"),
	ASTRONOMY("astronomy"),
	CONDITIONS("conditions"),
	CURRENT_HURRICANE("currenthurricane"),
	FORECAST("forecast");
	
	private final String name;
	
	Feature(String f) {
		this.name = f;
	}
	
	public String toString() {
		return this.name;
	}
	
}
