package havis.app.processviewer.ui.resourcebundle;

import com.google.gwt.resources.client.CssResource;

public interface CssResources extends CssResource {

	String label();

	String listSelected();

	String idValue();

	String idTextField();

	String idLabelField();

	String leftPosition();

	String middlePosition();

	String rightPosition();

	String taskSelected();

	@ClassName("info-html-background")
	String infoHtmlBackground();

	@ClassName("info-html-close")
	String infoHtmlClose();

	@ClassName("info-html-dialog")
	String infoHtmlDialog();

	@ClassName("webui-ListBox")
	String webuiListBox();

	@ClassName("webui-TextBox")
	String webuiTextBox();

	@ClassName("webui-Apply-Button")
	String webuiApplyButton();

	String commonLabel();

	String commonLabelTask();

	String commonLabelBold();

	String deleteButton();

	String deleteReadPointButton();

	String deleteReadPointButtonEnabled();

	String deleteReadPointButtonMinus();

	String addButton();

	String pwdToggle();

	@ClassName("webui-message-popup-panel")
	String webuiMessagePopupPanel();

	@ClassName("webui-message-popup-panel-error-dot")
	String webuiMessagePopupPanelErrorDot();

	String readPointStyle();

	String readPointStyleEnabled();

	String fileButton();

	String buttons();

	String upload();

	@ClassName("clear-button")
	String clearButton();

	@ClassName("export-button")
	String exportButton();

	@ClassName("refresh-button")
	String refreshButton();

	String infoStyle();

	String quickDetails();
	
	String connect();

}