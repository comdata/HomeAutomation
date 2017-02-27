package cm.homeautomation.configuration;

import java.util.List;

import javax.persistence.EntityManager;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.ConfigurationSetting;

/**
 * provide configuration setting information
 * 
 * @author cmertins
 *
 */
public class ConfigurationService {

	/**
	 * get configuration setting value
	 * 
	 * @param settingsGroup
	 * @param property
	 * @return
	 */
	public static String getConfigurationProperty(String settingsGroup, String property) {
		
		EntityManager em = EntityManagerService.getNewManager();
		
		List<ConfigurationSetting> resultList = (List<ConfigurationSetting>)em.createQuery("select c from ConfigurationSetting c where c.settingsGroup=:settingsGroup and c.property=:property").setParameter("settingsGroup", settingsGroup).setParameter("property", property).getResultList();
		
		em.close();
		
		if (resultList!=null && !resultList.isEmpty()) {
			return resultList.get(0).getValue();
		}
		
		return null;
	}
}
