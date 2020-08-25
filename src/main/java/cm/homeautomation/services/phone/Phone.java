package cm.homeautomation.services.phone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.logging.log4j.LogManager;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.PhoneCallEvent;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;

/**
 * Phone Call recording and event handling
 * 
 * @author christoph
 *
 */
@Path("phone")
public class Phone extends BaseService {

	/**
	 * accepts call events from external systems and creates a {@link EventObject}
	 * message of type {@link PhoneCallEvent} to all interested systems
	 * 
	 * @param event
	 * @param mode
	 * @param internalNumber
	 * @param externalNumber
	 * @return
	 */
	@Path("status/{event}/{mode}/{internalNumber}/{externalNumber}")
	@GET
	public GenericStatus setStatus(@PathParam("event") String event, @PathParam("mode") String mode,
			@PathParam("internalNumber") String internalNumber, @PathParam("externalNumber") String externalNumber) {

		LogManager.getLogger(this.getClass()).info(
				"Phone call: " + mode + " internalNumber: " + internalNumber + " external number: " + externalNumber);

		PhoneCallEvent phoneCallEvent = new PhoneCallEvent(event, mode, internalNumber, externalNumber);

		EntityManager em = EntityManagerService.getManager();
		em.getTransaction().begin();

		em.persist(phoneCallEvent);

		em.getTransaction().commit();

		EventBusService.getEventBus().post(new EventObject(phoneCallEvent));

		return new GenericStatus(true);
	}

	/**
	 * provide a list of calls recorded in the system
	 * 
	 * @return
	 */
	@GET
	@Path("getCallList")
	public List<PhoneCallEvent> getCallList() {

		EntityManager em = EntityManagerService.getManager();

		return em.createQuery("select p from PhoneCallEvent p order by p.timestamp desc", PhoneCallEvent.class).getResultList();

	}

	/*
	 * preparation for watching for call events internally
	 */
	public static void main(String[] argv) {
		String configurationProperty = ConfigurationService.getConfigurationProperty("phone", "phoneserverip");
		new Phone().getCall(configurationProperty, 1012);

		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				LogManager.getLogger(Phone.class).info("Thread interrupted.");
			}
		}
	}

	private void getCall(String host, int portNum) {

		LogManager.getLogger(this.getClass()).info("Host " + host + "; port " + portNum);
		try (Socket s = new Socket(host, portNum)) {
			new Pipe(s.getInputStream(), System.out).start();
			new Pipe(System.in, s.getOutputStream()).start();
		} catch (IOException e) {
			LogManager.getLogger(this.getClass()).info(e);
			return;
		}
		LogManager.getLogger(this.getClass()).info("Connected OK");
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
		
		@Override
		public void run() {
			// FIXME add implementation
			super.run();
		}

	}

}
