package havis.custom.harting.processviewer.ui.client.tasks;

import havis.custom.harting.processviewer.ui.resourcebundle.AppResources;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
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
import com.google.gwt.event.dom.client.HasDragEndHandlers;
import com.google.gwt.event.dom.client.HasDragLeaveHandlers;
import com.google.gwt.event.dom.client.HasDragOverHandlers;
import com.google.gwt.event.dom.client.HasDragStartHandlers;
import com.google.gwt.event.dom.client.HasDropHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class TaskContainer extends Composite implements HasDragStartHandlers, HasDragOverHandlers, HasDropHandlers, HasDragEndHandlers, HasDragLeaveHandlers {
	private AppResources res = AppResources.INSTANCE;
	private static DetailsUiBinder uiBinder = GWT.create(DetailsUiBinder.class);

	private HandlerRegistration updateHandler = null;
	private HandlerRegistration deleteHandler = null;

	interface DetailsUiBinder extends UiBinder<Widget, TaskContainer> {
	}

	private Map<String, Integer> readPointPosition = new HashMap<String, Integer>();

	// LinePosition -165, -99, -32

	@UiField
	protected FlowPanel readPoints;

	@UiField
	protected FlowPanel icon;

	@UiField
	protected FlowPanel stepPointer;

	@UiField
	protected Label name;

	@UiField
	protected Image input;

	@UiField
	protected Image process;

	@UiField
	protected Image output;

	@UiField
	protected FlowPanel delete;

	@UiField
	protected FlowPanel deleteInput;

	@UiField
	protected FlowPanel deleteProcess;

	@UiField
	protected FlowPanel deleteOutput;

	@UiField
	protected Image innerIcon;

	@UiField
	protected FlowPanel connected;

	@UiField
	protected FlowPanel container;

	public TaskContainer() {
		initWidget(uiBinder.createAndBindUi(this));
		res.css().ensureInjected();
		container.addDomHandler(new MouseOverHandler() {
			@Override
			public void onMouseOver(MouseOverEvent event) {
				setIconsVisible(true);
			}
		}, MouseOverEvent.getType());

		container.addDomHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				setIconsVisible(false);
			}
		}, MouseOutEvent.getType());

		container.sinkEvents(Event.ONCONTEXTMENU);
		container.addHandler(new ContextMenuHandler() {
			@Override
			public void onContextMenu(ContextMenuEvent event) {
				event.preventDefault();
				event.stopPropagation();
				if (delete.isVisible()) {
					setIconsVisible(false);
				} else {
					setIconsVisible(true);
				}
			}
		}, ContextMenuEvent.getType());
	}

	private void setIconsVisible(boolean visible) {
		delete.setVisible(visible);
		if (visible) {
			deleteInput.addStyleName(res.css().deleteReadPointButtonEnabled());
			deleteProcess.addStyleName(res.css().deleteReadPointButtonEnabled());
			deleteOutput.addStyleName(res.css().deleteReadPointButtonEnabled());
		} else {
			deleteInput.removeStyleName(res.css().deleteReadPointButtonEnabled());
			deleteProcess.removeStyleName(res.css().deleteReadPointButtonEnabled());
			deleteOutput.removeStyleName(res.css().deleteReadPointButtonEnabled());
		}

	}

	public void addReadPoint(String readPoint, int position) {
		switch (position) {
		case 0:
			if (readPoint.length() > 0) {
				input.addStyleName(res.css().readPointStyleEnabled());
				deleteInput.addStyleName(res.css().deleteReadPointButtonMinus());
			} else {
				input.removeStyleName(res.css().readPointStyleEnabled());
				deleteInput.removeStyleName(res.css().deleteReadPointButtonMinus());
			}
			break;
		case 1:
			if (readPoint.length() > 0) {
				process.addStyleName(res.css().readPointStyleEnabled());
				deleteProcess.addStyleName(res.css().deleteReadPointButtonMinus());
			} else {
				process.removeStyleName(res.css().readPointStyleEnabled());
				deleteProcess.removeStyleName(res.css().deleteReadPointButtonMinus());
			}
			break;
		case 2:
			if (readPoint.length() > 0) {
				output.addStyleName(res.css().readPointStyleEnabled());
				deleteOutput.addStyleName(res.css().deleteReadPointButtonMinus());
			} else {
				output.removeStyleName(res.css().readPointStyleEnabled());
				deleteOutput.removeStyleName(res.css().deleteReadPointButtonMinus());
			}
			break;
		}
		readPointPosition.put(readPoint, position);
	}

	public void setSelectedReadPoint(String readPoint) {
		if (readPointPosition.containsKey(readPoint)) {
			stepPointer.setVisible(true);
			icon.addStyleName(res.css().taskSelected());
			switch (readPointPosition.get(readPoint)) {
			case 0:
				stepPointer.addStyleName(res.css().leftPosition());
				break;
			case 1:
				stepPointer.addStyleName(res.css().middlePosition());
				break;
			case 2:
				stepPointer.addStyleName(res.css().rightPosition());
				break;
			}
		}
	}

	public void setSelected() {
		icon.addStyleName(res.css().taskSelected());
	}

	public void clear() {
		stepPointer.setVisible(false);
		icon.removeStyleName(res.css().taskSelected());
		stepPointer.removeStyleName(res.css().leftPosition());
		stepPointer.removeStyleName(res.css().middlePosition());
		stepPointer.removeStyleName(res.css().rightPosition());
	}

	public void setName(String name) {
		this.name.setText(name);
	}

	public HandlerRegistration addChangeReadpointClickHandler(ClickHandler handler, int position) {
		switch (position) {
		case 0:
			return input.addClickHandler(handler);
		case 1:
			return process.addClickHandler(handler);
		case 2:
			return output.addClickHandler(handler);
		}
		return null;
	}

	public HandlerRegistration addDeleteReadpointClickHandler(ClickHandler handler, int position) {
		switch (position) {
		case 0:
			return deleteInput.addDomHandler(handler, ClickEvent.getType());
		case 1:
			return deleteProcess.addDomHandler(handler, ClickEvent.getType());
		case 2:
			return deleteOutput.addDomHandler(handler, ClickEvent.getType());
		}
		return null;
	}

	public HandlerRegistration addDeleteHandler(ClickHandler handler) {
		if (deleteHandler != null) {
			deleteHandler.removeHandler();
		}
		deleteHandler = delete.addDomHandler(handler, ClickEvent.getType());
		return deleteHandler;
	}

	public HandlerRegistration addUpdateHandler(ClickHandler handler) {
		if (updateHandler != null) {
			updateHandler.removeHandler();
		}
		updateHandler = innerIcon.addClickHandler(handler);
		return updateHandler;
	}

	public void setImageUrl(String url) {
		if (!url.equals(res.defaultTask().getSafeUri().asString())) {
			innerIcon.setUrl(url);
		}
	}

	public void setConnected(boolean connected) {
		this.connected.setVisible(connected);
	}

	@Override
	public HandlerRegistration addDropHandler(DropHandler handler) {
		return container.addDomHandler(handler, DropEvent.getType());
	}

	@Override
	public HandlerRegistration addDragOverHandler(DragOverHandler handler) {
		return container.addDomHandler(handler, DragOverEvent.getType());
	}

	@Override
	public HandlerRegistration addDragStartHandler(DragStartHandler handler) {
		return container.addDomHandler(handler, DragStartEvent.getType());
	}

	@Override
	public HandlerRegistration addDragEndHandler(DragEndHandler handler) {
		return container.addDomHandler(handler, DragEndEvent.getType());
	}

	@Override
	public HandlerRegistration addDragLeaveHandler(DragLeaveHandler handler) {
		return container.addDomHandler(handler, DragLeaveEvent.getType());
	}

}
