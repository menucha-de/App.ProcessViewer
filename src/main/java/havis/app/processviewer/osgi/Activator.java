package havis.app.processviewer.osgi;

import havis.app.processviewer.ConfigurationManager;
import havis.app.processviewer.TaskManager;
import havis.app.processviewer.rest.RESTApplication;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Application;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {
	private final static Logger log = Logger.getLogger(Activator.class
			.getName());

	TaskManager taskManager;
	private ServiceRegistration<Application> app;

	@Override
	public void start(BundleContext context) throws Exception {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(
					Activator.class.getClassLoader());
			ConfigurationManager config = new ConfigurationManager();
			taskManager = new TaskManager(config);
		} catch (Throwable t) {
			log.log(Level.SEVERE, "Failed to start TaskManager ", t);
		} finally {
			Thread.currentThread().setContextClassLoader(loader);
		}

		app = context.registerService(Application.class, new RESTApplication(
				taskManager), null);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (app != null) {
			app.unregister();
			app = null;
		}
		if (taskManager != null) {
			taskManager.close();
		}
	}
}