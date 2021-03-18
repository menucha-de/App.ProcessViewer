package havis.app.processviewer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ImageManager {
	private Map<String, String> images = new HashMap<String, String>();
	private final static ObjectMapper mapper = new ObjectMapper();

	@SuppressWarnings("unchecked")
	public ImageManager() throws ConfigurationManagerException {
		try {
			File imageFile = new File(Environment.CUSTOM_ICON_FILE);
			if (imageFile.exists()) {
				this.images = mapper.readValue(new File(
						Environment.CUSTOM_ICON_FILE), Map.class);
			} else {
				this.images = mapper.readValue(
						ConfigurationManager.class.getClassLoader()
								.getResourceAsStream(
										Environment.DEFAULT_ICON_FILE),
						Map.class);
			}
		} catch (Exception e) {
			throw new ConfigurationManagerException(e);
		}
	}

	public String getImage(String task) {
		return images.get(task);
	}

	public void addImage(String task, String image)
			throws ConfigurationManagerException {
		images.put(task, image);
		set();
	}
	
	public void removeImage(String task) throws ConfigurationManagerException{
		images.remove(task);
		set();
	}

	public void set() throws ConfigurationManagerException {
		try {
			File imageFile = new File(Environment.CUSTOM_ICON_FILE);
			Files.createDirectories(imageFile.toPath().getParent(),
					new FileAttribute<?>[] {});
			mapper.writerWithDefaultPrettyPrinter().writeValue(imageFile,
					images);
		} catch (Exception e) {
			throw new ConfigurationManagerException(e);
		}
	}
}
