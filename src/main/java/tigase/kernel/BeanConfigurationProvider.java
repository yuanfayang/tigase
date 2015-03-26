package tigase.kernel;

import java.util.Map;

public interface BeanConfigurationProvider {

	Map<String, Object> getConfiguration(BeanConfig beanConfig);

}
