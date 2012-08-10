package co.mwater.clientapp.ui.map;

import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import co.mwater.clientapp.db.SourcesTable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class SourceItemizedOverlay extends ItemizedOverlay<OverlayItem> {
	Cursor sourceCursor;
	int columnCode, columnName, columnLat, columnLong;
	SourceTapped sourceTapped;

	public SourceItemizedOverlay(Drawable marker, Cursor sourceCursor,
			SourceTapped sourceTapped) {
		super(boundCenterBottom(marker));
		this.sourceCursor = sourceCursor;
		this.sourceTapped = sourceTapped;

		columnCode = sourceCursor.getColumnIndex(SourcesTable.COLUMN_CODE);
		columnName = sourceCursor.getColumnIndex(SourcesTable.COLUMN_NAME);
		columnLat = sourceCursor.getColumnIndex(SourcesTable.COLUMN_LAT);
		columnLong = sourceCursor.getColumnIndex(SourcesTable.COLUMN_LONG);
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		sourceCursor.moveToPosition(i);
		double latitude = sourceCursor.getDouble(columnLat);
		double longitude = sourceCursor.getDouble(columnLong);
		String code = sourceCursor.getString(columnCode);
		String name = sourceCursor.getString(columnName);

		OverlayItem newItem = new OverlayItem(new GeoPoint((int) (latitude * 1000000), (int) (longitude * 1000000)),
				code, name != null ? name : "");
		return newItem;
	}

	@Override
	protected boolean onTap(int index)
	{
		if (sourceTapped != null) {
			sourceCursor.moveToPosition(index);
			long id = sourceCursor.getLong(sourceCursor.getColumnIndex(SourcesTable.COLUMN_ID));
			sourceTapped.onSourceTapped(id);
			// return true to indicate we've taken care of it
			return true;
		}
		return false;
	}

	@Override
	public int size() {
		return sourceCursor.getCount();
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (!shadow)
			super.draw(canvas, mapView, shadow);
	}

	public interface SourceTapped {
		void onSourceTapped(long id);
	}
}
