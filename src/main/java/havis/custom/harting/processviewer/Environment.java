package havis.custom.harting.processviewer;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Environment {

	private final static Logger log = Logger.getLogger(Environment.class.getName());
	private final static Properties properties = new Properties();

	static {
		try (InputStream stream = Environment.class.getClassLoader().getResourceAsStream("havis.custom.harting.processviewer.properties")) {
			properties.load(stream);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Failed to load environment properties", e);
		}
	}

	public static final String LOCK = properties.getProperty("havis.custom.harting.processviewer.lock", "conf/havis/custom/harting/processviewer/lock");
	public static final String SPEC = properties.getProperty("havis.custom.harting.processviewer.spec", "conf/havis/custom/harting/processviewer/spec");

	public static final String JDBC_URL = properties.getProperty("havis.custom.harting.processviewer.jdbcUrl",
			"jdbc:h2:./processviewer;INIT=RUNSCRIPT FROM 'conf/havis/custom/harting/processviewer/history.sql'");
	public static final String JDBC_DRIVER = properties.getProperty("havis.custom.harting.processviewer.jdbcDriver", "org.h2.Driver");
	public static final String JDBC_USERNAME = properties.getProperty("havis.custom.harting.processviewer.jdbcUsername", "sa");
	public static final String JDBC_PASSWORD = properties.getProperty("havis.custom.harting.processviewer.jdbcPassword", "");
	public static final int MAX_RECORD_COUNT = Integer.valueOf(properties.getProperty("havis.custom.harting.processviewer.maxRecordCount", "1000"));
	public static final String CUSTOM_CONFIG_FILE = properties.getProperty("havis.custom.harting.processviewer.customConfigFile",
			"conf/havis/custom/harting/processviewer/config.json");
	public static final String DEFAULT_CONFIG_FILE = properties.getProperty("havis.custom.harting.processviewer.defaultConfigFile",
			"havis/custom/harting/processviewer/config/default.json");
	public static final String CUSTOM_ICON_FILE = properties.getProperty("havis.custom.harting.processviewer.customIconsFile",
			"conf/havis/custom/harting/processviewer/icons.json");
	public static final String DEFAULT_ICON_FILE = properties.getProperty("havis.custom.harting.processviewer.defaultIconsFile",
			"havis/custom/harting/processviewer/config/defaultIcons.json");
	public static final String EC_TEMPLATE = properties.getProperty("havis.custom.harting.processviewer.ecTemplateFile",
			"havis/custom/harting/processviewer/config/ecTemplate.json");
	public static final String LR_TEMPLATE = properties.getProperty("havis.custom.harting.processviewer.lrTemplateFile",
			"havis/custom/harting/processviewer/config/lrTemplate.json");

	public static final String REPORT_PORT = properties.getProperty("havis.custom.harting.processviewer.reportPort", "9898");

}