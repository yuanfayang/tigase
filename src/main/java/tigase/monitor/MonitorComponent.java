package tigase.monitor;

import java.util.HashMap;
import java.util.Map;

import tigase.component.AbstractComponent;
import tigase.component.modules.Module;
import tigase.conf.ConfigurationException;
import tigase.util.TimerTask;

public class MonitorComponent extends AbstractComponent implements TimerTaskService {

	private final TimerTaskService timerTaskService = new TimerTaskService() {

		@Override
		public void addTimerTask(TimerTask task, long delay) {
			MonitorComponent.this.addTimerTask(task, delay);
		}

		@Override
		public void addTimerTask(TimerTask task, long initialDelay, long period) {
			MonitorComponent.this.addTimerTask(task, initialDelay, period);
		}
	};

	public MonitorComponent() {
	}

	@Override
	public String getComponentVersion() {
		String version = this.getClass().getPackage().getImplementationVersion();
		return version == null ? "0.0.0" : version;
	}

	@Override
	protected Map<String, Class<? extends Module>> getDefaultModulesList() {
		final Map<String, Class<? extends Module>> result = new HashMap<String, Class<? extends Module>>();

		return result;
	}

	@Override
	public String getDiscoCategory() {
		return "component";
	}

	@Override
	public String getDiscoCategoryType() {
		return "monitor";
	}

	@Override
	public String getDiscoDescription() {
		return "Monitor Component";
	}

	@Override
	public boolean isDiscoNonAdmin() {
		return true;
	}

	@Override
	public void setProperties(Map<String, Object> props) throws ConfigurationException {
		super.setProperties(props);
	}

}
