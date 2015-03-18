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
import tigase.kernel.Kernel;

public class EventBusComponentRegistrar extends AbstractComponentRegistrar {

	@Override
	public void register(Kernel kernel) {
		super.register(kernel);
		kernel.registerBean("localEventBus", EventBusFactory.getInstance());

		kernel.registerBeanClass("component", EventBusComponent.class);

		kernel.registerBeanClass("scriptEngineManager", ScriptEngineManager.class);
		kernel.registerBeanClass("scriptsRegistrar", ListenerScriptRegistrar.class);
		kernel.registerBeanClass("subscriptionStore", SubscriptionStore.class);
		kernel.registerBeanClass("affiliationStore", AffiliationStore.class);

		kernel.registerBeanClass(SubscribeModule.ID, SubscribeModule.class);
		kernel.registerBeanClass(UnsubscribeModule.ID, UnsubscribeModule.class);
		kernel.registerBeanClass(EventReceiverModule.ID, EventReceiverModule.class);
		kernel.registerBeanClass(EventPublisherModule.ID, EventPublisherModule.class);

		kernel.registerBeanClass(XmppPingModule.ID, XmppPingModule.class);
		kernel.registerBeanClass(JabberVersionModule.ID, JabberVersionModule.class);
		kernel.registerBeanClass(AdHocCommandModule.ID, AdHocCommandModule.class);
		kernel.registerBeanClass(DiscoveryModule.ID, DiscoveryModule.class);

		kernel.registerBeanClass(RemoveListenerScriptCommand.ID, RemoveListenerScriptCommand.class);
		kernel.registerBeanClass(AddListenerScriptCommand.ID, AddListenerScriptCommand.class);
	}

}
