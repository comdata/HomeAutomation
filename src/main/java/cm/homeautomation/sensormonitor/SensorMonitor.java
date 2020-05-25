package cm.homeautomation.sensormonitor;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import cm.homeautomation.db.EntityManagerService;

public class SensorMonitor {

	public static void checkSensors() {
		
		EntityManager em = EntityManagerService.getManager();
		
		String qlString = "select (select max(sd.validThru) from SensorData sd where sd.sensor=s) from Sensor s";
		System.out.println("SQL: "+qlString);
		List<Date> resultList = em.createQuery(qlString).getResultList();
		
		for (Date date : resultList) {
			if (date!=null) { 
			System.out.println("Date: "+date.toLocaleString());
			} else {
				System.out.println("Date is null");
			}
		}

	}
	
	public static void main(String[] args) {
		SensorMonitor.checkSensors();
	}
}
