package dynamo.core.services;

import java.util.List;

import dynamo.core.model.TaskExecutor;

public class PluginOption {

	private LabelledClass taskClass;
	private List<LabelledClass> executorOptions;
	private Class<? extends TaskExecutor> value;

	public PluginOption(LabelledClass taskClass, List<LabelledClass> executorOptions, Class<? extends TaskExecutor> value) {
		super();
		this.taskClass = taskClass;
		this.executorOptions = executorOptions;
		this.value = value;
	}

	public LabelledClass getTaskClass() {
		return taskClass;
	}

	public List<LabelledClass> getExecutorOptions() {
		return executorOptions;
	}
	
	public Class<? extends TaskExecutor> getValue() {
		return value;
	}

}
