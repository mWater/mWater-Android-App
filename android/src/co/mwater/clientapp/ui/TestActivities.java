package co.mwater.clientapp.ui;

import co.mwater.clientapp.R;
import co.mwater.clientapp.db.testresults.Risk;
import co.mwater.clientapp.ui.petrifilm.PetrifilmTestDetailsActivity;

public class TestActivities {
	@SuppressWarnings("rawtypes")
	public static Class getDetailActivity(int testType) {
		switch (testType) {
		case 0:
			return PetrifilmTestDetailsActivity.class;
		default:
			return null;
		}
	}

	public static int getRiskColor(Risk risk) {
		switch (risk) {
		case UNSPECIFIED:
			return R.color.risk_unspecified;
		case BLUE:
			return R.color.risk_blue;
		case GREEN:
			return R.color.risk_green;
		case YELLOW:
			return R.color.risk_yellow;
		case ORANGE:
			return R.color.risk_orange;
		case RED:
			return R.color.risk_red;
		default:
			return R.color.risk_unspecified;
		}
	}
}
