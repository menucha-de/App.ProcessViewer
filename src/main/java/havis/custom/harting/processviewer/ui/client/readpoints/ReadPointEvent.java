package havis.custom.harting.processviewer.ui.client.readpoints;

import havis.custom.harting.processviewer.ReadPoint;
import havis.custom.harting.processviewer.Task;
import havis.custom.harting.processviewer.ui.client.readpoints.ReadPointEditorView.Mode;
import havis.custom.harting.processviewer.ui.client.readpoints.ReadPointEvent.ReadPointHandler;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class ReadPointEvent extends GwtEvent<ReadPointHandler> {

	public interface ReadPointHandler extends EventHandler {
		public void onReadPointCreate(ReadPointEvent event);

		public void onReadPointChange(ReadPointEvent event);
	}

	public static final Type<ReadPointHandler> TYPE = new Type<ReadPointHandler>();

	private ReadPoint readpoint;
	private Mode mode;
	private Task parentTask;
	private ReadPoint oldReadPoint;

	public ReadPointEvent(ReadPoint readpoint, Mode mode, Task parentTask) {
		this.readpoint = readpoint;
		this.mode = mode;
		this.parentTask = parentTask;
	}

	public ReadPointEvent(ReadPoint readpoint, Mode mode, Task parentTask,
			ReadPoint oldReadPoint) {
		this.readpoint = readpoint;
		this.mode = mode;
		this.parentTask = parentTask;
		this.oldReadPoint = oldReadPoint;
	}

	public ReadPoint getReadPoint() {
		return readpoint;
	}

	public Task getParentTask() {
		return parentTask;
	}
	
	public ReadPoint getOldReadPoint(){
		return oldReadPoint;
	}

	@Override
	public Type<ReadPointHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(ReadPointHandler handler) {
		switch (mode) {
		case CHANGE:
			handler.onReadPointChange(this);
			break;
		case CREATE:
			handler.onReadPointCreate(this);
			break;
		}
	}

}