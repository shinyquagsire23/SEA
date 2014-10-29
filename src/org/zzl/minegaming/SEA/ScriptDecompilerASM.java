package org.zzl.minegaming.SEA;

import java.util.HashMap;

public class ScriptDecompilerASM {

	//int[] bytecode;
	String script = "";
	Database ddb = Main.Commands;
	private HashMap<String,Integer> SectionLocations = new HashMap<String,Integer>();
	private HashMap<Integer,PointerType> SectionTypes = new HashMap<Integer,PointerType>();
	
	public String decompile(byte[] rom_signed, int datalocation)
	{
		script = "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n@ Script ASM for offset 0x08" + toHexString(datalocation) + " @\n@@@@@@@ Generated using SEA @@@@@@@@\n      @@@@@@@@@@@@@@@@@@@@@@@       \n.include \"scr_names.asm\"\n\nscr_8" + toHexString(datalocation) + ": \n";
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
		System.out.println(toHexString(datalocation));
		
		while(!completelydecompiled)
		{
			i++;
			String toWrite = "";
			
			
			if(mainscriptdone && subscriptdone)
			{
				subscriptdone = false;
				SectionLocations.remove("0x" + String.format("%06X", currentSubscript));
				if(!SectionLocations.isEmpty()) //If the subscript is decompiled, go to the next subscripts/strings
				{
					currentSubscript = (Integer)SectionLocations.values().toArray()[0];
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
				String sectionType = "scrloc_";
				if(lastwasmsg)
					toWrite += "\n";
				toWrite += "\n\n" + sectionType + toDwordString(datalocation,true) + ": "; //If it's just a script reference, mark a section and continue.
				if(SectionTypes.containsKey(datalocation)) 
				{
					if(SectionTypes.get(datalocation) == PointerType.Text) //Check if we have a string here.
					{
						/*sectionType = "scrmsg_";
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
						continue;*/ //We don't want to get anything else after the string, so skip the rest of this loop.
					}
					else
						toWrite += "\n";
				}
			}
			
			lastwasmsg = false;
			int instruction = rom[i];
			Command cmd = ddb.GetCommandInfo(instruction);
			if(cmd.Name.equalsIgnoreCase("error"))
			{
				toWrite += ".byte 0x" + toHexString(instruction) + " ";
				System.out.println("Unknown bytecode " + toHexString(instruction));
			}
			else
				toWrite += ".byte " + cmd.Name + " ";
			int args = 1;

			for(int j = 0; j < cmd.NumParams; j++)
			{
				try
				{
				switch(cmd.ParamFormat.charAt(j)) //Parse parameters
				{
				case '1':
					toWrite += "\n.byte 0x" + toHexString(rom[i + args]) + " ";
					args += 1;
					break;
				case '2':
					toWrite += "\n.word 0x" + toHexString(rom[i + args + 1]) + toHexString(rom[i + args], true) + " ";
					args += 2;
					break;
				case '3':
					String dword = "0x" + byteToStringNoZero(rom[i + args + 3] - 8) + toHexString(rom[i + args + 2], true) + toHexString(rom[i + args + 1], true) + toHexString(rom[i + args], true);
					int dwint = 0;
					boolean tooLarge = false;
					try
					{
						dwint = Integer.parseInt(dword.replace("0x", "").trim(),16);
					}
					catch(Exception e)
					{
						tooLarge = true;
					}
					String sectionType = "scrloc_";
					boolean isString = false;
					if(rom[i + args + 4] == ddb.GetCommandInfo("callstd").HexCode && !tooLarge) 
					{
						//If the next command is callstd, this is probably a message...
						//SectionLocations.put(dword, dwint);
						//System.out.println("Registered string at "+ dword + "\n0x" + toHexString(dwint));
						//SectionTypes.put(dwint, PointerType.Text);
						sectionType = "scrmsg_";
						isString = true;
					}
					else if((rom[i] == ddb.GetCommandInfo("goto").HexCode || rom[i] == ddb.GetCommandInfo("call").HexCode || rom[i] == ddb.GetCommandInfo("if1").HexCode || rom[i] == ddb.GetCommandInfo("if2").HexCode) && !tooLarge)
					{
						SectionLocations.put(dword, dwint);
						SectionTypes.put(dwint, PointerType.Script);
					}
					else if(rom[i] == ddb.GetCommandInfo("applymovement").HexCode && !tooLarge)
					{
						SectionLocations.put(dword, dwint);
						SectionTypes.put(dwint, PointerType.Movement);
					}
					if(SectionLocations.containsKey(dword) || isString)
					{
						System.out.println("Found section at " + dword + "! Type: " + sectionType.replace("_", ""));
						toWrite += "\n.long " + dword.replace("0x", sectionType);
					}
					else
						toWrite += "\n.long " + dword + " ";
					break;
				case 0:
				default:
					break;
				}
				}
				catch(Exception e){e.printStackTrace();}
			}
			
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
					//We don't want strings for now.
					currentSubscript = (Integer)SectionLocations.values().toArray()[0];
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
