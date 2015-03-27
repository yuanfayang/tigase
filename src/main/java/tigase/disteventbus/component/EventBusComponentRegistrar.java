package tigase.disteventbus.component;

import javax.script.ScriptEngineManager;

import tigase.component.AbstractComponentRegistrar;
import tigase.component.modules.impl.AdHocCommandModule;
import tigase.component.modules.impl.DiscoveryModule;
import tigase.component.modules.impl.JabberVersionModule;
import tigase.component.modules.impl.XmppPingModule;
import tigase.disteventbus.EventBusFactory;
import tigase.disteventbus.component.stores.AffiliationStore;
import tigase.disteventbus.component.stores.SubscriptionStore;
import tigase.kernel.core.Kernel;

public class EventBusComponentRegistrar extends AbstractComponentRegistrar {

	@Override
	public void register(Kernel kernel) {
		super.register(kernel);
		kernel.registerBean("localEventBus").asInstance(EventBusFactory.getInstance()).exec();

		kernel.registerBean("component").asClass(EventBusComponent.class).exec();

		kernel.registerBean("scriptEngineManager").asClass(ScriptEngineManager.class).exec();
		kernel.registerBean("scriptsRegistrar").asClass(ListenerScriptRegistrar.class).exec();
		kernel.registerBean("subscriptionStore").asClass(SubscriptionStore.class).exec();
		kernel.registerBean("affiliationStore").asClass(AffiliationStore.class).exec();

		kernel.registerBean(SubscribeModule.ID).asClass(SubscribeModule.class).exec();
		kernel.registerBean(UnsubscribeModule.ID).asClass(UnsubscribeModule.class).exec();
		kernel.registerBean(EventReceiverModule.ID).asClass(EventReceiverModule.class).exec();
		kernel.registerBean(EventPublisherModule.ID).asClass(EventPublisherModule.class).exec();

		kernel.registerBean(XmppPingModule.ID).asClass(XmppPingModule.class).exec();
		kernel.registerBean(JabberVersionModule.ID).asClass(JabberVersionModule.class).exec();
		kernel.registerBean(AdHocCommandModule.ID).asClass(AdHocCommandModule.class).exec();
		kernel.registerBean(DiscoveryModule.ID).asClass(DiscoveryModule.class).exec();

		kernel.registerBean(RemoveListenerScriptCommand.ID).asClass(RemoveListenerScriptCommand.class).exec();
		kernel.registerBean(AddListenerScriptCommand.ID).asClass(AddListenerScriptCommand.class).exec();
	}

}
