package cm.homeautomation.services.overview;

public class OverviewTile {

	private String icon;
	private String number;
	private String numberUnit;
	private String title;
	private String info;
	private String infoState;
   /* "icon" : "inbox",
    "number" : "89",
    "title" : "Approve Leave Requests",
    "info" : "Overdue",
    "infoState" : "Error"*/
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getNumberUnit() {
		return numberUnit;
	}
	public void setNumberUnit(String numberUnit) {
		this.numberUnit = numberUnit;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public String getInfoState() {
		return infoState;
	}
	public void setInfoState(String infoState) {
		this.infoState = infoState;
	}
}
