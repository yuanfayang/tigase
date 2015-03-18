package tigase.disteventbus.component;

import tigase.component.modules.AbstractModule;

public abstract class AbstractEventBusModule extends AbstractModule {

	private static long id = 0;

	protected String nextStanzaID() {
		synchronized (this) {
			return "" + (++id);
		}

	}

}
