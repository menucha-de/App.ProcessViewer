package havis.custom.harting.processviewer.ui.resourcebundle;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.ImageResource;

public interface AppResources extends ClientBundle {

	public static final AppResources INSTANCE = GWT.create(AppResources.class);

	@Source("resources/CssResources.css")
	CssResources css();

	@Source("resources/close.png")
	ImageResource close();

	@Source("resources/default.png")
	ImageResource defaultTask();

	@Source("resources/add.png")
	ImageResource add();

	@Source("resources/readpoint.png")
	ImageResource readpoint();

	@Source("resources/context_opened.png")
	DataResource contextOpened();

	@Source("resources/context_closed.png")
	DataResource contextClosed();

	@Source("resources/icon_error.png")
	ImageResource errorIcon();

	@Source("resources/upload.png")
	DataResource upload();

	@Source("resources/LLRP_List_Export.png")
	ImageResource export();

	@Source("resources/BT_40x40_Delete.png")
	ImageResource clear();

	@Source("resources/delete_task.png")
	ImageResource delete();

	@Source("resources/list_refresh.png")
	ImageResource refresh();

	@Source("resources/readpoint_info.png")
	ImageResource info();

	@Source("resources/context2.png")
	ImageResource connect();

}