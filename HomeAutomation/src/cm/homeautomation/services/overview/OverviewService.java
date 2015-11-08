package cm.homeautomation.services.overview;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.entities.SensorData;
import cm.homeautomation.entities.Switch;
import cm.homeautomation.entities.Weather;
import cm.homeautomation.services.base.BaseService;

@Path("overview")
public class OverviewService extends BaseService {

	@Path("get")
	@GET
	public OverviewTiles getOverviewTiles() {
		OverviewTiles overviewTiles = new OverviewTiles();

		/*
		 * String temperature =
		 * Sensors.getInstance().getSensors().get(0).getValue(); String humidity
		 * = Sensors.getInstance().getSensors().get(1).getValue();
		 */
		EntityManager em = EntityManagerService.getNewManager();

		List results = em.createQuery("select r FROM Room r").getResultList();

		for (Object result : results) {
			if (result instanceof Room) {
				OverviewTile roomTile = new OverviewTile();
				Room room = (Room) result;
				String temperature = "";
				String humidity = "";
				String icon = "";

				Date sensorDate=null;
				
				List<Sensor> sensors = room.getSensors();
				for (Sensor sensor : sensors) {

					List latestDataList = em
							.createQuery(
									"select sd from SensorData sd where sd.sensor=:sensor order by sd.dateTime desc")
							.setParameter("sensor", sensor).setMaxResults(1).getResultList();
					if (latestDataList != null && !latestDataList.isEmpty()) {

						Object latestData = latestDataList.get(0);

						if (latestData != null) {
							if (latestData instanceof SensorData) {
								SensorData data = (SensorData) latestData;
								
								

								if ("TEMPERATURE".equals(sensor.getSensorType())) {
									temperature = data.getValue();
									sensorDate=data.getValidThru();
								}
								if ("HUMIDITY".equals(sensor.getSensorType())) {
									humidity = data.getValue();
									sensorDate=data.getValidThru();
								}

							}
						}
					}

				}

				List switchResult = em.createQuery("select sw from Switch sw where sw.room=:room")
						.setParameter("room", room).getResultList();

				for (Object singleSwitchAnon : switchResult) {
					if (singleSwitchAnon instanceof Switch) {
						Switch singleSwitch = (Switch) singleSwitchAnon;

						if ("LIGHT".equals(singleSwitch.getSwitchType())) {
							if ("ON".equals(singleSwitch.getLatestStatus())) {
								icon = "sap-icon://lightbulb";
							}

						}
						
						if ("SPEAKER".equals(singleSwitch.getSwitchType())) {
							if ("ON".equals(singleSwitch.getLatestStatus())) {
								icon = "sap-icon://marketing-campaign";
							}

						}
					}

				}
				String number = (temperature != null && !"".equals(temperature))
						? (temperature + ((humidity != null && !"".equals(humidity)) ? " / " + humidity : "")) : "";
				roomTile.setNumber(number);
				roomTile.setNumberUnit("°C / %");
				roomTile.setTitle(room.getRoomName());
				roomTile.setRoomName(room.getRoomName());
				roomTile.setInfo((sensorDate!=null) ? sensorDate.toLocaleString(): "");
				roomTile.setInfoState("Success");
				roomTile.setRoomId(Long.toString(room.getId()));
				roomTile.setIcon(icon);

				overviewTiles.getOverviewTiles().add(roomTile);

			}

		}

		List wResults = em.createQuery("select w from Weather w").getResultList();

		Weather weather = null;

		if (!wResults.isEmpty()) {
			Object singleResult = wResults.get(0);
			if (singleResult instanceof Weather) {
				weather = (Weather) singleResult;
				OverviewTile weatherTile = new OverviewTile();

				String humidity = weather.getHumidity();
				humidity = (humidity != null) ? " / " + humidity : "";
				weatherTile.setNumber(weather.getTempC() + humidity);
				weatherTile.setNumberUnit("°C");
				weatherTile.setTitle("Wetter");
				weatherTile.setInfo(weather.getFetchDate().toLocaleString());
				overviewTiles.getOverviewTiles().add(weatherTile);
			}
		}

		return overviewTiles;
	}
}
