package cm.homeautomation.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
public class CalendarEntry {

	private String dateTimeStamp;
	
	@Id
	private String uid;
	private Date start;
	private Date end;
	private String summary;
	private String location;
	private String geoLocation;
	private String description;
	private String transparent;
	
	public  CalendarEntry() {
		
	}

	public String getDateTimeStamp() {
		return dateTimeStamp;
	}

	public void setDateTimeStamp(String dateTimeStamp) {
		this.dateTimeStamp = dateTimeStamp;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTransparent() {
		return transparent;
	}

	public void setTransparent(String transparent) {
		this.transparent = transparent;
	}

	public String getGeoLocation() {
		return geoLocation;
	}

	public void setGeoLocation(String geoLocation) {
		this.geoLocation = geoLocation;
	}

	/*
	 * DTSTART DTEND SUMMARY LOCATION GEO TRANSP DESCRIPTION
	 */
}
