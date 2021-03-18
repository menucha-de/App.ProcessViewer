package havis.app.processviewer.ui.resourcebundle;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.Constants;

public interface ConstantsResource extends Constants {

	public static final ConstantsResource INSTANCE = GWT.create(ConstantsResource.class);

	String header();
	String buttonApply();
	
	String taskHeader();
	String name();
	String icon();
	
	String readpointHeader();
	String remoteMiddleware();
	String middlewareHost();
	String readerType();
	String readerHost();
	String specificAntenna();
	
	String expandLog();
	String collapseLog();
	
	
}