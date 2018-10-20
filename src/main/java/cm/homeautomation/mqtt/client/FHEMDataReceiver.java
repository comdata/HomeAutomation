package cm.homeautomation.mqtt.client;

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
		System.out.println("FHEM message for topic: " + topic + " message: " + messageContent);

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

						if (deviceType != null) {

							switch (deviceType) {
							case WINDOW:
								FHEMWindowDataReceiver.receive(topic, messageContent, fhemDevice);
								LogManager.getLogger(FHEMDataReceiver.class).error("Add implementation for device: " + device);
								break;
							case SWITCH:
								LogManager.getLogger(FHEMDataReceiver.class).error("Add implementation for device: " + device);
								break;
							case WINDOWBLIND:
								LogManager.getLogger(FHEMDataReceiver.class).error("Add implementation for device: " + device);
								break;
							case DEVICE:
								FHEMDeviceDataReceiver.receive(topic, messageContent, fhemDevice);
								LogManager.getLogger(FHEMDataReceiver.class).error("Add implementation for device: " + device);
								break;
								
							default:
								LogManager.getLogger(FHEMDataReceiver.class).error(
										"Device type: " + deviceType + " for device: " + device + " not mapped.");
								break;
							}

						} else {
							LogManager.getLogger(FHEMDataReceiver.class).error("Device type for device: " + device);
						}
					}
				} else {
					LogManager.getLogger(FHEMDataReceiver.class).error("FHEM Device not found for device: " + device);
					createNewFHEMDevices(device);					
				}

			}

		}
	}

	private static void createNewFHEMDevices(String device) {
		EntityManager em = EntityManagerService.getNewManager();
		
		em.getTransaction().begin();
		
		FHEMDevice fhemDevice=new FHEMDevice();
		
		fhemDevice.setName(device);
		em.persist(fhemDevice);
		
		em.getTransaction().commit();
	}

}
