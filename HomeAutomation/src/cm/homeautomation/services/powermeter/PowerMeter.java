package cm.homeautomation.services.powermeter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.EntityManager;
import javax.persistence.SqlResultSetMapping;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.services.base.BaseService;

@Path("power")
public class PowerMeter extends BaseService {

	@GET
	@Path("readInterval")
	public List<PowerIntervalData> getPowerDataForIntervals() {
		EntityManager em = EntityManagerService.getNewManager();

		List<PowerIntervalData> results = em.createNativeQuery(
				"select count(*)/1000 as KWH, FROM_UNIXTIME(ROUND(UNIX_TIMESTAMP(TIMESTAMP)/(15 * 60))*15*60) as TIMESLICE from POWERMETERPING GROUP BY ROUND(UNIX_TIMESTAMP(TIMESTAMP)/(15 * 60));",
				"PowerIntervalMapping").getResultList();
		return results;
	}

}
