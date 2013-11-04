package org.zzl.minegaming.SEA;

public class Command 
{
	public String Name;
	public String Description;
	public int TotalSize;
	public int NumParams;
	public int HexCode;
	public String ParamFormat;
	public Command(String Name, int Hex, String Description, int TotalSize, 
			int NumParams, String ParamFormat)
	{
		this.Name = Name;
		this.HexCode = Hex;
		this.Description = Description;
		this.TotalSize = TotalSize;
		this.NumParams = NumParams;
		this.ParamFormat = ParamFormat;
	}
	
	public Command(String Name, int Hex, String Description, int TotalSize)
	{
		new Command(Name,Hex,Description,TotalSize, 0, "");
	}
}
