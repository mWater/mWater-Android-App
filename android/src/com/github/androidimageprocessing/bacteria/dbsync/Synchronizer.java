package com.github.androidimageprocessing.bacteria.dbsync;

public class Synchronizer {
	SyncClient client;
	SyncServer server;

	public Synchronizer(SyncClient client, SyncServer server) {
		this.client = client;
		this.server = server;
	}

	public void synchronize(DataSlice dataSlice) throws SyncServerException {
		// First upload
		upload();

		boolean repeatDownload; // Set when upload was done after changes gotten
		do {
			// Download change set
			String since = client.getSince(dataSlice);
			ChangeSet downloadSet = server.downloadChangeSet(dataSlice, since);
			if (downloadSet == null)
				return;
			
			// Only repeat if another upload is needed
			repeatDownload = false;

			// Apply, trying until uploads are no longer required
			boolean uploadNeeded;
			do {
				try {
					client.applyChangeSet(downloadSet, dataSlice);
					uploadNeeded = false;
				} catch (PendingChangesException pce) {
					// Perform upload and try again
					uploadNeeded = true;
					repeatDownload = true;
					upload();
				}
			} while (uploadNeeded);
		} while (repeatDownload);
	}

	private void upload() throws SyncServerException {
		ChangeSet uploadSet = client.getChangeSet();
		if (uploadSet != null) {
			server.uploadChangeSet(uploadSet);
			client.markChangeSetSent(uploadSet.getUntil());
		}
	}

	public void cancel() {
		server.cancel();
	}
}
