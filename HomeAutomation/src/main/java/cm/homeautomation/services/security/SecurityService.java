package cm.homeautomation.services.security;

import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import cm.homeautomation.entities.SecurityZone;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;

@Path("security")
public class SecurityService extends BaseService {

	@Path("getZones")
	public List<SecurityZone> getZones() {
		
		return null;
	}
	
	@Path("setZoneState/{id}/{state}")
	public GenericStatus setZoneState(@PathParam("id") Long id, @PathParam("state") String state) {
		return new GenericStatus(true);
		
	}
	
}
