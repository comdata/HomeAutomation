package cm.homeautomation.moquette.server;

import io.moquette.spi.security.IAuthenticator;

public class MoquetteAuthenticator implements IAuthenticator {

	@Override
	public boolean checkValid(String user, byte[] password) {
		
		return true;
	}

}
