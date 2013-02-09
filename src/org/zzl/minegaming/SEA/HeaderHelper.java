package org.zzl.minegaming.SEA;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class HeaderHelper {

	private HeaderHelper(){}
	public static String GetHeader(String name)
	{
		File file = new File(GlobalVars.MainDir + File.separator + name);
		URI uri = file.toURI();
		byte[] bytes = null;
		try
		{
		          bytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(uri));
		}
		catch(IOException e) { e.printStackTrace(); return "derp"; }
		  
		return new String(bytes);
	}
}
