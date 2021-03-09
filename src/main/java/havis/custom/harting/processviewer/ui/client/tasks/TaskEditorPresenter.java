package havis.custom.harting.processviewer.ui.client.tasks;

import havis.custom.harting.processviewer.Task;
import havis.custom.harting.processviewer.ui.client.tasks.TaskEditorView.Mode;
import havis.custom.harting.processviewer.ui.resourcebundle.AppResources;

import com.google.web.bindery.event.shared.SimpleEventBus;

public class TaskEditorPresenter implements TaskEditorView.Presenter {
	private TaskEditorView view;
	private Mode currentMode;
	private SimpleEventBus eventbus;
	private Task oldTask;

	public TaskEditorPresenter(final TaskEditorView view,
			SimpleEventBus eventbus) {
		this.view = view;
		this.eventbus = eventbus;
		view.setPresenter(this);
	}

	@Override
	public void setOldTask(Task task) {
		view.getNameBox().setText(task.getName());
		oldTask = task;
	}

	@Override
	public void setCurrentMode(Mode currentMode) {
		this.currentMode = currentMode;
	}

	@Override
	public void setImageUrl(String url) {
		view.getImage().setUrl(url);
	}

	@Override
	public void onAcceptClick() {
		Task task = new Task();
		task.setName(view.getNameBox().getText().trim());
		if (currentMode.equals(Mode.CHANGE)) {
			task.setPosition(oldTask.getPosition());
			task.getReadPoints().addAll(oldTask.getReadPoints());
		}
		fireEvent(task);
	}

	@Override
	public void setVisible(boolean visible) {
		view.setVisible(visible);
	}

	@Override
	public void onCloseClick() {
		view.setVisible(false);
	}

	@Override
	public void clear() {
		view.getImage().setUrl(
				AppResources.INSTANCE.defaultTask().getSafeUri().asString());
	}

	private void fireEvent(Task task) {
		switch (currentMode) {
		case CREATE:
			eventbus.fireEvent(new TaskEvent(task, Mode.CREATE, view.getImage()
					.getUrl()));
			break;
		case CHANGE:
			eventbus.fireEvent(new TaskEvent(task, Mode.CHANGE, oldTask, view
					.getImage().getUrl()));
			break;
		default:
			break;
		}

	}
}
