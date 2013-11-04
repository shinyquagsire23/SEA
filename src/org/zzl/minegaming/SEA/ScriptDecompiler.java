package org.zzl.minegaming.SEA;

import java.util.HashMap;

public class ScriptDecompiler {

	//int[] bytecode;
	String script = "";
	Database ddb = Main.Commands;
	private HashMap<String,Integer> SectionLocations = new HashMap<String,Integer>();
	private HashMap<Integer,PointerType> SectionTypes = new HashMap<Integer,PointerType>();
	
	public String decompile(byte[] rom_signed, int datalocation)
	{
		script = "#0x" + toHexString(datalocation) + "\n\n";
		int[] rom = new int[rom_signed.length];
		for(int i = 0; i < rom.length; i++)
		{
			rom[i] = (int) (rom_signed[i] & 0xFF);
		}
		
		//bytecode = rom.
		boolean mainscriptdone = false;
		boolean completelydecompiled = false;
		boolean subscriptdone = false;
		boolean lastwasmsg = false;
		int currentSubscript = 0;
		int i = datalocation - 1;
		while(!completelydecompiled)
		{
			i++;
			String toWrite = "";
			System.out.println(toHexString(datalocation));
			
			if(mainscriptdone && subscriptdone)
			{
				subscriptdone = false;
				SectionLocations.remove("0x" + String.format("%06X", currentSubscript));
				if(!SectionLocations.isEmpty()) //If the subscript is decompiled, go to the next subscripts/strings
				{
					currentSubscript = (int)SectionLocations.values().toArray()[0];
					i = currentSubscript;
					datalocation = i;
				}
				else
				{
					completelydecompiled = true;
					break;
				}
			}
			
			if(completelydecompiled)
				break;
			
			if(SectionLocations.containsValue(datalocation)) //Check if we have a data reference here...
			{
				String sectionType = "loc_";
				if(lastwasmsg)
					toWrite += "\n";
				toWrite += "\n:" + sectionType + toDwordString(datalocation,true) + " "; //If it's just a script reference, mark a section and continue.
				if(SectionTypes.containsKey(datalocation)) 
				{
					if(SectionTypes.get(datalocation) == PointerType.Text) //Check if we have a string here.
					{
						sectionType = "msg_";
						toWrite = "";
						if(lastwasmsg)
							toWrite += "";
						else
							toWrite += "\n";
						toWrite += ":" + sectionType + toDwordString(datalocation,true) + " ";
						int position = 0;
						toWrite += "("; //Begin string with parentheses.
						while(rom[i + position] != 0xFF)
						{
							toWrite += ddb.GetTextFromHex(rom[i + position]);
							position++;
						}
						toWrite += ")";
						
						i += position;
						datalocation += position + 1;
						addLine(toWrite);
						subscriptdone = true;
						lastwasmsg = true;
						continue; //We don't want to get anything else after the string, so skip the rest of this loop.
					}
					else
						toWrite += "\n";
				}
			}
			
			lastwasmsg = false;
			int instruction = rom[i];
			Command cmd = ddb.GetCommandInfo(instruction);
			
			//Command Aliases
			if(cmd.Name.equalsIgnoreCase("preparemsg") && rom[i + 5] == ddb.GetCommandInfo("callstd").HexCode)
			{
				toWrite += "msgbox("; //Start our command alias
				int args = 1;
				
				//Parse the DWord value
				String dword = "0x" + byteToStringNoZero(rom[i + args + 3] - 8) + toHexString(rom[i + args + 2], true) + toHexString(rom[i + args + 1], true) + toHexString(rom[i + args], true);
				int dwint = Integer.parseInt(dword.replace("0x", "").trim(),16);
				SectionLocations.put(dword, dwint);
				System.out.println("Registered string at "+ dword + "\n0x" + toHexString(dwint));
				
				//Register our string in SectionTypes
				SectionTypes.put(dwint, PointerType.Text);
				String sectionType = "msg_";
				System.out.println("Found section at " + dword + "! Type: " + sectionType.replace("_", ""));
				
				//Write the DWord
				toWrite += dword.replace("0x", sectionType);
				toWrite += " " + rom[i + 6] + ")";
				
				//Add to our data counter, and push the new line to our text viewer
				i += 6;
				datalocation += 6;
				addLine(toWrite.trim());
				Main.scriptEditor.setText(script.trim());
				continue;
			}
			
			if(cmd.Name.equalsIgnoreCase("if1") || cmd.Name.equalsIgnoreCase("if2"))
			{
				toWrite += "if("; //Start our command alias
				int args = 1;
				
				toWrite += "0x" + toHexString(rom[i + args]) + " "; //Write our factor thingy
				args += 1;
				
				//Write goto or call depending on the if type
				if(cmd.Name.equalsIgnoreCase("if1"))
					toWrite += "goto ";
				else
					toWrite += "call ";
				
				//Write our location
				String dword = "0x" + byteToStringNoZero(rom[i + args + 3] - 8) + toHexString(rom[i + args + 2], true) + toHexString(rom[i + args + 1], true) + toHexString(rom[i + args], true);
				int dwint = Integer.parseInt(dword.replace("0x", "").trim(),16);
				String sectionType = "loc_";
				
				//Register it
				SectionLocations.put(dword, dwint);
				SectionTypes.put(dwint, PointerType.Script);
				
				System.out.println("Found section at " + dword + "! Type: " + sectionType.replace("_", ""));
				toWrite += dword.replace("0x", sectionType) + ") ";
				
				//Add to our data counter, and push the new line to our text viewer
				i += 5;
				datalocation += 5;
				addLine(toWrite.trim());
				Main.scriptEditor.setText(script.trim());
				continue;
			}
			
			toWrite += cmd.Name + " ";
			int args = 1;
			if(cmd.NumParams > 0)
			{
				toWrite = toWrite.trim() + "("; //Open paremeter parentheses, trim any spaces before it.
			}
			for(int j = 0; j < cmd.NumParams; j++)
			{
				try
				{
				switch(cmd.ParamFormat.charAt(j)) //Parse parameters
				{
				case '1':
					toWrite += "0x" + toHexString(rom[i + args]) + " ";
					args += 1;
					break;
				case '2':
					toWrite += "0x" + toHexString(rom[i + args + 1]) + toHexString(rom[i + args], true) + " ";
					args += 2;
					break;
				case '3':
					String dword = "0x" + byteToStringNoZero(rom[i + args + 3] - 8) + toHexString(rom[i + args + 2], true) + toHexString(rom[i + args + 1], true) + toHexString(rom[i + args], true);
					int dwint = Integer.parseInt(dword.replace("0x", "").trim(),16);
					String sectionType = "loc_";
					if(rom[i + args + 4] == ddb.GetCommandInfo("callstd").HexCode) 
					{
						//If the next command is callstd, this is probably a message...
						SectionLocations.put(dword, dwint);
						System.out.println("Registered string at "+ dword + "\n0x" + toHexString(dwint));
						SectionTypes.put(dwint, PointerType.Text);
						sectionType = "msg_";
					}
					else if(rom[i] == ddb.GetCommandInfo("goto").HexCode || rom[i] == ddb.GetCommandInfo("call").HexCode || rom[i] == ddb.GetCommandInfo("if1").HexCode || rom[i] == ddb.GetCommandInfo("if2").HexCode)
					{
						SectionLocations.put(dword, dwint);
						SectionTypes.put(dwint, PointerType.Script);
					}
					else if(rom[i] == ddb.GetCommandInfo("applymovement").HexCode)
					{
						SectionLocations.put(dword, dwint);
						SectionTypes.put(dwint, PointerType.Movement);
					}
					if(SectionLocations.containsKey(dword))
					{
						System.out.println("Found section at " + dword + "! Type: " + sectionType.replace("_", ""));
						toWrite += dword.replace("0x", sectionType);
					}
					else
						toWrite += dword + " ";
					break;
				case 0:
				default:
					break;
				}
				}
				catch(Exception e){e.printStackTrace();}
			}
			if(cmd.NumParams > 0)
				toWrite = toWrite.trim() + ")"; //Close paremeter parentheses and trim of any spaces.
			if(cmd.TotalSize > 0)
			{
				i += cmd.TotalSize - 1;
				datalocation += cmd.TotalSize - 1;
			}
			datalocation++;
			addLine(toWrite.trim());
			Main.scriptEditor.setText(script.trim());
			
			if(cmd.Name.equals("end") && !mainscriptdone)
			{
				mainscriptdone = true;
				if(!SectionLocations.isEmpty()) //If the main script is decompiled, start on the subscript/strings
				{
					currentSubscript = (int)SectionLocations.values().toArray()[0];
					i = currentSubscript - 1;
					datalocation = currentSubscript;
				}
			}
			else if((cmd.Name.equals("end") || cmd.Name.equals("goto") || cmd.Name.equals("return")) && mainscriptdone)
			{
				subscriptdone = true;
			}
		}
		return script;
	}
	
	public void addLine(String line)
	{
		script = script + line + "\n";
	}
	
	public String toHexString(int b)
	{
		return toHexString(b,false);
	}
	
	public String toHexString(int b, boolean spacing)
	{
		if(spacing)
			return String.format("%02X", Math.abs(b)); //Use absolute value to prevent negative bytes
		else
			return String.format("%X", Math.abs(b));
	}
	
	public String toDwordString(int b, boolean spacing)
	{
		if(spacing)
			return String.format("%06X", Math.abs(b)); //Use absolute value to prevent negative bytes
		else
			return String.format("%X", Math.abs(b));
	}
	
	public String byteToStringNoZero(int b)
	{
		if(b != 0)
			return String.format("%X", Math.abs(b));
		else
			return "";
	}
}
