package tigase.monitor.tasks;

import tigase.component.PacketWriter;
import tigase.form.Field;
import tigase.form.Form;
import tigase.kernel.Initializable;
import tigase.kernel.Inject;
import tigase.monitor.InfoTask;
import tigase.monitor.MonitorTask;
import tigase.monitor.TimerTaskService;
import tigase.server.Packet;
import tigase.util.TigaseStringprepException;
import tigase.util.TimerTask;
import tigase.xml.Element;

public class MemoryMonitorTask implements MonitorTask, Initializable, InfoTask {

	private final Runtime runtime = Runtime.getRuntime();

	@Inject(bean = "timerTaskService")
	private TimerTaskService timerTaskService;

	protected long usedMemory = -1;

	private final TimerTask worker = new TimerTask() {

		@Override
		public void run() {
			usedMemory = runtime.totalMemory() - runtime.freeMemory();

			if (usedMemory / 1024 / 1024 > 133) {
				Element m = new Element("message", new String[] { "to" }, new String[] { "alice@coffeebean.local" });
				m.addChild(new Element("body", "Uuuuu! Pamieci malo!!!! Uzyto " + (usedMemory / 1024 / 1024) + " MB"));

				Packet p;
				try {
					p = Packet.packetInstance(m);
					p.setXMLNS(Packet.CLIENT_XMLNS);
					writer.write(p);
				} catch (TigaseStringprepException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	};

	@Inject
	private PacketWriter writer;

	public MemoryMonitorTask() {

	}

	@Override
	public Form getTaskInfo() {
		Form f = new Form("result", "Memory Monitor Info ", null);

		f.addField(Field.fieldTextSingle("mem-usage", "" + (usedMemory / 1024 / 1024), "Memory usage [MB]"));

		return f;
	}

	public TimerTaskService getTimerTaskService() {
		return timerTaskService;
	}

	public PacketWriter getWriter() {
		return writer;
	}

	@Override
	public void initialize() {
		timerTaskService.addTimerTask(worker, 1000l, 1000l);
	}

	public void setTimerTaskService(TimerTaskService timerTaskService) {
		this.timerTaskService = timerTaskService;
	}

	public void setWriter(PacketWriter writer) {
		this.writer = writer;
	}

}
