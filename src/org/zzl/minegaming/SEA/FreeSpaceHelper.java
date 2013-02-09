package org.zzl.minegaming.SEA;

public class FreeSpaceHelper 
{
	public static byte freeSpaceByte = (byte)0xFF;
	public static int find(int length)
	{
		return find(length, 0);
	}
	
	public static int find(int length, int startingLocation)
	{
		byte free = freeSpaceByte;
		if(!GlobalVars.FileOpened)
			free = 0;
		 byte[] searching = new byte[length];
		 for(int i = 0; i < length; i++)
			 searching[i] = free;
		 int numMatches = 0;
		 int freespace = -1;
		 for(int i = startingLocation; i < GlobalVars.ROM.length; i++)
		 {
			 byte b = GlobalVars.ROM[i];
			 byte c = searching[numMatches];
			 if(b == c)
			 {
				 numMatches++;
				 if(numMatches == searching.length - 1)
				 {
					 freespace = i - searching.length + 2;
					 break;
				 }
			 }
			 else
				 numMatches = 0;
		 }
		 return freespace;
	}
}
