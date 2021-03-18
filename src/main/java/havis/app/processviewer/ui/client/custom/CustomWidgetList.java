package havis.app.processviewer.ui.client.custom;

import havis.net.ui.shared.client.list.WidgetList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiHandler;

public class CustomWidgetList extends WidgetList {

	private int lastSelected = -1;

	@UiHandler("panel")
	public void onClick(ClickEvent event) {
		if (items.getCellForEvent(event) != null) {
			if (items.getCellForEvent(event).getRowIndex() == lastSelected) {
				setSelected(-1);
				lastSelected = -1;
			} else {
				super.onClick(event);
				lastSelected = items.getCellForEvent(event).getRowIndex();
			}
		}
	}
}
