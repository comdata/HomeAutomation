package cm.homeautomation.services.actor;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;

@Path("thermostat")
public class ThermostatService extends BaseService {

	@GET
	@Path("setValue/{id}/{value}")
	public GenericStatus setValue(@PathParam("id") String id, @PathParam("value") float value) {

		//TODO bind FHEM
		System.out.println("Set "+id+" to value: "+value);
		
		return new GenericStatus();
	}

}
