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

import java.io.IOException;

/**
 * A simple interface for a sensor.
 * 
 * @author Marcus Hirt
 */
public interface Sensor {
	/**
	 * @return the identity of the sensor. The format of the ID depends on the
	 *         kind of sensor. For example, Dallas protocol sensors will use
	 *         their unique device ID. 2302 devices will have two sensors per
	 *         device, the id consisting of the kind of sensor and the gpio-port
	 *         used.
	 */
	String getID();

	/**
	 * The value of the sensor.
	 * 
	 * @return the value of the sensor.
	 * @throws IOException
	 *             if there was a problem accessing the sensor.
	 */
	Number getValue() throws IOException;

	/**
	 * @return the kind of physical quantity the value represents, for example
	 *         Temperature.
	 *         
	 * @see PhysicalQuantity
	 */
	PhysicalQuantity getPhysicalQuantity();

	/**
	 * @return the unit string post-fix to use when rendering the value, for example %.
	 */
	String getUnitString();
}
