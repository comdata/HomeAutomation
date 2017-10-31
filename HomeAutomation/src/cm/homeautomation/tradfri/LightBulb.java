package cm.homeautomation.tradfri;

import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LightBulb {

	private final ArrayList<TradfriBulbListener> listners = new ArrayList<>();

	private final TradfriGateway gateway;
	// Immutable information
	private final int id;
	private String name;

	private String manufacturer;

	private String type;
	private String firmware;

	// Status
	private boolean online;

	// State of the bulb
	private boolean on;

	private int intensity;

	private String color;

	// Dates
	private Date dateInstalled;

	private Date dateLastSeen;

	public LightBulb(final int id, final TradfriGateway gateway) {
		this.id = id;
		this.gateway = gateway;
	}

	public LightBulb(final int id, final TradfriGateway gateway, final CoapResponse response) {
		this.id = id;
		this.gateway = gateway;
		if (response != null) {
			parseResponse(response);
		}
	}

	public void addLightBulbListner(final TradfriBulbListener l) {
		listners.add(l);
	}

	public void clearLightBulbListners() {
		listners.clear();
	}

	public String getColor() {
		return color;
	}

	public Date getDateInstalled() {
		return dateInstalled;
	}

	public Date getDateLastSeen() {
		return dateLastSeen;
	}

	public String getFirmware() {
		return firmware;
	}

	public int getId() {
		return id;
	}

	public int getIntensity() {
		return intensity;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public boolean isOn() {
		return on;
	}

	public boolean isOnline() {
		return online;
	}

	protected void parseResponse(final CoapResponse response) {
		boolean updateListeners = false;
		gateway.getLogger().log(Level.INFO, response.getResponseText());
		try {
			final JSONObject json = new JSONObject(response.getResponseText());

			final String new_name = json.getString(TradfriConstants.NAME);
			if ((name == null) || !name.equals(new_name)) {
				updateListeners = true;
			}
			name = new_name;

			dateInstalled = new Date(json.getLong(TradfriConstants.DATE_INSTALLED) * 1000);
			dateLastSeen = new Date(json.getLong(TradfriConstants.DATE_LAST_SEEN) * 1000);

			final boolean new_online = json.getInt(TradfriConstants.DEVICE_REACHABLE) != 0;
			if (new_online != online) {
				updateListeners = true;
			}
			online = new_online;

			manufacturer = json.getJSONObject("3").getString("0");
			type = json.getJSONObject("3").getString("1");
			firmware = json.getJSONObject("3").getString("3");

			final JSONObject light = json.getJSONArray(TradfriConstants.LIGHT).getJSONObject(0);

			if (light.has(TradfriConstants.ONOFF) && light.has(TradfriConstants.DIMMER)) {
				final boolean new_on = (light.getInt(TradfriConstants.ONOFF) != 0);
				final int new_intensity = light.getInt(TradfriConstants.DIMMER);
				if (on != new_on) {
					updateListeners = true;
				}
				if (intensity != new_intensity) {
					updateListeners = true;
				}
				on = new_on;
				intensity = new_intensity;
			} else {
				if (online) {
					updateListeners = true;
				}
				online = false;
			}
			if (light.has(TradfriConstants.COLOR)) {
				final String new_color = light.getString(TradfriConstants.COLOR);
				if ((color == null) || !color.equals(new_color)) {
					updateListeners = true;
				}
				color = new_color;
			}
		} catch (final JSONException e) {
			System.err.println("Cannot update bulb info: error parsing the response from the gateway.");
			e.printStackTrace();
		}
		if (updateListeners) {
			for (final TradfriBulbListener l : listners) {
				l.bulb_state_changed(this);
			}
		}
	}

	public void removeLightBulbListner(final TradfriBulbListener l) {
		listners.remove(l);
	}

	public void setColor(final String color) {
		try {
			final JSONObject json = new JSONObject();
			final JSONObject settings = new JSONObject();
			final JSONArray array = new JSONArray();
			array.put(settings);
			json.put(TradfriConstants.LIGHT, array);
			settings.put(TradfriConstants.COLOR, color);
			final String payload = json.toString();
			gateway.set(TradfriConstants.DEVICES + "/" + this.getId(), payload);

		} catch (final JSONException ex) {
			Logger.getLogger(TradfriGateway.class.getName()).log(Level.SEVERE, null, ex);
		}
		this.color = color;
	}

	public void setIntensity(final int intensity) {
		try {
			final JSONObject json = new JSONObject();
			final JSONObject settings = new JSONObject();
			final JSONArray array = new JSONArray();
			array.put(settings);
			json.put(TradfriConstants.LIGHT, array);
			settings.put(TradfriConstants.DIMMER, intensity);
			final String payload = json.toString();
			gateway.set(TradfriConstants.DEVICES + "/" + this.getId(), payload);

		} catch (final JSONException ex) {
			Logger.getLogger(TradfriGateway.class.getName()).log(Level.SEVERE, null, ex);
		}
		this.intensity = intensity;
	}

	public void setOn(final boolean on) {
		try {
			final JSONObject json = new JSONObject();
			final JSONObject settings = new JSONObject();
			final JSONArray array = new JSONArray();
			array.put(settings);
			json.put(TradfriConstants.LIGHT, array);
			settings.put(TradfriConstants.ONOFF, (on) ? 1 : 0);
			final String payload = json.toString();
			gateway.set(TradfriConstants.DEVICES + "/" + this.getId(), payload);

		} catch (final JSONException ex) {
			Logger.getLogger(TradfriGateway.class.getName()).log(Level.SEVERE, null, ex);
		}
		this.on = on;
	}

	@Override
	public String toString() {
		String result = "[BULB " + id + "]";
		if (online) {
			result += "\ton:" + on + "\tdim:" + intensity + "\tcolor:" + color;
		} else {
			result += "  ********** OFFLINE *********** ";
		}
		result += "\ttype: " + type + "\tname: " + name;
		return result;
	}

	protected void updateBulb() {
		final CoapResponse response = gateway.get(TradfriConstants.DEVICES + "/" + id);
		if (response != null) {
			parseResponse(response);
		}
	}

}
