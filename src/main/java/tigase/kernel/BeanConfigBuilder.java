package tigase.kernel;

import tigase.kernel.BeanConfig.State;
import tigase.kernel.configbuilder.BeanFactoryBuilder;
import tigase.kernel.configbuilder.ConfigExecutorBuilder;
import tigase.kernel.configbuilder.TypeBeanBuilder;

public class BeanConfigBuilder implements ConfigExecutorBuilder, TypeBeanBuilder, BeanFactoryBuilder {

	private BeanConfig beanConfig;

	private Object beanInstance;

	private final String beanName;

	private final DependencyManager dependencyManager;

	private BeanConfig factoryBeanConfig;

	private final Kernel kernel;

	BeanConfigBuilder(Kernel kernel, DependencyManager dependencyManager, String beanName) {
		this.kernel = kernel;
		this.dependencyManager = dependencyManager;
		this.beanName = beanName;
	}

	@Override
	public BeanConfigBuilder asClass(Class<?> cls) {
		if (this.beanConfig != null)
			throwException(new KernelException("Class or instance is already defined for bean '" + beanName + "'"));

		this.beanConfig = dependencyManager.createBeanConfig(beanName, cls);
		return this;
	}

	@Override
	public BeanConfigBuilder asInstance(Object bean) {
		if (this.beanConfig != null)
			throwException(new KernelException("Class or instance is already defined for bean '" + beanName + "'"));

		this.beanConfig = dependencyManager.createBeanConfig(beanName, bean.getClass());
		this.beanInstance = bean;
		return this;
	}

	@Override
	public void exec() {
		if (factoryBeanConfig != null) {
			kernel.unregisterInt(factoryBeanConfig.getBeanName());
			dependencyManager.register(factoryBeanConfig);
		}
		kernel.unregisterInt(beanConfig.getBeanName());
		dependencyManager.register(beanConfig);

		if (beanInstance != null) {
			kernel.getBeanInstances().put(beanConfig, beanInstance);
			beanConfig.setState(State.initialized);
		}

		kernel.currentlyUsedConfigBuilder = null;
		kernel.injectIfRequired(beanConfig);
	}

	public String getBeanName() {
		return beanName;
	}

	protected void throwException(KernelException e) {
		kernel.currentlyUsedConfigBuilder = null;
		throw e;
	}

	@Override
	public BeanConfigBuilder withFactory(Class<?> beanFactoryClass) {
		if (beanInstance != null)
			throwException(new KernelException("Cannot register factory to bean '" + beanName + "' registered as instance."));
		if (factoryBeanConfig != null)
			throwException(new KernelException("Factory for bean '" + beanName + "' is already registered."));

		this.factoryBeanConfig = dependencyManager.createBeanConfig(beanName + "#FACTORY", beanFactoryClass);
		beanConfig.setFactory(factoryBeanConfig);

		return this;
	}

}
