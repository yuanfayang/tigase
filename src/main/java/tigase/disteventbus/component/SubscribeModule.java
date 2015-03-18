package tigase.disteventbus.component;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import tigase.component.exceptions.ComponentException;
import tigase.component.responses.AsyncCallback;
import tigase.criteria.Criteria;
import tigase.disteventbus.EventHandler;
import tigase.disteventbus.component.stores.Affiliation;
import tigase.disteventbus.component.stores.AffiliationStore;
import tigase.disteventbus.component.stores.Subscription;
import tigase.disteventbus.component.stores.SubscriptionStore;
import tigase.disteventbus.impl.EventName;
import tigase.disteventbus.impl.LocalEventBus;
import tigase.disteventbus.impl.LocalEventBus.LocalEventBusListener;
import tigase.kernel.Initializable;
import tigase.kernel.Inject;
import tigase.kernel.UnregisterAware;
import tigase.server.Packet;
import tigase.server.Permissions;
import tigase.util.TigaseStringprepException;
import tigase.xml.Element;
import tigase.xmpp.Authorization;
import tigase.xmpp.JID;
import tigase.xmpp.StanzaType;

public class SubscribeModule extends AbstractEventBusModule implements Initializable, UnregisterAware {

	private static final Criteria CRIT = new ElemPathCriteria(new String[] { "iq", "pubsub", "subscribe" }, new String[] {
			null, "http://jabber.org/protocol/pubsub", null });

	public final static String ID = "subscribe";

	@Inject
	private AffiliationStore affiliationStore;

	@Inject
	private EventBusComponent component;

	private final LocalEventBusListener eventBusListener = new LocalEventBusListener() {

		@Override
		public void onAddHandler(String name, String xmlns, EventHandler handler) {
			SubscribeModule.this.onAddHandler(name, xmlns);
		}

		@Override
		public void onFire(String name, String xmlns, Element event) {
		}

		@Override
		public void onRemoveHandler(String name, String xmlns, EventHandler handler) {
		}
	};

	@Inject(bean = "localEventBus")
	private LocalEventBus localEventBus;

	@Inject
	private SubscriptionStore subscriptionStore;

	@Override
	public void beforeUnregister() {
		localEventBus.removeListener(eventBusListener);
	}

	public void clusterNodeConnected(String node) {
		// context.getSubscriptionStore().addSubscription(null,
		// "tigase:eventbus", JID.jidInstanceNS("eventbus", node, null));

		if (log.isLoggable(Level.FINER))
			log.finer("Node " + node + " is connected.");

		Set<Element> pubsubNodes = new HashSet<Element>();
		for (EventName eventName : localEventBus.getAllListenedEvents()) {
			pubsubNodes.add(prepareSubscribeElement(eventName, component.getComponentId(), null));
		}

		for (EventName eventName : subscriptionStore.getSubscribedEvents()) {
			Collection<Subscription> subscriptions = subscriptionStore.getSubscribersJIDs(eventName.getName(),
					eventName.getXmlns());
			for (Subscription subscription : subscriptions) {
				if (subscription.getServiceJID() != null)
					pubsubNodes.add(prepareSubscribeElement(eventName, subscription.getJid(),
							subscription.getServiceJID().toString()));
			}
		}

		if (!pubsubNodes.isEmpty())
			sendSubscribeRequest("eventbus@" + node, pubsubNodes);
	}

	public void clusterNodeDisconnected(String node) {
		if (log.isLoggable(Level.FINER))
			log.finer("Node " + node + " is disconnected.");
		subscriptionStore.remove(new Subscription(JID.jidInstanceNS("eventbus", node, null)));
	}

	public AffiliationStore getAffiliationStore() {
		return affiliationStore;
	}

	public EventBusComponent getComponent() {
		return component;
	}

	@Override
	public String[] getFeatures() {
		return new String[] { "http://jabber.org/protocol/pubsub#subscribe" };
	}

	public LocalEventBus getLocalEventBus() {
		return localEventBus;
	}

	@Override
	public Criteria getModuleCriteria() {
		return CRIT;
	}

	public SubscriptionStore getSubscriptionStore() {
		return subscriptionStore;
	}

	@Override
	public void initialize() {
		localEventBus.addListener(eventBusListener);

	}

	protected boolean isClusteredEventBus(final JID jid) {
		return jid.getLocalpart().equals("eventbus") && component.getConnectedNodes().contains(jid.getDomain());
	}

	protected void onAddHandler(String eventName, String eventXmlns) {
		for (String node : component.getConnectedNodes()) {
			Element se = prepareSubscribeElement(new EventName(eventName, eventXmlns), component.getComponentId(), null);
			sendSubscribeRequest("eventbus@" + node, Collections.singleton(se));
		}
	}

	private Element prepareSubscribeElement(EventName event, JID jid, String service) {
		Element subscribeElem = new Element("subscribe");
		subscribeElem.addAttribute("node", event.toEventBusNode());
		subscribeElem.addAttribute("jid", jid.toString());

		if (service != null) {
			subscribeElem.addChild(new Element("service", service));
		}

		return subscribeElem;
	}

	@Override
	public void process(Packet packet) throws ComponentException, TigaseStringprepException {
		if (packet.getType() == StanzaType.set) {
			processSet(packet);
		} else
			throw new ComponentException(Authorization.NOT_ALLOWED);
	}

	protected Element processClusterSubscription(final Packet packet) throws TigaseStringprepException {
		// subscription from cluster node
		List<Element> subscribeElements = packet.getElemChildrenStaticStr(new String[] { "iq", "pubsub" });

		for (Element subscribe : subscribeElements) {
			Element serviceItem = subscribe.getChild("service");

			final EventName parsedName = NodeNameUtil.parseNodeName(subscribe.getAttributeStaticStr("node"));
			final JID jid = JID.jidInstance(subscribe.getAttributeStaticStr("jid"));
			final String service;

			if (serviceItem != null && serviceItem.getCData() != null) {
				service = serviceItem.getCData();
			} else {
				service = null;
			}

			if (log.isLoggable(Level.FINER))
				log.finer("Node " + jid + " subscribed for events " + parsedName);

			Subscription subscription = new Subscription(jid);
			subscription.setServiceJID(JID.jidInstanceNS(service));

			subscriptionStore.addSubscription(parsedName.getName(), parsedName.getXmlns(), subscription);

		}
		return null;
	}

	protected Element processNonClusterSubscription(final Packet packet) throws TigaseStringprepException, ComponentException {
		// subscription from something out of cluster
		final Affiliation affiliation = affiliationStore.getAffiliation(packet.getStanzaFrom());

		if (!affiliation.isSubscribe())
			throw new ComponentException(Authorization.FORBIDDEN);

		List<Element> subscribeElements = packet.getElemChildrenStaticStr(new String[] { "iq", "pubsub" });
		Element response = new Element("pubsub", new String[] { "xmlns" }, new String[] { "http://jabber.org/protocol/pubsub" });

		final Set<Element> subscribedNodes = new HashSet<Element>();
		for (Element subscribe : subscribeElements) {
			EventName parsedName = NodeNameUtil.parseNodeName(subscribe.getAttributeStaticStr("node"));
			JID jid = JID.jidInstance(subscribe.getAttributeStaticStr("jid"));

			if (log.isLoggable(Level.FINER))
				log.finer("Entity " + jid + " subscribed for events " + parsedName);

			subscriptionStore.addSubscription(parsedName.getName(), parsedName.getXmlns(),
					new Subscription(jid, packet.getStanzaTo()));

			subscribedNodes.add(prepareSubscribeElement(parsedName, jid, packet.getStanzaTo().toString()));

			response.addChild(new Element("subscription", new String[] { "node", "jid", "subscription" }, new String[] {
					parsedName.toEventBusNode(), jid.toString(), "subscribed" }));
		}

		if (log.isLoggable(Level.FINER))
			log.finer("Forwarding subscription to: " + component.getConnectedNodes());

		for (String node : component.getConnectedNodes()) {
			sendSubscribeRequest("eventbus@" + node, subscribedNodes);
		}

		return response;
	}

	private void processSet(final Packet packet) throws TigaseStringprepException, ComponentException {
		Element subscriptionResponse;
		if (isClusteredEventBus(packet.getStanzaFrom())) {
			subscriptionResponse = processClusterSubscription(packet);
		} else {
			subscriptionResponse = processNonClusterSubscription(packet);
		}

		Packet response = packet.okResult(subscriptionResponse, 0);
		response.setPermissions(Permissions.ADMIN);
		write(response);
	}

	protected void sendSubscribeRequest(String to, Collection<Element> subscriptionElement) {
		try {
			Element iq = new Element("iq", new String[] { "from", "to", "type", "id" }, new String[] {
					component.getComponentId().toString(), to, "set", nextStanzaID() });

			Element pubsubElem = new Element("pubsub", new String[] { "xmlns" },
					new String[] { "http://jabber.org/protocol/pubsub" });
			iq.addChild(pubsubElem);

			for (Element node : subscriptionElement) {
				pubsubElem.addChild(node);
			}

			final Packet packet = Packet.packetInstance(iq);
			packet.setPermissions(Permissions.ADMIN);
			packet.setXMLNS(Packet.CLIENT_XMLNS);

			write(packet, new AsyncCallback() {

				@Override
				public void onError(Packet responseStanza, String errorCondition) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onSuccess(Packet responseStanza) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onTimeout() {
					// TODO Auto-generated method stub

				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setAffiliationStore(AffiliationStore affiliationStore) {
		this.affiliationStore = affiliationStore;
	}

	public void setComponent(EventBusComponent component) {
		this.component = component;
	}

	public void setLocalEventBus(LocalEventBus localEventBus) {
		this.localEventBus = localEventBus;
	}

	public void setSubscriptionStore(SubscriptionStore subscriptionStore) {
		this.subscriptionStore = subscriptionStore;
	}

}
