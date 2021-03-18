package havis.app.processviewer.ui.client.readpoints;

import havis.app.processviewer.ReadPoint;
import havis.app.processviewer.Task;
import havis.app.processviewer.ui.client.readpoints.ReadPointEditorView.Mode;

import com.google.gwt.user.client.ui.ListBox;
import com.google.web.bindery.event.shared.SimpleEventBus;

public class ReadPointEditorPresenter implements ReadPointEditorView.Presenter {
	private ReadPointEditorView view;
	private Mode currentMode;
	private int position = 0;
	private SimpleEventBus eventBus;
	private Task parentTask;
	private ReadPoint currentReadPoint;

	public ReadPointEditorPresenter(final ReadPointEditorView view,
			SimpleEventBus eventBus) {
		this.view = view;
		this.eventBus = eventBus;
		view.setPresenter(this);
		clear();
	}

	@Override
	public void setCurrentMode(Mode currentMode) {
		this.currentMode = currentMode;
	}

	@Override
	public void setCurrentPosition(int position) {
		this.position = position;
	};

	@Override
	public void setParentTask(Task task) {
		this.parentTask = task;
	}

	@Override
	public void setCurrentReadPoint(ReadPoint readpoint) {
		this.currentReadPoint = readpoint;
		if (currentReadPoint.getName().length()!=0) {
			view.getNameBox().setText(readpoint.getName());
			String aleHost = readpoint.getAleHost();
			view.getMiddlewareHostBox().setText(aleHost);
			if (aleHost.equals("http://localhost")) {
				view.getRemoteCheckbox().setValue(false);
			} else {
				view.getRemoteCheckbox().setValue(true);
				view.getMiddlewareHostBox().setEnabled(true);
			}

			if (readpoint.getReaderType().equals("BuiltIn")) {
				view.getReaderHostBox().setText("http://localhost");
			} else {
				view.getReaderHostBox()
						.setText(readpoint.getReaderConnection());
				view.getReaderHostBox().setEnabled(true);
			}
			view.getReaderTypeListBox().setSelectedIndex(
					findItem(readpoint.getReaderType(),
							view.getReaderTypeListBox()));

			if (readpoint.getAntenna() == 0) {
				view.getSpecificAntennaCheckBox().setValue(false);
			} else {
				view.getSpecificAntennaCheckBox().setValue(true);
				view.getSpecificAntennatListBox().setItemText(0, "1");
				view.getSpecificAntennatListBox().setEnabled(true);

			}
			view.getSpecificAntennatListBox().setSelectedIndex(
					findItem("" + readpoint.getAntenna(),
							view.getSpecificAntennatListBox()));
		} else {
			this.currentMode = Mode.CREATE;
		}

	}

	@Override
	public void onAcceptClick() {
		ReadPoint readpoint = new ReadPoint();
		readpoint.setName(view.getNameBox().getText().trim());

		if (view.getRemoteCheckbox().getValue()) {
			readpoint.setAleHost(view.getMiddlewareHostBox().getText());
		} else {
			readpoint.setAleHost("http://localhost");
		}

		readpoint.setAleUser(view.getMiddlewareUserBox().getText());
		readpoint.setAlePassword(view.getMiddlewarePasswdBox().getText());
		
		String readerType = view.getReaderTypeListBox().getSelectedItemText();
		readpoint.setReaderType(readerType);

		if (readerType.equals("BuiltIn")) {
			readpoint.setReaderConnection("http://localhost");
		} else {
			readpoint.setReaderConnection(view.getReaderHostBox().getText());
		}

		if (view.getSpecificAntennaCheckBox().getValue()) {
			readpoint.setAntenna(Integer.parseInt(view
					.getSpecificAntennatListBox().getSelectedItemText()));
		} else {
			readpoint.setAntenna(0);
		}
		readpoint.setPosition(position);
		fireEvent(readpoint);
		view.getNameBox().setValue(null);
	}

	@Override
	public void setVisible(boolean visible) {
		view.setVisible(visible);
	}

	@Override
	public void onCloseClick() {
		view.setVisible(false);
	}

	private void fireEvent(ReadPoint readpoint) {
		switch (currentMode) {
		case CREATE:
			eventBus.fireEvent(new ReadPointEvent(readpoint, Mode.CREATE,
					parentTask));
			break;
		case CHANGE:
			eventBus.fireEvent(new ReadPointEvent(readpoint, Mode.CHANGE,
					parentTask, currentReadPoint));
			break;
		default:
			break;
		}
	}

	private void clear() {
		view.getNameBox().setValue(null);
		view.getRemoteCheckbox().setValue(false);
		view.getMiddlewareHostBox().setText("http://localhost");
		view.getMiddlewareHostBox().setEnabled(false);
		view.getReaderTypeListBox().setSelectedIndex(0);
		view.getReaderHostBox().setText("http://localhost");
		view.getReaderHostBox().setEnabled(false);
		view.getSpecificAntennaCheckBox().setValue(false);
		view.getSpecificAntennatListBox().setSelectedIndex(0);
		view.getSpecificAntennatListBox().setEnabled(false);
		
	}

	private int findItem(String value, ListBox in) {
		for (int i = 0; i < in.getItemCount(); i++) {
			if (in.getItemText(i).equals(value)) {
				return i;
			}
		}
		return 0;
	}

}
