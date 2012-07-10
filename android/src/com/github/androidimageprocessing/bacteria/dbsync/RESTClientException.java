package com.github.androidimageprocessing.bacteria.dbsync;

import java.io.IOException;

public class RESTClientException extends Exception {
	public final int responseCode;

	public RESTClientException(IOException innerException) {
		super(innerException);
		responseCode = -1;
	}

	public RESTClientException(int responseCode, IOException innerException) {
		super(innerException);
		this.responseCode = responseCode;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -7423840748549846366L;
}
