package cm.homeautomation.tv.panasonic.test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class TVServer extends Thread {

	private boolean run = true;
	private String statusCode = "";
	private ServerSocket welcomeSocket;
	
	public TVServer(int tvPort) {

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
