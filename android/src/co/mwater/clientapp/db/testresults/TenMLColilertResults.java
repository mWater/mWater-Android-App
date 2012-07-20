package co.mwater.clientapp.db.testresults;

import org.json.JSONException;
import org.json.JSONObject;

public class TenMLColilertResults extends Results {
	public Boolean ecoli, tc;

	public TenMLColilertResults() {
	}

	public TenMLColilertResults(String json) {
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
		if (ecoli == null)
			return Risk.UNSPECIFIED;

		if (ecoli)
			return Risk.RED;
		return getEColi100mLRisk(dilution * 10 - 1);
	}

	@Override
	public String toJson() {
		try {
			JSONObject jt = new JSONObject();
			jt.put("ecoli", ecoli);
			jt.put("tc", tc);

			return jt.toString();
		} catch (JSONException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public void fromJson(String json) {
		try {
			JSONObject jt = new JSONObject(json);
			this.ecoli = (Boolean) jt.opt("ecoli");
			this.tc = (Boolean) jt.opt("tc");
		} catch (JSONException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
