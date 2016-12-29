package cm.homeautomation.services.networkmonitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.NetworkDevice;
import cm.homeautomation.networkMonitor.NetworkScanner;
import cm.homeautomation.services.base.BaseService;

/** 
 * service to get all hosts from the {@link NetworkScanner} internal list
 * 
 * @author christoph
 *
 */
@Path("networkdevices")
public class NetworkDevicesService extends BaseService {

	@Path("getAll")
	@GET
	public List<NetworkDevice> readAll() {
		EntityManager em = EntityManagerService.getNewManager();
		@SuppressWarnings("unchecked")
		List<NetworkDevice> resultList = (List<NetworkDevice>)em.createQuery("select n from NetworkDevice n").getResultList();
	
		if (resultList==null) {
			resultList=new ArrayList<NetworkDevice>();
		}
		
		return resultList;
	}
	
}
