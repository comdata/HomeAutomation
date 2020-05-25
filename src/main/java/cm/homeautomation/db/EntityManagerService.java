package cm.homeautomation.db;

import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class EntityManagerService {

	private static final String PERSISTENCE_UNIT_NAME = "HA";

	private EntityManagerService() {

	}

	public static EntityManager getManager() {

		return getNewManager();
	}

	@Produces
	public static EntityManager getNewManager() {
		EntityManagerFactory factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
		return factory.createEntityManager();

	}
}