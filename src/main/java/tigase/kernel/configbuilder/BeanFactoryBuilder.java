package tigase.kernel.configbuilder;

import tigase.kernel.BeanConfigBuilder;

public interface BeanFactoryBuilder extends ConfigExecutorBuilder {

	public BeanConfigBuilder withFactory(Class<?> beanFactoryClass);

}
