
package cm.homeautomation.fhem;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;
import org.greenrobot.eventbus.Subscribe;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.FHEMDevice;
import cm.homeautomation.entities.FHEMDevice.FHEMDeviceType;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.services.base.AutoCreateInstance;

/**
 * receive and event stream from FHEM via MQTT
 * 
 * @author christoph
 *
 */
@AutoCreateInstance
@ApplicationScoped
public class FHEMDataReceiver {

	private FHEMDataReceiver() {
		EventBusService.getEventBus().register(this);
	}

	@Subscribe
	public void handleFHEMData(FHEMDataEvent fhemDataEvent) {
		String messageContent=fhemDataEvent.getPayload();
		String topic=fhemDataEvent.getTopic();
		
		receiveFHEMData(topic, messageContent);
	}
	
	
	public  void receiveFHEMData(String topic, String messageContent) {
//		LogManager.getLogger(FHEMDataReceiver.class).debug("FHEM message for topic: {} message: {}", topic,  messageContent);

		EntityManager em = EntityManagerService.getManager();

		if (topic != null) {
			String[] topicParts = topic.split("/");

			if (topicParts != null && topicParts.length >= 4) {

				String device = topicParts[2];

				List<FHEMDevice> resultList = em.createQuery("select f from FHEMDevice f where f.name=:name", FHEMDevice.class)
						.setParameter("name", device).getResultList();

				if (resultList != null && !resultList.isEmpty()) {
					for (FHEMDevice fhemDevice : resultList) {
						FHEMDeviceType deviceType = fhemDevice.getDeviceType();

						handleDeviceSpecific(topic, messageContent, device, fhemDevice, deviceType);
					}
				} else {
//					LogManager.getLogger(FHEMDataReceiver.class).error("FHEM Device not found for device: {}", device);
					createNewFHEMDevices(device);					
				}

			}

		}
	}

	private static void handleDeviceSpecific(String topic, String messageContent, String device, FHEMDevice fhemDevice,
			FHEMDeviceType deviceType) {
		if (deviceType != null) {

			FHEMBatteryStateReceiver.receive(topic, messageContent, fhemDevice);
			
			switch (deviceType) {
			case WINDOW:
				FHEMWindowDataReceiver.receive(topic, messageContent, fhemDevice);
				break;
			case SWITCH:
				LogManager.getLogger(FHEMDataReceiver.class).error("Add implementation for device: " + device);
				break;
			case WINDOWBLIND:
				FHEMWindowBlindDataReceiver.receive(topic, messageContent, fhemDevice);
				break;
			case DEVICE:
				FHEMDeviceDataReceiver.receive(topic, messageContent, fhemDevice);
				break;
				
			default:
//				LogManager.getLogger(FHEMDataReceiver.class).error(
//						"Device type: " + deviceType + " for device: " + device + " not mapped.");
				break;
			}

		} else {
//			LogManager.getLogger(FHEMDataReceiver.class).debug("Device type for device: " + device);
		}
	}

	private static void createNewFHEMDevices(String device) {
		EntityManager em = EntityManagerService.getManager();
		
		em.getTransaction().begin();
		
		FHEMDevice fhemDevice=new FHEMDevice();
		
		fhemDevice.setName(device);
		em.persist(fhemDevice);
		
		em.getTransaction().commit();
	}

}
