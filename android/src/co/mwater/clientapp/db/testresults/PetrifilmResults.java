package co.mwater.clientapp.db.testresults;

import org.json.JSONException;
import org.json.JSONObject;

public class PetrifilmResults extends Results {
	public Integer autoEcoli, autoTC, autoOther;
	public Integer manualEcoli, manualTC, manualOther;
	public Integer autoAlgo;

	public PetrifilmResults() {
	}

	public PetrifilmResults(String json) {
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
		if (manualEcoli == null && autoEcoli == null)
			return Risk.UNSPECIFIED;

		int ecoli;
		if (manualEcoli != null)
			ecoli = manualEcoli;
		else
			ecoli = autoEcoli;

		if (ecoli == 0)
			return getEColi100mLRisk(dilution * 100 - 1);
		
		return getEColi100mLRisk(ecoli * dilution * 100);
	}

	@Override
	public String toJson() {
		try {
			JSONObject jt = new JSONObject();
			jt.put("autoEcoli", autoEcoli);
			jt.put("autoTC", autoTC);
			jt.put("autoOther", autoOther);

			jt.put("manualEcoli", manualEcoli);
			jt.put("manualTC", manualTC);
			jt.put("manualOther", manualOther);

			jt.put("autoAlgo", autoAlgo);

			return jt.toString();
		} catch (JSONException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public void fromJson(String json) {
		try {
			JSONObject jt = new JSONObject(json);
			this.autoEcoli = (Integer) jt.opt("autoEcoli");
			this.autoTC = (Integer) jt.opt("autoTC");
			this.autoOther = (Integer) jt.opt("autoOther");

			this.manualEcoli = (Integer) jt.opt("manualEcoli");
			this.manualTC = (Integer) jt.opt("manualTC");
			this.manualOther = (Integer) jt.opt("manualOther");

			this.autoAlgo = (Integer) jt.opt("autoAlgo");
		} catch (JSONException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
