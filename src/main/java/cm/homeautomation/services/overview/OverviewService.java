package cm.homeautomation.services.overview;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.entities.SensorData;
import cm.homeautomation.entities.Switch;
import cm.homeautomation.services.base.BaseService;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;

@Startup
@ApplicationScoped
@Path("overview/")
public class OverviewService extends BaseService {

	OverviewTiles overviewTiles;

	@Inject
	EntityManager em;

	public OverviewService() {
		super();
	}

	private void decorateRoomTile(OverviewTile roomTile) {
		final Map<Sensor, SensorData> sensorData = roomTile.getSensorData();
		String temperature = null;
		String humidity = null;
		Date sensorDate = null;
		String icon = null;

		if (sensorData != null) {
			final Set<Sensor> sensorKeys = sensorData.keySet();

			for (final Sensor sensor : sensorKeys) {
				if (sensor.isShowData()) {
					final SensorData data = sensorData.get(sensor);

					String value = data.getValue();

					if (value != null && value.contains(" ")) {
						value = value.split(" ")[0];
					}

					if ("TEMPERATURE".equals(sensor.getSensorType()) && value != null
							&& Float.parseFloat(value.replace(',', '.')) <= 50) {
						temperature = value;
						sensorDate = data.getValidThru();
					}
					if ("HUMIDITY".equals(sensor.getSensorType()) && value != null
							&& Float.parseFloat(value.replace(',', '.')) <= 100) {
						humidity = value;
						sensorDate = data.getValidThru();
					}
				}
			}
		}

		icon = getIconForRoomTile(roomTile);

		addDetailsToRoomTile(roomTile, temperature, humidity, sensorDate, icon);

	}

	private void addDetailsToRoomTile(OverviewTile roomTile, String temperature, String humidity, Date sensorDate,
			String icon) {
		String humidityString = ((humidity != null) && !"".equals(humidity)) ? " / " + humidity.replace(",", ".") : "";
		final String number = ((temperature != null) && !"".equals(temperature))
				? (temperature.replace(",", ".") + humidityString)
				: "";
		roomTile.setNumber(number);
		roomTile.setNumberUnit("°C " + (number.contains("/") ? "/ %" : ""));
		roomTile.setTitle(roomTile.getRoom().getRoomName());
		roomTile.setRoomName(roomTile.getRoom().getRoomName());
		roomTile.setInfo((sensorDate != null) ? DateFormat.getDateInstance().format(sensorDate) : "");
		roomTile.setInfoState("Success");
		roomTile.setRoomId(Long.toString(roomTile.getRoom().getId()));
		roomTile.setIcon(icon);
		roomTile.setTileType("room");
	}

	private String getIconForRoomTile(OverviewTile roomTile) {
		String icon = "";
		final Set<Switch> switches = roomTile.getSwitches();

		for (final Switch singleSwitch : switches) {
			if ("LIGHT".equals(singleSwitch.getSwitchType()) && "ON".equals(singleSwitch.getLatestStatus())) {
				icon = "sap-icon://lightbulb";
			}

			if ("SPEAKER".equals(singleSwitch.getSwitchType()) && "ON".equals(singleSwitch.getLatestStatus())) {
				icon = "sap-icon://marketing-campaign";
			}
		}
		return icon;
	}

	private OverviewTile getOverviewTileForRoom(EntityManager em, Room room) {
		final OverviewTile roomTile = new OverviewTile();
		roomTile.setRoom(room);

		final List<Sensor> sensors = roomTile.getRoom().getSensors();

		if (sensors != null) {
			for (final Sensor sensor : sensors) {
				if (sensor.isShowData()) {

					final List<SensorData> latestDataList = em.createQuery(
							"select sd from SensorData sd where sd.sensor=:sensor order by sd.dateTime desc",
							SensorData.class).setParameter("sensor", sensor).setMaxResults(1).getResultList();
					if ((latestDataList != null) && !latestDataList.isEmpty()) {

						final SensorData latestData = latestDataList.get(0);
						roomTile.getSensorData().put(sensor, latestData);
					}
				}

			}
		}

		final List<Switch> switchResult = em.createQuery("select sw from Switch sw where sw.room=:room", Switch.class)
				.setParameter("room", room).getResultList();

		if (switchResult != null) {
			for (final Switch singleSwitch : switchResult) {
				roomTile.getSwitches().add(singleSwitch);

			}
		}

		decorateRoomTile(roomTile);

		return roomTile;
	}

	@Path("get")
	@GET
	public OverviewTiles getOverviewTiles() {

		if (overviewTiles == null) {
			init();
		}

		return overviewTiles;
	}

	@Scheduled(every = "60s")
	public void init() {

		OverviewTiles overviewTilesTemp = new OverviewTiles();

		final List<Room> results = em
				.createQuery("select r FROM Room r where r.visible=true order by r.sortOrder", Room.class)
				.getResultList();

		for (final Room room : results) {

			final OverviewTile roomTile = getOverviewTileForRoom(em, room);

			overviewTilesTemp.getOverviewTiles().add(roomTile);

		}

		overviewTiles = overviewTilesTemp;
	}

	public OverviewTile updateOverviewTile(SensorData sensorData) {
		init();
		Room room = sensorData.getSensor().getRoom();

		if (room != null) {
			final OverviewTile tileForRoom = overviewTiles.getTileForRoom(room.getId().toString());

			if (tileForRoom != null) {

				if (sensorData != null) {
					tileForRoom.getSensorData().put(sensorData.getSensor(), sensorData);
				}
				decorateRoomTile(tileForRoom);

			}
			return tileForRoom;
		} else {
			return null;
		}
	}
}
