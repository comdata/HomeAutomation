package cm.homeautomation.services.trips;

import java.io.IOException;
import java.net.URL;

import javax.persistence.EntityManager;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.CalendarEntry;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.CalendarComponent;

public class TripsICalReader {
	private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder().appendYear(4, 4)
			.appendMonthOfYear(2).appendDayOfMonth(2).toFormatter();

	public static void main(String[] args) throws IOException, ParserException {
		loadTrips(args);
	}
	
	public static void loadTrips(String[] args) throws IOException, ParserException {
		CalendarBuilder calendarBuilder = new CalendarBuilder();
		
		EntityManager em = EntityManagerService.getNewManager();
		em.getTransaction().begin();
		em.createQuery("delete from CalendarEntry").executeUpdate();

		String url = args[0];
		URL trips = new URL(url);
		Calendar calendar = calendarBuilder.build(trips.openStream());

		ComponentList<CalendarComponent> components = calendar.getComponents();

		for (CalendarComponent calendarComponent : components) {
			System.out.println("===================================");
			PropertyList properties = calendarComponent.getProperties();

			CalendarEntry calendarEntry = new CalendarEntry();

			for (Property property : properties) {
				String propertyName = property.getName();
				String propertyValue = property.getValue();
				System.out.println(propertyName + " - " + propertyValue);

				switch (propertyName) {
				case "DTSTAMP":
					calendarEntry.setDateTimeStamp(propertyValue);
					break;
				case "UID":
					calendarEntry.setUid(propertyValue);
					break;
				case "DTSTART":
					calendarEntry.setStart(DATE_FORMATTER.parseDateTime(propertyValue).toDate());
					break;
				case "DTEND":
					calendarEntry.setEnd(DATE_FORMATTER.parseDateTime(propertyValue).toDate());
					break;
				case "SUMMARY":
					calendarEntry.setSummary(propertyValue);
					break;
				case "LOCATION":
					calendarEntry.setLocation(propertyValue);
					break;
				case "GEO":
					calendarEntry.setGeoLocation(propertyValue);
					break;
				case "TRANSP":
					calendarEntry.setTransparent(propertyValue);
					break;
				case "DESCRIPTION":
					calendarEntry.setDescription(propertyValue);
					break;
				}

			}
			em.persist(calendarEntry);
			System.out.println("===================================");

		}
		
		
		em.getTransaction().commit();

	}

}
