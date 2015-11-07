package cm.homeautomation.services.base;

import java.io.File;

import it.sauronsoftware.cron4j.Scheduler;

/**
 * provide refresh methods for the scheduler
 * 
 * @author mertins
 *
 */
public class SchedulerThread extends Thread {
	private static SchedulerThread instance=null;
	private Scheduler scheduler;
	private boolean run=true;
	
	public static SchedulerThread getInstance() {
		if (instance==null) {
			instance=new SchedulerThread();
		}
		
		return instance;
	}
	
	@Override
	public void run() {
		super.run();
		
		reloadScheduler();
		
		while (run) {
			
			
		
			try {
				Thread.sleep(5*60*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void reloadScheduler() {
		Scheduler newScheduler = new Scheduler();
		File scheduleFile = new File("schedule.cron");
		newScheduler.scheduleFile(scheduleFile);
		
		// minimize downtime
		Scheduler scheduler = getScheduler();
		if (scheduler != null) {
			scheduler.stop();
		}
		
		newScheduler.start();
		setScheduler(newScheduler);
	}
	
	public void stopThread() {
		run=false;
		getScheduler().stop();
	}

	public Scheduler getScheduler() {
		return scheduler;
	}

	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}
}
