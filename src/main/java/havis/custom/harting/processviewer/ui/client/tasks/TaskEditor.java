package havis.custom.harting.processviewer.ui.client.tasks;

import havis.net.ui.shared.client.upload.File;
import havis.net.ui.shared.client.upload.FileList;
import havis.net.ui.shared.client.upload.MultipleFileUpload;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import elemental.client.Browser;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.html.FileReader;

public class TaskEditor extends Composite implements TaskEditorView {
	private static SubscriberEditorUiBinder uiBinder = GWT
			.create(SubscriberEditorUiBinder.class);

	interface SubscriberEditorUiBinder extends UiBinder<Widget, TaskEditor> {
	}

	@UiField
	TextBox nameBox;

	private File imageFile;

	@UiField
	Image image;

	@UiField
	MultipleFileUpload upload;

	private Presenter presenter;

	public TaskEditor() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiHandler("infoCloseLabel")
	void onCloseClick(ClickEvent event) {
		presenter.onCloseClick();
	}

	@UiHandler("acceptButton")
	void onAcceptClick(ClickEvent event) {
		presenter.onAcceptClick();
	}

	@Override
	public TextBox getNameBox() {
		return nameBox;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@UiHandler("uploadButton")
	public void onClick(ClickEvent handler) {
		upload.click();
	}

	@UiHandler("upload")
	void onChooseFile(ChangeEvent event) {
		FileList fl = upload.getFileList();
		imageFile = fl.html5Item(0);
		if (upload.getFilename() != null && !upload.getFilename().isEmpty()) {
			loadImage(imageFile);
		}
	}

	private void loadImage(File file) {
		final FileReader reader = Browser.getWindow().newFileReader();
		reader.addEventListener("load", new EventListener() {

			@Override
			public void handleEvent(Event evt) {
				imageLoaded((String) reader.getResult());
			}
		}, false);
		reader.readAsDataURL(file);
	}

	private void imageLoaded(String image) {
		this.image.setUrl(image);
	}

	@Override
	public Image getImage() {
		return image;
	}
}