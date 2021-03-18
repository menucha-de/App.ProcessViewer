package havis.app.processviewer.ui.client.custom;

import havis.app.processviewer.HistoryEntry;
import havis.app.processviewer.ui.client.custom.HistoryEvent.HistoryHandler;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class HistoryEvent extends GwtEvent<HistoryHandler> {

	public interface HistoryHandler extends EventHandler {
		public void onChange(HistoryEvent event);
	}

	public static final Type<HistoryHandler> TYPE = new Type<HistoryHandler>();

	private HistoryEntry entry;
	
	public HistoryEvent(HistoryEntry entry) {
		this.entry = entry;
	}

	public HistoryEntry getEntry() {
		return entry;
	}
	
	@Override
	public Type<HistoryHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(HistoryHandler handler) {
		handler.onChange(this);
	}

}
