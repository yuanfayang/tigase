package tigase.kernel;

import java.util.HashSet;

public class DependencyGrapher {

	private Kernel kernel;

	public DependencyGrapher() {
	}

	public DependencyGrapher(Kernel krnl) {
		setKernel(krnl);
	}

	public String getDependencyGraph() {
		StringBuilder sb = new StringBuilder();
		sb.append("digraph ").append(kernel.getName()).append(" {\n");

		for (BeanConfig bc : kernel.getDependencyManager().getBeanConfigs()) {
			sb.append('"').append(bc.getBeanName()).append('"').append("[");

			sb.append("label=<");
			sb.append(bc.getBeanName()).append("<br/>").append("(").append(bc.getClazz().getName()).append(")");

			sb.append(">");

			sb.append("];\n");
		}

		int c = 0;
		HashSet<String> tmp = new HashSet<String>();
		for (BeanConfig bc : kernel.getDependencyManager().getBeanConfigs()) {
			++c;
			for (Dependency dp : bc.getFieldDependencies().values()) {
				BeanConfig[] dBeans = kernel.getDependencyManager().getBeanConfig(dp);
				for (BeanConfig dBean : dBeans) {
					StringBuilder sbi = new StringBuilder();
					sbi.append('"').append(bc.getBeanName()).append('"');
					// sb.append(':').append(dp.getField().getName());
					sbi.append("->");
					if (dBean == null)
						sbi.append("{UNKNOWN_").append(c).append("[label=\"").append(dp).append(
								"\", fillcolor=red, style=filled, shape=box]}");
					else
						sbi.append('"').append(dBean.getBeanName()).append('"');

					tmp.add(sbi.toString());

				}
			}
		}

		for (String string : tmp) {
			sb.append(string).append('\n');
		}

		sb.append("}\n");
		return sb.toString();
	}

	public Kernel getKernel() {
		return kernel;
	}

	public void setKernel(Kernel kernel) {
		this.kernel = kernel;
	}

}
