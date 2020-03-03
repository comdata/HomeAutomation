package cm.homeautomation.services;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import cm.homeautomation.entities.MQTTTopic;
import cm.homeautomation.services.base.BaseService;

@Path("mqtttopic")
public class MQTTTopicService extends BaseService {

	@Inject
	EntityManager em;
	
	@GET
	@Path("getAll")
	public List<MQTTTopic> getAll() {
		
		return em.createQuery("select t from MQTTTopic t", MQTTTopic.class).getResultList();
	}
}
