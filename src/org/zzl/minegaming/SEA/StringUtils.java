package org.zzl.minegaming.SEA;

import java.io.File;

public class StringUtils
{
	public static String fixFileLocation(String filePath)
	{
		if(System.getProperty("os.name").toUpperCase().contains("WIN"))
			filePath = filePath.replaceAll("/", File.separator);
		else
			filePath = filePath.replaceAll("\\\\", File.separator);
		return filePath;
	}
}
