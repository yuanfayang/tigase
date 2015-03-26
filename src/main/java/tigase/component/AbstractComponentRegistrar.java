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
		kernel.registerBean("kernel").asInstance(kernel).exec();
		kernel.registerBean("adHocCommandManager").asClass(AdHocCommandManager.class).exec();
		kernel.registerBean("eventBus").asClass(ComponentEventBus.class).exec();
		kernel.registerBean("scriptCommandProcessor").asClass(ComponenScriptCommandProcessor.class).exec();
		kernel.registerBean("writer").asClass(DefaultPacketWriter.class).exec();
		kernel.registerBean("stanzaProcessor").asClass(StanzaProcessor.class).exec();
		kernel.registerBean("responseManager").asClass(ResponseManager.class).exec();
	}

}
