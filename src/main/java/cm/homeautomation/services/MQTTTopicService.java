package cm.homeautomation.services;

import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.MQTTTopic;
import cm.homeautomation.services.base.BaseService;

@Path("mqtttopic")
public class MQTTTopicService extends BaseService {

	@GET
	@Path("getAll")
	public List<MQTTTopic> getAll() {
		EntityManager em = EntityManagerService.getManager();
		return em.createQuery("select t from MQTTTopic t", MQTTTopic.class).getResultList();
	}
}
