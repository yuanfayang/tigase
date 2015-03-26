package tigase.kernel.configbuilder;

public interface TypeBeanBuilder {

	public BeanFactoryBuilder asClass(Class<?> cls);

	public ConfigExecutorBuilder asInstance(Object bean);
}
