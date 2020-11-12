package cm.homeautomation.services.networkmonitor;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.DashButton;
import cm.homeautomation.entities.NetworkDevice;
import cm.homeautomation.mqtt.client.MQTTSendEvent;
import cm.homeautomation.networkmonitor.NetworkScanner;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;

/**
 * service to get all hosts from the {@link NetworkScanner} internal list
 *
 * @author christoph
 *
 */
@Singleton
@Path("networkdevices/")
public class NetworkDevicesService extends BaseService {

	@Inject
	EventBus bus;

	@GET
	@Path("delete/{name}/{ip}/{mac}")
	public GenericStatus delete(@PathParam("name") final String name, @PathParam("ip") final String ip,
			@PathParam("mac") final String mac) {

		EntityManager em = EntityManagerService.getManager();

		em.getTransaction().begin();

		em.createQuery("delete from DashButton db where db.mac=:mac", DashButton.class).setParameter("mac", mac)
				.executeUpdate();

		em.getTransaction().commit();

		return new GenericStatus(true);
	}

	@Path("getAll")
	@GET
	public List<NetworkDevice> readAll() {
		final EntityManager em = EntityManagerService.getManager();
		@SuppressWarnings("unchecked")
		List<NetworkDevice> resultList = em.createQuery("select n from NetworkDevice n").getResultList();

		if (resultList == null) {
			resultList = new ArrayList<>();
		}
		return resultList;
	}

	@GET
	@Path("wake/{mac}")
	public GenericStatus wakeUp(@PathParam("mac") final String macStr) {
		return wakeUp(new NetworkWakeupEvent(macStr));
	}

	@ConsumeEvent(value = "NetworkWakeUpEvent", blocking = true)
	public GenericStatus wakeUp(NetworkWakeupEvent event) {

		try {
			String topic = "networkServices/wakeup";
			ObjectMapper objectMapper = new ObjectMapper();
			String payload = objectMapper.writeValueAsString(event);
			System.out.println(payload);

			MQTTSendEvent mqttSendEvent = new MQTTSendEvent(topic, payload);
			bus.publish("MQTTSendEvent", mqttSendEvent);
			return new GenericStatus(true);
		} catch (JsonProcessingException e) {
			return new GenericStatus(false);
		}
	}
}
