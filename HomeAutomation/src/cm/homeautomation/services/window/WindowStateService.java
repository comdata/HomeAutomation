package cm.homeautomation.services.window;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.device.DeviceService;
import cm.homeautomation.entities.WindowState;
import cm.homeautomation.sensors.window.WindowStateData;
import cm.homeautomation.services.base.BaseService;

@Path("window")
public class WindowStateService extends BaseService {
	
	@GET
	@Path("readAll")
	public List<WindowStateData> get() {
		
		List<WindowStateData> windowStateList=new ArrayList<WindowStateData>();
		
		EntityManager em = EntityManagerService.getNewManager();
		
		List<WindowState> results = em.createQuery("select ws from WindowState ws where ws.id in (select max(w.id) from WindowState w group by w.mac)").getResultList();
		
		for (WindowState windowState : results) {
			String mac = windowState.getMac();			
			
			WindowStateData windowStateData=new WindowStateData();
			
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

}
