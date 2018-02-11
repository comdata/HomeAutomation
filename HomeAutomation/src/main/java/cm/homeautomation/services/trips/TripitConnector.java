package cm.homeautomation.services.trips;

import org.springframework.social.tripit.connect.TripItConnectionFactory;

public class TripitConnector {
	public static void main(String[] args) {
		TripItConnectionFactory tripItConnectionFactory = new TripItConnectionFactory(
				"c75d5440599a0207e1e5398c1e548a5f76b82ae3", "b41bf1c7e804d558e71b647ac7a28ce8e424798f");

		System.out.println(tripItConnectionFactory.getProviderId());
		
		
		//tripItConnectionFactory.getOAuthOperations().fetchRequestToken(arg0, arg1)
	}
}
