package tools;

public class CoordinateConvert {
	
	public static boolean isCoordinate(String coords) {
		String[] c = coords.replace("%2C", ",").split(",");
	
		try {
			if (c.length == 2 && c[0].length() > 2 && c[1].length() > 2) {
				
				if(Character.isUpperCase(c[0].charAt(c[0].length() - 1)) && Character.isUpperCase(c[1].charAt(c[1].length() - 1))) {
					c[0] = c[0].substring(0, c[0].length() - 1);
					c[1] = c[1].substring(0, c[1].length() - 1);
				}
				
				Double.parseDouble(c[0]);
				Double.parseDouble(c[1]);
				return true;				
			} else {
				return false;
			}
		} catch(NumberFormatException e) {
			return false;
		}
	}
	
	public static String toStrippedCoordinates(String coords) {
		return strip(coords.replace("%2C", ",").replace("%2c", ",").split(","));
	}
	
	public static String strip(String[] c) {
		
		// location = Math.abs(latitude)+(latitude < 0 ? "S" : "N")+","+Math.abs(longitude)+(longitude < 0 ? "W" : "E");
		
		char lastLat = c[0].charAt(c[0].length() - 1);
		char lastLon = c[1].charAt(c[1].length() - 1);
		if(Character.isUpperCase(lastLat) && Character.isUpperCase(lastLon)) {
			c[0] = c[0].substring(0, c[0].length() - 1);
			c[1] = c[1].substring(0, c[1].length() - 1);
		}
		
		Double lat = new Double(c[0]);
		Double lon = new Double(c[1]);
		
		if (lastLat == 'S') {
			lat = -1*lat;
		}
		
		if (lastLon == 'W') {
			lon = -1*lon;
		}
		
		return String.format("%.1f", lat)+','+String.format("%.1f", lon);
	}
	
}
