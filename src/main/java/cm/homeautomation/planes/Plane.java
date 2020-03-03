package cm.homeautomation.planes;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "hex", "squawk", "flight", "lat", "lon", "validposition", "altitude", "vert_rate", "track",
		"validtrack", "speed", "messages", "seen" })
public class Plane implements Serializable {

	@JsonProperty("hex")
	private String hex;
	@JsonProperty("squawk")
	private String squawk;
	@JsonProperty("flight")
	private String flight;
	@JsonProperty("lat")
	private double lat;
	@JsonProperty("lon")
	private double lon;
	@JsonProperty("validposition")
	private int validposition;
	@JsonProperty("altitude")
	private int altitude;
	@JsonProperty("vert_rate")
	private int vertRate;
	@JsonProperty("track")
	private int track;
	@JsonProperty("validtrack")
	private int validtrack;
	@JsonProperty("speed")
	private int speed;
	@JsonProperty("messages")
	private int messages;
	@JsonProperty("seen")
	private int seen;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<>();
	private static final long serialVersionUID = -195055166643612312L;

	@JsonProperty("hex")
	public String getHex() {
		return hex;
	}

	@JsonProperty("hex")
	public void setHex(String hex) {
		this.hex = hex;
	}

	public Plane withHex(String hex) {
		this.hex = hex;
		return this;
	}

	@JsonProperty("squawk")
	public String getSquawk() {
		return squawk;
	}

	@JsonProperty("squawk")
	public void setSquawk(String squawk) {
		this.squawk = squawk;
	}

	public Plane withSquawk(String squawk) {
		this.squawk = squawk;
		return this;
	}

	@JsonProperty("flight")
	public String getFlight() {
		return flight;
	}

	@JsonProperty("flight")
	public void setFlight(String flight) {
		this.flight = flight;
	}

	public Plane withFlight(String flight) {
		this.flight = flight;
		return this;
	}

	@JsonProperty("lat")
	public double getLat() {
		return lat;
	}

	@JsonProperty("lat")
	public void setLat(double lat) {
		this.lat = lat;
	}

	public Plane withLat(double lat) {
		this.lat = lat;
		return this;
	}

	@JsonProperty("lon")
	public double getLon() {
		return lon;
	}

	@JsonProperty("lon")
	public void setLon(double lon) {
		this.lon = lon;
	}

	public Plane withLon(double lon) {
		this.lon = lon;
		return this;
	}

	@JsonProperty("validposition")
	public int getValidposition() {
		return validposition;
	}

	@JsonProperty("validposition")
	public void setValidposition(int validposition) {
		this.validposition = validposition;
	}

	public Plane withValidposition(int validposition) {
		this.validposition = validposition;
		return this;
	}

	@JsonProperty("altitude")
	public int getAltitude() {
		return altitude;
	}

	@JsonProperty("altitude")
	public void setAltitude(int altitude) {
		this.altitude = altitude;
	}

	public Plane withAltitude(int altitude) {
		this.altitude = altitude;
		return this;
	}

	@JsonProperty("vert_rate")
	public int getVertRate() {
		return vertRate;
	}

	@JsonProperty("vert_rate")
	public void setVertRate(int vertRate) {
		this.vertRate = vertRate;
	}

	public Plane withVertRate(int vertRate) {
		this.vertRate = vertRate;
		return this;
	}

	@JsonProperty("track")
	public int getTrack() {
		return track;
	}

	@JsonProperty("track")
	public void setTrack(int track) {
		this.track = track;
	}

	public Plane withTrack(int track) {
		this.track = track;
		return this;
	}

	@JsonProperty("validtrack")
	public int getValidtrack() {
		return validtrack;
	}

	@JsonProperty("validtrack")
	public void setValidtrack(int validtrack) {
		this.validtrack = validtrack;
	}

	public Plane withValidtrack(int validtrack) {
		this.validtrack = validtrack;
		return this;
	}

	@JsonProperty("speed")
	public int getSpeed() {
		return speed;
	}

	@JsonProperty("speed")
	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public Plane withSpeed(int speed) {
		this.speed = speed;
		return this;
	}

	@JsonProperty("messages")
	public int getMessages() {
		return messages;
	}

	@JsonProperty("messages")
	public void setMessages(int messages) {
		this.messages = messages;
	}

	public Plane withMessages(int messages) {
		this.messages = messages;
		return this;
	}

	@JsonProperty("seen")
	public int getSeen() {
		return seen;
	}

	@JsonProperty("seen")
	public void setSeen(int seen) {
		this.seen = seen;
	}

	public Plane withSeen(int seen) {
		this.seen = seen;
		return this;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

	public Plane withAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
		return this;
	}

}