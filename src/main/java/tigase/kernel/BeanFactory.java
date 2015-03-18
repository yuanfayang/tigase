package tigase.kernel;

public interface BeanFactory<T> {

	T createInstance() throws KernelException;

}
