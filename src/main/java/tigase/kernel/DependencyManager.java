package tigase.kernel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DependencyManager {

	private static Field[] getAllFields(Class<?> klass) {
		List<Field> fields = new ArrayList<Field>();
		fields.addAll(Arrays.asList(klass.getDeclaredFields()));
		if (klass.getSuperclass() != null) {
			fields.addAll(Arrays.asList(getAllFields(klass.getSuperclass())));
		}
		return fields.toArray(new Field[] {});
	}

	private final Map<String, BeanConfig> beanConfigs = new HashMap<String, BeanConfig>();

	protected BeanConfig createBeanConfig(final String beanName, final Class<?> beanClass) {
		BeanConfig result = new BeanConfig(beanName, beanClass);

		prepareDependencies(result);

		return result;
	}

	private Map<Field, Inject> createFieldsDependencyList(final Class<?> cls) {
		Map<Field, Inject> deps = new HashMap<Field, Inject>();
		for (Field field : getAllFields(cls)) {
			Inject injectAnnotation = field.getAnnotation(Inject.class);
			if (injectAnnotation != null) {
				deps.put(field, injectAnnotation);
			}
		}
		return deps;
	}

	public BeanConfig[] getBeanConfig(Dependency dependency) {
		if (dependency.getBeanName() != null) {
			return new BeanConfig[] { beanConfigs.get(dependency.getBeanName()) };
		} else if (dependency.getType() != null) {
			List<BeanConfig> bcs = getBeanConfigs(dependency.getType());
			return bcs.toArray(new BeanConfig[] {});
		} else
			throw new RuntimeException("Unsupported dependecy type.");
	}

	public BeanConfig getBeanConfig(String beanName) {
		return this.beanConfigs.get(beanName);
	}

	public Collection<BeanConfig> getBeanConfigs() {
		return Collections.unmodifiableCollection(beanConfigs.values());
	}

	public List<BeanConfig> getBeanConfigs(Class<?> type) {
		ArrayList<BeanConfig> result = new ArrayList<BeanConfig>();
		for (BeanConfig bc : beanConfigs.values()) {
			if (type.isAssignableFrom(bc.getClazz())) {
				result.add(bc);
			}
		}
		return result;
	}

	public Collection<Dependency> getDependenciesTo(BeanConfig destination) {
		HashSet<Dependency> result = new HashSet<Dependency>();
		for (BeanConfig candidate : beanConfigs.values()) {
			for (Dependency dp : candidate.getFieldDependencies().values()) {
				List<BeanConfig> bcs = Arrays.asList(getBeanConfig(dp));
				if (bcs.contains(destination)) {
					result.add(dp);
				}
			}
		}
		return result;
	}

	public HashSet<BeanConfig> getDependentBeans(final BeanConfig beanConfig) {
		HashSet<BeanConfig> result = new HashSet<BeanConfig>();
		for (BeanConfig candidate : beanConfigs.values()) {
			for (Dependency dp : candidate.getFieldDependencies().values()) {
				List<BeanConfig> bcs = Arrays.asList(getBeanConfig(dp));
				if (bcs.contains(beanConfig)) {
					result.add(candidate);
				}
			}
		}
		return result;
	}

	public boolean isBeanClassRegistered(String beanName) {
		return beanConfigs.containsKey(beanName);
	}

	protected void prepareDependencies(BeanConfig beanConfig) {
		final String id = beanConfig.getBeanName();
		final Class<?> cls = beanConfig.getClazz();

		Map<Field, Inject> deps = createFieldsDependencyList(cls);
		for (Entry<Field, Inject> e : deps.entrySet()) {
			Dependency d = new Dependency(beanConfig);
			d.setField(e.getKey());
			d.setNullAllowed(e.getValue().nullAllowed());
			if (!e.getValue().bean().isEmpty()) {
				d.setBeanName(e.getValue().bean());
			} else if (e.getValue().type() != Inject.EMPTY.class) {
				d.setType(e.getValue().type());
			} else if (e.getKey().getType().isArray()) {
				d.setType(e.getKey().getType().getComponentType());
			} else {
				d.setType(e.getKey().getType());
			}

			beanConfig.getFieldDependencies().put(e.getKey(), d);
		}
	}

	public BeanConfig registerBeanClass(final String beanName, final Class<?> beanClass) {
		BeanConfig c = createBeanConfig(beanName, beanClass);

		beanConfigs.put(beanName, c);

		return c;
	}

	public BeanConfig unregister(String beanName) {
		return beanConfigs.remove(beanName);
	}
}
