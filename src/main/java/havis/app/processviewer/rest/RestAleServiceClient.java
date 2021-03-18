package havis.app.processviewer.rest;

import havis.middleware.ale.base.exception.ImplementationException;
import havis.middleware.ale.base.exception.NoSuchIdException;
import havis.middleware.ale.base.exception.NoSuchPathException;
import havis.middleware.ale.base.exception.SecurityException;
import havis.middleware.ale.service.mc.MCEventCycleSpec;
import havis.middleware.ale.service.mc.MCLogicalReaderSpec;
import havis.middleware.ale.service.mc.MCSubscriberSpec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RestAleServiceClient {
	private static SSLContext trustAllContext;
	private static HostnameVerifier trustAllVerifier;
	private String baseUri;
	private String auth;
	private final static ObjectMapper mapper = new ObjectMapper();
	private final static int TIMEOUT = 5000;
	private CookieManager cookieManager = new CookieManager();
	private boolean isHttps = false;
	private String localHostAddress = null;

	static {
		try {
			trustAllContext = SSLContext.getInstance("SSL");
			trustAllContext.init(null, new TrustManager[] { new X509TrustManager() {

				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
				}

				@Override
				public void checkServerTrusted(X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
				}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			} }, null);
			trustAllVerifier = new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};
		} catch (Exception e) {
			// ignore
		}
	}

	private static final Logger log = Logger.getLogger(RestAleServiceClient.class.getName());

	public RestAleServiceClient(String baseUri, String user, String password) throws Exception {
		URI uri = new URI(baseUri);
		this.baseUri = baseUri;
		switch (uri.getScheme()) {
		case "http":
			isHttps = false;
			break;
		case "https":
			isHttps = true;
			break;
		default:
			throw new Exception("Unknown scheme '" + uri.getScheme() + "'");
		}

		if (uri.getHost() == null) {
			throw new Exception("No host specified for " + uri.toString());
		}

		auth = new Authenticator(user, password).getBasicAuthentication();

	}

	public void close() {
		log.info("Close REST service at ");
	}

	@SuppressWarnings("unchecked")
	public List<MCLogicalReaderSpec> getLRSpecs() throws MalformedURLException, IOException, URISyntaxException, ImplementationException, NoSuchIdException,
			NoSuchPathException, SecurityException {
		return (List<MCLogicalReaderSpec>) sendReceive(Methods.GET, "/ale/lr/specs", new TypeReference<List<MCLogicalReaderSpec>>() {
		});
	}

	@SuppressWarnings("unchecked")
	public List<MCEventCycleSpec> getECSpecs() throws MalformedURLException, ImplementationException, NoSuchIdException, NoSuchPathException,
			SecurityException, IOException, URISyntaxException {
		return (List<MCEventCycleSpec>) sendReceive(Methods.GET, "/ale/ec/specs", new TypeReference<List<MCEventCycleSpec>>() {
		});
	}

	@SuppressWarnings("unchecked")
	public List<MCSubscriberSpec> getSubscibers(String ec) throws MalformedURLException, ImplementationException, NoSuchIdException, NoSuchPathException,
			SecurityException, IOException, URISyntaxException {
		return (List<MCSubscriberSpec>) sendReceive(Methods.GET, "/ale/ec/specs/" + ec + "/subscribers", new TypeReference<List<MCSubscriberSpec>>() {
		});
	}

	public void addLRSpec(MCLogicalReaderSpec spec) throws MalformedURLException, ProtocolException, URISyntaxException, IOException {
		send(Methods.POST, "/ale/lr/specs", spec);
	}

	public void addECSpec(MCEventCycleSpec spec) throws MalformedURLException, ProtocolException, URISyntaxException, IOException {
		send(Methods.POST, "/ale/ec/specs", spec);
	}

	public void addSubscriber(String ec, MCSubscriberSpec spec) throws MalformedURLException, ProtocolException, URISyntaxException, IOException {
		send(Methods.POST, "/ale/ec/specs/" + ec + "/subscribers", spec);
	}

	public void deleteECSpec(String specId) throws MalformedURLException, ImplementationException, NoSuchIdException, NoSuchPathException, SecurityException,
			IOException, URISyntaxException {
		send(Methods.DELETE, "/ale/ec/specs/" + specId);
	}

	public void deleteLRSpec(String lrId) throws MalformedURLException, ImplementationException, NoSuchIdException, NoSuchPathException, SecurityException,
			IOException, URISyntaxException {
		send(Methods.DELETE, "/ale/lr/specs/" + lrId);
	}

	private void send(Methods method, String path) throws URISyntaxException, MalformedURLException, ProtocolException, IOException {
		final HttpURLConnection connection = prepare(method, path);
		try {
			postprocessing(connection);
		} finally {
			connection.disconnect();
		}
	}

	private void send(Methods method, String path, Object object) throws URISyntaxException, MalformedURLException, ProtocolException, IOException {
		final HttpURLConnection connection = prepare(method, path);
		try {
			try (OutputStreamWriter stream = new OutputStreamWriter(connection.getOutputStream())) {
				mapper.writeValue(stream, object);
			}
			postprocessing(connection);
		} finally {
			connection.disconnect();
		}
	}

	private Object sendReceive(Methods method, String path, TypeReference<?> type) throws MalformedURLException, IOException, URISyntaxException,
			ImplementationException, NoSuchIdException, NoSuchPathException, SecurityException {
		final HttpURLConnection connection = prepare(method, path);
		try {
			postprocessing(connection);
			List<?> readValue = mapper.readValue(connection.getInputStream(), type);
			return readValue;
		} finally {
			connection.disconnect();
		}
	}

	private void getLocalAddress(HttpURLConnection connection) {
		InetAddress address = null;
		try {
			Object networkClient;
			Class<?> networkClientClass;
			if (connection instanceof HttpsURLConnection) {
				Field delegateField = connection.getClass().getDeclaredField("delegate");
				delegateField.setAccessible(true);
				Object httpConnection = delegateField.get(connection);

				Field field = httpConnection.getClass().getSuperclass().getSuperclass().getDeclaredField("http");
				field.setAccessible(true);
				networkClient = field.get(httpConnection);
				networkClientClass = networkClient.getClass().getSuperclass().getSuperclass();
			} else {
				Field field = connection.getClass().getDeclaredField("http");
				field.setAccessible(true);
				networkClient = field.get(connection);
				networkClientClass = networkClient.getClass().getSuperclass();
			}

			Method method = networkClientClass.getDeclaredMethod("getLocalAddress");
			method.setAccessible(true);
			address = (InetAddress) method.invoke(networkClient);
		} catch (Throwable e) {
			e.printStackTrace();
			// ignore
		}
		localHostAddress = address.getHostAddress();
	}

	private HttpURLConnection prepare(Methods method, String path) throws URISyntaxException, IOException, MalformedURLException, ProtocolException {
		URI uri = new URI(baseUri + path);
		final HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
		if (isHttps) {
			((HttpsURLConnection) connection).setSSLSocketFactory(trustAllContext.getSocketFactory());
			((HttpsURLConnection) connection).setHostnameVerifier(trustAllVerifier);
		}
		connection.setConnectTimeout(TIMEOUT);
		connection.setReadTimeout(TIMEOUT);
		switch (method) {
		case DELETE:
		case GET:
			connection.setDoOutput(false);
			break;
		case POST:
		case PUT:
			connection.setDoOutput(true);
		}
		connection.setRequestMethod(method.toString());
		connection.setRequestProperty("Content-type", "application/json");

		if (cookieManager.getCookieStore().getCookies().size() > 0) {
			// While joining the Cookies, use ',' or ';' as needed,
			// most servers are using ';'
			StringBuilder value = new StringBuilder();
			for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
				if (value.length() > 0)
					value.append(';');
				// always use simple format
				value.append(cookie.getName() + "=" + cookie.getValue());
			}
			connection.setRequestProperty("Cookie", value.toString());
		}

		connection.setRequestProperty("Authorization", auth);
		connection.connect();
		if (getLocalHostAddress() == null) {
			getLocalAddress(connection);
		}
		return connection;
	}

	private void postprocessing(final HttpURLConnection connection) throws IOException {
		int code = connection.getResponseCode();
		if (code < HttpURLConnection.HTTP_OK || code >= HttpURLConnection.HTTP_OK + 100) {
			try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
				byte[] buffer = new byte[1024];
				int length;
				if (connection.getErrorStream() != null) {
					while ((length = connection.getErrorStream().read(buffer)) != -1) {
						result.write(buffer, 0, length);
					}
				}
				throw new IOException("HTTP " + code + ": " + connection.getResponseMessage() + " cause:" + result.toString());
			}
		}
		for (Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
			if ("Set-Cookie".equalsIgnoreCase(entry.getKey()) && entry.getValue() != null) {
				for (String cookie : entry.getValue()) {
					cookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
				}
			}
		}

	}

	public String getLocalHostAddress() {
		return localHostAddress;
	}
}
