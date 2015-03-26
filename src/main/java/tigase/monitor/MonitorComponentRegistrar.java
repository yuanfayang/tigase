package tigase.monitor;

import javax.script.ScriptEngineManager;

import tigase.component.AbstractComponentRegistrar;
import tigase.component.modules.impl.AdHocCommandModule;
import tigase.component.modules.impl.DiscoveryModule;
import tigase.component.modules.impl.JabberVersionModule;
import tigase.component.modules.impl.XmppPingModule;
import tigase.kernel.Kernel;
import tigase.monitor.modules.AdHocCommandMonitorModule;
import tigase.monitor.modules.AddScriptTaskCommand;
import tigase.monitor.modules.AddTimerScriptTaskCommand;
import tigase.monitor.modules.ConfigureTaskCommand;
import tigase.monitor.modules.DeleteScriptTaskCommand;
import tigase.monitor.modules.DiscoveryMonitorModule;
import tigase.monitor.modules.InfoTaskCommand;
import tigase.server.monitor.MonitorRuntime;

public class MonitorComponentRegistrar extends AbstractComponentRegistrar {

	@Override
	public void register(Kernel kernel) {
		super.register(kernel);
		kernel.registerBean("component").asClass(MonitorComponent.class).exec();

		kernel.registerBean(XmppPingModule.ID).asClass(XmppPingModule.class).exec();
		kernel.registerBean(JabberVersionModule.ID).asClass(JabberVersionModule.class).exec();
		kernel.registerBean(AdHocCommandModule.ID).asClass(AdHocCommandMonitorModule.class).exec();
		kernel.registerBean(DiscoveryModule.ID).asClass(DiscoveryMonitorModule.class).exec();

		kernel.registerBean(TasksScriptRegistrar.ID).asClass(TasksScriptRegistrar.class).exec();
		kernel.registerBean("timerTaskService").asClass(MonitorTimerTaskService.class).exec();
		kernel.registerBean("scriptEngineManager").asClass(ScriptEngineManager.class).exec();
		// XXX kernel.registerBean("bindings",
		// scriptEngineManager.getBindings());
		kernel.registerBean("runtime").asInstance(MonitorRuntime.getMonitorRuntime()).exec();

		kernel.registerBean(AddScriptTaskCommand.ID).asClass(AddScriptTaskCommand.class).exec();
		kernel.registerBean(AddTimerScriptTaskCommand.ID).asClass(AddTimerScriptTaskCommand.class).exec();
		kernel.registerBean(DeleteScriptTaskCommand.ID).asClass(DeleteScriptTaskCommand.class).exec();
		kernel.registerBean(InfoTaskCommand.NODE).asClass(InfoTaskCommand.class).exec();
		kernel.registerBean(ConfigureTaskCommand.NODE).asClass(ConfigureTaskCommand.class).exec();
	}

}
