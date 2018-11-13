package cm.homeautomation.tradfri;

import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.log4j.LogManager;
import org.eclipse.californium.core.CoapResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LightBulb {

	public static TradfriColorPoint convertRGBToCIE(double red, double green, double blue) {

		// Apply a gamma correction to the RGB values, which makes the color more vivid
		// and more the like the color displayed on the screen of your device
		red = (red > 0.04045) ? Math.pow((red + 0.055) / (1.0 + 0.055), 2.4) : (red / 12.92);
		green = (green > 0.04045) ? Math.pow((green + 0.055) / (1.0 + 0.055), 2.4) : (green / 12.92);
		blue = (blue > 0.04045) ? Math.pow((blue + 0.055) / (1.0 + 0.055), 2.4) : (blue / 12.92);

		// RGB values to XYZ using the Wide RGB D65 conversion formula
		final double X = (red * 0.664511) + (green * 0.154324) + (blue * 0.162028);
		final double Y = (red * 0.283881) + (green * 0.668433) + (blue * 0.047685);
		final double Z = (red * 0.000088) + (green * 0.072310) + (blue * 0.986039);

		// Calculate the xy values from the XYZ values
		double x = new Long(Math.round((X / (X + Y + Z)) * 1000)).doubleValue() / 1000;
		double y = new Long(Math.round((Y / (X + Y + Z)) * 1000)).doubleValue() / 1000;

		if (Double.isNaN(x)) {
			x = 0;
		}

		if (Double.isNaN(y)) {
			y = 0;
		}

		return new TradfriColorPoint(x, y);

	}

	public static void main(final String[] args) {
		final String color = "ff0000";
		final Integer red = Integer.valueOf(color.substring(0, 2), 16);
		final Integer green = Integer.valueOf(color.substring(2, 4), 16);
		final Integer blue = Integer.valueOf(color.substring(4, 6), 16);
		final TradfriColorPoint convertRGBToCIE = convertRGBToCIE(red, green, blue);

		LogManager.getLogger(LightBulb.class).debug(
				red + " " + green + " " + blue + "x: " + convertRGBToCIE.getX() + " y: " + convertRGBToCIE.getY());
	}

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

	private boolean colorLight = false;

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

	public String getManufacturer() {
		return manufacturer;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public boolean isColorLight() {
		return colorLight;
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

			updateListeners = updateListenersRequired(updateListeners, light);
		} catch (final JSONException e) {
			LogManager.getLogger(this.getClass())
					.error("Cannot update bulb info: error parsing the response from the gateway.", e);
		}
		if (updateListeners) {
			for (final TradfriBulbListener l : listners) {
				l.bulb_state_changed(this);
			}
		}
	}

	private boolean updateListenersRequired(boolean updateListeners, final JSONObject light) {
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
			setColorLight(true);
			final String new_color = light.getString(TradfriConstants.COLOR);
			if ((color == null) || !color.equals(new_color)) {
				updateListeners = true;
			}
			color = new_color;
		}
		return updateListeners;
	}

	public void removeLightBulbListner(final TradfriBulbListener l) {
		listners.remove(l);
	}

	public void setColor(final String color) {
		try {

			final JSONObject json = new JSONObject();
			final JSONObject settings = new JSONObject();
			final JSONArray array = new JSONArray();

			final int red = Integer.valueOf(color.substring(0, 2), 16).intValue();
			final int green = Integer.valueOf(color.substring(2, 4), 16).intValue();
			final int blue = Integer.valueOf(color.substring(4, 6), 16).intValue();

			final TradfriColor colorPoint = TradfriColor.fromRGBValues(red, green, blue, 254);

			array.put(settings);
			json.put(TradfriConstants.LIGHT, array);
			settings.put(TradfriConstants.COLOR_X, colorPoint.getXyX().intValue());
			settings.put(TradfriConstants.COLOR_Y, colorPoint.getXyY().intValue());
			final String payload = json.toString();
			gateway.set(TradfriConstants.DEVICES + "/" + this.getId(), payload);

		} catch (final JSONException ex) {
			Logger.getLogger(TradfriGateway.class.getName()).log(Level.SEVERE, null, ex);
		}
		this.color = color;
	}

	public void setColorLight(final boolean colorLight) {
		this.colorLight = colorLight;
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

	public void setManufacturer(final String manufacturer) {
		this.manufacturer = manufacturer;
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
