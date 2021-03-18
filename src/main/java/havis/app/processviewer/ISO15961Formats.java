package havis.app.processviewer;

import java.util.HashMap;
import java.util.Map;

public class ISO15961Formats {
	private static Map<Integer, String> formats = new HashMap<Integer, String>();

	static {
		formats.put(9, "GS1 Application Identifier");
	}

	public static String getFormatString(int number) {
		return formats.get(number);
	}

}
