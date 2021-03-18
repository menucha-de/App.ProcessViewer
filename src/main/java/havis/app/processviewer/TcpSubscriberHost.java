package havis.app.processviewer;

import havis.middleware.ale.service.ec.ECReport;
import havis.middleware.ale.service.ec.ECReports;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;

public class TcpSubscriberHost {
	private ServerSocket serverChannel;
	private ReportListener reportListener;
	private Future<?> listener;
	private ExecutorService pool;
	private boolean stopped = false;

	public TcpSubscriberHost(int port, ReportListener listener)
			throws URISyntaxException, IOException {
		this.reportListener = listener;
		serverChannel = new ServerSocket(port);
		pool = Executors.newFixedThreadPool(1);
	}

	public void open() {
		listener = pool.submit(new TCPListener());
	}

	public void close() throws IOException, InterruptedException,
			ExecutionException {
		stopped = true;
		serverChannel.close();
		listener.get();
		pool.shutdown();

	}

	private class TCPListener implements Runnable {
		public void run() {
			Socket connection;
			while (true) {
				try {
					connection = serverChannel.accept();
					BufferedReader inFromClient = new BufferedReader(
							new InputStreamReader(connection.getInputStream()));
					String content = inFromClient.readLine();
					ECReports reports = (ECReports) getDeserializedObject(
							content, ECReports.class);
					if (reports != null && reports.getReports() != null) {
						for (ECReport report : reports.getReports().getReport()) {
							reportListener.fire(report);
						}
					}
					connection.close();
				} catch (Throwable e) {
					if (stopped) {
						break;
					}
				}
			}
		}
	}

	public Object getDeserializedObject(String xml, Class<?> type)
			throws JAXBException {
		xml = xml.substring(xml.indexOf("<"));
		if (xml != null && type != null) {
			JAXBContext jaxbContext = JAXBContext.newInstance(type);
			StringReader reader = new StringReader(xml);
			// Unmarshalling
			return jaxbContext.createUnmarshaller()
					.unmarshal(new StreamSource(reader), type).getValue();
		}
		return null;
	}

}
