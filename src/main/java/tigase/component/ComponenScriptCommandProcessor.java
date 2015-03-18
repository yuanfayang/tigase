package tigase.component;

import java.util.List;
import java.util.Queue;

import tigase.component.modules.impl.AdHocCommandModule.ScriptCommandProcessor;
import tigase.kernel.Inject;
import tigase.server.Packet;
import tigase.xml.Element;
import tigase.xmpp.JID;

public class ComponenScriptCommandProcessor implements ScriptCommandProcessor {

	@Inject
	private AbstractComponent component;

	public AbstractComponent getComponent() {
		return component;
	}

	@Override
	public List<Element> getScriptItems(String node, JID jid, JID from) {
		return component.getScriptItems(node, jid, from);
	}

	@Override
	public boolean processScriptCommand(Packet pc, Queue<Packet> results) {
		return component.processScriptCommand(pc, results);
	}

	public void setComponent(AbstractComponent component) {
		this.component = component;
	}
}
