package com.homeautomation.tv.panasonic.test;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.homeautomation.tv.panasonic.PanasonicTVBinding;

public class PanasonicTVBindingTest {

	private final int tvPort = 55000;
	private final String tvIp = "127.0.0.1";
	private TVServer tvServer;

	@Before
	public void setup() {
		tvServer = new TVServer();
		tvServer.start();
	}
	
	@After
	public void tearDown() {
		tvServer.setRun(false);
		tvServer.interrupt();
	}
	
	@Test
	public void testCheckAlive() throws Exception {
		System.out.println("Running test for checkAlive");
		tvServer.setStatusCode("HTTP/1.1 200 OK");
		PanasonicTVBinding panasonicTVBinding = new PanasonicTVBinding();
		boolean checkAlive = panasonicTVBinding.checkAlive(tvIp);
		
		assertTrue(checkAlive);
	}
	
	@Test
	public void testCheckAliveFalse() throws Exception {
		System.out.println("Running test for checkAliveFalse");
		tvServer.setStatusCode("HTTP/1.1 404 Not found");
		PanasonicTVBinding panasonicTVBinding = new PanasonicTVBinding();
		boolean checkAlive = panasonicTVBinding.checkAlive(tvIp);
		
		assertTrue(checkAlive);
	}

	class TVServer extends Thread {

		private boolean run = true;
		private String statusCode = "";
		private ServerSocket welcomeSocket;
		
		public TVServer() {

			try {
				welcomeSocket = new ServerSocket(tvPort);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("socket creation failed");
			}
		}

		public void run() {
			System.out.println("Starting server");
			while (run) {

				try {
					
					Socket connectionSocket = welcomeSocket.accept();
					BufferedReader inFromClient = new BufferedReader(
							new InputStreamReader(connectionSocket.getInputStream()));
					DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

					boolean clientData = true;
					while (clientData) {
						String clientSentence = inFromClient.readLine();
						if (clientSentence.equals("")) {
							clientData = false;
						}
					}

					outToClient.writeBytes(statusCode);

					welcomeSocket.close();

				} catch (IOException e) {
				}
			}
		}

		public boolean isRun() {
			return run;
		}

		public void setRun(boolean run) {
			this.run = run;
		}

		public String getStatusCode() {
			return statusCode;
		}

		public void setStatusCode(String statusCode) {
			this.statusCode = statusCode;
		}
	}
}
