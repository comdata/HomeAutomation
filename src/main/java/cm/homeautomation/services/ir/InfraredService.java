package cm.homeautomation.services.ir;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.IRCommand;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.mqtt.client.MQTTSender;
import cm.homeautomation.sensors.IRData;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;
import io.quarkus.vertx.ConsumeEvent;

/**
 * Handle IR messages
 *
 * @author christoph
 *
 */
@Path("ir")
public class InfraredService extends BaseService {
	@Inject
	MQTTSender mqttSender;
	@Inject
	EntityManager em;

	@Inject
	ConfigurationService configurationService;

	private static InfraredService instance;

	public static InfraredService getInstance() {
		if (instance == null) {
			instance = new InfraredService();
		}

		return instance;
	}

	public static void setInstance(final InfraredService instance) {
		InfraredService.instance = instance;
	}

	public InfraredService() {
	}

	/**
	 * provide a list of all IRCommands
	 *
	 * @return
	 */
	@GET
	@Path("get")
	public List<IRCommand> getIRCommands() {

		return em.createQuery("select ic from IRCommand ic", IRCommand.class).getResultList();
	}

	/**
	 * receive IR events and register new values in DB
	 *
	 * @param event
	 */
	@ConsumeEvent(value = "EventObject", blocking = true)
	
	public void handleEvent(final EventObject event) {

		final Object data = event.getData();

		if (data instanceof IRData) {
			final IRData irData = (IRData) data;

			final String typeClear = irData.getTypeClear();
			final String address = irData.getAddress();
			final String command = irData.getCommand();

			if ("UNKNOWN".equals(typeClear)) {
				return;
			}

			final List<IRCommand> resultList = em.createQuery(
					"select ic from IRCommand ic where ic.typeClear=:typeClear and ic.address=:address and ic.command=:command and ic.data=:data",
					IRCommand.class).setParameter("data", irData.getData()).setParameter("typeClear", typeClear)
					.setParameter("command", command).setParameter("address", address).getResultList();

			if ((resultList == null) || resultList.isEmpty()) {
				// not found create an entry

				final IRCommand irCommand = new IRCommand();

				irCommand.setAddress(address);
				irCommand.setCommand(command);
				irCommand.setTypeClear(typeClear);
				irCommand.setType(irData.getType());

				final List<String> valuesString = new ArrayList<>();
				final List<Integer> values = irData.getRawCode();
				for (final Integer integer : values) {
					valuesString.add(integer.toString());
				}
				irCommand.setValues(valuesString);
				irCommand.setData(irData.getData());

				em.persist(irCommand);
			}
		}

	}

	/**
	 * send an IR command
	 *
	 * @param id
	 * @return
	 * @throws JsonProcessingException
	 */
	@GET
	@Path("sendCommand/{id}")
	public GenericStatus sendCommand(@PathParam("id") final Long id) throws JsonProcessingException {
		final List<IRCommand> resultList = em
				.createQuery("select ic from IRCommand ic where ic.id=:id", IRCommand.class).setParameter("id", id)
				.getResultList();

		if ((resultList != null) && !resultList.isEmpty()) {
			final IRCommand irCommand = resultList.get(0);
			sendIRCommand(irCommand);
		}

		return new GenericStatus(true);
	}

	/**
	 * send IR command and if specified the follow up command
	 *
	 * @param irCommand
	 * @throws JsonProcessingException
	 */
	private void sendIRCommand(final IRCommand irCommand) throws JsonProcessingException {
		final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		final String jsonMessage = ow.writeValueAsString(irCommand);

		mqttSender.sendMQTTMessage("/irmessage", jsonMessage);

		final IRCommand followUpCommand = irCommand.getFollowUpCommand();
		if (followUpCommand != null) {
			sendIRCommand(followUpCommand);
		}
	}
}
