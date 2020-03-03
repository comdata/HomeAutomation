package cm.homeautomation.tv.panasonic;

/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;

/**
 * This class in mainly used for receiving internal command and to send them to
 * the Panasonic TV.
 * 
 * @author André Heuer
 * @since 1.7.0
 */
public class PanasonicTVBinding {

	/**
	 * Listening port of the TV
	 */
	private static final int TV_PORT = 55000;

	public PanasonicTVBinding() {
		// nothing to do here
	}

	public boolean checkAlive(String tvIp) {
		boolean alive = false;

		int sendCommandStatus;
		try {
			sendCommandStatus = sendCommand(tvIp, "DUMMY");

			if (sendCommandStatus == 200) {
				alive = true;
			}

		} catch (TVNotReachableException e) {
			LogManager.getLogger(this.getClass()).debug("TVNotReachableException",e );
		}

		return alive;
	}

	/**
	 * This methods sends the command to the TV
	 * 
	 * @return HTTP response code from the TV (should be 200)
	 * @throws TVNotReachableException
	 */
	public int sendCommand(String tvIp, String command) throws TVNotReachableException {
		final String soaprequest_skeleton = "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">"
				+ "<s:Body><u:X_SendKey xmlns:u=\"urn:panasonic-com:service:p00NetworkControl:1\">"
				+ "<X_KeyEvent>NRC_%s</X_KeyEvent></u:X_SendKey></s:Body></s:Envelope>\r";
		String soaprequest = "";

		if (command.toUpperCase().startsWith("HDMI")) {
			soaprequest = String.format(soaprequest_skeleton, command);
		} else {
			soaprequest = String.format(soaprequest_skeleton, command + "-ONOFF");
		}

		if ((tvIp == null) || tvIp.isEmpty()) {
			return 0;
		}
		
		try (Socket client = new Socket(tvIp, TV_PORT);
				BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8));) {

			String header = "POST /nrc/control_0/ HTTP/1.1\r\n";
			header = header + "Host: " + tvIp + ":" + TV_PORT + "\r\n";
			header = header + "SOAPACTION: \"urn:panasonic-com:service:p00NetworkControl:1#X_SendKey\"\r\n";
			header = header + "Content-Type: text/xml; charset=\"utf-8\"\r\n";
			header = header + "Content-Length: " + soaprequest.length() + "\r\n";
			header = header + "\r\n";

			wr.write(header);
			wr.write(soaprequest);

			wr.flush();

			InputStream inFromServer = client.getInputStream();

			BufferedReader reader = new BufferedReader(new InputStreamReader(inFromServer));

			String response = reader.readLine();

			return Integer.parseInt(response.split(" ")[1]);
		} catch (Exception e) {
			throw new TVNotReachableException();
		}
	}
}
