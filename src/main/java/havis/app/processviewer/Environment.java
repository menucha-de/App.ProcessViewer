package havis.app.processviewer;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Environment {

	private final static Logger log = Logger.getLogger(Environment.class.getName());
	private final static Properties properties = new Properties();

	static {
		try (InputStream stream = Environment.class.getClassLoader().getResourceAsStream("havis.app.processviewer.properties")) {
			properties.load(stream);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Failed to load environment properties", e);
		}
	}

	public static final String LOCK = properties.getProperty("havis.app.processviewer.lock", "conf/havis/app/processviewer/lock");
	public static final String SPEC = properties.getProperty("havis.app.processviewer.spec", "conf/havis/app/processviewer/spec");

	public static final String JDBC_URL = properties.getProperty("havis.app.processviewer.jdbcUrl",
			"jdbc:h2:./processviewer;INIT=RUNSCRIPT FROM 'conf/havis/app/processviewer/history.sql'");
	public static final String JDBC_DRIVER = properties.getProperty("havis.app.processviewer.jdbcDriver", "org.h2.Driver");
	public static final String JDBC_USERNAME = properties.getProperty("havis.app.processviewer.jdbcUsername", "sa");
	public static final String JDBC_PASSWORD = properties.getProperty("havis.app.processviewer.jdbcPassword", "");
	public static final int MAX_RECORD_COUNT = Integer.valueOf(properties.getProperty("havis.app.processviewer.maxRecordCount", "1000"));
	public static final String CUSTOM_CONFIG_FILE = properties.getProperty("havis.app.processviewer.customConfigFile",
			"conf/havis/app/processviewer/config.json");
	public static final String DEFAULT_CONFIG_FILE = properties.getProperty("havis.app.processviewer.defaultConfigFile",
			"havis/app/processviewer/config/default.json");
	public static final String CUSTOM_ICON_FILE = properties.getProperty("havis.app.processviewer.customIconsFile",
			"conf/havis/app/processviewer/icons.json");
	public static final String DEFAULT_ICON_FILE = properties.getProperty("havis.app.processviewer.defaultIconsFile",
			"havis/app/processviewer/config/defaultIcons.json");
	public static final String EC_TEMPLATE = properties.getProperty("havis.app.processviewer.ecTemplateFile",
			"havis/app/processviewer/config/ecTemplate.json");
	public static final String LR_TEMPLATE = properties.getProperty("havis.app.processviewer.lrTemplateFile",
			"havis/app/processviewer/config/lrTemplate.json");

	public static final String REPORT_PORT = properties.getProperty("havis.app.processviewer.reportPort", "9898");

}