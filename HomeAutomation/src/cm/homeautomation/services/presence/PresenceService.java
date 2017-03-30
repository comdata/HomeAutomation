package cm.homeautomation.services.presence;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Person;
import cm.homeautomation.entities.PresenceState;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;

@Path("presence")
public class PresenceService extends BaseService {

	public GenericStatus purgeStates() {
		EntityManager em = EntityManagerService.getNewManager();
		
		em.getTransaction().begin();
		
		em.createQuery("delete from PresenceState ps").executeUpdate();
		
		em.getTransaction().commit();
		
		return new GenericStatus(true);
	}
	
	@GET
	@Path("setPresence/{id}/{state}")
	public GenericStatus setPresence(@PathParam("id") Long id, @PathParam("state") String state) {

		EntityManager em = EntityManagerService.getNewManager();

		Person person = null;

		List<Person> resultList = (List<Person>) em.createQuery("select p from Person p where p.id=:id")
				.setParameter("id", id).getResultList();

		if (resultList != null && !resultList.isEmpty()) {
			person = resultList.get(0);
		}

		if (person != null) {
			PresenceState presenceState = null;
			List<PresenceState> result = (List<PresenceState>) em
					.createQuery("select p from PresenceState p where p.person=:person order by p.date desc")
					.setParameter("person", person).getResultList();

			if (result != null && !result.isEmpty()) {
				presenceState = result.get(0);
			}

			if ((presenceState != null && !presenceState.equals(state)) || presenceState==null) {
				
				em.getTransaction().begin();
				
				PresenceState newState = new PresenceState();
				
				newState.setDate(new Date());
				newState.setPerson(person);
				newState.setState(state);
				
				em.persist(newState);
				
				EventBusService.getEventBus().post(new EventObject(newState));

				em.getTransaction().commit();
			}
		}
		
		em.close();
		
		return new GenericStatus(true);
	}

	@GET
	@Path("getAll")
	public List<PresenceState> getPresences() {
		
		List<PresenceState> foundPresenceStates=new ArrayList<PresenceState>();
		
		EntityManager em = EntityManagerService.getNewManager();


		List<Person> personList = (List<Person>) em.createQuery("select p from Person p").getResultList();
		
		for (Person person: personList) {
			
			if (person != null) {
				PresenceState presenceState = null;
				List<PresenceState> result = (List<PresenceState>) em
						.createQuery("select p from PresenceState p where p.person=:person order by p.date desc")
						.setParameter("person", person).getResultList();
				
				if (result != null && !result.isEmpty()) {
					foundPresenceStates.add(result.get(0));
				}
			}
			
		}
		return foundPresenceStates;
	}

	
}
