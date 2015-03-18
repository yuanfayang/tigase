package tigase.kernel;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import tigase.kernel.BeanConfig.State;

public class Kernel {

	static String prepareAccessorMainPartName(final String fieldName) {
		if (fieldName.length() == 1) {
			return fieldName.toUpperCase();
		}

		String r;
		if (Character.isUpperCase(fieldName.charAt(1))) {
			r = fieldName.substring(0, 1);
		} else {
			r = fieldName.substring(0, 1).toUpperCase();
		}

		r += fieldName.substring(1);

		return r;
	}

	private final Map<BeanConfig, Object> beanInstances = new HashMap<BeanConfig, Object>();

	private final DependencyManager dependencyManager = new DependencyManager();

	protected final Logger log = Logger.getLogger(this.getClass().getName());

	private String name;

	public Kernel() {
		this.name = "<unknown>";
	}

	public Kernel(String name) {
		this.name = name;
	}

	private Object createNewInstance(BeanConfig beanConfig) {
		try {
			if (beanConfig.getFactory() != null) {
				BeanFactory<?> factory = (BeanFactory<?>) beanInstances.get(beanConfig.getFactory());
				return factory.createInstance();
			} else {
				if (log.isLoggable(Level.FINER))
					log.finer("[" + getName() + "] Creating instance of bean " + beanConfig.getBeanName());
				Class<?> clz = beanConfig.getClazz();

				return clz.newInstance();
			}
		} catch (Exception e) {
			throw new KernelException("Can't create instance of bean '" + beanConfig.getBeanName() + "'", e);
		}
	}

	DependencyManager getDependencyManager() {
		return dependencyManager;
	}

	@SuppressWarnings("unchecked")
	public <T> T getInstance(Class<T> beanClass) {
		// if (!initialized)
		// init();

		final List<BeanConfig> bcs = dependencyManager.getBeanConfigs(beanClass);

		if (bcs.size() > 1)
			throw new KernelException("Too many beans implemented class " + beanClass);
		else if (bcs.isEmpty())
			throw new KernelException("Can't find bean implementing  class " + beanClass);

		BeanConfig bc = bcs.get(0);

		if (bc.getState() != State.initialized) {
			try {
				initBean(bc, new HashSet<BeanConfig>(), 0);
			} catch (Exception e) {
				e.printStackTrace();
				throw new KernelException(e);
			}
		}

		Object result = beanInstances.get(bc);

		return (T) result;
	}

	@SuppressWarnings("unchecked")
	public <T> T getInstance(String beanName) {
		final BeanConfig bc = dependencyManager.getBeanConfig(beanName);

		if (bc == null)
			throw new KernelException("Unknown bean '" + beanName + "'.");

		if (bc.getState() != State.initialized) {
			try {
				initBean(bc, new HashSet<BeanConfig>(), 0);
			} catch (Exception e) {
				e.printStackTrace();
				throw new KernelException(e);
			}
		}

		Object result = beanInstances.get(bc);

		return (T) result;
	}

	public String getName() {
		return name;
	}

	public Collection<String> getNamesOf(Class<?> beanType) {
		ArrayList<String> result = new ArrayList<String>();
		List<BeanConfig> bcs = dependencyManager.getBeanConfigs(beanType);
		for (BeanConfig beanConfig : bcs) {
			result.add(beanConfig.getBeanName());
		}
		return Collections.unmodifiableCollection(result);
	}

	public void initAll() {
		try {
			for (BeanConfig bc : dependencyManager.getBeanConfigs()) {
				if (bc.getState() != State.initialized) {
					initBean(bc, new HashSet<BeanConfig>(), 0);
				}
			}
		} catch (Exception e) {
			throw new KernelException("Can't initialize all beans", e);
		}
	}

	private void initBean(BeanConfig beanConfig, Set<BeanConfig> createdBeansConfig, int deep) throws IllegalAccessException,
	IllegalArgumentException, InvocationTargetException, InstantiationException {

		System.out.println("INIT " + beanConfig);

		if (beanConfig.getState() == State.initialized)
			return;

		Object bean;
		if (beanConfig.getState() == State.registered) {
			beanConfig.setState(State.instanceCreated);
			if (beanConfig.getFactory() != null && beanConfig.getFactory().getState() != State.initialized) {
				initBean(beanConfig.getFactory(), new HashSet<BeanConfig>(), 0);
			}
			bean = createNewInstance(beanConfig);
			beanInstances.put(beanConfig, bean);
			createdBeansConfig.add(beanConfig);
		} else {
			bean = beanInstances.get(beanConfig);
		}

		for (final Dependency dep : beanConfig.getFieldDependencies().values()) {
			injectDependencies(bean, dep, createdBeansConfig, deep);
		}

		if (deep == 0) {
			for (BeanConfig bc : createdBeansConfig) {
				Object bi = beanInstances.get(bc);
				bc.setState(State.initialized);
				if (bi instanceof Initializable) {
					((Initializable) bi).initialize();
				}
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void inject(Object[] data, Dependency dependency, Object toBean) throws IllegalAccessException,
	IllegalArgumentException, InvocationTargetException, InstantiationException {

		if (!dependency.isNullAllowed() && data == null)
			throw new KernelException("Can't inject <null> to field " + dependency.getField());

		if (data == null) {
			Method setter = prepareSetterMethod(dependency.getField());
			setter.invoke(toBean, (Object) null);
		} else if (Collection.class.isAssignableFrom(dependency.getField().getType())) {
			Collection o;

			if (!dependency.getField().getType().isInterface()) {
				o = (Collection) dependency.getField().getType().newInstance();
			} else if (dependency.getField().getType().isAssignableFrom(Set.class)) {
				o = new HashSet();
			} else {
				o = new ArrayList();
			}

			o.addAll(Arrays.asList(data));

			Method setter = prepareSetterMethod(dependency.getField());
			setter.invoke(toBean, o);
		} else {
			Object o;
			if (data != null && dependency.getField().getType().equals(data.getClass())) {
				o = data;
			} else {
				int l = Array.getLength(data);
				if (l > 1)
					throw new KernelException("Can't put many objects to single field " + dependency.getField());
				if (l == 0)
					o = null;
				else
					o = Array.get(data, 0);
			}

			Method setter = prepareSetterMethod(dependency.getField());
			setter.invoke(toBean, o);
		}
	}

	private void injectDependencies(Object bean, Dependency dep, Set<BeanConfig> createdBeansConfig, int deep)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
		BeanConfig[] dependentBeansConfigs = dependencyManager.getBeanConfig(dep);
		ArrayList<Object> dataToInject = new ArrayList<Object>();

		for (BeanConfig b : dependentBeansConfigs) {
			if (!beanInstances.containsKey(b)) {
				initBean(b, createdBeansConfig, deep + 1);
			}
			Object beanToInject = beanInstances.get(b);
			// if (beanToInject != null)
			dataToInject.add(beanToInject);
		}
		Object[] d;
		if (dataToInject.size() > 1 && dep.getType() != null) {
			Object[] z = (Object[]) Array.newInstance(dep.getType(), 1);
			d = dataToInject.toArray(z);
		} else
			d = dataToInject.toArray();

		if (log.isLoggable(Level.FINER))
			log.finer("[" + getName() + "] Injecting " + Arrays.toString(d) + " to " + dep.getBeanConfig() + "#" + dep);

		inject(d, dep, bean);

	}

	private void injectIfRequired(final BeanConfig beanConfig) {
		try {
			Collection<Dependency> dps = dependencyManager.getDependenciesTo(beanConfig);
			for (Dependency dep : dps) {
				BeanConfig depbc = dep.getBeanConfig();

				if (depbc.getState() == State.initialized) {
					if (beanConfig.getState() != State.initialized)
						initBean(beanConfig, new HashSet<BeanConfig>(), 0);
					Object bean = beanInstances.get(depbc);

					injectDependencies(bean, dep, new HashSet<BeanConfig>(), 0);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new KernelException("Can't inject bean " + beanConfig + " to dependend beans.", e);
		}
	}

	public boolean isBeanClassRegistered(String beanName) {
		return dependencyManager.isBeanClassRegistered(beanName);
	}

	private Method prepareSetterMethod(Field f) {
		String t = prepareAccessorMainPartName(f.getName());
		String sm;
		@SuppressWarnings("unused")
		String gm;
		if (f.getType().isPrimitive() && f.getType().equals(boolean.class)) {
			sm = "set" + t;
			gm = "is" + t;
		} else {
			sm = "set" + t;
			gm = "get" + t;
		}

		try {
			Method m = f.getDeclaringClass().getMethod(sm, f.getType());
			return m;
		} catch (NoSuchMethodException e) {
			throw new KernelException("Class " + f.getDeclaringClass().getName() + " has no setter of field " + f.getName(), e);
		}
	}

	public void registerBean(String beanName, Object bean) {
		if (log.isLoggable(Level.FINER))
			log.finer("[" + getName() + "] Registering bean " + beanName);

		unregisterInt(beanName);

		BeanConfig bc = dependencyManager.registerBeanClass(beanName, bean.getClass());
		bc.setState(State.initialized);
		beanInstances.put(bc, bean);

		injectIfRequired(bc);
	}

	public void registerBeanClass(String beanName, Class<?> beanClass) {
		if (log.isLoggable(Level.FINER))
			log.finer("[" + getName() + "] Registering bean " + beanName + " with class " + beanClass);

		unregisterInt(beanName);

		BeanConfig bc = dependencyManager.registerBeanClass(beanName, beanClass);
		bc.setState(State.registered);

		injectIfRequired(bc);
	}

	public void registerBeanClass(String beanName, Class<?> beanClass, Class<? extends BeanFactory<?>> beanFactoryClass) {
		if (log.isLoggable(Level.FINER))
			log.finer("[" + getName() + "] Registering bean " + beanName + " with class " + beanClass);

		unregisterInt(beanName);

		BeanConfig bc = dependencyManager.registerBeanClass(beanName, beanClass);
		bc.setState(State.registered);

		if (beanFactoryClass != null) {
			BeanConfig bfc = dependencyManager.registerBeanClass(beanName + "#FACTORY", beanFactoryClass);
			bfc.setState(State.registered);
			bc.setFactory(bfc);
		}

		injectIfRequired(bc);
	}

	public void setName(String name) {
		this.name = name;
	}

	public void unregister(final String beanName) {
		if (log.isLoggable(Level.FINER))
			log.finer("[" + getName() + "] Unregistering bean " + beanName);
		unregisterInt(beanName);
		try {
			for (BeanConfig bc : dependencyManager.getBeanConfigs()) {
				if (bc.getState() != State.initialized)
					continue;
				Object ob = beanInstances.get(bc);
				for (Dependency d : bc.getFieldDependencies().values()) {
					BeanConfig[] cbcs = dependencyManager.getBeanConfig(d);

					if (cbcs.length == 1) {// Clearing single-instance
						// dependency. Like single field.
						// BeanConfig cbc = cbcs[0];
						// if (cbc != null && cbc.equals(removingBC)) {
						inject(null, d, ob);
						// }
					} else if (cbcs.length > 1) { // Clearing multi-instance
						// dependiency. Like
						// collections and arrays.

						injectDependencies(ob, d, new HashSet<BeanConfig>(), 0);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new KernelException("Can't unregister bean", e);
		} finally {
			dependencyManager.unregister(beanName);
		}
	}

	private void unregisterInt(String beanName) {
		if (dependencyManager.isBeanClassRegistered(beanName)) {
			// unregistering
			if (log.isLoggable(Level.FINER))
				log.finer("[" + getName() + "] Found registred bean " + beanName + ". Unregistering...");

			BeanConfig oldBeanConfig = dependencyManager.unregister(beanName);
			Object i = beanInstances.remove(oldBeanConfig);
			if (i != null && i instanceof UnregisterAware) {
				try {
					((UnregisterAware) i).beforeUnregister();
				} catch (Exception e) {
					e.printStackTrace();
					log.log(Level.WARNING, "Problem during unregistering bean", e);
				}
			}
		}
	}
}
