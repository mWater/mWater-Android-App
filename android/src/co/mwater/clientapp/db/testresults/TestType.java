package co.mwater.clientapp.db.testresults;

import android.annotation.SuppressLint;
import java.util.HashMap;
import java.util.Map;

public enum TestType {
	PETRIFILM(0),
	TEN_ML_COLILERT(1),
	HUNDRED_ML_ECOLI(2);

	@SuppressLint("UseSparseArrays")
	private static final Map<Integer, TestType> intToTypeMap = new HashMap<Integer, TestType>();
	
	static {
		for (TestType type : TestType.values()) {
			intToTypeMap.put(type.value, type);
		}
	}
	private final int value;

	public int getValue() {
		return value;
	}
	
	private TestType(int value) {
		this.value = value;
	}

	public static TestType fromInt(int i) {
		TestType type = intToTypeMap.get(Integer.valueOf(i));
		if (type == null)
			return null;
		return type;
	}
}