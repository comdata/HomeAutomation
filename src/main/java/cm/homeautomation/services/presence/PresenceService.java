package cm.homeautomation.services.presence;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.Person;
import cm.homeautomation.entities.PresenceState;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;
import io.vertx.core.eventbus.EventBus;

@Singleton
@Path("presence/")
public class PresenceService extends BaseService {

	@Inject
	EventBus bus;
	
	@Inject
	EntityManager em;

	@Inject
	ConfigurationService configurationService;
	
	public GenericStatus purgeStates() {
		
		
		em.getTransaction().begin();
		
		em.createQuery("delete from PresenceState ps").executeUpdate();
		
		em.getTransaction().commit();
		
		return new GenericStatus(true);
	}
	
	@GET
	@Path("setPresence/{id}/{state}")
	public GenericStatus setPresence(@PathParam("id") Long id, @PathParam("state") String state) {

		

		Person person = null;

		@SuppressWarnings("unchecked")
		List<Person> resultList = em.createQuery("select p from Person p where p.id=:id")
				.setParameter("id", id).getResultList();

		if (resultList != null && !resultList.isEmpty()) {
			person = resultList.get(0);
		}

		if (person != null) {
			PresenceState presenceState = null;
			@SuppressWarnings("unchecked")
			List<PresenceState> result = em
					.createQuery("select p from PresenceState p where p.person=:person order by p.date desc")
					.setParameter("person", person).getResultList();

			if (result != null && !result.isEmpty()) {
				presenceState = result.get(0);
			}

			if ((presenceState != null && !presenceState.getState().equals(state)) || presenceState==null) {
				
				em.getTransaction().begin();
				
				PresenceState newState = new PresenceState();
				
				newState.setDate(new Date());
				newState.setPerson(person);
				newState.setState(state);
				
				em.persist(newState);
				
				bus.publish("EventObject", new EventObject(newState));

				em.getTransaction().commit();
			}
		}
		
		
		return new GenericStatus(true);
	}

	@GET
	@Path("getAll")
	public List<PresenceState> getPresences() {
		
		List<PresenceState> foundPresenceStates=new ArrayList<>();
		
		


		@SuppressWarnings("unchecked")
		List<Person> personList = em.createQuery("select p from Person p").getResultList();
		
		for (Person person: personList) {
			
			if (person != null) {
				List<PresenceState> result = em
						.createQuery("select p from PresenceState p where p.person=:person order by p.date desc", PresenceState.class)
						.setParameter("person", person).getResultList();
				
				if (result != null && !result.isEmpty()) {
					foundPresenceStates.add(result.get(0));
				}
			}
			
		}
		return foundPresenceStates;
	}

	
}
