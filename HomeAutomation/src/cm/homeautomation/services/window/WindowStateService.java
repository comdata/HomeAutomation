package cm.homeautomation.services.window;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.device.DeviceService;
import cm.homeautomation.entities.WindowState;
import cm.homeautomation.sensors.window.WindowStateData;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;

@Path("window")
public class WindowStateService extends BaseService {

	@GET
	@Path("readAll")
	public List<WindowStateData> get() {

		final List<WindowStateData> windowStateList = new ArrayList<>();

		final EntityManager em = EntityManagerService.getNewManager();

		final List<WindowState> results = em.createQuery(
				"select ws from WindowState ws where ws.id in (select max(w.id) from WindowState w group by w.mac)")
				.getResultList();

		for (final WindowState windowState : results) {
			final String mac = windowState.getMac();

			final WindowStateData windowStateData = new WindowStateData();

			windowStateData.setMac(windowState.getMac());
			windowStateData.setState(windowState.getState());

			windowStateData.setRoom(DeviceService.getRoomForMac(mac));
			windowStateData.setDevice(DeviceService.getDeviceForMac(mac));
			windowStateData.setDate(windowState.getTimestamp());

			windowStateList.add(windowStateData);
		}

		em.close();

		return windowStateList;
	}

	@GET
	@Path("setState/{windowState}/{state}")
	public GenericStatus handleWindowState(@PathParam("windowState") Long windowState,
			@PathParam("state") String state) {

		return new GenericStatus(true);
	}

}
