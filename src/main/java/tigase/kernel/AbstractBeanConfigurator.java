package tigase.kernel;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public abstract class AbstractBeanConfigurator implements BeanConfigurator {

	@Override
	public void configure(BeanConfig beanConfig, Object bean) throws KernelException {
		try {
			Map<String, Object> ccc = getConfiguration(beanConfig);

			final Field[] fields = DependencyManager.getAllFields(beanConfig.getClazz());
			for (Field field : fields) {
				if (!ccc.containsKey(field.getName()))
					continue;

				Object valueToSet = ccc.get(field.getName());

				Method setter = Kernel.prepareSetterMethod(field);
				if (setter != null) {
					setter.invoke(bean, valueToSet);
				} else {
					field.setAccessible(true);
					field.set(bean, valueToSet);
				}
			}
		} catch (Exception e) {
			throw new KernelException("Cannot inject configuration to bean " + beanConfig.getBeanName(), e);
		}
	}

	protected abstract Map<String, Object> getConfiguration(BeanConfig beanConfig);

}
