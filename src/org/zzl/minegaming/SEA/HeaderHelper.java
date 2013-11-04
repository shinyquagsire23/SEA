package org.zzl.minegaming.SEA;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class HeaderHelper {

	private HeaderHelper(){}
	public static String GetHeader(String name)
	{
		if(name.contains("<") && name.contains(">")) //Global Header ex import <default.rbh>
			return GetGlobalHeader(StringUtils.fixFileLocation(name.replaceAll("\"", "").replace("<", "").replace(">", "")));
		else //Local Header ex import "headers/pokemon.rbh" or import something.rbh
			return GetLocalHeader(StringUtils.fixFileLocation(name.replaceAll("\"", ""))); //We want to make sure that the file name works reguardless of OS
	}
	
	public static String GetLocalHeader(String name)
	{
		File file = new File(GlobalVars.WorkingDir + File.separator + name);
		URI uri = file.toURI();
		byte[] bytes = null;
		try
		{
		          bytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(uri));
		}
		catch(IOException e) 
		{ 
			return "ERROR! " + java.nio.file.Paths.get(uri).getFileName();  
		}
		  
		return new String(bytes);
	}
	
	public static String GetGlobalHeader(String name)
	{
		File file = new File(GlobalVars.MainDir + File.separator + name);
		URI uri = file.toURI();
		byte[] bytes = null;
		try
		{
		          bytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(uri));
		}
		catch(IOException e) 
		{ 
			return "ERROR! " + java.nio.file.Paths.get(uri).getFileName(); 
		}
		  
		return new String(bytes);
	}
}
