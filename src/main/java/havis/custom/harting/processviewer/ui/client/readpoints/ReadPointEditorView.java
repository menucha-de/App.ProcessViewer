package havis.custom.harting.processviewer.ui.client.readpoints;

import havis.custom.harting.processviewer.ReadPoint;
import havis.custom.harting.processviewer.Task;

import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;

public interface ReadPointEditorView {

	public void setPresenter(Presenter presenter);

	public TextBox getNameBox();

	public ToggleButton getRemoteCheckbox();

	public TextBox getMiddlewareHostBox();
	public TextBox getMiddlewareUserBox();
	public TextBox getMiddlewarePasswdBox();

	public ListBox getReaderTypeListBox();

	public TextBox getReaderHostBox();

	public ToggleButton getSpecificAntennaCheckBox();

	public ListBox getSpecificAntennatListBox();

	public void setVisible(boolean visible);

	interface Presenter {
		void onAcceptClick();

		void onCloseClick();

		void setCurrentMode(Mode currentMode);
		
		void setCurrentPosition (int position);
		
		void setParentTask (Task task);
		
		void setCurrentReadPoint(ReadPoint readppoint);

		void setVisible(boolean visible);
	}

	public enum Mode {
		CREATE, CHANGE;
	}

}
