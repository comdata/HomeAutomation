package cm.homeautomation.services.gasmeter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.GasIntervalData;
import cm.homeautomation.services.base.BaseService;

@Path("gas")
public class GasMeterService extends BaseService {
	@Inject
	EntityManager em;

	@Inject
	ConfigurationService configurationService;

	@GET
	@Path("readInterval")
	public List<GasIntervalData> getPowerDataForIntervals() {

		String minutes = "60";

		@SuppressWarnings("unchecked")
		List<Object[]> rawResultList = em
				.createNativeQuery("select count(*)/100 as QM, FROM_UNIXTIME(ROUND(UNIX_TIMESTAMP(TIMESTAMP)/("
						+ minutes + " * 60))*" + minutes
						+ "*60) as TIMESLICE from GASMETERPING where date(TIMESTAMP)>=date(now()- interval 7 day) GROUP BY ROUND(UNIX_TIMESTAMP(TIMESTAMP)/("
						+ minutes + " * 60));", Object[].class)
				.getResultList();

		List<GasIntervalData> results = new ArrayList<GasIntervalData>();

		for (Object[] resultElement : rawResultList) {
			GasIntervalData gasIntervalData = new GasIntervalData((BigDecimal) resultElement[0],
					(Timestamp) resultElement[1]);
			results.add(gasIntervalData);
		}

		return results;
	}

}
