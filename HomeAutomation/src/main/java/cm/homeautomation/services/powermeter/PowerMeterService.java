package cm.homeautomation.services.powermeter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.EntityManager;
import javax.persistence.SqlResultSetMapping;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.PowerIntervalData;
import cm.homeautomation.sensors.powermeter.PowerMeterSensor;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;

@Path("power")
public class PowerMeterService extends BaseService {

	@GET
	@Path("readInterval")
	public List<PowerIntervalData> getPowerDataForIntervals() {
		EntityManager em = EntityManagerService.getNewManager();
		String minutes = "60";

		List<Object[]> rawResultList = em.createNativeQuery(
				"select sum(POWERCOUNTER)/1000 as KWH, FROM_UNIXTIME(FLOOR(UNIX_TIMESTAMP(TIMESTAMP)/(" + minutes
						+ " * 60))*" + minutes
						+ "*60) as TIMESLICE from POWERMETERPING where date(TIMESTAMP)>=date(now()- interval 7 day) GROUP BY FLOOR(UNIX_TIMESTAMP(TIMESTAMP)/("
						+ minutes + " * 60));")
				.getResultList();

		List<PowerIntervalData> results = new ArrayList<PowerIntervalData>();

		for (Object[] resultElement : rawResultList) {
			PowerIntervalData powerIntervalData = new PowerIntervalData((BigDecimal) resultElement[0],
					(Timestamp) resultElement[1]);
			results.add(powerIntervalData);
		}
		em.close();

		return results;
	}

	@GET
	@Path("compress/{numberOfHours}")
	public GenericStatus compress(@PathParam("numberOfHours") String hours) {
		PowerMeterSensor.compress(new String[] {hours});
		
		return new GenericStatus(true);
	}
}
