package cm.homeautomation.services.mqtt;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.MQTTTopic;
import cm.homeautomation.services.base.BaseService;

@Path("mqtttopic")
public class MQTTTopicService extends BaseService {

	@Inject
	EntityManager em;

	@Inject
	ConfigurationService configurationService;

	@GET
	@Path("getAll")
	public List<MQTTTopic> getAll() {

		List<MQTTTopic> resultList = em.createQuery("select t from MQTTTopic t", MQTTTopic.class).getResultList();

		if (resultList == null) {
			resultList = new ArrayList<>();
		}

		return resultList;
	}
}
