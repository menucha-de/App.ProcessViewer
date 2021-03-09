package havis.custom.harting.processviewer.ui.client.readpoints;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

public class ReadPointEditor extends Composite implements ReadPointEditorView {
	private static SubscriberEditorUiBinder uiBinder = GWT
			.create(SubscriberEditorUiBinder.class);

	interface SubscriberEditorUiBinder extends
			UiBinder<Widget, ReadPointEditor> {
	}

	@UiField
	TextBox nameBox;

	@UiField
	ToggleButton remoteMiddleware;

	@UiField
	TextBox middlewareHost;
	
	@UiField
	TextBox user;
	
	@UiField
	TextBox passwd;

	@UiField
	ListBox readerType;

	@UiField
	TextBox readerHost;

	@UiField
	ToggleButton specificAntenna;

	@UiField
	ListBox antenna;

	private Presenter presenter;

	public ReadPointEditor() {
		initWidget(uiBinder.createAndBindUi(this));
		onRemoteClick(null);
		onAntennaClick(null);
		onReaderTypeChange(null);
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

	@Override
	public ToggleButton getRemoteCheckbox() {
		return remoteMiddleware;
	}

	@Override
	public TextBox getMiddlewareHostBox() {
		return middlewareHost;
	}
	
	@Override
	public TextBox getMiddlewareUserBox() {
		return user;
	}
	
	@Override
	public TextBox getMiddlewarePasswdBox() {
		return passwd;
	}	

	@Override
	public ListBox getReaderTypeListBox() {
		return readerType;
	}

	@Override
	public TextBox getReaderHostBox() {
		return readerHost;
	}

	@Override
	public ToggleButton getSpecificAntennaCheckBox() {
		return specificAntenna;
	}

	@Override
	public ListBox getSpecificAntennatListBox() {
		return antenna;
	}

	@UiHandler("remoteMiddleware")
	public void onRemoteClick(ClickEvent clicked) {
		if (!remoteMiddleware.getValue()) {
			middlewareHost.setText("http://localhost");
			middlewareHost.setEnabled(false);
			onMiddlewareChanged(null);
		} else {
			middlewareHost.setEnabled(true);
		}
	}
	
	@UiHandler("specificAntenna")
	public void onAntennaClick(ClickEvent clicked) {
		if (!specificAntenna.getValue()){
			antenna.setItemText(0, "");
			antenna.setSelectedIndex(0);
			antenna.setEnabled(false);
		} else {
			antenna.setItemText(0, "1");
			antenna.setSelectedIndex(0);
			antenna.setEnabled(true);
		}
	}
	
	@UiHandler("readerType")
	public void onReaderTypeChange(ChangeEvent changed){
		if (readerType.getSelectedItemText().equals("BuiltIn")){
			readerHost.setText(middlewareHost.getText());
			readerHost.setEnabled(false);
		} else {
			readerHost.setText("tcp://{host}:{port}");
			readerHost.setEnabled(true);
		}
	}
	
	@UiHandler("middlewareHost")
	public void onMiddlewareChanged(ChangeEvent changed){
		if (readerType.getSelectedItemText().equals("BuiltIn")){
			readerHost.setText(middlewareHost.getText());
		}
	}
	
	

}