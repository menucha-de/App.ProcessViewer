package havis.app.processviewer.ui.client.sections;

import havis.app.processviewer.ReadPoint;
import havis.app.processviewer.Task;
import havis.app.processviewer.ui.client.tasks.TaskContainer;
import havis.app.processviewer.ui.resourcebundle.AppResources;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DragEndHandler;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class ViewerSection extends Composite {
	private AppResources res = AppResources.INSTANCE;
	private static CameraSectionUiBinder uiBinder = GWT.create(CameraSectionUiBinder.class);

	interface CameraSectionUiBinder extends UiBinder<Widget, ViewerSection> {
	}

	@UiField
	protected FlowPanel area;

	@UiField
	protected FlowPanel newTask;

	@UiField
	protected FlowPanel icon;

	private Map<String, TaskContainer> tasks = new HashMap<String, TaskContainer>();
	private Map<String, String> readpoints = new HashMap<String, String>();
	private String lastAdded = "";

	public ViewerSection() {
		initWidget(uiBinder.createAndBindUi(this));
		res.css().ensureInjected();
	}

	public void addTask(Task task) {
		if (tasks.get(task.getName()) == null) {
			newTask.removeFromParent();
			TaskContainer taskContainer = new TaskContainer();
			String name = task.getName();
			taskContainer.setName(name);
			for (ReadPoint readpoint : task.getReadPoints()) {
				taskContainer.addReadPoint(readpoint.getName(), readpoint.getPosition());
				readpoints.put(readpoint.getName(), name);
			}
			area.add(taskContainer);
			lastAdded = name;
			area.add(newTask);
			tasks.put(task.getName(), taskContainer);
		}
	}

	public void removeLastConnected() {
		TaskContainer task = tasks.get(lastAdded);
		if (task != null) {
			task.setConnected(false);
		}
	}

	public void clear() {
		area.clear();
		tasks.clear();
		readpoints.clear();
		area.add(newTask);
	}

	public void updateTask(String taskName, Task newTask) {
		TaskContainer taskContainer = tasks.get(taskName);
		taskContainer.setName(newTask.getName());
		for (ReadPoint readpoint : newTask.getReadPoints()) {
			taskContainer.addReadPoint(readpoint.getName(), readpoint.getPosition());
			readpoints.put(readpoint.getName(), newTask.getName());
		}
		tasks.remove(taskName);
		tasks.put(newTask.getName(), taskContainer);
	}

	public void addReadPoint(String taskName, ReadPoint readpoint) {
		TaskContainer task = tasks.get(taskName);
		task.addReadPoint(readpoint.getName(), readpoint.getPosition());
		readpoints.put(readpoint.getName(), taskName);
	}

	public void updateReadPoint(String taskName, String oldName, ReadPoint readpoint) {
		TaskContainer task = tasks.get(taskName);
		task.addReadPoint(readpoint.getName(), readpoint.getPosition());
		readpoints.remove(oldName);
		readpoints.put(readpoint.getName(), taskName);
	}

	public void setSelectedReadPoint(String readPoint) {
		String task = readpoints.get(readPoint);
		TaskContainer taskContainer = tasks.get(task);
		if (task != null) {
			taskContainer.setSelectedReadPoint(readPoint);
		}
	}

	public void clearSelectedReadPoints() {
		for (TaskContainer task : tasks.values()) {
			task.clear();
		}
	}

	public HandlerRegistration addNewTaskClickHandler(ClickHandler handler) {
		return icon.addDomHandler(handler, ClickEvent.getType());
	}

	public void addDeleteTaskHandler(String task, ClickHandler handler) {
		tasks.get(task).addDeleteHandler(handler);
	}

	public void addChangeTaskHandler(String task, ClickHandler handler) {
		tasks.get(task).addUpdateHandler(handler);
	}

	public HandlerRegistration addReadpointClickHandler(String task, ClickHandler handler, int position) {
		return tasks.get(task).addChangeReadpointClickHandler(handler, position);
	}

	public HandlerRegistration addReadpointAddRemoveClickHandler(String task, ClickHandler handler, int position) {
		return tasks.get(task).addDeleteReadpointClickHandler(handler, position);
	}

	public void setTaskIcon(String task, String url) {
		tasks.get(task).setImageUrl(url);
	}

	public int getTaskCount() {
		return tasks.size();
	}

	public HandlerRegistration addDragHandler(String task, DragStartHandler handler) {
		return tasks.get(task).addDragStartHandler(handler);
	}

	public HandlerRegistration addDragEndHandler(String task, DragEndHandler handler) {
		return tasks.get(task).addDragEndHandler(handler);
	}

	public HandlerRegistration addDragOverHandler(String task, DragOverHandler handler) {
		return tasks.get(task).addDragOverHandler(handler);
	}

	public HandlerRegistration addDragLeaveHandler(String task, DragLeaveHandler handler) {
		return tasks.get(task).addDragLeaveHandler(handler);
	}

	public HandlerRegistration addDropHandler(String task, DropHandler handler) {
		return tasks.get(task).addDropHandler(handler);
	}

	public void setContainerBorder(String taskName) {
		tasks.get(taskName).setSelected();
	}

}
