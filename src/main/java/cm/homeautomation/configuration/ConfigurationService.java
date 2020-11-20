package cm.homeautomation.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transactional;
import javax.transaction.UserTransaction;

import cm.homeautomation.entities.ConfigurationSetting;

/**
 * provide configuration setting information
 * 
 * @author cmertins
 *
 */
@ApplicationScoped
public class ConfigurationService {

	@Inject
	EntityManager em;
	
	@Inject UserTransaction transaction;

	private ConfigurationService() {
		// not to be created
	}

	/**
	 * get configuration setting value
	 * 
	 * @param settingsGroup
	 * @param property
	 * @return
	 */
	
	public String getConfigurationProperty(String settingsGroup, String property) {

		
		List<ConfigurationSetting> resultList = em.createQuery(
				"select c from ConfigurationSetting c where c.settingsGroup=:settingsGroup and c.property=:property",
				ConfigurationSetting.class).setParameter("settingsGroup", settingsGroup)
				.setParameter("property", property).getResultList();

		if (resultList != null && !resultList.isEmpty()) {
			return resultList.get(0).getValue();
		}

		return null;
	}

	/**
	 * set configuration parameters
	 * 
	 * @param settingsGroup
	 * @param property
	 * @param value
	 */
	public void createOrUpdate(String settingsGroup, String property, String value) {
		@SuppressWarnings("unchecked")
		List<ConfigurationSetting> resultList = (em.createQuery(
				"select c from ConfigurationSetting c where c.settingsGroup=:settingsGroup and c.property=:property")
				.setParameter("settingsGroup", settingsGroup).setParameter("property", property).getResultList());

		ConfigurationSetting configSetting;
		try {
			transaction.begin();
		} catch (NotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (resultList != null && !resultList.isEmpty()) {
			configSetting = resultList.get(0);
			configSetting.setValue(value);
			em.merge(configSetting);
		} else {
			configSetting = new ConfigurationSetting();
			configSetting.setProperty(property);
			configSetting.setSettingsGroup(settingsGroup);
			configSetting.setValue(value);
			em.persist(configSetting);
		}

		try {
			transaction.commit();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RollbackException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HeuristicMixedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HeuristicRollbackException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public  void purgeAllSettings() {

		try {
			transaction.begin();
		} catch (NotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		em.createQuery("delete from ConfigurationSetting").executeUpdate();

		try {
			transaction.commit();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RollbackException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HeuristicMixedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HeuristicRollbackException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<ConfigurationSetting> getAllSettings() {

		@SuppressWarnings("unchecked")
		List<ConfigurationSetting> resultList = em.createQuery("select c from ConfigurationSetting c").getResultList();

		if (resultList != null && resultList.isEmpty()) {
			return new ArrayList<>();
		}

		return resultList;

	}
}
