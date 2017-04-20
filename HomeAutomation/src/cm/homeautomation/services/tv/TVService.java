package cm.homeautomation.services.tv;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.logging.log4j.LogManager;

import com.google.common.eventbus.Subscribe;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.PhoneCallEvent;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;
import cm.homeautomation.tv.panasonic.PanasonicTVBinding;
import cm.homeautomation.tv.panasonic.TVNotReachableException;

/**
 * base TV service for PanasonicTVs currently only
 * 
 * @author christoph
 *
 */
@Path("tv")
public class TVService extends BaseService {

	private PanasonicTVBinding tvBinding;
	private String tvIp;
	private static boolean muted=false;

	public TVService() {
		tvBinding = new PanasonicTVBinding();
		tvIp = ConfigurationService.getConfigurationProperty("tv", "tvIp");
		EventBusService.getEventBus().register(this);
	}

	/**
	 * send a command to a TV
	 * 
	 * @param command
	 * @return
	 */
	@Path("sendCommand/{command}")
	@GET
	public GenericStatus sendCommand(@PathParam("command") String command) {

		try {
			// send the command to the TV
			tvBinding.sendCommand(tvIp, command);			
			
			// and tell everyone about it
			TVCommandEvent tvCommandEvent = new TVCommandEvent(tvIp, command);
			EventBusService.getEventBus().post(new EventObject(tvCommandEvent));
		} catch (TVNotReachableException e) {
			LogManager.getLogger(this.getClass()).error(e);
		}

		return new GenericStatus(true);
	}
	
	@GET
	@Path("alive")
	public boolean getAliveStatus() {
		return tvBinding.checkAlive(tvIp);
	}

	@Subscribe
	public void phoneEventHandler(EventObject eventObject) {

		Object eventData = eventObject.getData();

		if (eventData instanceof PhoneCallEvent) {

			PhoneCallEvent callEvent = (PhoneCallEvent) eventData;

			System.out.println("Tv IP: " + tvIp);
			String event = callEvent.getEvent();

			if ("ring".equals(event)) {
				try {
					muted=true;
					tvBinding.sendCommand(tvIp, "MUTE");

					LogManager.getLogger(this.getClass()).info("muting TV");
				} catch (TVNotReachableException e) {
					LogManager.getLogger(this.getClass()).error(e);
				}
			}

			if ("connect".equals(event) && !muted) {
				try {
					muted=true;
					tvBinding.sendCommand(tvIp, "MUTE");

					LogManager.getLogger(this.getClass()).info("muting TV");
				} catch (TVNotReachableException e) {
					LogManager.getLogger(this.getClass()).error(e);
				}
			}
			
			if ("disconnect".equals(event) && muted) {
				try {
					tvBinding.sendCommand(tvIp, "MUTE");
					LogManager.getLogger(this.getClass()).info("unmuting TV");
				} catch (TVNotReachableException e) {
					LogManager.getLogger(this.getClass()).error(e);
				}
			}
		}

	}

}
