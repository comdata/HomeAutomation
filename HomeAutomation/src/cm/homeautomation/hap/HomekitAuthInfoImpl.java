package cm.homeautomation.hap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.HomekitAuthInfo;
import com.beowulfe.hap.HomekitServer;

/**
 * Provides a mechanism to store authenticated homekit client details inside the
 * ESH StorageService, by implementing HomekitAuthInfo.
 *
 * @author Andy Lintner
 */
public class HomekitAuthInfoImpl implements HomekitAuthInfo {

	class StorageService {
		public Storage getStorage() {
			return new Storage("/home/hap/homekit.xml");
		}
	}

	class Storage {
		private String file;
		private Properties props;

		public Storage(String file) {
			this.file = file;
			try {
				props = new Properties();
				InputStream resourceAsStream = new FileInputStream(new File(file));
				

				if (resourceAsStream != null) {
					System.out.println("Loading properties");
					props.loadFromXML(resourceAsStream);

				} else {
					System.out.println("File empty");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void put(String key, String value) {
			props.put(key, value);
			store(props);
		}

		private void store(Properties properties) {
			try {
				File f = new File(file);
				OutputStream out = new FileOutputStream(f);
				properties.storeToXML(out, "This is an optional header comment string");
				out.flush();
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		public String get(String key) {
			if (props != null) {
				return (String) props.get(key);
			}
			return null;
		}

		public Collection<String> getKeys() {
			HashSet<String> keys = new HashSet<String>();

			Set<Object> keySet = props.keySet();

			for (Object keyObject : keySet) {

				String key = (String) keyObject;
				keys.add(key);
			}

			return keys;
		}

		public void remove(String key) {
			props.remove(key);
			store(props);
		}
	}

	private final Storage storage;
	private final String mac;
	private final BigInteger salt;
	private final byte[] privateKey;
	private final String pin;
	private Logger logger = LoggerFactory.getLogger(HomekitAuthInfoImpl.class);

	public HomekitAuthInfoImpl(String pin) throws InvalidAlgorithmParameterException {
		storage = new StorageService().getStorage();
		initializeStorage();
		this.pin = pin;
		mac = storage.get("mac");
		salt = new BigInteger(storage.get("salt"));
		privateKey = Base64.getDecoder().decode(storage.get("privateKey"));
	}

	@Override
	public void createUser(String username, byte[] publicKey) {
		storage.put(createUserKey(username), Base64.getEncoder().encodeToString(publicKey));
	}

	@Override
	public String getMac() {
		return mac;
	}

	@Override
	public String getPin() {
		return pin;
	}

	@Override
	public byte[] getPrivateKey() {
		return privateKey;
	}

	@Override
	public BigInteger getSalt() {
		return salt;
	}

	@Override
	public byte[] getUserPublicKey(String username) {
		String encodedKey = storage.get(createUserKey(username));
		if (encodedKey != null) {
			return Base64.getDecoder().decode(encodedKey);
		} else {
			return null;
		}
	}

	@Override
	public void removeUser(String username) {
		storage.remove(createUserKey(username));
	}

	@Override
	public boolean hasUser() {
		Collection<String> keys = storage.getKeys();
		boolean hasUsers = keys.stream().filter(k -> isUserKey(k)).count() > 0;
		
		System.out.println("Has users: "+hasUsers);
		
		return hasUsers;
	}

	public void clear() {
		for (String key : new HashSet<>(storage.getKeys())) {
			if (isUserKey("user_")) {
				storage.remove(key);
			}
		}
	}

	private String createUserKey(String username) {
		return "user_" + username;
	}

	private boolean isUserKey(String key) {
		return key.startsWith("user_");
	}

	private void initializeStorage() throws InvalidAlgorithmParameterException {
		if (storage.get("mac") == null) {
			logger.warn("Could not find existing MAC in " + storage.getClass().getName()
					+ ". Generating new MAC. This will require re-pairing of iOS devices.");
			storage.put("mac", HomekitServer.generateMac());
		}
		if (storage.get("salt") == null) {
			storage.put("salt", HomekitServer.generateSalt().toString());
		}
		if (storage.get("privateKey") == null) {
			storage.put("privateKey", Base64.getEncoder().encodeToString(HomekitServer.generateKey()));
		}
	}

}
