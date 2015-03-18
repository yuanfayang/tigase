package tigase.disteventbus.component;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import tigase.cluster.api.ClusterControllerIfc;
import tigase.cluster.api.ClusteredComponentIfc;
import tigase.component.AbstractComponent;
import tigase.component.modules.Module;
import tigase.conf.ConfigurationException;
import tigase.kernel.Inject;
import tigase.stats.StatisticsList;

public class EventBusComponent extends AbstractComponent implements ClusteredComponentIfc {

	public static final String COMPONENT_EVENTS_XMLNS = "tigase:eventbus";

	private final Set<String> connectedNodes = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

	@Inject
	private SubscribeModule subscribeModule;

	public EventBusComponent() {
		super();
	}

	@Override
	public String getComponentVersion() {
		String version = this.getClass().getPackage().getImplementationVersion();
		return version == null ? "0.0.0" : version;
	}

	public Collection<String> getConnectedNodes() {
		return Collections.unmodifiableCollection(connectedNodes);
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
		return "generic";
	}

	@Override
	public String getDiscoDescription() {
		return "Distributed EventBus";
	}

	@Override
	public void getStatistics(StatisticsList list) {
		super.getStatistics(list);

		list.add(getName(), "Known cluster nodes", connectedNodes.size(), Level.INFO);
	}

	public SubscribeModule getSubscribeModule() {
		return subscribeModule;
	}

	@Override
	public boolean isDiscoNonAdmin() {
		return false;
	}

	@Override
	public boolean isSubdomain() {
		return false;
	}

	@Override
	public void nodeConnected(String node) {
		connectedNodes.add(node);

		if (log.isLoggable(Level.FINEST))
			log.finest("Node added. Known nodes: " + connectedNodes);

		if (subscribeModule != null) {
			subscribeModule.clusterNodeConnected(node);
		}
	}

	@Override
	public void nodeDisconnected(String node) {
		connectedNodes.remove(node);

		if (log.isLoggable(Level.FINEST))
			log.finest("Node removed. Known nodes: " + connectedNodes);

		if (subscribeModule != null) {
			subscribeModule.clusterNodeDisconnected(node);
		}
	}

	@Override
	public void processPacket(tigase.server.Packet packet) {
		super.processPacket(packet);
	}

	@Override
	public void setClusterController(ClusterControllerIfc cl_controller) {
	}

	@Override
	public void setProperties(Map<String, Object> props) throws ConfigurationException {
		super.setProperties(props);
	}

	public void setSubscribeModule(SubscribeModule subscribeModule) {
		this.subscribeModule = subscribeModule;
	}

}
