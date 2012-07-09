package com.github.androidimageprocessing.bacteria.dbsync;

public class RESTClientException extends Exception {
	public final int responseCode;
	
	public RESTClientException(int responseCode)
	{
		this.responseCode = responseCode;
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7423840748549846366L;
}
