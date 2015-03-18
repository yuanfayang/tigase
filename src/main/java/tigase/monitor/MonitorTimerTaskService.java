package tigase.monitor;

import tigase.util.TimerTask;

public class MonitorTimerTaskService implements TimerTaskService {

	private MonitorComponent component;

	@Override
	public void addTimerTask(TimerTask task, long delay) {
		component.addTimerTask(task, delay);
	}

	@Override
	public void addTimerTask(TimerTask task, long initialDelay, long period) {
		component.addTimerTask(task, initialDelay, period);
	}

	public MonitorComponent getComponent() {
		return component;
	}

	public void setComponent(MonitorComponent component) {
		this.component = component;
	}

}
