package com.kreative.vexillo.ui;

public class OSUtils {
	private static String osName;
	private static String osVersion;
	private static boolean isMacOS;
	private static boolean isWindows;
	static {
		try {
			osName = System.getProperty("os.name");
			osVersion = System.getProperty("os.version");
			isMacOS = osName.toUpperCase().contains("MAC OS");
			isWindows = osName.toUpperCase().contains("WINDOWS");
		} catch (Exception e) {
			osName = "";
			osVersion = "";
			isMacOS = false;
			isWindows = false;
		}
	}
	
	public static String getOSName() {
		return osName;
	}
	
	public static String getOSVersion() {
		return osVersion;
	}
	
	public static boolean isMacOS() {
		return isMacOS;
	}
	
	public static boolean isWindows() {
		return isWindows;
	}
}