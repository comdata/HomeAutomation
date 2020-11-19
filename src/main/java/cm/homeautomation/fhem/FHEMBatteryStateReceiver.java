package cm.homeautomation.fhem;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;

import cm.homeautomation.entities.FHEMDevice;
import cm.homeautomation.entities.ManualTask;
import cm.homeautomation.fhem.BatteryStateResult.BatteryState;
import io.vertx.core.eventbus.EventBus;

@ApplicationScoped
public class FHEMBatteryStateReceiver {

	private static final String BATTERY = "battery";
	
	@Inject
	EventBus bus;
	
	@Inject
	EntityManager em;

	private FHEMBatteryStateReceiver() {
		// do nothing
	}

	public BatteryStateResult receive(String topic, String messageContent, FHEMDevice fhemDevice) {
		if (topic != null && topic.endsWith(BATTERY)) {

			BigDecimal value = new BigDecimal(messageContent.split(" ")[0]);

			BatteryStateResult batteryStateResult = new BatteryStateResult();
			batteryStateResult.setStateValue(value);

			batteryStateResult.setFhemDevice(fhemDevice);
			batteryStateResult.setState(BatteryState.NOTOK);
			if (value.compareTo(BigDecimal.valueOf(25)) > 0) {
				batteryStateResult.setState(BatteryState.OK);
			} else {
				BatteryLowEvent batteryLowEvent = new BatteryLowEvent();
				batteryLowEvent.setBatteryStateResult(batteryStateResult);
				bus.publish("BatteryLowEvent", batteryLowEvent);

				createTaskForBatteryDevice(fhemDevice);

			}

			return batteryStateResult;
		}
		return null;
	}

	private void createTaskForBatteryDevice(FHEMDevice fhemDevice) {

		List<ManualTask> resultList = em
				.createQuery("select t from ManualTask t where t.externalId=:externalId and t.type=:type",
						ManualTask.class)
				.setParameter("externalId", fhemDevice.getId()).setParameter("type", "FHEM").getResultList();

		if (resultList == null || resultList.isEmpty()) {
			ManualTask manualTask = new ManualTask("Battery", "Battery low for FHEM device: " + fhemDevice.getName(),
					"FHEM");

			manualTask.setExternalId(fhemDevice.getId());
			manualTask.setCreatedDateTime(new Date());

			em.getTransaction().begin();
			em.persist(manualTask);
			em.getTransaction().commit();
		}
	}

}
