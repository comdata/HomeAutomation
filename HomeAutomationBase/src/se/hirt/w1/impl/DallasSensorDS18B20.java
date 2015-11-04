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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import se.hirt.w1.PhysicalQuantity;
import se.hirt.w1.Sensor;

/**
 * DS18B20 type temperature sensor.
 * 
 * @author Marcus Hirt
 */
public class DallasSensorDS18B20 implements Sensor {
	private final File sensorFile;
	private final File valueFile;

	public DallasSensorDS18B20(File sensorFile) {
		this.sensorFile = sensorFile;
		this.valueFile = deriveValueFile(sensorFile);
	}

	@Override
	public String getID() {
		return sensorFile.getName();
	}

	@Override
	public Number getValue() throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(
				valueFile))) {
			String tmp = reader.readLine();
			int index = -1;
			while (tmp != null) {
				index = tmp.indexOf("t=");
				if (index >= 0) {
					break;
				}
				tmp = reader.readLine();
			}
			if (index < 0) {
				throw new IOException("Could not read sensor " + getID());
			}
			return Integer.parseInt(tmp.substring(index + 2)) / 1000f;
		}
	}

	private static File deriveValueFile(File sensorFile) {
		return new File(sensorFile, "w1_slave");
	}
	
	public String toString() {
		try {
			return String.format("Sensor ID: %s, Temperature: %2.3fC", getID(), getValue());
		} catch (IOException e) {
			return String.format("Sensor ID: %s - could not read temperature!C", getID());
		}
	}

	@Override
	public PhysicalQuantity getPhysicalQuantity() {
		return PhysicalQuantity.Temperature;
	}

	@Override
	public String getUnitString() {
		return "\u00B0C";
	}
}
