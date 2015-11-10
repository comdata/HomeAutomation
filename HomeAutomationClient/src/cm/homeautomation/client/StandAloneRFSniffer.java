package cm.homeautomation.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Future;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.jackson.JacksonFeature;

import cm.homeautomation.sensors.RFEvent;

public class StandAloneRFSniffer extends Thread {

	private String url;

	public StandAloneRFSniffer(String url) {
		this.url = url;

	}

	public StreamWrapper getStreamWrapper(InputStream is, String type) {
		return new StreamWrapper(is, type);
	}

	private class StreamWrapper extends Thread {
		InputStream is = null;
		String type = null;

		StreamWrapper(InputStream is, String type) {
			this.is = is;
			this.type = type;
		}

		public void run() {
			try {
				Client c = ClientBuilder.newBuilder().register(JacksonFeature.class).build();
				WebTarget r = c.target(url);

				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String line = null;
				while ((line = br.readLine()) != null) {
					if (line.startsWith("Received ")) {
						String code = line.replaceAll("Received ", "");

						int parsedCode = Integer.parseInt(code);
						System.out.println(parsedCode);

						try {
							RFEvent event = new RFEvent();
							event.setCode(parsedCode);

							r.request(MediaType.APPLICATION_JSON).async()
									.post( Entity.entity(event, MediaType.APPLICATION_JSON), new InvocationCallback<Response>() {

										@Override
										public void completed(Response response) {
											// TODO Auto-generated method stub
			
											System.out.println("Status: " + response.getStatus());
										}

										@Override
										public void failed(Throwable throwable) {
											// TODO Auto-generated method stub
											System.out.println("Error message: " + throwable.getMessage());
										}});
							
						} catch (Exception e) {

						}
					}

				}

			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		new StandAloneRFSniffer(args[1]).start();
	}

	public void run() {
		try {
			while (true) {
				Runtime rt = Runtime.getRuntime();

				StreamWrapper output;

				Process proc = rt.exec("RFSniffer");
				// error = rte.getStreamWrapper(proc.getErrorStream(), "ERROR");
				output = getStreamWrapper(proc.getInputStream(), "OUTPUT");
				int exitVal = 0;

				// error.start();
				output.start();
				// error.join(3000);
				output.join(3000);
				exitVal = proc.waitFor();
			}
		} catch (Exception e) {
			System.err.println("Could not sniff");
			e.printStackTrace();

		}
	}
}
