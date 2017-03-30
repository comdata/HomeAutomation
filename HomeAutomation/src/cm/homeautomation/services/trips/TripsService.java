package cm.homeautomation.services.trips;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.CalendarEntry;
import cm.homeautomation.services.base.BaseService;

@Path("trips")
public class TripsService extends BaseService{

	@Path("getUpcoming")
	@GET
	public List<CalendarEntry> getUpcomingTrips() {
		
		EntityManager em = EntityManagerService.getNewManager();
		
		List<CalendarEntry> resultList = em.createQuery("select c from CalendarEntry c where c.end >= :currentDate order by c.start ASC").setParameter("currentDate",new Date()).getResultList();
	
		return resultList;
	}
	
}
