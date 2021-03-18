package havis.app.processviewer.ui.client.custom;

import havis.app.processviewer.Field;
import havis.app.processviewer.HistoryEntry;
import havis.app.processviewer.ui.resourcebundle.AppResources;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

public class HistoryRow {
	private static final DateTimeFormat DATE = DateTimeFormat
			.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT);
	private static final DateTimeFormat TIME = DateTimeFormat
			.getFormat(DateTimeFormat.PredefinedFormat.TIME_MEDIUM);
	private static final Widget[] WIDGET_TYPE = new Widget[] {};
	private Label date;
	private Label time;
	private Label epc;
	private Label readpoint;
	private Label id;
	private Image img;
	private HistoryEntry entry;
	private static PopupPanel details = new PopupPanel();
	private Label detailsText;

	public HistoryRow(HistoryEntry entry) {
		this.entry = entry;
		Date date = new Date(entry.getTime());
		this.date = new Label(DATE.format(date));
		this.time = new Label(TIME.format(date));
		this.epc = new Label(entry.getEpc());
		this.epc.setTitle(entry.getEpc());
		this.readpoint = new Label(entry.getReadPoint());
		this.readpoint.setTitle(entry.getReadPoint());
		this.id = new Label("" + entry.getId());
	}

	public HistoryRow(HistoryEntry entry, boolean tagHistory) {
		String quickDetails = "";
		for (Field ai : entry.getFields()) {
			quickDetails += ai.getName() + ":" + ai.getValue() + "<br />";
		}
		this.entry = entry;
		Date date = new Date(entry.getTime());
		this.date = new Label(DATE.format(date));
		this.time = new Label(TIME.format(date));
		this.epc = new Label(entry.getEpc());
		this.readpoint = new Label(entry.getReadPoint());
		this.id = new Label("" + entry.getId());
		this.img = new Image(AppResources.INSTANCE.info());
		this.img.addStyleName(AppResources.INSTANCE.css().infoStyle());
		detailsText = new HTML(quickDetails);
		detailsText.addStyleName(AppResources.INSTANCE.css().quickDetails());
		this.img.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (details.isShowing()) {
					hideDetails();
				} else {
					showDetails(img);
				}
			}
		});

		this.img.addMouseOverHandler(new MouseOverHandler() {
			@Override
			public void onMouseOver(MouseOverEvent event) {
				showDetails(img);
			}
		});

		this.img.addMouseOutHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				hideDetails();
			}
		});
		
		this.epc.addMouseOverHandler(new MouseOverHandler() {
			@Override
			public void onMouseOver(MouseOverEvent event) {
				showDetails(epc);
			}
		});

		this.epc.addMouseOutHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				hideDetails();
			}
		});
		

		
		

	}

	private void showDetails(UIObject target) {
		details.clear();
		details.add(detailsText);
		details.showRelativeTo(target);
		details.show();
	}

	private void hideDetails() {
		details.hide();
	}

	public Widget[] getWidgets() {
		ArrayList<Widget> widgets = new ArrayList<Widget>();
		widgets.add(id);
		widgets.add(date);
		widgets.add(time);
		widgets.add(epc);
		widgets.add(readpoint);
		if (img != null) {
			widgets.add(img);
		}
		return widgets.toArray(WIDGET_TYPE);
	}

	public HistoryEntry getValue() {
		return entry;
	}
}
