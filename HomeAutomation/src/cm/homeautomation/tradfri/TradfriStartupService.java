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
	private static TradfriStartupService instance;

	public static TradfriStartupService getInstance() {
		return instance;
	}

	public static void setInstance(final TradfriStartupService instance) {
		TradfriStartupService.instance = instance;
	}

	private EntityManager em;

	private TradfriGateway gw;

	public TradfriStartupService() {
		init();
		instance = this;

	}

	public void dimBulb(final String id, final int dimValue) {

		for (final LightBulb b : gw.bulbs) {
			if (Integer.toString(b.getId()).equals(id)) {
				b.setIntensity(dimValue);
			}

		}

	}

	private void init() {

		gw = new TradfriGateway(ConfigurationService.getConfigurationProperty("tradfri", "gateway"),
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
