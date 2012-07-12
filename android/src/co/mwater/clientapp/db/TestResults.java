package co.mwater.clientapp.db;

import org.json.JSONException;
import org.json.JSONObject;

public class TestResults {
	public static class Petrifilm {
		public Integer autoEcoli, autoTC, autoOther;
		public Integer manualEcoli, manualTC, manualOther;
		public Integer autoAlgo;

		public static String toJson(Petrifilm t) {
			try {
				JSONObject jt = new JSONObject();
				jt.put("autoEcoli", t.autoEcoli);
				jt.put("autoTC", t.autoTC);
				jt.put("autoOther", t.autoOther);

				jt.put("manualEcoli", t.manualEcoli);
				jt.put("manualTC", t.manualTC);
				jt.put("manualOther", t.manualOther);

				jt.put("autoAlgo", t.autoAlgo);

				return jt.toString();
			} catch (JSONException e) {
				throw new IllegalArgumentException(e);
			}
		}

		public static Petrifilm fromJson(String j) {
			try {
				JSONObject jt = new JSONObject(j);
				Petrifilm t = new Petrifilm();
				t.autoEcoli = (Integer)jt.opt("autoEcoli");
				t.autoTC = (Integer)jt.opt("autoTC");
				t.autoOther = (Integer)jt.opt("autoOther");

				t.manualEcoli = (Integer)jt.opt("manualEcoli");
				t.manualTC = (Integer)jt.opt("manualTC");
				t.manualOther = (Integer)jt.opt("manualOther");

				t.autoAlgo = (Integer)jt.opt("autoAlgo");

				return t;
			} catch (JSONException e) {
				throw new IllegalArgumentException(e);
			}
		}
	}
}
