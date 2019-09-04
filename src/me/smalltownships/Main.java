package me.smalltownships;

import java.text.SimpleDateFormat;

public class Main {
	
	/**
	 * Get the current time, formatted for user readability.
	 * A test function.
	 * 
	 * @return The formatted time
	 */
	public static String getTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		
		return formatter.format(new java.util.Date());
	}
}
