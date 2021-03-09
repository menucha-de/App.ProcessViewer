package havis.custom.harting.processviewer.ui.client.tasks;

import havis.custom.harting.processviewer.Task;
import havis.custom.harting.processviewer.ui.client.tasks.TaskEditorView.Mode;
import havis.custom.harting.processviewer.ui.client.tasks.TaskEvent.TaskHandler;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class TaskEvent extends GwtEvent<TaskHandler> {

	public interface TaskHandler extends EventHandler {
		public void onTaskCreate(TaskEvent event);

		public void onTaskChange(TaskEvent event);
	}

	public static final Type<TaskHandler> TYPE = new Type<TaskHandler>();

	private Task task;
	private Mode mode;
	private Task oldTask;
	private String imageUrl;

	public TaskEvent(Task task, Mode mode, String imageUrl) {
		this.task = task;
		this.mode = mode;
		this.imageUrl = imageUrl;
	}

	public TaskEvent(Task task, Mode mode, Task oldTask, String imageUrl) {
		this(task, mode, imageUrl);
		this.oldTask = oldTask;
	}

	public Task getTask() {
		return task;
	}
	
	public String getImageUrl(){
		return imageUrl;
	}
	
	public Task getOldTask(){
		return oldTask;
	}

	@Override
	public Type<TaskHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(TaskHandler handler) {
		switch (mode) {
		case CHANGE:
			handler.onTaskChange(this);
			break;
		case CREATE:
			handler.onTaskCreate(this);
			break;
		}
	}

}
