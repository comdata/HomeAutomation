package cm.homeautomation.planes;

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
public class PlaneData {

	@JsonProperty("hex")
	private String hex;
	@JsonProperty("squawk")
	private String squawk;
	@JsonProperty("flight")
	private String flight;
	@JsonProperty("lat")
	private Double lat;
	@JsonProperty("lon")
	private Double lon;
	@JsonProperty("validposition")
	private Integer validposition;
	@JsonProperty("altitude")
	private Integer altitude;
	@JsonProperty("vert_rate")
	private Integer vertRate;
	@JsonProperty("track")
	private Integer track;
	@JsonProperty("validtrack")
	private Integer validtrack;
	@JsonProperty("speed")
	private Integer speed;
	@JsonProperty("messages")
	private Integer messages;
	@JsonProperty("seen")
	private Integer seen;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	/**
	 * 
	 * @return The hex
	 */
	@JsonProperty("hex")
	public String getHex() {
		return hex;
	}

	/**
	 * 
	 * @param hex
	 *            The hex
	 */
	@JsonProperty("hex")
	public void setHex(String hex) {
		this.hex = hex;
	}

	/**
	 * 
	 * @return The squawk
	 */
	@JsonProperty("squawk")
	public String getSquawk() {
		return squawk;
	}

	/**
	 * 
	 * @param squawk
	 *            The squawk
	 */
	@JsonProperty("squawk")
	public void setSquawk(String squawk) {
		this.squawk = squawk;
	}

	/**
	 * 
	 * @return The flight
	 */
	@JsonProperty("flight")
	public String getFlight() {
		return flight;
	}

	/**
	 * 
	 * @param flight
	 *            The flight
	 */
	@JsonProperty("flight")
	public void setFlight(String flight) {
		this.flight = flight;
	}

	/**
	 * 
	 * @return The lat
	 */
	@JsonProperty("lat")
	public Double getLat() {
		return lat;
	}

	/**
	 * 
	 * @param lat
	 *            The lat
	 */
	@JsonProperty("lat")
	public void setLat(Double lat) {
		this.lat = lat;
	}

	/**
	 * 
	 * @return The lon
	 */
	@JsonProperty("lon")
	public Double getLon() {
		return lon;
	}

	/**
	 * 
	 * @param lon
	 *            The lon
	 */
	@JsonProperty("lon")
	public void setLon(Double lon) {
		this.lon = lon;
	}

	/**
	 * 
	 * @return The validposition
	 */
	@JsonProperty("validposition")
	public Integer getValidposition() {
		return validposition;
	}

	/**
	 * 
	 * @param validposition
	 *            The validposition
	 */
	@JsonProperty("validposition")
	public void setValidposition(Integer validposition) {
		this.validposition = validposition;
	}

	/**
	 * 
	 * @return The altitude
	 */
	@JsonProperty("altitude")
	public Integer getAltitude() {
		return altitude;
	}

	/**
	 * 
	 * @param altitude
	 *            The altitude
	 */
	@JsonProperty("altitude")
	public void setAltitude(Integer altitude) {
		this.altitude = altitude;
	}

	/**
	 * 
	 * @return The vertRate
	 */
	@JsonProperty("vert_rate")
	public Integer getVertRate() {
		return vertRate;
	}

	/**
	 * 
	 * @param vertRate
	 *            The vert_rate
	 */
	@JsonProperty("vert_rate")
	public void setVertRate(Integer vertRate) {
		this.vertRate = vertRate;
	}

	/**
	 * 
	 * @return The track
	 */
	@JsonProperty("track")
	public Integer getTrack() {
		return track;
	}

	/**
	 * 
	 * @param track
	 *            The track
	 */
	@JsonProperty("track")
	public void setTrack(Integer track) {
		this.track = track;
	}

	/**
	 * 
	 * @return The validtrack
	 */
	@JsonProperty("validtrack")
	public Integer getValidtrack() {
		return validtrack;
	}

	/**
	 * 
	 * @param validtrack
	 *            The validtrack
	 */
	@JsonProperty("validtrack")
	public void setValidtrack(Integer validtrack) {
		this.validtrack = validtrack;
	}

	/**
	 * 
	 * @return The speed
	 */
	@JsonProperty("speed")
	public Integer getSpeed() {
		return speed;
	}

	/**
	 * 
	 * @param speed
	 *            The speed
	 */
	@JsonProperty("speed")
	public void setSpeed(Integer speed) {
		this.speed = speed;
	}

	/**
	 * 
	 * @return The messages
	 */
	@JsonProperty("messages")
	public Integer getMessages() {
		return messages;
	}

	/**
	 * 
	 * @param messages
	 *            The messages
	 */
	@JsonProperty("messages")
	public void setMessages(Integer messages) {
		this.messages = messages;
	}

	/**
	 * 
	 * @return The seen
	 */
	@JsonProperty("seen")
	public Integer getSeen() {
		return seen;
	}

	/**
	 * 
	 * @param seen
	 *            The seen
	 */
	@JsonProperty("seen")
	public void setSeen(Integer seen) {
		this.seen = seen;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}