package cm.homeautomation.eventbus;

import org.zeromq.ZMQ;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EventBus {

	public static final String ZEROMQPORT = "6559";

	private static EventBus instance = null;

	private static ZMQ.Socket publisher;

	private static ZMQ.Context context;

	public static EventBus getInstance() {
		if (instance == null) {
			instance = new EventBus();
			if (context == null && publisher == null) {
				context = ZMQ.context(1);

				publisher = context.socket(ZMQ.PUB);
				publisher.bind("tcp://*:" + ZEROMQPORT);
				publisher.bind("ipc://eventbus");
			}
		}

		return instance;
	}

	/**
	 * send a generic event
	 * 
	 * @param event
	 */
	public void sendMessage(EventObject event) {
		//try {
			if (event.getEventName() != null && !"".equals(event.getEventName())) {
				if (publisher != null) {
					ObjectMapper mapper = new ObjectMapper();

					//String jsonInString = mapper.writeValueAsString(event);
					//System.out.println("Sending event: " + jsonInString);
					//publisher.send(jsonInString.getBytes(), 0);
				}
			} else {
				System.err.println("Event name empty");
			}
		/*} catch (JsonProcessingException e) {
			// TODO throw
			e.printStackTrace();
		}*/
	}

	public void close() {
		publisher.close();
		context.term();
	}
}
