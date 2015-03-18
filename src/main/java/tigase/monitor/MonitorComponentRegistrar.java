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
		kernel.registerBeanClass("component", MonitorComponent.class);

		kernel.registerBeanClass(XmppPingModule.ID, XmppPingModule.class);
		kernel.registerBeanClass(JabberVersionModule.ID, JabberVersionModule.class);
		kernel.registerBeanClass(AdHocCommandModule.ID, AdHocCommandMonitorModule.class);
		kernel.registerBeanClass(DiscoveryModule.ID, DiscoveryMonitorModule.class);

		kernel.registerBeanClass(TasksScriptRegistrar.ID, TasksScriptRegistrar.class);
		kernel.registerBeanClass("timerTaskService", MonitorTimerTaskService.class);
		kernel.registerBeanClass("scriptEngineManager", ScriptEngineManager.class);
		// XXX kernel.registerBean("bindings",
		// scriptEngineManager.getBindings());
		kernel.registerBean("runtime", MonitorRuntime.getMonitorRuntime());

		kernel.registerBeanClass(AddScriptTaskCommand.ID, AddScriptTaskCommand.class);
		kernel.registerBeanClass(AddTimerScriptTaskCommand.ID, AddTimerScriptTaskCommand.class);
		kernel.registerBeanClass(DeleteScriptTaskCommand.ID, DeleteScriptTaskCommand.class);
		kernel.registerBeanClass(InfoTaskCommand.NODE, InfoTaskCommand.class);
		kernel.registerBeanClass(ConfigureTaskCommand.NODE, ConfigureTaskCommand.class);
	}

}
