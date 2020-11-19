package cm.homeautomation.services.trips;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.CalendarEntry;
import cm.homeautomation.services.base.BaseService;

@Path("trips")
public class TripsService extends BaseService {
	@Inject
	EntityManager em;

	@Inject
	ConfigurationService configurationService;

	@Path("getUpcoming")
	@GET
	public List<CalendarEntry> getUpcomingTrips() {


		List<CalendarEntry> resultList = em
				.createQuery("select c from CalendarEntry c where c.end >= :currentDate order by c.start ASC", CalendarEntry.class)
				.setParameter("currentDate", new Date()).getResultList();

		return resultList;
	}

}
