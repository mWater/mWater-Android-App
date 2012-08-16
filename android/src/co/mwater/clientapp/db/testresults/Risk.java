package co.mwater.clientapp.db.testresults;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;

public enum Risk {
	UNSPECIFIED(0),
	BLUE(1),
	GREEN(2),
	YELLOW(3),
	ORANGE(4),
	RED(5);

	@SuppressLint("UseSparseArrays")
	private static final Map<Integer, Risk> intToTypeMap = new HashMap<Integer, Risk>();

	static {
		for (Risk type : Risk.values()) {
			intToTypeMap.put(type.value, type);
		}
	}
	private final int value;

	public int getValue() {
		return value;
	}

	private Risk(int value) {
		this.value = value;
	}

	public static Risk fromInt(Integer i) {
		if (i == null)
			return null;
		Risk type = intToTypeMap.get(Integer.valueOf(i));
		if (type == null)
			return null;
		return type;
	}
}