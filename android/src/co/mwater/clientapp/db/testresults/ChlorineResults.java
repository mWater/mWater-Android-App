package co.mwater.clientapp.db.testresults;

import org.json.JSONException;
import org.json.JSONObject;

public class ChlorineResults extends Results {
	public Boolean present;
	public Double mgPerL;

	public ChlorineResults() {
	}

	public ChlorineResults(String json) {
		if (json != null)
			fromJson(json);
	}

	/**
	 * Risk level of test: 0 = unspecified 1 = blue=0/100ml 2 = green=0-10/100ml
	 * 3 = yellow=10-100/100ml 4 = orange=1-10/1ml 5 = red=>10/1ml
	 * 
	 * @return
	 */
	@Override
	public Risk getRisk(int dilution) {
		return Risk.UNSPECIFIED;
	}

	@Override
	public String toJson() {
		try {
			JSONObject jt = new JSONObject();
			jt.put("present", present);
			jt.put("mgPerL", mgPerL);

			return jt.toString();
		} catch (JSONException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public void fromJson(String json) {
		try {
			JSONObject jt = new JSONObject(json);
			this.present = (Boolean) jt.opt("present");
			this.mgPerL = jt.has("mgPerL") ? (Double) jt.optDouble("mgPerL") : null;
		} catch (JSONException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
