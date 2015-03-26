package tigase.kernel;

public interface BeanConfigurator {

	public static final String DEFAULT_CONFIGURATOR_NAME = "defaultBeanConfigurator";

	void configure(BeanConfig beanConfig, Object bean);

}
