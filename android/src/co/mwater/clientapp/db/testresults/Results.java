package co.mwater.clientapp.db.testresults;

import java.security.InvalidParameterException;


public abstract class Results {
	public static Results getResults(int testType, String results) throws InvalidParameterException {
		switch (testType) {
		case 0:
			return new PetrifilmResults(results);
		default:
			throw new InvalidParameterException("Test type unknown");
		}
	}
	
	public abstract Risk getRisk();
	
	public abstract String toJson();
	
	public abstract void fromJson(String json);
}