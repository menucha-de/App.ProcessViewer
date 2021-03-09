package havis.custom.harting.processviewer.ui.client.sections;

import havis.custom.harting.processviewer.HistoryEntry;
import havis.custom.harting.processviewer.rest.async.ProcessViewerServiceAsync;
import havis.custom.harting.processviewer.ui.client.custom.HistoryRow;
import havis.custom.harting.processviewer.ui.resourcebundle.AppResources;
import havis.net.ui.shared.client.ConfigurationSection;
import havis.net.ui.shared.client.list.WidgetList;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class TagHistorySection extends ConfigurationSection {
	private AppResources res = AppResources.INSTANCE;
	private static TagHistorySectionUiBinder uiBinder = GWT.create(TagHistorySectionUiBinder.class);

	interface TagHistorySectionUiBinder extends UiBinder<Widget, TagHistorySection> {
	}

	private ProcessViewerServiceAsync service = GWT.create(ProcessViewerServiceAsync.class);

	@UiField
	WidgetList logList;

	boolean autoScroll = true;
	int cursor = 0;

	private static final String[] FIELD_LABELS = new String[] { "", "Date", "Time", "EPC", "Read Point", "Details" };
	private Map<Long, HistoryRow> historyEntries = new LinkedHashMap<Long, HistoryRow>();
	private String selectedTag = null;

	@UiConstructor
	public TagHistorySection(String name) {
		super(name);
		initWidget(uiBinder.createAndBindUi(this));
		res.css().ensureInjected();
		setListHeader();
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

	public void setSelectedTag(String selected) {
		if (!selected.equals(selectedTag)) {
			reset();
		}
		if (autoScroll) {
			selectedTag = selected;
			refresh();
			// autoScroll = false;
		}
	}

	private void refresh() {
		loadLogEntries();
	}

	public void loadLogEntries() {
		service.getHistory(Integer.MAX_VALUE, cursor, selectedTag, new MethodCallback<List<HistoryEntry>>() {
			@Override
			public void onSuccess(Method method, List<HistoryEntry> response) {
				if (response.size() > 0) {
					for (HistoryEntry entry : response) {
						historyEntries.put(entry.getId(), new HistoryRow(entry, true));
						cursor++;
					}
					prependLogEntry(new ArrayList<HistoryRow>(historyEntries.values()));
					logList.getItemsContainter().scrollToBottom();
				}
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
			}
		});
	}

	public void reset() {
		logList.clear();
		historyEntries.clear();
		cursor = 0;
		autoScroll = true;
	}
}
