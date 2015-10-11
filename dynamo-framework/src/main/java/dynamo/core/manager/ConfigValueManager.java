package dynamo.core.manager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigValueManager {

	private static Map<String, Object> mockedConfiguration = new HashMap<>();
	private static Map<String, String> configuration = new HashMap<>();

	private static boolean mockedConfig = false;

	static class SingletonHolder {
		static ConfigValueManager instance = new ConfigValueManager();
	}

	public static ConfigValueManager getInstance() {
		return SingletonHolder.instance;
	}

	private ConfigValueManager() {
		try {
			Path p = Paths.get("config.json");
			if (Files.isReadable(p)) {
				try (InputStream input = Files.newInputStream(p)) {
					configuration = configurationMapper.readValue(input, Map.class);
				}
			}
		} catch (IOException e) {
			ErrorManager.getInstance().reportThrowable(e);
		}
	}

	private ObjectMapper configurationMapper = new ObjectMapper();

	public synchronized void persistConfiguration() throws JsonGenerationException, JsonMappingException, IOException {
		configurationMapper.writeValue(new File("config.json"), configuration);
	}

	public String getConfigString(String key, String defaultValue) {
		if (mockedConfig) {
			if (mockedConfiguration.containsKey(key)) {
				return mockedConfiguration.get(key).toString();
			}
			return null;
		}
		if (configuration.containsKey(key)) {
			return configuration.get(key);
		} else if (defaultValue != null) {
			configuration.put(key, defaultValue);
		}
		return defaultValue;
	}

	public static void mockConfiguration(String key, Object value) {
		mockedConfiguration.put(key, value);
		mockedConfig = true;
	}

	public int getConfigInt(String key, int i) {
		String value = getConfigString(key, "" + i);
		return Integer.parseInt(value);
	}

	public String getConfigString(String key) {
		return getConfigString(key, null);
	}

	public void setConfigString(String key, String value) {
		if (mockedConfig) {
			mockedConfiguration.put(key, value);
		} else {
			configuration.put(key, value);
		}
	}

}
