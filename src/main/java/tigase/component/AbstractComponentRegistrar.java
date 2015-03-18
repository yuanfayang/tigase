package tigase.component;

import tigase.component.AbstractComponent.DefaultPacketWriter;
import tigase.component.adhoc.AdHocCommandManager;
import tigase.component.modules.StanzaProcessor;
import tigase.component.responses.ResponseManager;
import tigase.kernel.Kernel;
import tigase.kernel.Registrar;

public abstract class AbstractComponentRegistrar implements Registrar {

	@Override
	public void register(Kernel kernel) {
		kernel.registerBean("kernel", kernel);
		kernel.registerBeanClass("adHocCommandManager", AdHocCommandManager.class);
		kernel.registerBeanClass("eventBus", ComponentEventBus.class);
		kernel.registerBeanClass("scriptCommandProcessor", ComponenScriptCommandProcessor.class);
		kernel.registerBeanClass("writer", DefaultPacketWriter.class);
		kernel.registerBeanClass("stanzaProcessor", StanzaProcessor.class);
		kernel.registerBeanClass("responseManager", ResponseManager.class);
	}

}
