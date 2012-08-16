package co.mwater.clientapp.ui.map;

import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.SourcesTable;
import co.mwater.clientapp.db.testresults.Risk;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class SourceItemizedOverlay extends ItemizedOverlay<OverlayItem> {
	Cursor sourceCursor;
	int columnCode, columnName, columnLat, columnLong, columnRisk;
	SourceTapped sourceTapped;

	HashMap<Risk, Drawable> markers = new HashMap<Risk, Drawable>();
	
	public SourceItemizedOverlay(Context context, Drawable marker, Cursor sourceCursor,
			SourceTapped sourceTapped) {
		super(boundCenterBottom(marker));
		this.sourceCursor = sourceCursor;
		this.sourceTapped = sourceTapped;

		markers.put(Risk.UNSPECIFIED, boundCenterBottom(context.getResources().getDrawable(R.drawable.marker_0)));
		markers.put(Risk.BLUE, boundCenterBottom(context.getResources().getDrawable(R.drawable.marker_1)));
		markers.put(Risk.GREEN, boundCenterBottom(context.getResources().getDrawable(R.drawable.marker_2)));
		markers.put(Risk.YELLOW, boundCenterBottom(context.getResources().getDrawable(R.drawable.marker_3)));
		markers.put(Risk.ORANGE, boundCenterBottom(context.getResources().getDrawable(R.drawable.marker_4)));
		markers.put(Risk.RED, boundCenterBottom(context.getResources().getDrawable(R.drawable.marker_5)));
		
		columnCode = sourceCursor.getColumnIndex(SourcesTable.COLUMN_CODE);
		columnName = sourceCursor.getColumnIndex(SourcesTable.COLUMN_NAME);
		columnLat = sourceCursor.getColumnIndex(SourcesTable.COLUMN_LAT);
		columnLong = sourceCursor.getColumnIndex(SourcesTable.COLUMN_LONG);
		columnRisk = sourceCursor.getColumnIndex(SourcesTable.COLUMN_RISK);
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
		
		// Get risk
		Risk risk = null;
		if (sourceCursor.isNull(columnRisk))
			risk = Risk.UNSPECIFIED;
		else 
			risk = Risk.fromInt(sourceCursor.getInt(columnRisk));

		newItem.setMarker(markers.get(risk));
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
