package havis.app.processviewer.ui.client.sections;

import havis.app.processviewer.HistoryEntry;
import havis.app.processviewer.rest.async.ProcessViewerServiceAsync;
import havis.app.processviewer.ui.client.custom.CustomWidgetList;
import havis.app.processviewer.ui.client.custom.HistoryEvent;
import havis.app.processviewer.ui.client.custom.HistoryRow;
import havis.app.processviewer.ui.client.details.Details;
import havis.app.processviewer.ui.resourcebundle.AppResources;
import havis.net.rest.shared.data.SerializableValue;
import havis.net.ui.shared.client.ConfigurationSection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.web.bindery.event.shared.SimpleEventBus;

public class HistorySection extends ConfigurationSection {
	private AppResources res = AppResources.INSTANCE;
	private static HistoryUiBinder uiBinder = GWT.create(HistoryUiBinder.class);

	interface HistoryUiBinder extends UiBinder<Widget, HistorySection> {
	}

	@UiField
	CustomWidgetList logList;

	@UiField
	Details details;

	@UiField
	Button delete;

	@UiField
	Button export;

	@UiField
	Button refresh;

	private static final String[] FIELD_LABELS = new String[] { "", "Date", "Time", "EPC", "Read Point" };
	private static final String DOWNLOAD_LINK = GWT.getHostPageBaseURL() + "rest/processviewer/tasks/history/export";

	private ProcessViewerServiceAsync service = GWT.create(ProcessViewerServiceAsync.class);

	private Map<Long, HistoryRow> historyEntries = new LinkedHashMap<Long, HistoryRow>();
	private String selectedTag = null;

	boolean autoScroll = true;
	private int cursor = 0;
	private Timer timer;
	private SimpleEventBus eventbus;

	@UiConstructor
	public HistorySection(String name) {
		super(name);
		initWidget(uiBinder.createAndBindUi(this));
		res.css().ensureInjected();
		setListHeader();
		onObserve();
	}

	public void setEventbus(SimpleEventBus eventbus) {
		this.eventbus = eventbus;
	}

	private void refresh() {
		service.getHistorySize(new MethodCallback<SerializableValue<Integer>>() {
			@Override
			public void onSuccess(Method method, SerializableValue<Integer> response) {
				if (response.getValue() > 0) {
					loadLogEntries();
				} else {
					logList.clear();
				}
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
			}
		});
	}

	public void onObserve() {
		refresh();

		timer = new Timer() {

			@Override
			public void run() {
				refresh();
			}
		};
		timer.scheduleRepeating(1000);

	}

	private void setListHeader() {
		for (String item : FIELD_LABELS) {
			logList.addHeaderCell(item);
		}
	}

	private void prependLogEntry(ArrayList<HistoryRow> rows) {
		logList.clear();
		for (HistoryRow row : rows) {

			logList.addItem(row.getWidgets());
		}
	}

	public void loadLogEntries() {
		service.getHistory(Integer.MAX_VALUE, cursor, selectedTag, new MethodCallback<List<HistoryEntry>>() {
			@Override
			public void onSuccess(Method method, List<HistoryEntry> response) {
				if (response.size() > 0) {
					for (HistoryEntry entry : response) {
						historyEntries.put(entry.getId(), new HistoryRow(entry));
						cursor++;
					}
					if (selectedTag == null) {
						prependLogEntry(new ArrayList<HistoryRow>(historyEntries.values()));
						logList.getItemsContainter().scrollToBottom();
					}
				}
				showLastSeen();
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
			}
		});
	}

	private void showLastSeen() {
		HistoryEntry lastSeenEntry = null;
		for (ListIterator<HistoryRow> iterator = new ArrayList<HistoryRow>(historyEntries.values()).listIterator(historyEntries.size()); iterator.hasPrevious();) {
			HistoryEntry historyEntry = iterator.previous().getValue();
			if (selectedTag != null && selectedTag.equals(historyEntry.getEpc())) {
				if (selectedTag != null && selectedTag.equals(historyEntry.getEpc())) {
					lastSeenEntry = historyEntry;
					break;
				}
			}
		}
		if (lastSeenEntry != null) {
			details.clear();
			details.setValues(lastSeenEntry);
			eventbus.fireEvent(new HistoryEvent(lastSeenEntry));
		}
	}

	@UiHandler("logList")
	public void onChanged(SelectionChangeEvent changed) {
		if (logList.getSelectedItem() != null) {
			Label selected = (Label) logList.getSelectedItem()[0];
			HistoryRow historyRow = historyEntries.get(Long.parseLong(selected.getText()));
			HistoryEntry historyEntry = historyRow.getValue();
			details.setValues(historyEntry);
			eventbus.fireEvent(new HistoryEvent(historyEntry));
			selectedTag = historyEntry.getEpc();
		}

		if (logList.getSelectedIndex() == -1) {
			prependLogEntry(new ArrayList<HistoryRow>(historyEntries.values()));
			details.clear();
			selectedTag = null;
			eventbus.fireEvent(new HistoryEvent((HistoryEntry) null));
		}
	}

	@UiHandler("delete")
	void onClearHistory(ClickEvent event) {
		service.deleteHistory(new MethodCallback<Void>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
			}

			@Override
			public void onSuccess(Method method, Void response) {
				logList.clear();
				historyEntries.clear();
				cursor = 0;
			}
		});
	}

	@UiHandler("export")
	void onExport(ClickEvent event) {
		service.getHistorySize(new MethodCallback<SerializableValue<Integer>>() {
			@Override
			public void onSuccess(Method method, SerializableValue<Integer> response) {
				Window.Location.assign(DOWNLOAD_LINK);
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
			}
		});
	}

	@UiHandler("refresh")
	void onRefresh(ClickEvent event) {
		logList.setSelected(-1);
	}

	@Override
	protected void onCloseSection() {
		export.setVisible(false);
		delete.setVisible(false);
		refresh.setVisible(false);
		super.onCloseSection();
	}

	@Override
	protected void onOpenSection() {
		export.setVisible(true);
		delete.setVisible(true);
		refresh.setVisible(true);
		super.onOpenSection();
	}
}
