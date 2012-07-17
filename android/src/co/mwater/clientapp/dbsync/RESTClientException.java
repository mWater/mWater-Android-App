package co.mwater.clientapp.dbsync;

import java.io.IOException;

public class RESTClientException extends Exception {
	public final int responseCode;

	public RESTClientException(IOException innerException) {
		super(innerException);
		responseCode = -1;
	}

	public RESTClientException(int responseCode, String message, IOException innerException) {
		super(message, innerException);
		this.responseCode = responseCode;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -7423840748549846366L;
}
