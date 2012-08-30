package co.mwater.clientapp.db.testresults;

import java.security.InvalidParameterException;

public abstract class Results {
	public static Results getResults(TestType testType, String results) throws InvalidParameterException {
		switch (testType) {
		case PETRIFILM:
			return new PetrifilmResults(results);
		case TEN_ML_COLILERT:
			return new TenMLColilertResults(results);
		case HUNDRED_ML_ECOLI:
			return new HundredMLEColiResults(results);
		case CHLORINE:
			return new ChlorineResults(results);
		default:
			throw new InvalidParameterException("Test type unknown");
		}
	}

	public abstract Risk getRisk(int dilution);

	public abstract String toJson();

	public abstract void fromJson(String json);

	protected Risk getEColi100mLRisk(int count) {
		if (count == 0)
			return Risk.BLUE;
		if (count < 10)
			return Risk.GREEN;
		if (count < 100)
			return Risk.YELLOW;
		if (count < 1000)
			return Risk.ORANGE;
		return Risk.RED;
	}
}