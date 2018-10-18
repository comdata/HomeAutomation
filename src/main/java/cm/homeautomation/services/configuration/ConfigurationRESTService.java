package cm.homeautomation.services.configuration;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.ConfigurationSetting;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;

/**
 * Configuration Service to provide backend methods for configuring the
 * application
 * 
 * @author christoph
 *
 */
@Path("configuration")
public class ConfigurationRESTService extends BaseService {

	@GET
	@Path("getAll")
	public List<ConfigurationSetting> getAllConfigSettings() {
		return ConfigurationService.getAllSettings();
	}

	@GET
	@Path("update/{settingsGroup}/{propery}/{value}")
	public GenericStatus updateSetting(@PathParam("settingsGroup") String settingsGroup,
			@PathParam("property") String property, @PathParam("value") String value) {
		ConfigurationService.createOrUpdate(settingsGroup, property, value);

		return new GenericStatus(true);
	}

}
