package co.mwater.clientapp.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PreferenceUtils {
	public static String listToString(List<String> items) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < items.size(); i++)
		{
			if (i > 0)
				sb.append(",");
			sb.append(items.get(i));
		}
		return sb.toString();
	}

	public static List<String> stringToList(String str) {
		if (str.length() == 0)
			return new ArrayList<String>();
		
		return new ArrayList<String>(Arrays.asList(str.split(",")));
	}

}
