package havis.app.processviewer.ui.client.details;

import havis.app.processviewer.Field;
import havis.app.processviewer.HistoryEntry;
import havis.app.processviewer.ui.resourcebundle.AppResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class Details extends Composite {
	private AppResources res = AppResources.INSTANCE;
	private static DetailsUiBinder uiBinder = GWT.create(DetailsUiBinder.class);

	interface DetailsUiBinder extends UiBinder<Widget, Details> {
	}

	@UiField
	protected ScrollPanel itemContainer;

	@UiField
	protected TextBox format;

	@UiField
	protected Label formatLabel;

	@UiField
	protected FlowPanel values;

	public Details() {
		initWidget(uiBinder.createAndBindUi(this));
		res.css().ensureInjected();
	}

	public void setContentHeight(String height) {
		itemContainer.setHeight(height);
	};

	public void setValues(HistoryEntry entry) {
		clear();
		formatLabel.setVisible(true);
		format.setVisible(true);
		format.setText(entry.getFormat());
		if (!entry.getFormat().equals("Unknown")) {
			for (Field ai : entry.getFields()) {
				FlowPanel aiPanel = new FlowPanel();
				aiPanel.addStyleName(res.css().idValue());
				String[] split = ai.getName().split("\\.");
				Label name = new Label("ID " + split[split.length - 1]);
				name.addStyleName(res.css().idLabelField());
				aiPanel.add(name);
				TextBox value = new TextBox();
				value.setText(ai.getValue());
				value.addStyleName(res.css().idTextField());
				value.setEnabled(false);
				aiPanel.add(value);
				values.add(aiPanel);
			}
		}
	}

	public void clear() {
		formatLabel.setVisible(false);
		format.setText("");
		format.setVisible(false);
		values.clear();
	}

}
