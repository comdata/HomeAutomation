package cm.homeautomation.services.powermeter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.PowerIntervalData;
import cm.homeautomation.sensors.powermeter.PowerMeterSensor;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;

@Path("power/")
public class PowerMeterService extends BaseService {
	@Inject
	EntityManager em;

	@Inject
	ConfigurationService configurationService;
	
	@Inject
	PowerMeterSensor powerMeterSensor;
	
	@GET
	@Path("readInterval")
	public List<PowerIntervalData> getPowerDataForIntervals() {
		
		String minutes = "60";

		List<Object[]> rawResultList = em.createNativeQuery(
				"select sum(POWERCOUNTER)/1000 as KWH, FROM_UNIXTIME(FLOOR(UNIX_TIMESTAMP(TIMESTAMP)/(" + minutes
						+ " * 60))*" + minutes
						+ "*60) as TIMESLICE from POWERMETERPING where date(TIMESTAMP)>=date(now()- interval 7 day) GROUP BY FLOOR(UNIX_TIMESTAMP(TIMESTAMP)/("
						+ minutes + " * 60));",Object[].class)
				.getResultList();

		List<PowerIntervalData> results = new ArrayList<PowerIntervalData>();

		for (Object[] resultElement : rawResultList) {
			PowerIntervalData powerIntervalData = new PowerIntervalData((BigDecimal) resultElement[0],
					(Timestamp) resultElement[1]);
			results.add(powerIntervalData);
		}


		return results;
	}

	@GET
	@Path("compress/{numberOfHours}")
	public GenericStatus compress(@PathParam("numberOfHours") String hours) {
		try {
			powerMeterSensor.compress(new String[] {hours});
		} catch (SecurityException | IllegalStateException | NotSupportedException | SystemException | RollbackException
				| HeuristicMixedException | HeuristicRollbackException e) {
			// TODO Auto-generated catch block
			return new GenericStatus(false);
		}
		
		return new GenericStatus(true);
	}
}
