package co.mwater.clientapp.dbsync;

import android.database.Cursor;

/**
 * Contains a list of changes to rows in a database up until a certain moment.
 * 
 * @author Clayton
 * 
 */
public class ChangeSet {
	private long until;
	private Table[] tables;

	public ChangeSet(long until, Table[] tables) {
		this.until = until;
		this.tables = tables;
	}

	/**
	 * Until which moment change set contains
	 */
	public long getUntil() {
		return until;
	}

	/**
	 * Tables in the change set in topological order
	 */
	public Table[] getTables() {
		return tables;
	}

	/**
	 * Always call when done with changeset
	 */
	public void close() {
		for (Table table : tables) {
			if (table.upserts != null)
				table.upserts.close();
			if (table.deletes != null)
				table.deletes.close();
		}
	}

	/**
	 * Get a table by name
	 * 
	 * @returns null if not included
	 */
	public Table getTable(String tableName) {
		for (Table table : tables) {
			if (table.tableName.equals(tableName))
				return table;
		}
		return null;
	}

	/**
	 * Contains the changes for a single table.
	 * 
	 * @author Clayton
	 * 
	 */
	static public class Table {
		public String tableName;

		/**
		 * Rows to be updated. Includes row version
		 */
		public Cursor upserts;

		/**
		 * UID only of rows to be deleted
		 */
		public Cursor deletes;
	}
}
