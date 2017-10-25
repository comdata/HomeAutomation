package cm.homeautomation.services.ir;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.eventbus.Subscribe;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.IRCommand;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.mqtt.client.MQTTSender;
import cm.homeautomation.sensors.IRData;
import cm.homeautomation.services.base.AutoCreateInstance;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;

/**
 * Handle IR messages
 * 
 * @author christoph
 *
 */
@Path("ir")
public class InfraredService extends BaseService {

	public InfraredService() {
		EventBusService.getEventBus().register(this);
	}

	@GET
	@Path("get")
	public List<IRCommand> getIRCommands() {
		EntityManager em = EntityManagerService.getNewManager();
		@SuppressWarnings("unchecked")
		List<IRCommand> resultList = (List<IRCommand>)em.createQuery("select ic from IRCommand ic").getResultList();
		return resultList;
	}
	
	@GET
	@Path("sendCommand/{id}")
	public GenericStatus sendCommand(@PathParam("id") Long id) throws JsonProcessingException {
		EntityManager em = EntityManagerService.getNewManager();
		@SuppressWarnings("unchecked")
		List<IRCommand> resultList = (List<IRCommand>)em.createQuery("select ic from IRCommand ic where ic.id=:id").setParameter("id", id).getResultList();
		
		if (resultList!=null && !resultList.isEmpty()) {
			IRCommand irCommand = resultList.get(0);
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String jsonMessage = ow.writeValueAsString(irCommand);
			
			MQTTSender.sendMQTTMessage("/irmessage", jsonMessage);
		}
		
		return new GenericStatus(true);
	}
	
	@Subscribe
	public void handleEvent(EventObject event) {

		Object data = event.getData();
		
		if (data instanceof IRData) {
			IRData irData=(IRData)data;
			
			EntityManager em = EntityManagerService.getNewManager();
			
			
			String typeClear = irData.getTypeClear();
			String address = irData.getAddress();
			String command = irData.getCommand();
			
			@SuppressWarnings("unchecked")
			List<IRCommand> resultList = (List<IRCommand>)em.createQuery("select ic from IRCommand ic where ic.typeClear=:typeClear and ic.address=:address and ic.command=:command and ic.data=:data").setParameter("data", irData.getData()).setParameter("typeClear", typeClear).setParameter("command", command).setParameter("address", address).getResultList();
		
			if (resultList!=null && !resultList.isEmpty()) {
				
			} else {
				// not found create an entry
				
				em.getTransaction().begin();
				IRCommand irCommand = new IRCommand();
				
				irCommand.setAddress(address);
				irCommand.setCommand(command);
				irCommand.setTypeClear(typeClear);
				irCommand.setType(irData.getType());
				
				List<String> valuesString = new ArrayList<String>();
				List<Integer> values = irData.getRawCode();
				for (Integer integer : values) {
					valuesString.add(integer.toString());
				}
				irCommand.setValues(valuesString);
				irCommand.setData(irData.getData());
				
				em.persist(irCommand);
				em.getTransaction().commit();
			}
		}

	}
}
