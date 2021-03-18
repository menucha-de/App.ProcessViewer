package havis.app.processviewer;

public class CustomSpecException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CustomSpecException(String message, Throwable cause) {
		super(message, cause);
	}

	public CustomSpecException(String message) {
		super(message);
	}

	public CustomSpecException(Throwable cause) {
		super(cause);
	}
	
	

}
