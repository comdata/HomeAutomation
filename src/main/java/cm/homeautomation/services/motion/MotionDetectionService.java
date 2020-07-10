package cm.homeautomation.services.motion;

import org.greenrobot.eventbus.Subscribe;

import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.services.base.BaseService;

public class MotionDetectionService extends BaseService {

	public MotionDetectionService() {
		EventBusService.getEventBus().register(this);

	}

	@Subscribe
	public void registerMotionEvent(final EventObject event) {

		final Object eventData = event.getData();
		if (eventData instanceof MotionEvent) {
			final MotionEvent motionEvent = (MotionEvent) eventData;

			final boolean state = motionEvent.isState();

			// TODO refactor this. add suppression and flip detection logic
			// add
			System.out.println(motionEvent.getMessageString());

		}
	}

}
