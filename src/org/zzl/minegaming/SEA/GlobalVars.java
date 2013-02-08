package org.zzl.minegaming.SEA;

import java.io.File;

public class GlobalVars {
	public static String DebugCommand = "gvbam %f";
	public static boolean FileOpened = false;
	public static String FileLoc = "";
	public static String LastDir = File.pathSeparator +"home" + File.pathSeparator + "maxamillion" + File.pathSeparator + "ROM Hacking" + File.pathSeparator;
	public static byte[] ROM = new byte[0x2000000];
	public static byte[] NewROM = new byte[0x2000000];
	public static String SettingsDir = DefaultDirectory() + File.pathSeparator + "settings" + File.pathSeparator;
	public static String MainDir = DefaultDirectory();
	
	private static String DefaultDirectory()
	{
	    String OS = System.getProperty("os.name").toUpperCase();
	    if (OS.contains("WIN"))
	        return System.getenv("APPDATA") + "\\.sea";
	    else if (OS.contains("MAC"))
	        return System.getProperty("user.home") + "/Library/Application "
	                + "Support/.sea";
	    else if (OS.contains("NUX"))
	        return System.getProperty("user.home") + "/.sea";
	    return System.getProperty("user.dir");
	}
}
