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
		default:
			throw new InvalidParameterException("Test type unknown");
		}
	}
	
	public abstract Risk getRisk();
	
	public abstract String toJson();
	
	public abstract void fromJson(String json);
}