package co.mwater.clientapp.dbsync;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Simple data slice representing the entire database
 * 
 * @author Clayton
 * 
 */
public class CompleteDataSlice implements DataSlice {
	public String getSliceId() {
		return "";
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		return;
	}

	public static final Parcelable.Creator<CompleteDataSlice> CREATOR = new Parcelable.Creator<CompleteDataSlice>() {
		public CompleteDataSlice createFromParcel(Parcel in) {
			return new CompleteDataSlice();
		}

		public CompleteDataSlice[] newArray(int size) {
			return new CompleteDataSlice[size];
		}
	};
}
