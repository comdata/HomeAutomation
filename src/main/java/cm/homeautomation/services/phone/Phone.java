package cm.homeautomation.services.phone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.logging.log4j.LogManager;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.PhoneCallEvent;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;
import io.vertx.core.eventbus.EventBus;

/**
 * Phone Call recording and event handling
 * 
 * @author christoph
 *
 */
@ApplicationScoped
@Path("phone")
public class Phone extends BaseService {

	@Inject
	EventBus bus;

	@Inject
	EntityManager em;

	@Inject
	ConfigurationService configurationService;

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

		em.persist(phoneCallEvent);

		bus.publish("EventObject", new EventObject(phoneCallEvent));

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

		return em.createQuery("select p from PhoneCallEvent p order by p.timestamp desc", PhoneCallEvent.class)
				.getResultList();

	}

	// TODO
	/*
	 * preparation for watching for call events internally
	 */
	public void main(String[] argv) {
		String configurationProperty = configurationService.getConfigurationProperty("phone", "phoneserverip");
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
