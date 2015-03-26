package tigase.kernel;

import java.util.Set;

@Bean(name = "bean1")
public class Bean1 {

	@Inject
	private Bean2 bean2;

	@Inject
	private Bean3 bean3;

	@Inject
	private Special[] ss;

	@Inject(type = Special.class)
	private Set<Special> xxx;

	public Bean2 getBean2() {
		return bean2;
	}

	public Bean3 getBean3() {
		return bean3;
	}

	public Special[] getSs() {
		return ss;
	}

	public Set<Special> getXxx() {
		return xxx;
	}

	public void setBean2(Bean2 bean2) {
		this.bean2 = bean2;
	}

	public void setBean3(Bean3 bean3) {
		this.bean3 = bean3;
	}

	public void setSs(Special[] ss) {
		this.ss = ss;
	}

	public void setXxx(Set<Special> xxx) {
		this.xxx = xxx;
	}

}
