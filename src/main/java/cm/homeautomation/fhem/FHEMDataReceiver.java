
package cm.homeautomation.fhem;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.FHEMDevice;
import cm.homeautomation.entities.FHEMDevice.FHEMDeviceType;

/**
 * receive and event stream from FHEM via MQTT
 * 
 * @author christoph
 *
 */
public class FHEMDataReceiver {

	private FHEMDataReceiver() {
		// do nothing
	}

	public static void receiveFHEMData(String topic, String messageContent) {
//		LogManager.getLogger(FHEMDataReceiver.class).debug("FHEM message for topic: {} message: {}", topic,  messageContent);

		EntityManager em = EntityManagerService.getManager();

		if (topic != null) {
			String[] topicParts = topic.split("/");

			if (topicParts != null && topicParts.length >= 4) {

				String device = topicParts[2];

				@SuppressWarnings("unchecked")
				List<FHEMDevice> resultList = em.createQuery("select f from FHEMDevice f where f.name=:name")
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
