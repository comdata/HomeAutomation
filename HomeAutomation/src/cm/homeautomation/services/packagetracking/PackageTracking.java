package cm.homeautomation.services.packagetracking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Package;
import cm.homeautomation.entities.PackageHistory;
import cm.homeautomation.entities.PackageHistoryPK;
import cm.homeautomation.entities.PackagePK;

@Path("packages")
public class PackageTracking {
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36";
	private static final String acceptHeader = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8";
	private static final Map<String, String> carrierMap = createCarrierMap();

	private static Map<String, String> createCarrierMap() {
		Map<String, String> carrierMap = new HashMap<String, String>();
		carrierMap.put("ex007", "007EX");
		carrierMap.put("ten13", "13ten");
		carrierMap.put("post17", "17PostService");
		carrierMap.put("px", "4PX");
		carrierMap.put("a1", "A-1 Courier");
		carrierMap.put("abf", "ABF Freight");
		carrierMap.put("acs", "ACS Courier");
		carrierMap.put("aliex", "AliExpress Standard Shipping");
		carrierMap.put("allied", "Allied Express");
		carrierMap.put("anpost", "Anpost");
		carrierMap.put("aramex", "Aramex");
		carrierMap.put("armen", "Armenia Post (Haypost)");
		carrierMap.put("apc", "APC overnight");
		carrierMap.put("apcpli", "APC-PLI");
		carrierMap.put("asendia", "Asendia USA");
		carrierMap.put("asmred", "ASM");
		carrierMap.put("at", "Austrian Post");
		carrierMap.put("au", "Australia Post");
		carrierMap.put("azer", "Azerpost");
		carrierMap.put("bartol", "Bartolini");
		carrierMap.put("blp", "Belpost");
		carrierMap.put("bluedart", "Blue Dart");
		carrierMap.put("bpost", "bpost");
		carrierMap.put("bring", "Bring");
		carrierMap.put("boxb", "Boxberry");
		carrierMap.put("bolg", "Bulgarian Post");
		carrierMap.put("cp", "Canada Post");
		carrierMap.put("canpar", "Canpar");
		carrierMap.put("celeritas", "Celeritas");
		carrierMap.put("ceska", "Česká pošta");
		carrierMap.put("ceva", "Ceva Logistics");
		carrierMap.put("chilex", "Chilexpress");
		carrierMap.put("cems", "China Post EMS");
		carrierMap.put("china", "China Post Registered");
		carrierMap.put("chinasm", "China Post Small Packet");
		carrierMap.put("chrono", "Chronopost");
		carrierMap.put("cityex", "City Express");
		carrierMap.put("adrexo", "Colis Prive - Adrexo");
		carrierMap.put("colp", "Collect+");
		carrierMap.put("colomb", "Colombia post (4-72)");
		carrierMap.put("cope", "COPE");
		carrierMap.put("cor", "Correos");
		carrierMap.put("chrexp", "Correos Express (Chrono Express)");
		carrierMap.put("corarg", "Correo Argentino");
		carrierMap.put("corurg", "Correo Uruguayo");
		carrierMap.put("chile", "Correos de Chile");
		carrierMap.put("corm", "Correos de Mexico");
		carrierMap.put("corbra", "Correios");
		carrierMap.put("coup", "Courier Post");
		carrierMap.put("couple", "Couriers Please");
		carrierMap.put("cse", "CSE");
		carrierMap.put("cneexp", "CNE Express");
		carrierMap.put("cnzexp", "CNZExpress");
		carrierMap.put("ctt", "CTT");
		carrierMap.put("cypr", "Cyprus Post");
		carrierMap.put("dk", "Danmark Post");
		carrierMap.put("schen", "DB Schenker Sweden");
		carrierMap.put("sch", "DB Schenker");
		carrierMap.put("dellin", "Delovie Linii");
		carrierMap.put("delmas", "Delmas");
		carrierMap.put("dp", "Deutsche Post");
		carrierMap.put("dhl", "DHL Express");
		carrierMap.put("dhlgm", "DHL Global Mail");
		carrierMap.put("dhlgf", "DHL Global Forwarding");
		carrierMap.put("dhlnlpcode", "DHL Netherlands");
		carrierMap.put("dhlpoland", "DHL Poland");
		carrierMap.put("dhlserv", "DHL Servicepoint");
		carrierMap.put("dicom", "Dicom Express");
		carrierMap.put("directf", "Direct Freight Express");
		carrierMap.put("direct", "Direct Link");
		carrierMap.put("dimex", "Dimex");
		carrierMap.put("directl", "Directlog");
		carrierMap.put("dtdc", "DTDC India");
		carrierMap.put("dpd", "DPD");
		carrierMap.put("exa", "DPD France - Exapaq");
		carrierMap.put("dpdie", "DPD Ireland");
		carrierMap.put("dpdpoland", "DPD Poland");
		carrierMap.put("dpdrus", "DPD Russia");
		carrierMap.put("dpduk", "DPD UK");
		carrierMap.put("dpexw", "DPEX Worldwide");
		carrierMap.put("dsv", "DSV");
		carrierMap.put("dynalogic", "Dynalogic");
		carrierMap.put("dynamex", "Dynamex");
		carrierMap.put("ecfirst", "EC-Firstclass");
		carrierMap.put("econt", "Econt Express");
		carrierMap.put("edos", "edostavka");
		carrierMap.put("ee", "Eesti Post");
		carrierMap.put("ego", "E-Go");
		carrierMap.put("elta", "Elta");
		carrierMap.put("eltac", "Elta Courier");
		carrierMap.put("ems", "EMS Russian Post");
		carrierMap.put("emirates", "Emirates Post");
		carrierMap.put("emsfalcon", "EMS Falcon");
		carrierMap.put("emps", "EMPS");
		carrierMap.put("ensend", "Ensenda");
		carrierMap.put("envia", "envialia");
		carrierMap.put("estafe", "Estafeta");
		carrierMap.put("eshopw", "eShopWorld");
		carrierMap.put("espeed", "Espeedpost");
		carrierMap.put("ets", "ETS Express");
		carrierMap.put("expru", "Express.ru");
		carrierMap.put("fastau", "Fastway AU");
		carrierMap.put("fastie", "Fastway Ireland");
		carrierMap.put("fastnz", "Fastway NZ");
		carrierMap.put("fedex", "FedEx");
		carrierMap.put("fedpl", "FedEx Poland");
		carrierMap.put("feduk", "FedEx UK");
		carrierMap.put("fiji", "Fiji Post");
		carrierMap.put("flyt", "Flyt Express");
		carrierMap.put("garpos", "Garantpost");
		carrierMap.put("geis", "Geis");
		carrierMap.put("geodis", "Geodis");
		carrierMap.put("geolog", "Geologistics");
		carrierMap.put("gover", "General-Overnight");
		carrierMap.put("gdex", "GDEX Malaysia");
		carrierMap.put("globg", "Globegistics");
		carrierMap.put("gls", "GLS");
		carrierMap.put("glsit", "GLS Italy");
		carrierMap.put("glsnl", "GLS Netherlands");
		carrierMap.put("gso", "GSO");
		carrierMap.put("greyh", "Greyhound Courier Express");
		carrierMap.put("halc", "Halcourier");
		carrierMap.put("hdnl", "HDNL");
		carrierMap.put("hermes", "Hermes");
		carrierMap.put("hermesit", "Hermes Italy");
		carrierMap.put("hk", "HongKong Post");
		carrierMap.put("hr", "Hrvatska pošta");
		carrierMap.put("hunter", "Hunter Express");
		carrierMap.put("in", "India Post");
		carrierMap.put("indon", "Indonesia Post");
		carrierMap.put("intelc", "Intelcom");
		carrierMap.put("il", "Israel Post");
		carrierMap.put("iloxx", "iloxx");
		carrierMap.put("inter", "Interlink");
		carrierMap.put("interp", "Interparcel");
		carrierMap.put("intime", "In-time");
		carrierMap.put("inpost", "InPost Paczkomaty");
		carrierMap.put("ipar", "i-parcel");
		carrierMap.put("jp", "Japan Post");
		carrierMap.put("jde", "JDE");
		carrierMap.put("kex", "K-EX");
		carrierMap.put("keavo", "Keavo");
		carrierMap.put("kerry", "Kerry Express");
		carrierMap.put("kiala", "Kiala");
		carrierMap.put("kor", "Korea Post");
		carrierMap.put("kz", "Kazpost");
		carrierMap.put("landmark", "Landmark Global");
		carrierMap.put("laser", "Lasership");
		carrierMap.put("lp", "La Poste");
		carrierMap.put("laos", "Laos Post");
		carrierMap.put("lv", "Latvijas Post");
		carrierMap.put("liban", "LibanPost");
		carrierMap.put("litva", "Lietuvos paštas");
		carrierMap.put("logibox", "Logibox");
		carrierMap.put("loom", "Loomis Express");
		carrierMap.put("lso", "Lone Star Overnight");
		carrierMap.put("hung", "Magyar Posta");
		carrierMap.put("malta", "MaltaPost");
		carrierMap.put("major", "Major Express");
		carrierMap.put("malpos", "Malaysia Post");
		carrierMap.put("matka", "Matkahuolto");
		carrierMap.put("meest", "Meest");
		carrierMap.put("meestg", "Meest Group");
		carrierMap.put("relay", "Mondial Relay");
		carrierMap.put("moldov", "Moldova Post");
		carrierMap.put("mrw", "MRW");
		carrierMap.put("mscgva", "MSC");
		carrierMap.put("msiw", "MSI Worldwide Mail");
		carrierMap.put("mty", "MTY");
		carrierMap.put("myher", "Hermes (UK)");
		carrierMap.put("nacex", "NACEX");
		carrierMap.put("newp", "NewPost");
		carrierMap.put("nexive", "Nexive");
		carrierMap.put("nightline", "Nightline");
		carrierMap.put("nor", "Norway Post");
		carrierMap.put("nzp", "New Zealand Post");
		carrierMap.put("norco", "Norco Delivery Services");
		carrierMap.put("oca", "OCA Argentina");
		carrierMap.put("ows", "One World Express");
		carrierMap.put("ont", "Ontrac");
		carrierMap.put("p2g", "P2G");
		carrierMap.put("p4d", "P4D");
		carrierMap.put("pk", "Pakistan Post");
		carrierMap.put("parcelpnt", "ParcelPoint");
		carrierMap.put("ppool", "ParcelPool");
		carrierMap.put("paquet", "Paquetexpress");
		carrierMap.put("pbi", "PBI - Pitney Bowes");
		carrierMap.put("pec", "PEC");
		carrierMap.put("phlpost", "PhilPost");
		carrierMap.put("poland", "Poczta Polska");
		carrierMap.put("polar", "Polar Express");
		carrierMap.put("pony", "Pony Express");
		carrierMap.put("roman", "Poşta Română");
		carrierMap.put("it", "Poste Italiane");
		carrierMap.put("items", "Poste Italiane EMS");
		carrierMap.put("pclub", "PostclubUSA");
		carrierMap.put("posti", "Posti Finland - Itella");
		carrierMap.put("postiec", "Posti Finland Economy");
		carrierMap.put("postnord", "Postnord Logistics");
		carrierMap.put("pres", "Prestige");
		carrierMap.put("prfc", "Parcelforce");
		carrierMap.put("ppl", "PPL");
		carrierMap.put("slv", "Pošta Slovenije");
		carrierMap.put("ptl", "Post Luxembourg");
		carrierMap.put("posthas", "Post Haste");
		carrierMap.put("puro", "Purolator");
		carrierMap.put("puropost", "Puropost");
		carrierMap.put("qxpress", "Qxpress");
		carrierMap.put("qwintry", "Qwintry Air");
		carrierMap.put("ratek", "Ratek");
		carrierMap.put("redpack", "Redpack");
		carrierMap.put("redyser", "Redyser");
		carrierMap.put("rm", "Royal Mail");
		carrierMap.put("rp", "Russian Post");
		carrierMap.put("ruston", "Ruston");
		carrierMap.put("safmar", "Safmarine");
		carrierMap.put("sagawa", "Sagawa Express");
		carrierMap.put("sailpost", "Sailpost");
		carrierMap.put("safr", "SAPO");
		carrierMap.put("saudi", "Saudi Post");
		carrierMap.put("scs", "SCS Express");
		carrierMap.put("sda", "SDA");
		carrierMap.put("seabour", "Seabourne Logistics");
		carrierMap.put("selektv", "Selektvracht");
		carrierMap.put("serpost", "Serpost");
		carrierMap.put("sfc", "SendFromChina");
		carrierMap.put("serbia", "Serbia Post");
		carrierMap.put("seur", "Seur");
		carrierMap.put("seych", "Seychelles Post");
		carrierMap.put("sf", "SF Express");
		carrierMap.put("sing", "Singapore Post");
		carrierMap.put("siodem", "Siódemka");
		carrierMap.put("shiptor", "Shiptor");
		carrierMap.put("sky56", "Sky56");
		carrierMap.put("skynetm", "Skynet Malaysia");
		carrierMap.put("skynetw", "SkyNet Worldwide Express");
		carrierMap.put("slovak", "Slovenská pošta");
		carrierMap.put("soget", "Sogetras");
		carrierMap.put("smsa", "SMSA Express");
		carrierMap.put("stadt", "Stadtbote");
		carrierMap.put("star", "StarTrack Express");
		carrierMap.put("spsr", "SPSR");
		carrierMap.put("se", "Swedish Post");
		carrierMap.put("swiss", "Swiss Post");
		carrierMap.put("tw", "Taiwan Post");
		carrierMap.put("teapost", "TEA Post");
		carrierMap.put("thai", "Thailand Post");
		carrierMap.put("tipsac", "Tipsa");
		carrierMap.put("transm", "TransMission");
		carrierMap.put("tkkit", "TK KIT");
		carrierMap.put("toll", "Toll");
		carrierMap.put("tourline", "Tourline Express");
		carrierMap.put("tnt", "TNT");
		carrierMap.put("tntau", "TNT Australia");
		carrierMap.put("tntfr", "TNT France");
		carrierMap.put("tntuk", "TNT UK");
		carrierMap.put("tntit", "TNT Italy");
		carrierMap.put("trpack", "TrakPak");
		carrierMap.put("trans", "trans-o-flex");
		carrierMap.put("tntp", "Post NL");
		carrierMap.put("tntpit", "Post NL (International)");
		carrierMap.put("turk", "PTT");
		carrierMap.put("udsa", "UDSA");
		carrierMap.put("ukm", "UK Mail");
		carrierMap.put("ukr", "Ukrpost");
		carrierMap.put("ukrems", "Ukraine Post EMS");
		carrierMap.put("ups", "UPS");
		carrierMap.put("upsmi", "UPS Mail Innovations");
		carrierMap.put("usps", "USPS");
		carrierMap.put("uzbek", "Uzbekistan Post");
		carrierMap.put("vanuatu", "Vanuatu Post");
		carrierMap.put("vietnam", "Vietnam Post");
		carrierMap.put("vp", "VPost");
		carrierMap.put("wedo", "Wedo Express");
		carrierMap.put("wpost", "Westpost");
		carrierMap.put("winit", "Winit");
		carrierMap.put("wish", "Wish Post");
		carrierMap.put("wnd", "wnDirect");
		carrierMap.put("wscn", "World Shipping Post");
		carrierMap.put("worldnet", "WorldNet Express");
		carrierMap.put("xfwul", "XF Wuliu Logistics");
		carrierMap.put("xru", "XRU");
		carrierMap.put("yamato", "Yamato");
		carrierMap.put("yanwen", "Yanwen");
		carrierMap.put("yodel", "Yodel");
		carrierMap.put("yun", "Yun Express");
		carrierMap.put("zel", "Zeleris");
		carrierMap.put("zhy", "Zhy Express");
		carrierMap.put("zto", "ZTO Express");
		return carrierMap;
	}

	public static void main(String[] args) throws KeyManagementException, ClientProtocolException,
			NoSuchAlgorithmException, KeyStoreException, IOException {
		updateTrackingInformation(args);
	}

	private static Date parseDate(List<SimpleDateFormat> knownPatterns, String candidate) {
		for (SimpleDateFormat pattern : knownPatterns) {
			try {
				// Take a try
				return new Date(pattern.parse(candidate).getTime());

			} catch (ParseException pe) {
				// Loop on
			}
		}
		return null;
	}

	public static void updateTrackingInformation(String[] args) throws ClientProtocolException, IOException,
			NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		SSLContext sslContext = SSLContext.getInstance("SSL");

		// set up a TrustManager that trusts everything
		sslContext.init(null, new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				System.out.println("getAcceptedIssuers =============");
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
				System.out.println("checkClientTrusted =============");
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
				System.out.println("checkServerTrusted =============");
			}
		} }, new SecureRandom());

		SSLSocketFactory sf = new SSLSocketFactory(sslContext);
		Scheme httpsScheme = new Scheme("https", 443, sf);
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(httpsScheme);

		// apache HttpClient version >4.2 should use
		// BasicClientConnectionManager
		ClientConnectionManager cm = new SingleClientConnManager(schemeRegistry);
		HttpClient client = new DefaultHttpClient(cm);
		String url = "https://data.parcelapp.net/data.php?caller=yes&version=4";

		String cookie = args[0];
		// System.out.println(cookie);

		HttpGet request = new HttpGet(url);

		// add request header
		request.addHeader("User-Agent", USER_AGENT);
		request.addHeader("Accept", acceptHeader);
		request.addHeader("Cookie", cookie);
		HttpResponse response = client.execute(request);

		System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
			// System.out.println(line);
		}

		String resultString = result.toString();

		resultString = resultString.substring(1);
		resultString = resultString.substring(0, resultString.length() - 1);
		resultString = "{\"packages\":" + resultString + "}";

		// System.out.println(resultString);

		List<SimpleDateFormat> knownPatterns = new ArrayList<SimpleDateFormat>();
		knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd HH:mm"));
		knownPatterns.add(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"));
		knownPatterns.add(new SimpleDateFormat("dd.MM.yyyy HH:mm"));

		JsonFactory factory = new JsonFactory();
		ObjectMapper om = new ObjectMapper(factory).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		JsonNode rootNode = om.readTree(resultString);

		Iterator<Map.Entry<String, JsonNode>> fieldsIterator = rootNode.fields();
		while (fieldsIterator.hasNext()) {

			Map.Entry<String, JsonNode> field = fieldsIterator.next();
			// System.out.println("Key: " + field.getKey() + "\tValue:" +
			// field.getValue());

			JsonNode jsonNode = field.getValue().get(0);

			for (int i = 0; i < jsonNode.size(); i++) {

				Package trackedPackage = new Package();

				JsonNode singleShipment = jsonNode.get(i);

				System.out.println(singleShipment);

				String trackingNumber = singleShipment.get(0).asText();
				String packageName = singleShipment.get(1).asText();
				String carrier = singleShipment.get(2).asText();
				boolean delivered = (singleShipment.get(3).asText().equals("no")) ? true : false;

				JsonNode steps = singleShipment.get(4);
				System.out.println("Steps:" + steps.get(0));
				String dateAdded = singleShipment.get(6).asText();
				String dateChanged = singleShipment.get(7).asText();
				System.out.println("Status: " + singleShipment.get(8).asText());

				String carrierFullname = carrierMap.get(carrier);

				trackedPackage.setId(new PackagePK(trackingNumber, carrier));
				trackedPackage.setPackageName(packageName);
				trackedPackage.setDateAdded(dateAdded);
				trackedPackage.setDateModified(dateChanged);
				trackedPackage.setDelivered(delivered);

				List<PackageHistoryPK> hashCodes = new ArrayList<PackageHistoryPK>();
				for (JsonNode step : steps) {

					PackageHistory historyEntry = new PackageHistory();
					Date timestamp = parseDate(knownPatterns, step.get(1).asText());

					if (timestamp == null) {
						timestamp = new Date(0);
					}
					String statusText = step.get(0).asText().trim();
					PackageHistoryPK id = new PackageHistoryPK(trackingNumber, carrier, timestamp, statusText);

					if (!hashCodes.contains(id)) {

						historyEntry.setId(id);
						historyEntry.setTrackedPackage(trackedPackage);

						historyEntry.setLocationText(step.get(3).asText());
						// historyEntry.setTrackedPackage(trackedPackage);
						trackedPackage.getPackageHistory().add(historyEntry);
						hashCodes.add(id);
						System.out.println("adding history event");
					} else {
						System.out.println("duplicate found");
					}
				}

				mergeTrackedPackage(trackedPackage);

				System.out.println("Tracking No:" + trackingNumber + " Delivered: " + delivered + " sCarrier: "
						+ carrierFullname + " Name: " + packageName);
			}
		}
	}

	@Path("getAllOpen")
	@GET
	public List<Package> getAllOpen() {
		EntityManager em = EntityManagerService.getNewManager();

		List<Package> resultList = (List<Package>) em.createQuery("select p from Package p where p.delivered=0").getResultList();
		
		for (Package singlePackage : resultList) {
			
			List<PackageHistory> phList = (List<PackageHistory>) em
					.createQuery("select p from PackageHistory p where p.id.trackingNumber=:trackingNumber and p.id.carrier=:carrier")

					.setParameter("trackingNumber", singlePackage.getId().getTrackingNumber())
					.setParameter("carrier", singlePackage.getId().getCarrier()).getResultList();

			singlePackage.setPackageHistory(phList);
		}
		
		return resultList;
	}

	private static void mergeTrackedPackage(Package trackedPackage) {
		EntityManager em = EntityManagerService.getNewManager();

		List<Package> results = (List<Package>) em
				.createQuery(
						"select p from Package p where p.id.trackingNumber=:trackingNumber and p.id.carrier=:carrier")
				.setParameter("trackingNumber", trackedPackage.getId().getTrackingNumber())
				.setParameter("carrier", trackedPackage.getId().getCarrier()).getResultList();

		List<PackageHistory> packageHistories = trackedPackage.getPackageHistory();
		List<PackageHistory> mergedPackageHistories = new ArrayList<PackageHistory>();
		for (PackageHistory packageHistory : packageHistories) {
			PackageHistory mergedPackageHistory = mergePackageHistory(em, packageHistory);
			em.merge(mergedPackageHistory);
			System.out.println("persisting history");
			mergedPackageHistories.add(mergedPackageHistory);

		}
		trackedPackage.setPackageHistory(mergedPackageHistories);

		if (results == null || results.isEmpty()) {
			em.getTransaction().begin();
			em.persist(trackedPackage);
			em.getTransaction().commit();
		} else {
			Package existingPackage = results.get(0);
			
			em.getTransaction().begin();
			System.out.println("performing package merge");

			
			existingPackage.setDateModified(trackedPackage.getDateModified());
			existingPackage.setDelivered(trackedPackage.isDelivered());
			existingPackage.setPackageHistory(trackedPackage.getPackageHistory());
			em.merge(existingPackage);
			em.getTransaction().commit();
		}

	}

	private static PackageHistory mergePackageHistory(EntityManager em, PackageHistory packageHistory) {
		List<PackageHistory> results = (List<PackageHistory>) em
				.createQuery("select p from PackageHistory p where p.id=:id")

				.setParameter("id", packageHistory.getId()).getResultList();

		if (results == null || results.isEmpty()) {
			
			em.persist(packageHistory);
			System.out.println("added new history");
			return packageHistory;
		} else {
			System.out.println("found existing history");
			return results.get(0);
		}

	}

}
