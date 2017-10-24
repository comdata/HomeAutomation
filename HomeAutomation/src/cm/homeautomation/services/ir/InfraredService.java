package cm.homeautomation.services.ir;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import com.google.common.eventbus.Subscribe;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.IRCommand;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.sensors.IRData;
import cm.homeautomation.services.base.AutoCreateInstance;
import cm.homeautomation.services.base.BaseService;

/**
 * Handle IR messages
 * 
 * @author christoph
 *
 */
@AutoCreateInstance
public class InfraredService extends BaseService {

	public InfraredService() {
		EventBusService.getEventBus().register(this);
	}

	@Subscribe
	public void handleEvent(EventObject event) {

		Object data = event.getData();
		
		if (data instanceof IRData) {
			IRData irData=(IRData)data;
			
			EntityManager em = EntityManagerService.getNewManager();
			
			
			String typeClear = irData.getTypeClear();
			int address = irData.getAddress();
			int command = irData.getCommand();
			
			@SuppressWarnings("unchecked")
			List<IRCommand> resultList = (List<IRCommand>)em.createQuery("select ic from IRCommand ic where ic.typeClear=:typeClear and ic.address=:address and ic.command=:command").setParameter("typeClear", typeClear).setParameter("command", command).setParameter("address", address).getResultList();
		
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
				
				em.persist(irCommand);
				em.getTransaction().commit();
			}
		}

	}
}
