package cm.homeautomation.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@XmlRootElement
public class Package {

	@EmbeddedId
	private PackagePK id;

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

	private String packageName;
	private String dateAdded;
	private String dateModified;
	
	@OneToMany(orphanRemoval = true)
	@JoinColumns({
			@JoinColumn(updatable = false, insertable = false, name = "carrier", referencedColumnName = "carrier"),
			@JoinColumn(updatable = false, insertable = false, name = "trackingNumber", referencedColumnName = "trackingNumber"),

	})
	@JsonManagedReference("package")
	private List<PackageHistory> packageHistory;

	@Transient
	private String carrierName;

	private boolean delivered;

	public PackagePK getId() {
		return id;
	}

	public void setId(PackagePK id) {
		this.id = id;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getDateAdded() {
		return dateAdded;
	}

	public void setDateAdded(String dateAdded) {
		this.dateAdded = dateAdded;
	}

	public String getDateModified() {
		return dateModified;
	}

	public void setDateModified(String dateModified) {
		this.dateModified = dateModified;
	}

	@Transient
	private String getCarrierName() {
		return carrierMap.get(getId().getCarrier());
	}

	@Transient
	public void setCarrierName(String carrierName) {

	}

	public void setDelivered(boolean delivered) {
		this.delivered = delivered;

	}

	public boolean isDelivered() {
		return delivered;
	}

	public List<PackageHistory> getPackageHistory() {
		if (packageHistory == null) {
			packageHistory = new ArrayList<PackageHistory>();
		}
		return packageHistory;
	}

	public void setPackageHistory(List<PackageHistory> packageHistory) {
		this.packageHistory = packageHistory;
	}

}
