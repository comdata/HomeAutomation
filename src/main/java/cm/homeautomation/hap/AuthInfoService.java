package cm.homeautomation.hap;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.codec.binary.Base64;

import com.beowulfe.hap.HomekitAuthInfo;
import com.beowulfe.hap.HomekitServer;

/**
 * This is a simple implementation that should never be used in actual
 * production. The mac, salt, and privateKey are being regenerated every time
 * the application is started. The user store is also not persisted. This means
 * pairing needs to be re-done every time the app restarts.
 *
 * @author Andy Lintner
 */
public class AuthInfoService implements HomekitAuthInfo {

	private static String PIN = "032-45-154";

	private static String mac;
	private static BigInteger salt;
	private static byte[] privateKey;
	private static ConcurrentMap<String, byte[]> userKeyMap = new ConcurrentHashMap<>();

	public AuthInfoService() throws InvalidAlgorithmParameterException {

		Properties props = new Properties();
		try {
			InputStream resourceAsStream = new FileInputStream(new File("/home/hap/" + "server.properties"));

			if (resourceAsStream != null) {
				props.load(resourceAsStream);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (props.getProperty("mac") != null) {
			System.out.println("using existing config");
			mac = props.getProperty("mac");
			salt = new BigInteger(Base64.decodeBase64((String) props.get("salt")));

			privateKey = readKey("privateKey");

			System.out.println(new String(privateKey));
			System.out.println(mac);
			System.out.println(salt);

			loadUsers();

		} else {
			System.out.println("generating new config");
			mac = "04:a1:51:69:94:55";// HomekitServer.generateMac();
			salt = HomekitServer.generateSalt();
			privateKey = HomekitServer.generateKey();
			System.out.println("SALT: " + salt);
			System.out.println("KEY: " + new String(privateKey));
			saveParamChanges(mac, salt, privateKey);

			saveKey("privateKey", privateKey);
		}

		System.out.println(
				"Auth info is generated each time the sample application is started. Pairings are not persisted.");
		System.out.println("The PIN for pairing is " + PIN);
	}

	public byte[] readKey(String string) {
		try {
			File f = new File("/home/hap/" + string);
			FileInputStream fis = new FileInputStream(f);
			DataInputStream dis = new DataInputStream(fis);
			byte[] keyBytes = new byte[(int) f.length()];
			dis.readFully(keyBytes);
			dis.close();

			return keyBytes;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void saveKey(String name, byte[] privateKey2) {
		// TODO Auto-generated method stub
		DataOutputStream keyfos;
		try {
			keyfos = new DataOutputStream(new FileOutputStream("/home/hap/" + name, false));

			keyfos.write(privateKey2);
			keyfos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void loadUsers() {

		try {
			InputStream resourceAsStream = new FileInputStream(new File("/home/hap/" + "usermap.xml"));
			Properties props = new Properties();

			if (resourceAsStream != null) {
				System.out.println("Loading user map");
				props.loadFromXML(resourceAsStream);

				Set<Object> keySet = props.keySet();

				for (Object keyObject : keySet) {

					String key = (String) keyObject;

					byte[] publicKey = readKey(key);
					if (publicKey != null) {
						System.out.println("Loading " + key + " " + new String(publicKey));
						//userKeyMap.putIfAbsent(key, publicKey);
					}
				}
			} else {
				System.out.println("File empty");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveParamChanges(String mac, BigInteger salt, byte[] privateKey) {
		try {
			Properties props = new Properties();
			props.setProperty("mac", mac);
			props.setProperty("salt", "" + new String(Base64.encodeBase64(salt.toByteArray())));

			File f = new File("/home/hap/" + "server.properties");
			OutputStream out = new FileOutputStream(f);
			props.store(out, "This is an optional header comment string");
			System.out.println("SALT: " + new BigInteger(Base64.decodeBase64((String) props.get("salt"))));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveUserMap() {
		try {
			Properties props = new Properties();

			Set<String> keySet = userKeyMap.keySet();
			for (String key : keySet) {
				props.setProperty(key, key);

				saveKey(key, userKeyMap.get(key));
			}

			File f = new File("/home/hap/" + "usermap.xml");
			OutputStream out = new FileOutputStream(f);
			props.storeToXML(out, "This is an optional header comment string");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public String getPin() {
		return PIN;
	}

	@Override
	public String getMac() {
		return mac;
	}

	@Override
	public BigInteger getSalt() {
		return salt;
	}

	@Override
	public byte[] getPrivateKey() {
		return privateKey;
	}

	@Override
	public void createUser(String username, byte[] publicKey) {

		if (userKeyMap.containsKey(username)) {
			System.out.println("Performing key update");
			userKeyMap.replace(username, publicKey);
		} else {
			System.out.println("Adding initial key");
			userKeyMap.putIfAbsent(username, publicKey);
		}

		saveUserMap();
		System.out.println("Added pairing for " + username + " " + new String(publicKey));
	}

	@Override
	public void removeUser(String username) {
		userKeyMap.remove(username);
		saveUserMap();
		System.out.println("Removed pairing for " + username);
	}

	@Override
	public byte[] getUserPublicKey(String username) {
		System.out.println();
		byte[] bs = userKeyMap.get(username);
		System.out.println("Requesting User:" + username + " " + new String(bs));

		return bs;
	}
	
	@Override
	public boolean hasUser() {
		
		return false; // userKeyMap.size()>0;
	}

}