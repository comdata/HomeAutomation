
package cm.homeautomation.fhem;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import cm.homeautomation.entities.FHEMDevice;
import cm.homeautomation.entities.FHEMDevice.FHEMDeviceType;
import io.quarkus.vertx.ConsumeEvent;

/**
 * receive and event stream from FHEM via MQTT
 * 
 * @author christoph
 *
 */
@ApplicationScoped
public class FHEMDataReceiver {

	@Inject
	FHEMBatteryStateReceiver batteryStateReceiver;

	@Inject
	FHEMDeviceDataReceiver fhemDeviceDataReceiver;

	@Inject
	EntityManager em;

	@ConsumeEvent(value = "FHEMDataEvent", blocking = true)
	public void handleFHEMData(FHEMDataEvent fhemDataEvent) {
		String messageContent = fhemDataEvent.getPayload();
		String topic = fhemDataEvent.getTopic();

		receiveFHEMData(topic, messageContent);
	}

	public void receiveFHEMData(String topic, String messageContent) {
//		//LogManager.getLogger(FHEMDataReceiver.class).debug("FHEM message for topic: {} message: {}", topic,  messageContent);

		if (topic != null) {
			String[] topicParts = topic.split("/");

			if (topicParts != null && topicParts.length >= 4) {

				String device = topicParts[2];

				List<FHEMDevice> resultList = em
						.createQuery("select f from FHEMDevice f where f.name=:name", FHEMDevice.class)
						.setParameter("name", device).getResultList();

				if (resultList != null && !resultList.isEmpty()) {
					for (FHEMDevice fhemDevice : resultList) {
						FHEMDeviceType deviceType = fhemDevice.getDeviceType();

						handleDeviceSpecific(topic, messageContent, device, fhemDevice, deviceType);
					}
				} else {
//					//LogManager.getLogger(FHEMDataReceiver.class).error("FHEM Device not found for device: {}", device);
					createNewFHEMDevices(device);
				}

			}

		}
	}

	private void handleDeviceSpecific(String topic, String messageContent, String device, FHEMDevice fhemDevice,
			FHEMDeviceType deviceType) {
		if (deviceType != null) {

			batteryStateReceiver.receive(topic, messageContent, fhemDevice);

			switch (deviceType) {
			case WINDOW:
				FHEMWindowDataReceiver.receive(topic, messageContent, fhemDevice);
				break;
			case SWITCH:
				//LogManager.getLogger(FHEMDataReceiver.class).error("Add implementation for device: " + device);
				break;
			case WINDOWBLIND:
				FHEMWindowBlindDataReceiver.receive(topic, messageContent, fhemDevice);
				break;
			case DEVICE:
				fhemDeviceDataReceiver.receive(topic, messageContent, fhemDevice);
				break;

			default:
//				//LogManager.getLogger(FHEMDataReceiver.class).error(
//						"Device type: " + deviceType + " for device: " + device + " not mapped.");
				break;
			}

		} else {
//			//LogManager.getLogger(FHEMDataReceiver.class).debug("Device type for device: " + device);
		}
	}

	
	private void createNewFHEMDevices(String device) {

		FHEMDevice fhemDevice = new FHEMDevice();

		fhemDevice.setName(device);
		em.persist(fhemDevice);

	}

}
