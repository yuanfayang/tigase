package tigase.disteventbus.component;

import java.util.Collection;
import java.util.logging.Level;

import tigase.component.exceptions.ComponentException;
import tigase.criteria.Criteria;
import tigase.disteventbus.EventElement;
import tigase.disteventbus.component.stores.Affiliation;
import tigase.disteventbus.component.stores.AffiliationStore;
import tigase.disteventbus.component.stores.Subscription;
import tigase.disteventbus.component.stores.SubscriptionStore;
import tigase.disteventbus.impl.LocalEventBus;
import tigase.kernel.Inject;
import tigase.server.Packet;
import tigase.util.TigaseStringprepException;
import tigase.xml.Element;
import tigase.xmpp.Authorization;

public class EventReceiverModule extends AbstractEventBusModule {

	private static final Criteria CRIT = new ElemPathCriteria(new String[] { "message", "event" }, new String[] { null,
			"http://jabber.org/protocol/pubsub#event" });

	public final static String ID = "receiver";

	@Inject
	private AffiliationStore affiliationStore;

	@Inject
	private EventPublisherModule eventPublisherModule;

	@Inject(bean = "localEventBus")
	private LocalEventBus localEventBus;

	@Inject
	private SubscriptionStore subscriptionStore;

	public AffiliationStore getAffiliationStore() {
		return affiliationStore;
	}

	public EventPublisherModule getEventPublisherModule() {
		return eventPublisherModule;
	}

	@Override
	public String[] getFeatures() {
		return null;
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
	public void process(Packet packet) throws ComponentException, TigaseStringprepException {
		final Affiliation affiliation = affiliationStore.getAffiliation(packet.getStanzaFrom());
		if (!affiliation.isPublishItem())
			throw new ComponentException(Authorization.FORBIDDEN);

		final String type = packet.getElement().getAttributeStaticStr("type");

		if (type != null && type.equals("error")) {
			if (log.isLoggable(Level.FINE))
				log.fine("Ignoring error message! " + packet);
			return;
		}

		if (log.isLoggable(Level.FINER))
			log.finer("Received event stanza: " + packet.toStringFull());

		Element eventElem = packet.getElement().getChild("event", "http://jabber.org/protocol/pubsub#event");
		Element itemsElem = eventElem.getChild("items");

		for (Element item : itemsElem.getChildren()) {
			if (!"item".equals(item.getName()))
				continue;
			for (Element ex : item.getChildren()) {
				final EventElement event = new EventElement(ex);
				String eventName = event.getName();
				String eventXmlns = event.getXMLNS();

				if (log.isLoggable(Level.FINER))
					log.finer("Received event (" + eventName + ", " + eventXmlns + "): " + event);

				localEventBus.doFire(eventName, eventXmlns, event);

				final Collection<Subscription> subscribers = subscriptionStore.getSubscribersJIDs(eventName, eventXmlns);
				eventPublisherModule.publishEvent(eventName, eventXmlns, event, subscribers);
			}
		}

	}

	public void setAffiliationStore(AffiliationStore affiliationStore) {
		this.affiliationStore = affiliationStore;
	}

	public void setEventPublisherModule(EventPublisherModule eventPublisherModule) {
		this.eventPublisherModule = eventPublisherModule;
	}

	public void setLocalEventBus(LocalEventBus localEventBus) {
		this.localEventBus = localEventBus;
	}

	public void setSubscriptionStore(SubscriptionStore subscriptionStore) {
		this.subscriptionStore = subscriptionStore;
	}

}
