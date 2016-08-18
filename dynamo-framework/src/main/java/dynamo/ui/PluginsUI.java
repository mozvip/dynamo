package dynamo.ui;

import java.io.IOException;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.collect.ImmutableList;

import dynamo.core.EventManager;
import dynamo.core.configuration.ClassDescription;
import dynamo.core.manager.ConfigAnnotationManager;
import dynamo.core.manager.ConfigurationManager;
import dynamo.core.model.Task;
import dynamo.core.model.TaskExecutor;

@ManagedBean(name="plugins")
@RequestScoped
public class PluginsUI extends DynamoManagedBean {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Map<Class<? extends Task>, Class<? extends TaskExecutor>> activePlugins = ConfigurationManager.getInstance().getActivePlugins();

	public Map<Class<? extends Task>, Class<? extends TaskExecutor>> getActivePlugins() {
		return activePlugins;
	}
	
	public ImmutableList<Map.Entry> getPluginsWithOptions() {
		return ImmutableList.<Map.Entry>builder().addAll( ConfigurationManager.getInstance().getPluginOptions().entrySet() ).build();
	}
	
	public void save() throws JsonGenerationException, JsonMappingException, IOException {
		for (Map.Entry<Class<? extends Task>, Class<? extends TaskExecutor>> entry : activePlugins.entrySet()) {
			ConfigurationManager.getInstance().setActivePlugin(entry.getKey(), entry.getValue());
			ConfigAnnotationManager.getInstance().persistConfiguration();
		}
		EventManager.getInstance().reportSuccess("Plugins set successfully");
	}
	
	public String getClassDescription( Class klass ) {
		ClassDescription annotation = (ClassDescription) klass.getAnnotation(ClassDescription.class);
		if (annotation != null) {
			return annotation.label();
		}
		return klass.getName();
	}

}
