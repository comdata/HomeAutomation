package cm.homeautomation.services.phone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.logging.log4j.LogManager;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.PhoneCallEvent;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.sensors.weather.WeatherService;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;

@Path("phone")
public class Phone extends BaseService {

	@Path("status/{event}/{mode}/{internalNumber}/{externalNumber}")
	@GET
	public GenericStatus setStatus(@PathParam("event") String event, @PathParam("mode") String mode, @PathParam("internalNumber") String internalNumber,
			@PathParam("externalNumber") String externalNumber) {
		System.out.println(
				"Phone call: " + mode + " internalNumber: " + internalNumber + " external number: " + externalNumber);

		
		
		PhoneCallEvent phoneCallEvent = new PhoneCallEvent(event, mode, internalNumber, externalNumber);
		
		EntityManager em = EntityManagerService.getNewManager();
		em.getTransaction().begin();
		
		em.persist(phoneCallEvent);
		
		em.getTransaction().commit();
		
		EventBusService.getEventBus().post(new EventObject(phoneCallEvent));

		return new GenericStatus(true);
	}

	public static void main(String[] argv) {
		new Phone().getCall("192.168.1.1", 1012);

		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				LogManager.getLogger(Phone.class).info("Thread interrupted.");
			}
		}
	}

	private void getCall(String host, int portNum) {

		System.out.println("Host " + host + "; port " + portNum);
		try {
			Socket s = new Socket(host, portNum);
			new Pipe(s.getInputStream(), System.out).start();
			new Pipe(System.in, s.getOutputStream()).start();
		} catch (IOException e) {
			System.out.println(e);
			return;
		}
		System.out.println("Connected OK");
	}

	/**
	 * This class handles one half of a full-duplex connection.
	 */
	class Pipe extends Thread {
		BufferedReader is;
		PrintStream os;

		Pipe(InputStream is, OutputStream os) {
			this.is = new BufferedReader(new InputStreamReader(is));
			this.os = new PrintStream(os);
		}

	}

}
