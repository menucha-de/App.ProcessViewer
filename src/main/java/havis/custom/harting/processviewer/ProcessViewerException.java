package havis.custom.harting.processviewer;

public class ProcessViewerException extends Exception {

	private static final long serialVersionUID = 1L;

	public ProcessViewerException(String message) {
		super(message);
	}

	public ProcessViewerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProcessViewerException(Throwable cause) {
		super(cause);
	}
}