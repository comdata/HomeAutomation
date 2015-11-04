/*
 *  Copyright (C) 2013 Marcus Hirt
 *                     www.hirt.se
 *
 * This software is free:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESSED OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright (C) Marcus Hirt, 2013
 */
package se.hirt.w1.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Helper class representing a DHT sensor. Uses the Adafruit_DHT driver,
 * which must be copied to somewhere on the path.
 * 
 * @author Marcus Hirt
 */
public class DHTSensor {
	private final static String TEMP_STR = "Temp=";
	private final static String HUM_STR = "Humidity=";
	private final int gpioPin;
	private final DHTType type;
	private String lastValue;
	private long lastCheck;

	public DHTSensor(DHTType type, int gpioPin) {
		this.type = type;
		this.gpioPin = gpioPin;
	}

	public String getID() {
		return String.valueOf(gpioPin);
	}

	public synchronized float getHumidity() {
		checkForUpdates();
		return parseHumidity(lastValue);
	}

	private void checkForUpdates() {
		long now = System.currentTimeMillis();
		if (now - lastCheck > 3000) {
			String newValues = readValues();
			if (newValues.indexOf('%') > 0) {
				lastValue = newValues;
				lastCheck = now;
			}
		}
	}

	public synchronized float getTemperature() {
		checkForUpdates();
		return parseTemperature(lastValue);
	}

	private float parseTemperature(String value) {
		if (value == null) {
			return Float.MIN_VALUE;
		}
		return Float.parseFloat(value.substring(value.indexOf(TEMP_STR)
				+ TEMP_STR.length(), value.indexOf('*')));
	}

	private float parseHumidity(String value) {
		if (value == null) {
			return Float.MIN_VALUE;
		}
		return Float.parseFloat(value.substring(value.indexOf(HUM_STR)
				+ HUM_STR.length(), value.indexOf('%')));
	}

	private String readValues() {
		String result = "";
		try {
			Process p = Runtime.getRuntime().exec(
					String.format("sudo AdafruitDHT.py %s %d", type.getCode(), gpioPin));
			BufferedReader in = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			String line = null;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		//	System.err.println(result);
		//	System.out.println(result);
		} catch (Exception e) {
			System.err.println(String.format("Could not read the %s sensor at pin %d", type.getCode(), gpioPin));
			e.printStackTrace();
			return null;
		}
		return result;
	}
}
