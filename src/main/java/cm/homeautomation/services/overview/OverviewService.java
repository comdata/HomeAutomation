package cm.homeautomation.services.overview;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.entities.SensorData;
import cm.homeautomation.entities.Switch;
import cm.homeautomation.services.base.AutoCreateInstance;
import cm.homeautomation.services.base.BaseService;

@AutoCreateInstance
@Path("overview")
public class OverviewService extends BaseService {

	private static OverviewTiles overviewTiles;

	public OverviewService() {
		super();
		init();
	}

	private static void decorateRoomTile(OverviewTile roomTile) {
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

					if ("TEMPERATURE".equals(sensor.getSensorType()) && data.getValue() != null && Float.parseFloat(data.getValue().replace(',', '.')) <= 50) {
						temperature = data.getValue();
						sensorDate = data.getValidThru();
					}
					if ("HUMIDITY".equals(sensor.getSensorType()) && data.getValue() != null && Float.parseFloat(data.getValue().replace(',', '.')) <= 100) {
						humidity = data.getValue();
						sensorDate = data.getValidThru();
					}
				}
			}
		}

		icon = getIconForRoomTile(roomTile);

		addDetailsToRoomTile(roomTile, temperature, humidity, sensorDate, icon);

	}

	private static void addDetailsToRoomTile(OverviewTile roomTile, String temperature, String humidity, Date sensorDate,
			String icon) {
		String humidityString = ((humidity != null) && !"".equals(humidity)) ? " / " + humidity.replace(",", ".") : "";
		final String number = ((temperature != null) && !"".equals(temperature))
				? (temperature.replace(",", ".")
						+ humidityString)
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

	private static String getIconForRoomTile(OverviewTile roomTile) {
		String icon="";
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

	private static OverviewTile getOverviewTileForRoom(EntityManager em, Room room) {
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
		return overviewTiles;
	}

	private static void init() {

		if (overviewTiles == null) {
			overviewTiles = new OverviewTiles();

			final EntityManager em = EntityManagerService.getNewManager();

			em.getTransaction().begin();
			final List<Room> results = em
					.createQuery("select r FROM Room r where r.visible=true order by r.sortOrder", Room.class)
					.getResultList();

			for (final Room room : results) {

				final OverviewTile roomTile = getOverviewTileForRoom(em, room);

				overviewTiles.getOverviewTiles().add(roomTile);

			}

			em.getTransaction().commit();
			em.close();
		}
	}

	public OverviewTile updateOverviewTile(SensorData sensorData) {
		final OverviewTile tileForRoom = overviewTiles
				.getTileForRoom(sensorData.getSensor().getRoom().getId().toString());

		tileForRoom.getSensorData().put(sensorData.getSensor(), sensorData);

		decorateRoomTile(tileForRoom);

		return tileForRoom;
	}
}
