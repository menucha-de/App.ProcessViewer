package havis.app.processviewer;

import havis.middleware.ale.service.ec.ECReport;
import havis.middleware.ale.service.ec.ECReportGroup;
import havis.middleware.ale.service.ec.ECReportGroupListMember;
import havis.middleware.ale.service.ec.ECReportMemberField;

import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;

public class TaskManager {
	private final static Logger log = Logger.getLogger(TaskManager.class.getName());
	private Map<String, SpecManager> aleHosts = new HashMap<String, SpecManager>();
	private Map<String, String> baseReaders = new HashMap<String, String>();
	private Map<String, ReadPoint> readPoints = new HashMap<String, ReadPoint>();

	private ConfigurationManager configManager;
	private Configuration config;

	private ReportListener handler;
	private TcpSubscriberHost host;

	private HistoryManager history;

	private Comparator<Task> taskComperator = new Comparator<Task>() {
		@Override
		public int compare(Task o1, Task o2) {
			return Integer.compare(o1.getPosition(), o2.getPosition());
		}
	};

	private class Reporter implements ReportListener {
		@Override
		public void fire(ECReport ecReport) throws JsonProcessingException, HistoryManagerException {
			List<ECReportGroup> group = ecReport.getGroup();
			if (group != null && group.size() > 0) {
				for (ECReportGroupListMember member : group.get(0).getGroupList().getMember()) {
					HistoryEntry entry = new HistoryEntry();
					entry.setEpc(member.getEpc().getValue());
					entry.setReadPoint(ecReport.getReportName());
					entry.setTime(new Date().getTime());
					List<ECReportMemberField> memberFields = member.getExtension().getFieldList().getField();
					int format = -1;
					for (ECReportMemberField memberField : memberFields) {
						Field field = new Field();
						String name = memberField.getName();
						field.setName(name);
						String value = memberField.getValue();
						field.setValue(value == null ? "" : value);
						entry.getFields().add(field);
						if (!name.endsWith("*")) {
							String[] identifiers = field.getName().split("\\.");
							try {
								if (identifiers.length > 2) {
									format = Integer.parseInt(identifiers[identifiers.length - 2]);
								}
							} catch (Exception e) {
								// Ignore Fields, if not parseable
							}
						}
					}
					String formatString = ISO15961Formats.getFormatString(format);
					if (formatString == null) {
						entry.setFormat("Unknown");
					} else {
						entry.setFormat(formatString);
					}

					history.add(entry);
				}
			}
		}
	}

	public TaskManager(ConfigurationManager configManager) throws URISyntaxException, IOException, HistoryManagerException, ProcessViewerException,
			ConfigurationManagerException {
		this.config = configManager.get();
		this.configManager = configManager;
		handler = new Reporter();
		history = new HistoryManager();
		sortTasks();
		for (Task task : config.getTasks()) {
			for (ReadPoint readPoint : task.getReadPoints()) {
				if (readPoint.getName().length() > 0) {
					try {
						addInternalReadpoint(task.getName(), readPoint);
					} catch (Exception e) {
						log.log(Level.SEVERE, "Unable to create Readpoint ", e);
					}
				}
			}
		}
		host = new TcpSubscriberHost(Integer.parseInt(Environment.REPORT_PORT), handler);
		host.open();
	}

	private void sortTasks() throws ConfigurationManagerException {
		Collections.sort(config.getTasks(), taskComperator);
		configManager.set(config);
	}

	public Configuration getTasks() {
		return config;
	}

	public Task getTask(String name) {
		for (Task task : config.getTasks()) {
			if (task.getName().equals(name)) {
				return task;
			}
		}
		return null;
	}

	public void addTask(Task task) throws ConfigurationManagerException, ProcessViewerException {
		if (task.getName().length() <= 0) {
			throw new ProcessViewerException("Task name should not be empty");
		}
		for (Task current : config.getTasks()) {
			if (current.getName().equals(task.getName())) {
				throw new ProcessViewerException("Task already exists");
			}
		}
		config.getTasks().add(task);
		configManager.set(config);
	}

	public void updateTask(String name, Task task) throws ConfigurationManagerException, ProcessViewerException {
		if (task.getName().length() <= 0) {
			throw new ProcessViewerException("Task name should not be empty");
		}
		boolean found = false;
		int index = -1;
		for (Task current : config.getTasks()) {
			index++;
			if (current.getName().equals(name)) {
				found = true;
				break;
			}
		}
		if (found) {
			if (config.getTasks().get(index).getPosition() != task.getPosition()) {
				config.getTasks().remove(index);
				config.getTasks().add(task.getPosition(), task);
				for (int i = 0; i < config.getTasks().size(); i++) {
					config.getTasks().get(i).setPosition(i);
				}
			} else {
				config.getTasks().set(index, task);
			}
			sortTasks();
			configManager.set(config);
		}
	}

	public void deleteTask(String name) throws ConfigurationManagerException {
		Task task = null;
		for (Task current : config.getTasks()) {
			if (current.getName().equals(name)) {
				task = current;
			}
		}
		if (task != null) {
			List<ReadPoint> readpoints = new ArrayList<ReadPoint>(task.getReadPoints());
			for (ReadPoint readpoint : readpoints) {
				deleteReadPoint(name, readpoint.getName());
			}
			config.getTasks().remove(task);
			for (int i = task.getPosition(); i < config.getTasks().size(); i++) {
				config.getTasks().get(i).setPosition(i);
			}
			sortTasks();
			configManager.set(config);
		}
	}

	public List<ReadPoint> getReadPoints(String name) {
		for (Task current : config.getTasks()) {
			if (current.getName().equals(name)) {
				return current.getReadPoints();
			}
		}
		return null;
	}

	public ReadPoint getReadPoint(String taskName, String name) {
		for (Task task : config.getTasks()) {
			if (task.getName().equals(taskName)) {
				for (ReadPoint current : task.getReadPoints()) {
					if (current.getName().equals(name)) {
						return current;
					}
				}
			}
		}
		return null;
	}

	private void addInternalReadpoint(String taskName, ReadPoint readPoint) throws ProcessViewerException, ConfigurationManagerException {
		String readPointName = readPoint.getName();
		if (readPoints.containsKey(readPointName)) {
			throw new ProcessViewerException("ReadPoint already exists");
		}
		String aleHost = readPoint.getAleHost();
		SpecManager currentManager = aleHosts.get(aleHost);
		if (currentManager == null) {
			try {
				currentManager = new SpecManager(aleHost, readPoint.getAleUser(), readPoint.getAlePassword());
			} catch (Exception e) {
				throw new ProcessViewerException(e);
			}
		} else {
			if (!readPoint.getAleUser().equals(currentManager.getUser()) || !readPoint.getAlePassword().equals(currentManager.getPasswd())) {
				try {
					currentManager = new SpecManager(aleHost, readPoint.getAleUser(), readPoint.getAlePassword());
				} catch (Exception e) {
					throw new ProcessViewerException(e);
				}
			}
		}
		aleHosts.put(aleHost, currentManager);
		
		String readerConnection = readPoint.getReaderConnection();
	
		if (!baseReaders.containsKey(aleHost + readerConnection)) {
			String baseReader = readPoint.getName() + "BaseReader";
			if (readPoint.getReaderType().equals("BuiltIn")) {
				baseReader = "BuiltIn";
			} else {
				try {
					currentManager.createLrSpec(baseReader, readerConnection, readPoint.getReaderType(), 0, null);
				} catch (CustomSpecException e) {
					log.log(Level.INFO, "CreateLRSpec failed: ", e);
				} catch (Exception e) {
					throw new ProcessViewerException(e);
				}
			}
			baseReaders.put(aleHost + readerConnection, baseReader);
		}

		if (!readPoints.containsKey(readPointName)) {
			String compositeReader = readPointName + "CompositeReader";
			String ecReadPoint = readPointName;
			try {
				currentManager.createLrSpec(compositeReader, readerConnection, readPoint.getReaderType(), readPoint.getAntenna(),
						baseReaders.get(aleHost + readerConnection));
			} catch (CustomSpecException e) {
				log.log(Level.INFO, "Create LRSpec failed: ", e);
			} catch (Exception e) {
				throw new ProcessViewerException(e);
			}

			try {
				currentManager.createEcSpec(ecReadPoint, compositeReader);
			} catch (CustomSpecException e) {
				log.log(Level.INFO, "Create ECSpec failed: ", e);
			} catch (Exception e) {
				throw new ProcessViewerException(e);
			}
			try {
				currentManager.subscribe(ecReadPoint);
			} catch (CustomSpecException e) {
				log.log(Level.INFO, "Subscribe failed: ", e);
			} catch (Exception e) {
				throw new ProcessViewerException(e);
			}
			readPoints.put(readPointName, readPoint);
		}
	}

	public void addReadpoint(String taskName, ReadPoint readPoint) throws ProcessViewerException, ConfigurationManagerException {
		Task task = null;
		String readPointName = readPoint.getName();
		for (Task current : config.getTasks()) {
			if (current.getName().equals(taskName)) {
				for (ReadPoint currentReadPoint : current.getReadPoints()) {
					if (currentReadPoint.getName().equals(readPointName)) {
						throw new ProcessViewerException("ReadPoint already exists");
					}
				}
				task = current;
				break;
			}
		}
		if (task != null) {
			addInternalReadpoint(taskName, readPoint);
			task.getReadPoints().set(readPoint.getPosition(), readPoint);
			configManager.set(config);
		} else {
			throw new ProcessViewerException("Task missing");
		}
	}

	public void updateReadpoint(String taskName, String readPointName, ReadPoint readPoint) throws Exception {
		Task oldTask = null;
		ReadPoint oldReadPoint = null;
		boolean found = false;
		for (Task task : config.getTasks()) {
			if (task.getName().equals(taskName)) {
				for (ReadPoint current : task.getReadPoints()) {
					if (current.getName().equals(readPointName)) {
						oldTask = task;
						oldReadPoint = current;
						found = true;
						break;
					}
				}
			}
			if (found) {
				break;
			}
		}
		if (oldTask != null && oldReadPoint != null) {
			deleteReadPoint(taskName, readPointName);
			addReadpoint(taskName, readPoint);
		}
	}

	public void deleteReadPoint(String taskName, String readPointName) throws ConfigurationManagerException {
		Task oldTask = null;
		ReadPoint oldReadPoint = null;
		boolean found = false;
		for (Task task : config.getTasks()) {
			if (task.getName().equals(taskName)) {
				for (ReadPoint current : task.getReadPoints()) {
					if (current.getName().equals(readPointName)) {
						oldTask = task;
						oldReadPoint = current;
						found = true;
						break;
					}
				}
			}
			if (found) {
				break;
			}
		}
		if (oldTask != null && oldReadPoint != null) {
			SpecManager specManager = aleHosts.get(oldReadPoint.getAleHost());
			try {
				specManager.removeEcSpec(readPointName);
			} catch (Exception e) {
				log.log(Level.INFO, "Delete ECSpec failed: ", e);
			}
			try {
				specManager.removeLrSpec(readPointName + "CompositeReader");
			} catch (Exception e) {
				log.log(Level.INFO, "Delete LRSpec failed: ", e);
			}

			try {
				specManager.removeLrSpec(baseReaders.get(oldReadPoint.getAleHost() + oldReadPoint.getReaderConnection()));
				baseReaders.remove(oldReadPoint.getAleHost() + oldReadPoint.getReaderConnection());
			} catch (Exception e) {
				log.log(Level.INFO, "Delete LRSpec failed: ", e);
			}
			ReadPoint element = new ReadPoint();
			element.setName("");
			element.setPosition(oldReadPoint.getPosition());
			oldTask.getReadPoints().set(oldReadPoint.getPosition(), element);
			configManager.set(config);
			readPoints.remove(readPointName);
		}

	}

	public List<HistoryEntry> getHistory(int limit, int offset) throws HistoryManagerException {
		List<HistoryEntry> entries = history.getEntries(limit, offset);
		return entries;
	}

	public List<HistoryEntry> getHistory(int limit, int offset, String epc) throws HistoryManagerException {
		return history.getEntries(limit, offset, epc);
	}

	public int getHistorySize() throws HistoryManagerException {
		return history.size();
	}

	public void deleteHistory() throws HistoryManagerException {
		history.clear();
	}

	public void close() throws HistoryManagerException, IOException, InterruptedException, ExecutionException, ConfigurationManagerException {
		host.close();
		history.close();
	}

	public void marshal(Writer writer) throws SQLException, IOException, HistoryManagerException {
		history.marshal(writer);
	}

}
