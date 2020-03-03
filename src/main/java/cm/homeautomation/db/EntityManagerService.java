package cm.homeautomation.db;

import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class EntityManagerService {

	private static EntityManager em;
	private static final String PERSISTENCE_UNIT_NAME = "HA";

	private EntityManagerService() {

	}

	public static EntityManager getManager() {
		if (em == null) {

			em = getNewManager();
		}
		return em;
	}

	@Produces
	public static EntityManager getNewManager() {
		EntityManagerFactory factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
		return factory.createEntityManager();

	}
}