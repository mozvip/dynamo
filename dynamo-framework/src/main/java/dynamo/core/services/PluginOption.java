package dynamo.core.services;

import java.util.List;

public class PluginOption {

	private LabelledClass taskClass;
	private List<LabelledClass> executorOptions;

	public PluginOption(LabelledClass taskClass, List<LabelledClass> executorOptions) {
		super();
		this.taskClass = taskClass;
		this.executorOptions = executorOptions;
	}

	public LabelledClass getTaskClass() {
		return taskClass;
	}

	public List<LabelledClass> getExecutorOptions() {
		return executorOptions;
	}

}
