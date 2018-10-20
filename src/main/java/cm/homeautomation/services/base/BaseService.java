package cm.homeautomation.services.base;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Base REST Webservice
 * 
 * @author christoph
 *
 */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public abstract class BaseService {

	private static BaseService instance;

	public BaseService() {
		instance=this;
	}
	
	public static <T extends BaseService> T getInstance() {
		return (T) instance;

	}
}
