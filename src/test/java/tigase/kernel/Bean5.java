package tigase.kernel;

public class Bean5 implements UnregisterAware {

	private Long value;

	@Override
	public void beforeUnregister() {
		System.out.println("Destroying Bean5 class");
	}

	public Long getValue() {
		return value;
	}

	public void setValue(Long value) {
		this.value = value;
	}

}
