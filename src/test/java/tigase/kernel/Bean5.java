package tigase.kernel;

public class Bean5 implements UnregisterAware {

	@Override
	public void beforeUnregister() {
		System.out.println("Destroying Bean5 class");
	}

}
