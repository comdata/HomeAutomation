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
import cm.homeautomation.services.base.BaseService;

@Path("overview")
public class OverviewService extends BaseService {

	@Path("get")
	@GET
	public OverviewTiles getOverviewTiles() {
		OverviewTiles overviewTiles = new OverviewTiles();

		EntityManager em = EntityManagerService.getNewManager();

		em.getTransaction().begin();
		List<Room> results = em.createQuery("select r FROM Room r").getResultList();

		for (Room room : results) {

			OverviewTile roomTile = getOverviewTileForRoom(em, room);

			overviewTiles.getOverviewTiles().add(roomTile);

		}

		em.getTransaction().commit();

		return overviewTiles;
	}

	public OverviewTile getOverviewTileForRoom(Room room) {
		EntityManager em = EntityManagerService.getNewManager();


		OverviewTile roomTile = getOverviewTileForRoom(em, room);
		
		return roomTile;
	}

	public OverviewTile getOverviewTileForRoom(EntityManager em, Room room) {
		OverviewTile roomTile = new OverviewTile();

		String temperature = "";
		String humidity = "";
		String icon = "";

		Date sensorDate = null;

		List<Sensor> sensors = room.getSensors();
		for (Sensor sensor : sensors) {

			List latestDataList = em
					.createQuery("select sd from SensorData sd where sd.sensor=:sensor order by sd.dateTime desc")
					.setParameter("sensor", sensor).setMaxResults(1).getResultList();
			if (latestDataList != null && !latestDataList.isEmpty()) {

				Object latestData = latestDataList.get(0);

				if (latestData != null) {
					if (latestData instanceof SensorData) {
						SensorData data = (SensorData) latestData;

						if ("TEMPERATURE".equals(sensor.getSensorType())) {
							temperature = data.getValue();
							sensorDate = data.getValidThru();
						}
						if ("HUMIDITY".equals(sensor.getSensorType())) {
							humidity = data.getValue();
							sensorDate = data.getValidThru();
						}

					}
				}
			}

		}

		List switchResult = em.createQuery("select sw from Switch sw where sw.room=:room").setParameter("room", room)
				.getResultList();

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
				? (temperature.replace(",", ".") + ((humidity != null && !"".equals(humidity)) ? " / " + humidity : ""))
				: "";
		roomTile.setNumber(number);
		roomTile.setNumberUnit("°C / %");
		roomTile.setTitle(room.getRoomName());
		roomTile.setRoomName(room.getRoomName());
		roomTile.setInfo((sensorDate != null) ? sensorDate.toLocaleString() : "");
		roomTile.setInfoState("Success");
		roomTile.setRoomId(Long.toString(room.getId()));
		roomTile.setIcon(icon);
		return roomTile;
	}
}
