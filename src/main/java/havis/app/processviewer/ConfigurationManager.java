package havis.app.processviewer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigurationManager {
	private final static ObjectMapper mapper = new ObjectMapper();
	private Configuration configuration;

	public ConfigurationManager() throws ConfigurationManagerException {
		try {
			File configFile = new File(Environment.CUSTOM_CONFIG_FILE);
			if (configFile.exists()) {
				this.configuration = mapper.readValue(new File(
						Environment.CUSTOM_CONFIG_FILE), Configuration.class);
			} else {
				this.configuration = mapper.readValue(
						ConfigurationManager.class.getClassLoader()
								.getResourceAsStream(
										Environment.DEFAULT_CONFIG_FILE),
						Configuration.class);
			}
		} catch (Exception e) {
			throw new ConfigurationManagerException(e);
		}
	}

	public Configuration get() {
		return this.configuration;
	}

	public void set(Configuration configuration)
			throws ConfigurationManagerException {
		try {
			File configFile = new File(Environment.CUSTOM_CONFIG_FILE);
			Files.createDirectories(configFile.toPath().getParent(),
					new FileAttribute<?>[] {});
			mapper.writerWithDefaultPrettyPrinter().writeValue(configFile,
					configuration);
			this.configuration = configuration;
		} catch (Exception e) {
			throw new ConfigurationManagerException(e);
		}
	}
}
