package cm.homeautomation.tradfri;

import java.util.Collection;

import javax.crypto.SecretKey;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.DimmableLight;
import cm.homeautomation.entities.RGBLight;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.services.base.AutoCreateInstance;

import cm.homeautomation.services.light.LightService;
import de.eckey.tradfrj.item.device.Device;
import de.eckey.tradfrj.item.device.light.Light;
import de.eckey.tradfrj.item.device.light.LightData;
import de.eckey.tradfrj.request.TradfrjRequestExecutor;
import de.eckey.tradfrj.request.TradfrjRequests;
import de.eckey.tradfrj.request.item.device.lamp.ModifyLightRequestBuilder;
import de.eckey.tradfrj.service.ServiceException;
import de.eckey.tradfrj.service.TradfrjService;
import de.eckey.tradfrj.service.lookup.AuthoritySupplier;
import de.eckey.tradfrj.service.security.SimpleUserPskStore;

@AutoCreateInstance
public class TradfriStartupService {

	
	private static final String TRADFRI = "TRADFRI";
	private EntityManager em;
	
	private static TradfriStartupService instance;
	private TradfrjRequestExecutor executor;

	Collection<Light> lightList = null;

	public static TradfriStartupService getInstance() {
		return instance;
	}

	public static void setInstance(final TradfriStartupService instance) {
		TradfriStartupService.instance = instance;
	}

	public TradfriStartupService() {
		instance = this;
		init();

	}

	public void dimBulb(final String id, final int dimValue) {
		
		Light light = new Light();
		light.setId(Integer.parseInt(id));
		ModifyLightRequestBuilder builder = ModifyLightRequestBuilder.modify(light);
		builder.withDimmer(dimValue);
		LogManager.getLogger(this.getClass()).debug("setting dimValue: {}", dimValue);

		try {
			executor.executeRequest(builder);
		} catch (ServiceException e) {
			LogManager.getLogger(this.getClass()).debug("setting dim value failed", e);
		}

	}

	private void init() {

		String gateway = ConfigurationService.getConfigurationProperty("tradfri", "gateway");
		String secret = ConfigurationService.getConfigurationProperty("tradfri", "secret");
		String token = ConfigurationService.getConfigurationProperty("tradfri", "token");
		String user = ConfigurationService.getConfigurationProperty("tradfri", "user");

		AuthoritySupplier authoritySupplier = new AuthoritySupplier(gateway, 5684);
		TradfrjService service = new TradfrjService(authoritySupplier);

		try {
			createPSK(service, secret, user, token);

			service.start();
			executor = new TradfrjRequestExecutor(service);

			updateDevices();

		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void updateDevices() throws ServiceException {
		Collection<Double> deviceIds = executor.executeRequest(TradfrjRequests.lookupDeviceIDs());

		lightList = executor.executeRequest(TradfrjRequests.lookupLights(deviceIds));

		for (Light deviceLight : lightList) {
			LogManager.getLogger(this.getClass()).debug("Found {} name: {}", deviceLight.getId(), deviceLight.getName());
		
			LogManager.getLogger(this.getClass()).trace("Bulb event registered");

			final cm.homeautomation.entities.Light light = LightService.getInstance().getLightForTypeAndExternalId("TRADFRI",
					Integer.toString(deviceLight.getId()));

			em = EntityManagerService.getNewManager();
			em.getTransaction().begin();

			if (light instanceof DimmableLight) {
				final DimmableLight dimLight = (DimmableLight) light;
				final int intensity = deviceLight.getLightData()[0].getDimmer();

				if (deviceLight.getReachable()==1) {
					dimLight.setBrightnessLevel(intensity);
				} else {
					dimLight.setBrightnessLevel(dimLight.getMinimumValue());
				}

			}

			if (light instanceof RGBLight) {
				final RGBLight rgbLight = (RGBLight) light;
				rgbLight.setColor( deviceLight.getLightData()[0].getColor());

			}

			if (deviceLight.getReachable()==1) {
				// set on or off
				light.setPowerState( deviceLight.getLightData()[0].getOnOff()==1);
			} else {
				light.setPowerState(false);
			}

			EventBusService.getEventBus().post(new EventObject(new LightChangedEvent(light)));

			em.merge(light);

			em.getTransaction().commit();
			LogManager.getLogger(this.getClass()).trace("Bulb event done");
		}
		
		
		
	}

	private String createToken(TradfrjService service, String secret, String user) throws ServiceException {
		return service.authenticateUser(secret, user);
	}

	private void createPSK(TradfrjService service, String secret, String user, String token) throws ServiceException {

		if (token==null || token.isEmpty()) {
			user = RandomStringUtils.randomAlphanumeric(8);
			token = createToken(service, secret, user);
			ConfigurationService.createOrUpdate("tradfri", "token", token);
			ConfigurationService.createOrUpdate("tradfri", "user", user);
		}

		service.setPskStore(new SimpleUserPskStore(user, token));
	}

	public void setColor(final String id, final String color) {

		Light light = new Light();
		light.setId(Integer.parseInt(id));
		ModifyLightRequestBuilder builder = ModifyLightRequestBuilder.modify(light);
		builder.withColor(color.toLowerCase());
		LogManager.getLogger(this.getClass()).debug("setting color: {}", color);

		try {
			executor.executeRequest(builder);
		} catch (ServiceException e) {
			LogManager.getLogger(this.getClass()).debug("setting color failed", e);
		}
	}
}
