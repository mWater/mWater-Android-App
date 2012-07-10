package co.mwater.clientapp.dbsync;

/**
 * Exception thrown by a sync server
 * @author Clayton
 *
 */
public class SyncServerException extends Exception {
	private static final long serialVersionUID = -1228523501220756392L;

	public SyncServerException() {
		super();
	}
	
	public SyncServerException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}
}
