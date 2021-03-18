package havis.app.processviewer.ui.client.tasks;

import havis.app.processviewer.Task;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextBox;

public interface TaskEditorView {

	public void setPresenter(Presenter presenter);

	public TextBox getNameBox();
	public Image getImage();

	public void setVisible(boolean visible);

	interface Presenter {
		void onAcceptClick();

		void onCloseClick();

		void setCurrentMode(Mode currentMode);

		void setOldTask(Task task);

		void setVisible(boolean visible);
		
		void setImageUrl(String url);
		
		void clear();
	}

	public enum Mode {
		CREATE, CHANGE;
	}

}
