package havis.custom.harting.processviewer.ui.client;

import havis.custom.harting.processviewer.Configuration;
import havis.custom.harting.processviewer.ReadPoint;
import havis.custom.harting.processviewer.Task;
import havis.custom.harting.processviewer.rest.async.ProcessViewerServiceAsync;
import havis.custom.harting.processviewer.ui.client.custom.HistoryEvent;
import havis.custom.harting.processviewer.ui.client.readpoints.ReadPointEditor;
import havis.custom.harting.processviewer.ui.client.readpoints.ReadPointEditorPresenter;
import havis.custom.harting.processviewer.ui.client.readpoints.ReadPointEditorView;
import havis.custom.harting.processviewer.ui.client.readpoints.ReadPointEvent;
import havis.custom.harting.processviewer.ui.client.sections.HistorySection;
import havis.custom.harting.processviewer.ui.client.sections.TagHistorySection;
import havis.custom.harting.processviewer.ui.client.sections.ViewerSection;
import havis.custom.harting.processviewer.ui.client.tasks.TaskEditor;
import havis.custom.harting.processviewer.ui.client.tasks.TaskEditorPresenter;
import havis.custom.harting.processviewer.ui.client.tasks.TaskEditorView;
import havis.custom.harting.processviewer.ui.client.tasks.TaskEditorView.Mode;
import havis.custom.harting.processviewer.ui.client.tasks.TaskEvent;
import havis.custom.harting.processviewer.ui.resourcebundle.AppResources;
import havis.net.rest.shared.data.SerializableValue;
import havis.net.ui.shared.client.event.MessageEvent.MessageType;
import havis.net.ui.shared.client.widgets.CustomMessageWidget;
import havis.net.ui.shared.resourcebundle.ResourceBundle;

import java.util.ArrayList;
import java.util.List;

import org.fusesource.restygwt.client.Defaults;
import org.fusesource.restygwt.client.FailedResponseException;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DragEndEvent;
import com.google.gwt.event.dom.client.DragEndHandler;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.SimpleEventBus;

public class ProcessViewer extends Composite implements EntryPoint {

	@UiField
	HistorySection history;

	@UiField
	ViewerSection viewer;

	@UiField
	TaskEditor taskEditor;

	@UiField
	ReadPointEditor readPointEditor;

	@UiField
	TagHistorySection tagHistory;

	private ResourceBundle res = ResourceBundle.INSTANCE;
	private AppResources appRes = AppResources.INSTANCE;

	private static final int MAX_READPOINTS = 3;

	private static ProcessViewerUiBinder uiBinder = GWT.create(ProcessViewerUiBinder.class);

	private ProcessViewerServiceAsync service = GWT.create(ProcessViewerServiceAsync.class);

	private final SimpleEventBus eventBus = new SimpleEventBus();

	private TaskEditorView.Presenter taskEditorPresenter;

	private ReadPointEditorView.Presenter readpointEditorPresenter;

	private List<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();

	private Task currentTask = null;
	private String currentImageUrl = null;

	@UiTemplate("ProcessViewer.ui.xml")
	interface ProcessViewerUiBinder extends UiBinder<Widget, ProcessViewer> {
	}

	private class DeleteClickHandler implements ClickHandler {
		private String name;

		public DeleteClickHandler(String name) {
			this.name = name;
		}

		@Override
		public void onClick(ClickEvent event) {
			service.deleteTask(name, new MethodCallback<Void>() {
				@Override
				public void onFailure(Method method, Throwable exception) {
					showExceptionResponse(exception);
				}

				@Override
				public void onSuccess(Method method, Void response) {
					refreshTasks();
				}
			});
		}
	}

	private class ChangeTaskClickHandler implements ClickHandler {
		private Task task;
		private String currentImageUrl;

		public ChangeTaskClickHandler(Task task, String currentImageUrl) {
			this.task = task;
			this.currentImageUrl = currentImageUrl;
		}

		@Override
		public void onClick(ClickEvent event) {
			taskEditorPresenter.setVisible(true);
			taskEditorPresenter.setCurrentMode(Mode.CHANGE);
			taskEditorPresenter.setOldTask(task);
			taskEditorPresenter.setImageUrl(currentImageUrl);
		}
	}

	private class ChangeReadpointClickHandler implements ClickHandler {
		private ReadPoint readpoint;
		private Task parentTask;

		public ChangeReadpointClickHandler(ReadPoint readpoint, Task parentTask) {
			this.readpoint = readpoint;
			this.parentTask = parentTask;
		}

		@Override
		public void onClick(ClickEvent event) {
			if (readpoint.getName().length() > 0) {
				readpointEditorPresenter.setVisible(true);
				readpointEditorPresenter.setCurrentMode(havis.custom.harting.processviewer.ui.client.readpoints.ReadPointEditorView.Mode.CHANGE);
				readpointEditorPresenter.setCurrentPosition(readpoint.getPosition());
				readpointEditorPresenter.setParentTask(parentTask);
				readpointEditorPresenter.setCurrentReadPoint(readpoint);
			}
		}

	}

	private class IconReadpointClickHandler implements ClickHandler {
		private ReadPoint readpoint;
		private Task parentTask;

		public IconReadpointClickHandler(ReadPoint readpoint, Task parentTask) {
			this.readpoint = readpoint;
			this.parentTask = parentTask;
		}

		@Override
		public void onClick(ClickEvent event) {
			if (readpoint.getName().length() > 0) {
				service.deleteReadPoint(parentTask.getName(), readpoint.getName(), new MethodCallback<Void>() {
					@Override
					public void onFailure(Method method, Throwable exception) {
						showExceptionResponse(exception);
					}

					@Override
					public void onSuccess(Method method, Void response) {
						refreshTasks();
					}
				});
			} else {
				readpointEditorPresenter.setVisible(true);
				readpointEditorPresenter.setCurrentMode(havis.custom.harting.processviewer.ui.client.readpoints.ReadPointEditorView.Mode.CREATE);
				readpointEditorPresenter.setCurrentPosition(readpoint.getPosition());
				readpointEditorPresenter.setParentTask(parentTask);
				readpointEditorPresenter.setCurrentReadPoint(readpoint);
			}
		}
	}

	public ProcessViewer() {
		initWidget(uiBinder.createAndBindUi(this));

		taskEditorPresenter = new TaskEditorPresenter(taskEditor, eventBus);

		readpointEditorPresenter = new ReadPointEditorPresenter(readPointEditor, eventBus);

		history.setEventbus(eventBus);

		eventBus.addHandler(TaskEvent.TYPE, new TaskEvent.TaskHandler() {
			@Override
			public void onTaskCreate(TaskEvent event) {
				final Task task = event.getTask();
				final String name = task.getName();
				final String imageUrl = event.getImageUrl();
				task.setName(name);
				task.setPosition(viewer.getTaskCount());
				for (int i = 0; i < MAX_READPOINTS; i++) {
					ReadPoint init = new ReadPoint();
					init.setName("");
					init.setPosition(i);
					task.getReadPoints().add(init);
				}
				service.addTask(task, new MethodCallback<Void>() {
					@Override
					public void onFailure(Method method, Throwable exception) {
						showExceptionResponse(exception);
					}

					@Override
					public void onSuccess(Method method, Void response) {
						service.setImage(task.getName(), new SerializableValue<String>(imageUrl), new MethodCallback<Void>() {

							@Override
							public void onFailure(Method method, Throwable exception) {
								showExceptionResponse(exception);
							}

							@Override
							public void onSuccess(Method method, Void response) {
								viewer.clear();
								refreshTasks();
								taskEditorPresenter.clear();
								taskEditor.setVisible(false);
							}
						});

					}
				});
			}

			@Override
			public void onTaskChange(final TaskEvent event) {
				final Task task = event.getTask();
				final Task oldtask = event.getOldTask();
				final String oldName = oldtask.getName();
				final String imageUrl = event.getImageUrl();
				service.updateTask(oldName, task, new MethodCallback<Void>() {
					@Override
					public void onFailure(Method method, Throwable exception) {
						showExceptionResponse(exception);
					}

					@Override
					public void onSuccess(Method method, Void response) {
						viewer.updateTask(oldName, task);
						viewer.setTaskIcon(task.getName(), imageUrl);
						service.setImage(task.getName(), new SerializableValue<String>(imageUrl), new MethodCallback<Void>() {
							@Override
							public void onFailure(Method method, Throwable exception) {
								showExceptionResponse(exception);
							}

							@Override
							public void onSuccess(Method method, Void response) {
								viewer.clear();
								refreshTasks();
								taskEditorPresenter.clear();
								taskEditor.setVisible(false);
							}
						});

					}
				});
			}
		});

		eventBus.addHandler(ReadPointEvent.TYPE, new ReadPointEvent.ReadPointHandler() {
			@Override
			public void onReadPointCreate(ReadPointEvent event) {
				final ReadPoint readpoint = event.getReadPoint();
				final Task task = event.getParentTask();
				final String taskName = task.getName();
				service.addReadPoint(taskName, readpoint, new MethodCallback<Void>() {
					@Override
					public void onFailure(Method method, Throwable exception) {
						showExceptionResponse(exception);
					}

					@Override
					public void onSuccess(Method method, Void response) {
						viewer.addReadPoint(taskName, readpoint);
						refreshTasks();
						readpointEditorPresenter.setVisible(false);
					}
				});
			}

			@Override
			public void onReadPointChange(ReadPointEvent event) {
				final ReadPoint readPoint = event.getReadPoint();
				final Task task = event.getParentTask();
				final String taskName = task.getName();
				final ReadPoint oldReadPoint = event.getOldReadPoint();
				service.updateReadPoint(taskName, oldReadPoint.getName(), readPoint, new MethodCallback<Void>() {
					@Override
					public void onSuccess(Method method, Void response) {
						viewer.updateReadPoint(taskName, oldReadPoint.getName(), readPoint);
						refreshTasks();
						readPointEditor.setVisible(false);
					}

					@Override
					public void onFailure(Method method, Throwable exception) {
						showExceptionResponse(exception);
					}
				});
			}
		});

		eventBus.addHandler(HistoryEvent.TYPE, new HistoryEvent.HistoryHandler() {
			@Override
			public void onChange(HistoryEvent event) {
				viewer.clearSelectedReadPoints();
				if (event.getEntry() != null) {
					viewer.setSelectedReadPoint(event.getEntry().getReadPoint());
					tagHistory.setSelectedTag(event.getEntry().getEpc());
				} else {
					tagHistory.reset();
				}
			}
		});

		Defaults.setDateFormat(null);
		ensureInjection();

		viewer.addNewTaskClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				taskEditorPresenter.setCurrentMode(Mode.CREATE);
				taskEditorPresenter.clear();
				taskEditor.setVisible(true);
			}
		});

		refreshTasks();

	}

	public void refreshTasks() {
		viewer.clear();
		service.getTasks(new MethodCallback<Configuration>() {
			@Override
			public void onSuccess(Method method, Configuration response) {
				removeReadpointClickHandlers();
				for (final Task task : response.getTasks()) {
					final String taskName = task.getName();
					viewer.addTask(task);
					service.getImage(task.getName(), new MethodCallback<SerializableValue<String>>() {
						@Override
						public void onFailure(Method method, Throwable exception) {
							showExceptionResponse(exception);
						}

						@Override
						public void onSuccess(Method method, SerializableValue<String> response) {
							if (response.getValue() != null) {
								final String imageUrl = response.getValue();
								viewer.setTaskIcon(taskName, imageUrl);
								for (ReadPoint readpoint : task.getReadPoints()) {
									addReadpointClickHandlers(readpoint, task);
								}
								viewer.addDeleteTaskHandler(taskName, new DeleteClickHandler(taskName));
								viewer.addChangeTaskHandler(taskName, new ChangeTaskClickHandler(task, imageUrl));

								viewer.addDragHandler(taskName, new DragStartHandler() {
									@Override
									public void onDragStart(DragStartEvent event) {
										currentTask = task;
										currentImageUrl = imageUrl;
										viewer.setContainerBorder(taskName);
									}
								});

								viewer.addDragEndHandler(taskName, new DragEndHandler() {
									@Override
									public void onDragEnd(DragEndEvent event) {
										viewer.clearSelectedReadPoints();
										currentTask = null;
										currentImageUrl = null;
									}
								});

								viewer.addDragOverHandler(taskName, new DragOverHandler() {
									@Override
									public void onDragOver(DragOverEvent event) {
										viewer.setContainerBorder(taskName);
									}
								});

								viewer.addDragLeaveHandler(taskName, new DragLeaveHandler() {
									@Override
									public void onDragLeave(DragLeaveEvent event) {
										viewer.clearSelectedReadPoints();
									}
								});

								viewer.addDropHandler(taskName, new DropHandler() {
									@Override
									public void onDrop(DropEvent event) {
										if (currentTask.getPosition() != task.getPosition()) {
											Task copy = new Task();
											copy.setName(currentTask.getName());
											copy.setPosition(task.getPosition());
											copy.getReadPoints().addAll(currentTask.getReadPoints());
											eventBus.fireEvent(new TaskEvent(copy, Mode.CHANGE, currentTask, currentImageUrl));
										}
										currentTask = null;
										currentImageUrl = null;
									}
								});

							}
						}
					});
				}
				viewer.removeLastConnected();
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				showExceptionResponse(exception);
			}
		});
	}

	@Override
	public void onModuleLoad() {
		RootLayoutPanel.get().add(this);
		history.setOpen(true);
	}

	private void ensureInjection() {
		res.css().ensureInjected();
		appRes.css().ensureInjected();
	}

	private void addReadpointClickHandlers(ReadPoint readpoint, final Task task) {
		handlers.add(viewer.addReadpointClickHandler(task.getName(), new ChangeReadpointClickHandler(readpoint, task), readpoint.getPosition()));
		handlers.add(viewer.addReadpointAddRemoveClickHandler(task.getName(), new IconReadpointClickHandler(readpoint, task), readpoint.getPosition()));
	}

	private void removeReadpointClickHandlers() {
		List<HandlerRegistration> removed = new ArrayList<HandlerRegistration>();
		for (HandlerRegistration handler : handlers) {
			removed.add(handler);
			handler.removeHandler();
		}
		handlers.removeAll(removed);
	}

	private void showExceptionResponse(Throwable exception) {
		String result = exception.getMessage();
		if (exception instanceof FailedResponseException) {

			Response response = ((FailedResponseException) exception).getResponse();
			JSONValue err = JSONParser.parseStrict(response.getText());

			result = err.isObject().get("value").isString().stringValue();

		}
		CustomMessageWidget errorPanel = new CustomMessageWidget();
		errorPanel.showMessage(result, MessageType.ERROR);
	}

}
