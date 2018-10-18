package cm.homeautomation.services.packagetracking;

import java.util.List;

import cm.homeautomation.entities.Package;

public class PackageTrackingTest {

	public static void main(String[] args) {

		PackageTracking packageTracking = new PackageTracking();
		List<Package> allOpen = packageTracking.getAllOpen();
		
		System.out.println("size of all open:"+allOpen.size());
	}

}
