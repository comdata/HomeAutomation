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
package se.hirt.w1;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import se.hirt.w1.impl.DHTType;
import se.hirt.w1.impl.DallasSensorDS18B20;
import se.hirt.w1.impl.DHTHygrometer;
import se.hirt.w1.impl.DHTSensor;
import se.hirt.w1.impl.DHTTemperature;

/**
 * Main API entry point. Usage: {@link Sensors#getSensors()}.
 * 
 * @author Marcus Hirt
 */
public class Sensors {
	/**
	 * Simple example, printing the number of sensors found, and then the identity and value of each 
	 * sensor, sleeping a second between each sensor reading. 
	 * 
	 * @param args not used.
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Set<Sensor> sensors = Sensors.getSensors();
		System.out.println(String.format("Found %d sensors!", sensors.size()));
		while (true) {
			for (Sensor sensor : sensors) {
				System.out.println(String.format("%s(%s):%3.2f%s", sensor.getPhysicalQuantity(), sensor.getID(), sensor.getValue(), sensor.getUnitString()));
				Thread.sleep(1000);
			}
			System.out.println("");
			System.out.flush();
		}		
	}

	/**
	 * @return all available sensors.
	 * @throws IOException
	 */
	public static Set<Sensor> getSensors() throws IOException {
		Set<Sensor> sensors = new HashSet<Sensor>();
		addDallasSensors(sensors);
		addDHTSensors(sensors);
		return sensors;
	}

	private static void addDHTSensors(Set<Sensor> sensors) {
		Properties props = new Properties();
		try {
			props.load(Sensors.class.getResourceAsStream("dhtsensors.properties"));
		} catch (IOException e) {
			System.out.println("Could not find sensors properties!");
			e.printStackTrace();
			return;
		}
		for (int i = 0; true; i++) {
			String typeStr = props.getProperty(String.format("sensor%d.type", i));
			if (typeStr == null) {
				break;
			}
			DHTType type = DHTType.getType(typeStr);
			if (type != null) {
				String pin = props.getProperty(String.format("sensor%d.pin", i));
				if (pin == null) {
					continue;
				}
				DHTSensor sensor = new DHTSensor(type, Integer.parseInt(pin));
				sensors.add(new DHTTemperature(sensor));
				sensors.add(new DHTHygrometer(sensor));
			} else {
				System.err.println("No support for type " + type + ". Continuing.");
			}
		}
	}

	private static void addDallasSensors(Set<Sensor> sensors)
			throws IOException {
		File sensorFolder = new File("/sys/bus/w1/devices");
		if (!sensorFolder.exists()) {
			throw new IOException("Could not find w1 devices! Please ensure that mods w1-gpio and w1-therm are loaded.");
		}
		for (File f : sensorFolder.listFiles()) {
			if (f.getName().startsWith("w1_bus_master")) {
				continue;
			}
			sensors.add(new DallasSensorDS18B20(f));
		}
	}
}
