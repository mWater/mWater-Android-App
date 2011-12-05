package ca.ilanguage.rhok.imageupload.ui;

import java.io.File;

import android.content.Context;
import android.net.Uri;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class GridImageAdapter extends BaseAdapter {

	private GridViewSourceSelection mContext;
	private Pair<Integer, String>[] imgs;

	public GridImageAdapter(Context c) {
		mContext = (GridViewSourceSelection) c;
		imgs = mContext.query();
	}

	public int getCount() {
		return imgs.length;
	}

	public Object getItem(int position) {
		return imgs[position];
	}

	public long getItemId(int position) {
		return imgs[position].first;
	}

	// create a new ImageView for each item referenced by the Adapter
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		if (convertView == null) { // if it's not recycled, initialize some
			// attributes
			imageView = new ImageView(mContext);
			imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageView.setPadding(8, 8, 8, 8);
		} else {
			imageView = (ImageView) convertView;
		}

		imageView.setImageURI(Uri.fromFile(new File(imgs[position].second)));
		return imageView;
	}

}