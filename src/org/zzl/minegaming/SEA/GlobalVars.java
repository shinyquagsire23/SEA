package org.zzl.minegaming.SEA;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class GlobalVars {
	public static String DebugCommand = "gvbam %f";
	public static boolean FileOpened = false;
	public static String FileLoc = "";
	public static String LastDir = File.separator +"home" + File.separator + "maxamillion" + File.separator + "ROM Hacking" + File.separator;
	public static byte[] ROM = new byte[0x2000000];
	public static byte[] NewROM = new byte[0x2000000];
	public static String SettingsDir = DefaultDirectory() + File.separator + "settings" + File.separator;
	public static String MainDir = DefaultDirectory();
	public static String WorkingDir = WorkingDirectory();
	
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
	
	private static String WorkingDirectory()
	{
		String path = GlobalVars.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		try
		{
			String decodedPath = URLDecoder.decode(path, "UTF-8");
			return decodedPath;
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
			System.out.println("Failed to determine working directory; Falling back to default directory " + MainDir);
			return MainDir;
		}
	}
}
