package com.github.androidimageprocessing.bacteria.dbsync;

/**
 * Interface implemented by a client database which is capable of
 * generating and taking in change sets
 * @author Clayton
 *
 */
public interface SyncClient {
	/**
	 * Gets all pending changes
	 * @return pending changes or null if none
	 */
	ChangeSet getChangeSet();
	
	/**
	 * Indicates that a particular set of changes that happened 
	 * on the client before "until" have been uploaded
	 * @param until taken from changeset that was sent
	 */
	void markChangeSetSent(long until);

	/**
	 * Attempts to apply the specified changeset for a slice
	 * @throws PendingChangesException if changes are pending to be sent
	 */
	void applyChangeSet(ChangeSet changeSet, DataSlice dataSlice) throws PendingChangesException;

	/**
	 * Gets the moment of the last changes for a slice have been downloaded 
	 * @return "until" sequence number, or 0 if never downloaded
	 */
	long getUntil(DataSlice dataSlice);	
}
