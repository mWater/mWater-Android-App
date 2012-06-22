package com.github.androidimageprocessing.bacteria.db;

import android.database.Cursor;

/**
 * Contains a list of changes to rows in a database up until a certain
 * moment.
 * @author Clayton
 *
 */
public class ChangeSet {
	private String until;
	private Table[] tables;

	public ChangeSet(String until, Table[] tables) {
		this.until = until;
		this.tables = tables;
	}

	/**
	 * Until which moment change set contains
	 */
	public String getUntil() {
		return until;
	}
	
	/**
	 * Tables in the change set in topological order
	 */
	public Table[] getTables() {
		return tables;
	}
	
	/**
	 * Contains the changes for a single table.
	 * @author Clayton
	 *
	 */
	static public class Table {
		/**
		 * Rows to be inserted. Includes row version
		 */
		public Cursor inserts;
		
		/**
		 * Rows to be updated. Includes row version
		 */
		public Cursor updates;
		
		/**
		 * UID only of rows to be deleted
		 */
		public Cursor deletes;
	}
}
