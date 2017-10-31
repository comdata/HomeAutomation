package cm.homeautomation.tradfri;

import javax.persistence.EntityManager;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.DimmableLight;
import cm.homeautomation.entities.Light;
import cm.homeautomation.services.base.AutoCreateInstance;
import cm.homeautomation.services.light.LightService;

@AutoCreateInstance
public class TradfriStartupService {
	private static final String TRADFRI = "TRADFRI";
	private EntityManager em;

	public TradfriStartupService() {
		init();

	}

	private void init() {

		final TradfriGateway gw = new TradfriGateway(
				ConfigurationService.getConfigurationProperty("tradfri", "gateway"),
				ConfigurationService.getConfigurationProperty("tradfri", "secret"));
		gw.initCoap();
		gw.dicoverBulbs();
		em = EntityManagerService.getNewManager();
		em.getTransaction().begin();

		for (final LightBulb b : gw.bulbs) {
			Light light = LightService.getInstance().getLightForTypeAndExternalId(TRADFRI, Integer.toString(b.getId()));

			if (light == null) {

				light = new DimmableLight();
				light.setName(b.getName());
				light.setExternalId(Integer.toString(b.getId()));
				light.setLightType(TRADFRI);
				light.setDateInstalled(b.getDateInstalled());
				light.setDateLastSeen(b.getDateLastSeen());
				light.setFirmware(b.getFirmware());
				light.setOnline(b.isOnline());

				em.persist(light);
			}

			System.out.println(b.toString());
		}
		em.getTransaction().commit();
	}
}
