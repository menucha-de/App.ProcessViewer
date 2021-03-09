package havis.custom.harting.processviewer;

import havis.custom.harting.processviewer.rest.RestAleServiceClient;
import havis.middleware.ale.base.exception.ImplementationException;
import havis.middleware.ale.base.exception.NoSuchIdException;
import havis.middleware.ale.base.exception.NoSuchPathException;
import havis.middleware.ale.base.exception.SecurityException;
import havis.middleware.ale.service.ec.ECReportSpec;
import havis.middleware.ale.service.ec.ECSpec;
import havis.middleware.ale.service.ec.ECSpec.LogicalReaders;
import havis.middleware.ale.service.lr.LRProperty;
import havis.middleware.ale.service.lr.LRSpec;
import havis.middleware.ale.service.lr.LRSpec.Properties;
import havis.middleware.ale.service.lr.LRSpec.Readers;
import havis.middleware.ale.service.mc.MCEventCycleSpec;
import havis.middleware.ale.service.mc.MCLogicalReaderSpec;
import havis.middleware.ale.service.mc.MCSubscriberSpec;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SpecManager {

	private RestAleServiceClient aleClient;
	private MCLogicalReaderSpec lrTemplate = null;
	private MCEventCycleSpec ecTemplate = null;
	private String user = null;
	private String passwd = null;
	ObjectMapper mapper = new ObjectMapper();

	public SpecManager(String aleBaseUri, String user, String pwd) throws Exception {
		String aleUri = aleBaseUri + "/Apps/rest";
		if (aleBaseUri.contains("localhost")) {
			aleUri = aleBaseUri + "/rest";
		}
		this.setUser(user);
		this.setPasswd(pwd);
		aleClient = new RestAleServiceClient(aleUri, user, pwd);
		resetTemplates(null);
	}

	public void createLrSpec(String name, String host, String type, int antenna, String baseReader) throws Exception {
		URI uri = null;

		if (host != null) {
			uri = new URI(host);
		}
		for (MCLogicalReaderSpec spec : aleClient.getLRSpecs()) {
			if (name.equals(spec.getName())) {
				throw new CustomSpecException("Reader already exists");
			}
		}

		LRSpec lrSpec = lrTemplate.getSpec();
		lrSpec.setCreationDate(new Date());
		if (baseReader != null) {
			lrSpec.setIsComposite(true);
			lrSpec.setReaders(new Readers());
			List<String> reader = lrSpec.getReaders().getReader();
			reader.add(baseReader);
			if (antenna != 0) {
				lrSpec.setProperties(new Properties());
				LRProperty property = new LRProperty();
				property.setName("AntennaID");
				property.setValue("" + antenna);
				lrSpec.getProperties().getProperty().add(property);
			}
		} else {
			lrSpec.setIsComposite(false);
			Properties properties = new Properties();
			LRProperty property = new LRProperty();
			property.setName("ReaderType");
			property.setValue(type);
			properties.getProperty().add(property);
			property = new LRProperty();
			property.setName("Connector.ConnectionType");
			if (uri.getScheme().equals("tcp")) {
				property.setValue("TCP");
			}
			properties.getProperty().add(property);
			property = new LRProperty();
			property.setName("Connector.Host");
			property.setValue(uri.getHost());
			properties.getProperty().add(property);
			property = new LRProperty();
			property.setName("Connector.Port");
			property.setValue("" + uri.getPort());
			properties.getProperty().add(property);
			lrSpec.setProperties(properties);
		}
		lrTemplate.setName(name);
		aleClient.addLRSpec(lrTemplate);
		resetTemplates(lrTemplate);

	}

	public void createEcSpec(String name, String compositeReader) throws Exception {
		for (MCEventCycleSpec spec : aleClient.getECSpecs()) {
			if (name.equals(spec.getName())) {
				throw new CustomSpecException("Cycle already exists");
			}
		}
		ECSpec ecSpec = ecTemplate.getSpec();
		ecSpec.setCreationDate(new Date());
		LogicalReaders logicalReaders = new LogicalReaders();
		logicalReaders.getLogicalReader().add(compositeReader);
		ecSpec.setLogicalReaders(logicalReaders);
		ECReportSpec reportSpec = ecSpec.getReportSpecs().getReportSpec().get(0);
		reportSpec.setReportName(name);
		ecTemplate.setName(name);
		aleClient.addECSpec(ecTemplate);
		resetTemplates(ecTemplate);
	}

	public void subscribe(String ecName) throws CustomSpecException, URISyntaxException, MalformedURLException, ImplementationException, NoSuchIdException,
			NoSuchPathException, SecurityException, IOException {
		String host = "tcp://" + aleClient.getLocalHostAddress() + ":" + Environment.REPORT_PORT;
		URI uri = new URI(host);
		String ecId = null;
		for (MCEventCycleSpec spec : aleClient.getECSpecs()) {
			if (ecName.equals(spec.getName())) {
				ecId = spec.getId();
			}
		}

		if (ecId == null) {
			throw new CustomSpecException("Unknown event cycle");
		}

		List<MCSubscriberSpec> subscribers = aleClient.getSubscibers(ecId);
		if (subscribers != null && subscribers.size() > 0) {
			for (MCSubscriberSpec subscriber : subscribers) {
				if (subscriber.getUri().equals(uri.toString())) {
					return;
				}
			}
		}

		MCSubscriberSpec subscriber = new MCSubscriberSpec();
		subscriber.setCreationDate(new Date());
		subscriber.setEnable(true);
		subscriber.setName(host);
		subscriber.setUri(uri.toString());
		aleClient.addSubscriber(ecId, subscriber);
	}

	public void removeLrSpec(String name) throws CustomSpecException, MalformedURLException, ImplementationException, NoSuchIdException, NoSuchPathException,
			SecurityException, IOException, URISyntaxException {
		if (!name.equals("BuiltIn")) {
			String lrId = null;
			for (MCLogicalReaderSpec spec : aleClient.getLRSpecs()) {
				if (name.equals(spec.getName())) {
					lrId = spec.getId();
				}
			}
			if (lrId != null) {
				aleClient.deleteLRSpec(lrId);
			}
		} else {
			throw new CustomSpecException("BuiltIn is not delete able");
		}
	}

	public void removeEcSpec(String name) throws MalformedURLException, ImplementationException, NoSuchIdException, NoSuchPathException, SecurityException,
			IOException, URISyntaxException {
		String ecId = null;
		for (MCEventCycleSpec spec : aleClient.getECSpecs()) {
			if (name.equals(spec.getName())) {
				ecId = spec.getId();
			}
		}
		if (ecId != null) {
			aleClient.deleteECSpec(ecId);
		}
	}

	private void resetTemplates(Object template) throws Exception {
		if (template == ecTemplate || template == null) {
			ecTemplate = mapper.readValue(SpecManager.class.getClassLoader().getResourceAsStream(Environment.EC_TEMPLATE), MCEventCycleSpec.class);
		}
		if (template == lrTemplate || template == null) {
			lrTemplate = mapper.readValue(SpecManager.class.getClassLoader().getResourceAsStream(Environment.LR_TEMPLATE), MCLogicalReaderSpec.class);
		}
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

}
