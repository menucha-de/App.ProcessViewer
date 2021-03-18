package havis.app.processviewer;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvResultSetWriter;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CsvContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HistoryManager {

	private final static int ID = 1, TIMESTAMP = 2, EPC = 3, READPOINT = 4, FORMAT = 5, FIELDS = 6;
	private final static String CLEAR = "DELETE FROM history";
	private final static String SIZE = "SELECT COUNT(id) FROM history";
	private final static String SELECT = "SELECT id, timestamp, epc, readpoint, format, fields FROM history";
	private final static String ORDER = "ORDER BY timestamp";
	private final static String INSERT = "INSERT INTO history (timestamp, epc, readpoint, format, fields) VALUES (?, ?, ?, ?, ?)";
	private final static String STRIP = "DELETE FROM history WHERE id <= ";
	private final static String FILTER = "WHERE epc = ?";
	private final static ObjectMapper objectMapper = new ObjectMapper();
	private Connection connection;

	private final static CellProcessor processor = new CellProcessor() {
		@SuppressWarnings("unchecked")
		@Override
		public String execute(Object value, CsvContext context) {
			if (value instanceof Clob) {
				Clob clob = (Clob) value;
				try {
					try (InputStream stream = clob.getAsciiStream()) {
						byte[] bytes = new byte[stream.available()];
						stream.read(bytes);
						return new String(bytes, StandardCharsets.UTF_8);
					}
				} catch (Exception e) {
					// log.log(Level.FINE, "Failed to read column data", e);
				}
			}
			return null;
		}
	};

	public HistoryManager() throws HistoryManagerException {
		try {
			connection = DriverManager.getConnection(Environment.JDBC_URL, Environment.JDBC_USERNAME, Environment.JDBC_PASSWORD);
		} catch (SQLException e) {
			throw new HistoryManagerException("Failed to get connection", e);
		}
	}

	public synchronized int clear() throws HistoryManagerException {
		try (Statement stmt = connection.createStatement()) {
			return stmt.executeUpdate(CLEAR);
		} catch (SQLException e) {
			throw new HistoryManagerException(e);
		}
	}

	public synchronized int size() throws HistoryManagerException {
		try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(SIZE)) {
			if (rs.next())
				return rs.getInt(1);
			return 0;
		} catch (SQLException e) {
			throw new HistoryManagerException(e);
		}
	}

	public synchronized List<HistoryEntry> getEntries(int limit, int offset) throws HistoryManagerException {
		List<HistoryEntry> result = new ArrayList<>();

		try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(SELECT + " " + ORDER + " limit " + limit + " offset " + offset)) {
			while (rs.next()) {
				HistoryEntry historyEntry = new HistoryEntry();
				historyEntry.setId(rs.getLong(ID));
				historyEntry.setTime(rs.getTimestamp(TIMESTAMP).getTime());
				historyEntry.setEpc(rs.getString(EPC));
				historyEntry.setReadPoint(rs.getString(READPOINT));
				historyEntry.setFormat(rs.getString(FORMAT));
				try {
					List<Field> fields = objectMapper.readValue(rs.getString(FIELDS), new TypeReference<List<Field>>() {
					});
					for (Field field : fields) {
						historyEntry.getFields().add(field);
					}
				} catch (IOException e) {
					throw new HistoryManagerException(e);
				}
				result.add(historyEntry);
			}
		} catch (SQLException e) {
			throw new HistoryManagerException(e);
		}
		return result;
	}

	public synchronized List<HistoryEntry> getEntries(int limit, int offset, String epc) throws HistoryManagerException {
		List<HistoryEntry> result = new ArrayList<>();

		try (PreparedStatement stmt = connection.prepareStatement(SELECT + " " + FILTER + ORDER + " limit " + limit + " offset " + offset);) {
			stmt.setString(1, epc);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					HistoryEntry historyEntry = new HistoryEntry();
					historyEntry.setId(rs.getLong(ID));
					historyEntry.setTime(rs.getTimestamp(TIMESTAMP).getTime());
					historyEntry.setEpc(rs.getString(EPC));
					historyEntry.setReadPoint(rs.getString(READPOINT));
					historyEntry.setFormat(rs.getString(FORMAT));
					try {
						List<Field> fields = objectMapper.readValue(rs.getString(FIELDS), new TypeReference<List<Field>>() {
						});
						for (Field field : fields) {
							historyEntry.getFields().add(field);
						}
					} catch (IOException e) {
						throw new HistoryManagerException(e);
					}
					result.add(historyEntry);
				}
			}
		} catch (SQLException e) {
			throw new HistoryManagerException(e);
		}
		return result;
	}

	public synchronized void add(HistoryEntry entry) throws HistoryManagerException {
		try (PreparedStatement stmt = connection.prepareStatement(INSERT)) {
			stmt.setTimestamp(TIMESTAMP - 1, new Timestamp(entry.getTime()));
			stmt.setString(EPC - 1, entry.getEpc());
			stmt.setString(READPOINT - 1, entry.getReadPoint());
			stmt.setString(FORMAT - 1, entry.getFormat());
			try {
				stmt.setString(FIELDS - 1, objectMapper.writeValueAsString(entry.getFields()));
			} catch (JsonProcessingException e) {
				throw new HistoryManagerException(e);
			}
			stmt.execute();
			try (ResultSet rs = stmt.getGeneratedKeys()) {
				while (rs.next())
					strip(rs.getInt(1) - Environment.MAX_RECORD_COUNT);
			}

		} catch (SQLException e) {
			throw new HistoryManagerException(e);
		}
	}

	private synchronized void strip(int min) throws SQLException {
		if (min > 0)
			try (Statement stmt = connection.createStatement()) {
				stmt.executeUpdate(STRIP + min);
			}
	}

	public synchronized void close() throws HistoryManagerException {
		try {
			connection.close();
		} catch (SQLException e) {
			throw new HistoryManagerException(e);
		}
	}

	public synchronized void marshal(Writer writer) throws SQLException, IOException, HistoryManagerException {
		try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(SELECT + " limit " + size() + " offset " + 0)) {
			ResultSetMetaData data = rs.getMetaData();
			CellProcessor[] processors = new CellProcessor[data.getColumnCount()];
			for (int i = 0; i < data.getColumnCount(); i++)
				if (data.getColumnType(i + 1) == Types.CLOB)
					processors[i] = processor;
			try (CsvResultSetWriter csv = new CsvResultSetWriter(writer, CsvPreference.EXCEL_PREFERENCE)) {
				csv.write(rs, processors);
				csv.flush();
			}
		}
	}

}