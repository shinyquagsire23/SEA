package org.zzl.minegaming.SEA;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class ScriptDecompilerASMSequential
{

	// int[] bytecode;
	Database ddb = Main.Commands;
	private HashMap<String, Integer> SectionLocations = new HashMap<String, Integer>();
	private HashMap<Integer, PointerType> SectionTypes = new HashMap<Integer, PointerType>();

	public void decompile(byte[] rom_signed, int datalocation, int enddata, ArrayList<Integer> forceScript, ArrayList<Integer> forceLvScript, ArrayList<Integer> forceStrings, ArrayList<Integer> forceJapStrings, ArrayList<Integer> forceMovement, boolean juststrings)
	{
		for(int i : forceScript)
		{
			SectionLocations.put("0x" + toHexString(i,true), i);
			SectionTypes.put(i, PointerType.Script);
		}
		for(int i : forceLvScript)
		{
			SectionLocations.put("0x" + toHexString(i,true), i);
			SectionTypes.put(i, PointerType.LevelScript);
		}
		for(int i : forceStrings)
		{
			SectionLocations.put("0x" + toHexString(i,true), i);
			SectionTypes.put(i, PointerType.Text);
		}
		for(int i : forceJapStrings)
		{
			SectionLocations.put("0x" + toHexString(i,true), i);
			SectionTypes.put(i, PointerType.JapText);
		}
		for(int i : forceMovement)
		{
			SectionLocations.put("0x" + toHexString(i,true), i);
			SectionTypes.put(i, PointerType.Movement);
		}
		try
		{
			File f = new File("scr_8" + toHexString(datalocation) + ".asm");
			if (f.exists())
				f.delete();

			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("scr_8" + toHexString(datalocation) + ".asm", true)));

			out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n@ Script ASM for offset 0x08" + toHexString(datalocation) + " @\n@@@@@@@ Generated using SEA @@@@@@@@\n      @@@@@@@@@@@@@@@@@@@@@@@       \n.include \"scr_names.asm\"\n.org 0x08" + toHexString(datalocation) + "\n\nscr_8" + toHexString(datalocation) + ": ");
			int[] rom = new int[rom_signed.length];
			for (int i = 0; i < rom.length; i++)
			{
				rom[i] = (int) (rom_signed[i] & 0xFF);
			}

			// bytecode = rom.
			boolean mainscriptdone = false;
			boolean completelydecompiled = false;
			boolean subscriptdone = false;
			boolean lastwasmsg = false;
			int currentSubscript = 0;
			int i = datalocation - 1;
			boolean textMode = juststrings;
			/*boolean isMovement = false;
			boolean isLevelScript = false;
			boolean isLevelScriptArray = false;*/
			PointerType currentData = PointerType.Script;
			int origDataLocation = datalocation;
			System.out.println(toHexString(datalocation));
			
			if(juststrings)
			{
				SectionLocations.put("0x08" + toHexString(datalocation), datalocation);
				SectionTypes.put(datalocation, PointerType.Text);
			}
			
			//Find strings
			for(int j = datalocation; j < enddata; j++)
			{
				int leg = 0;
				int illeg = 0;
				int start = j;
				String string = "";
				String japstring = "";
				boolean hasEnd = false;
				if(start == 0x1634B7)
					start = start;

				while(rom[j] != 0xFF && rom[j] != 0x2)
				{
					if(rom[j] > 0xAB)
						leg++;
					else if(rom[j] != 0x0)
						illeg++;
					string += ddb.GetTextFromHex(rom[j]);
					japstring += ddb.GetDesuFromHex(rom[j]);
					j++;
				}
				
				String nospaces = string.replace(" ", "");
				
				if(nospaces.length() == 0)
					nospaces = " ";
				
				if(leg/nospaces.length() <= 0.10) //Maybe Japanese?
					string = japstring;
				
				int ratio = (int) (((float)leg/nospaces.length())*100);
				if(ratio >= 50 && rom[j] != 0x2)
				{
					SectionLocations.put("0x" + toHexString(start,true), start);
					SectionTypes.put(start, PointerType.Text);
				}
				System.out.println(toHexString(start,true) + ": " + (string.length() > 20 ? string : string) + "\tRatio: " + ((float)leg/string.length())*100 + "% legible.");
			}
			
			System.out.println("Doing precompilation scan...");
			int currentSubroutine = datalocation;
			while (!completelydecompiled)
			{
				
				i++;

				if((mainscriptdone && subscriptdone) | completelydecompiled)
				{
					completelydecompiled = false;
					break;
				}

				if (SectionLocations.containsValue(datalocation)) // Check if we have a data reference here...
				{
					PointerType p = SectionTypes.get(datalocation);
					
					textMode = juststrings;
					currentData = p;
					if (p == PointerType.Text | p == PointerType.Braille | p == PointerType.Movement)
						textMode = true;
				}

				lastwasmsg = false;
				int instruction = rom[i];
				if(i == 0x1608D5) //161653
					i = i+1-1;

				Command cmd = ddb.GetCommandInfo(instruction);
				if (cmd.Name.equalsIgnoreCase("error") | textMode)
				{
					if(!SectionLocations.containsValue(currentSubroutine))
					{
						SectionLocations.put("0x" + toHexString(currentSubroutine,true), currentSubroutine);
						SectionTypes.put(currentSubroutine, PointerType.Text);
					}
				}
				
				int b = rom[i+1];
				if(cmd.Name.equalsIgnoreCase("trainerbattle"))
				{
					switch(b)
					{
						case 1:
						case 2:
						case 4:
						case 7:
							cmd.ParamFormat = "122333";
							cmd.NumParams = cmd.ParamFormat.length();
							cmd.TotalSize = 1 + 1 + 2 + 2 + 4 + 4 + 4;
						break;
						
						case 3:
							cmd.ParamFormat = "1223";
							cmd.NumParams = cmd.ParamFormat.length();
							cmd.TotalSize = 1 + 1 + 2 + 2 + 4;
						break;
						
						case 6:
						case 8:
							cmd.ParamFormat = "1223333";
							cmd.NumParams = cmd.ParamFormat.length();
							cmd.TotalSize = 1 + 1 + 2 + 2 + 4 + 4 + 4 + 4;
						break;
						
						case 0:
						case 5:
						case 9:
						default:
							cmd.ParamFormat = "12233";
							cmd.NumParams = cmd.ParamFormat.length();
							cmd.TotalSize = 1 + 1 + 2 + 2 + 4 + 4;
						break;
							
					}
				}
				
				if(cmd.HexCode <= 3) //nop, nop1, return, end
				{
					/*int by = rom[i+4];
					if(by != 0x8)
					{*/
						cmd.NumParams = 0;
						cmd.TotalSize = 1;
						cmd.ParamFormat = "";
					/*}
					else
					{
						by = rom[i+5];
						if(by != 0x0)
						{
							cmd.NumParams = 1;
							cmd.TotalSize = 5;
							cmd.ParamFormat = "3";
						}
						else
						{
							cmd.NumParams = 2;
							cmd.TotalSize = 6;
							cmd.ParamFormat = "31";
						}
					}*/
				}
				
				int args = 1;
				if(currentData == PointerType.LevelScript)
				{
					String dword = "0x" + byteToStringNoZero(rom[i + args + 3] - (rom[i + args + 3] == 8 | rom[i + args + 3] == 9 ? 8 : 0)) + toHexString(rom[i + args + 2], true) + toHexString(rom[i + args + 1], true) + toHexString(rom[i + args], true);
					int dwint = 0;
					try
					{
						dwint = Integer.parseInt(dword.replace("0x", "").trim(), 16);
					}
					catch(Exception e){}
					switch(instruction)
					{
						case 0:
							break;
						case 1:
						case 3:
						case 5:
						case 6:
						case 7:
							SectionLocations.put(dword, dwint);
							SectionTypes.put(dwint, PointerType.Script);
							i += 5 - 1;
							datalocation += 5 - 1;
							break;
						case 2:
						case 4:
							SectionLocations.put(dword, dwint);
							SectionTypes.put(dwint, PointerType.LevelScriptArray);
							i += 5 - 1;
							datalocation += 5 - 1;
							break;
					}
				}
				else if(currentData == PointerType.LevelScriptArray)
				{
					int var = (rom[i + 1] << 8) + rom[i];
					args = 0;
					while(var != 0)
					{
						args += 4;
						
						String dword = "0x" + byteToStringNoZero(rom[i + args + 3] - (rom[i + args + 3] == 8 | rom[i + args + 3] == 9 ? 8 : 0)) + toHexString(rom[i + args + 2], true) + toHexString(rom[i + args + 1], true) + toHexString(rom[i + args], true);
						int dwint = 0;
						try
						{
							dwint = Integer.parseInt(dword.replace("0x", "").trim(), 16);
						}
						catch(Exception e){}
						SectionLocations.put(dword, dwint);
						SectionTypes.put(dwint, PointerType.Script);
						args += 4;
						
						var = (rom[i + args + 1] << 8) + rom[i + args];
					}
					args += 2+1;
				}
				else if(currentData == PointerType.Mart)
				{
					int var = (rom[i + 1] << 8) + rom[i];
					args = 0;
					while(var != 0)
					{
						args += 4;
						var = (rom[i + args + 1] << 8) + rom[i + args];
					}
					args += 2+1;
				}
				else if (currentData == PointerType.Script && !textMode)
				{
					for (int j = 0; j < cmd.NumParams; j++)
					{
						try
						{
							switch (cmd.ParamFormat.charAt(j))
							// Parse parameters
							{
								case '1':
									args += 1;
									break;
								case '2':
									args += 2;
									break;
								case '3':
									String dword = "0x" + byteToStringNoZero(rom[i + args + 3] - (rom[i + args + 3] == 8 | rom[i + args + 3] == 9 ? 8 : 0)) + toHexString(rom[i + args + 2], true) + toHexString(rom[i + args + 1], true) + toHexString(rom[i + args], true);
									int dwint = 0;
									boolean tooLarge = false;
									
									try
									{
										dwint = Integer.parseInt(dword.replace("0x", "").trim(), 16);
									}
									catch (Exception e)
									{
										tooLarge = true;
									}
									
									boolean isString = false;

									if ((rom[i + args + 4] == ddb.GetCommandInfo("callstd").HexCode || rom[i] == ddb.GetCommandInfo("braille").HexCode || rom[i] == ddb.GetCommandInfo("braille2").HexCode || rom[i] == ddb.GetCommandInfo("nop1").HexCode || rom[i] == ddb.GetCommandInfo("goto").HexCode || rom[i] == ddb.GetCommandInfo("return").HexCode || rom[i] == ddb.GetCommandInfo("call").HexCode || rom[i] == ddb.GetCommandInfo("if1").HexCode || rom[i] == ddb.GetCommandInfo("if2").HexCode) || rom[i] == ddb.GetCommandInfo("loadpointer").HexCode  || rom[i] == ddb.GetCommandInfo("trainerbattle").HexCode && !tooLarge)
									{
										if (dwint > 0xA00000)
											tooLarge = true;
									}

									if((rom[i + args + 4] == ddb.GetCommandInfo("callstd").HexCode || rom[i] == ddb.GetCommandInfo("preparemsg").HexCode || (rom[i] == ddb.GetCommandInfo("trainerbattle").HexCode && (j < 5 || (b != 1 && b != 2)))) && !tooLarge )
									{
										//if(!SectionLocations.containsValue(datalocation))
										//{
											SectionLocations.put(dword, dwint);
											SectionTypes.put(dwint, PointerType.Text);
										
											isString = true;
										//}
									}
									else if ((rom[i] == ddb.GetCommandInfo("goto").HexCode || rom[i] == ddb.GetCommandInfo("end").HexCode || rom[i] == ddb.GetCommandInfo("return").HexCode || rom[i] == ddb.GetCommandInfo("nop1").HexCode || rom[i] == ddb.GetCommandInfo("call").HexCode || rom[i] == ddb.GetCommandInfo("if1").HexCode || rom[i] == ddb.GetCommandInfo("if2").HexCode || (rom[i] == ddb.GetCommandInfo("trainerbattle").HexCode && (j >= 5 && (b == 1 || b == 2)))))
									{
										SectionLocations.put(dword, dwint);
										SectionTypes.put(dwint, PointerType.Script);
									}
									else if (rom[i] == ddb.GetCommandInfo("applymovement").HexCode && !tooLarge)
									{
										SectionLocations.put(dword, dwint);
										SectionTypes.put(dwint, PointerType.Movement);
									}
									else if (rom[i] == ddb.GetCommandInfo("pokemart").HexCode && !tooLarge)
									{
										SectionLocations.put(dword, dwint);
										SectionTypes.put(dwint, PointerType.Mart);
									}
									else if(rom[i] == ddb.GetCommandInfo("braille").HexCode && !tooLarge)
									{
										SectionLocations.put(dword, dwint);
										SectionTypes.put(dwint, PointerType.Braille);
										isString = true;
									}

									args += 4;
									break;
								case 0:
								default:
									break;
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}

					if (cmd.TotalSize > 0)
					{
						i += args - 1;
						datalocation += args - 1;
					}
				}

				datalocation++;

				if ((!textMode && currentData == PointerType.Script && cmd.Name.equals("end")) | ((currentData == PointerType.Text || currentData == PointerType.JapText) && toHexString(instruction).equalsIgnoreCase("FF")) | (currentData == PointerType.Movement && toHexString(instruction).equalsIgnoreCase("FE")) | (currentData == PointerType.LevelScript && instruction == 0) | currentData == PointerType.LevelScriptArray | currentData == PointerType.Mart | SectionLocations.containsValue(datalocation))
				{
					textMode = false;
					currentSubroutine = datalocation;
					
					if(datalocation >= 0x17313D)
						Integer.parseInt("0");

					if(!SectionLocations.containsValue(datalocation))
					{
						int position = 0;
						boolean text = false;
						boolean containsEnd = false;
						boolean containsMovement = false;
						boolean japHints = false;
						
						while(rom[datalocation + position] != 0xFF)//ddb.GetCommandInfo("end").HexCode)
						{						
							if(SectionTypes.get(datalocation+position) == PointerType.Text)
								text = true;
							
							if(SectionTypes.get(datalocation+position) == PointerType.Movement)
								containsMovement = true;
							
							if(rom[datalocation + position] == 0x2 || rom[datalocation + position] == 0x3)
							{
								containsEnd = true;
							}
							
							//Look for |FD| strings
							if(rom[datalocation + position] == 0xFD && rom[datalocation + position - 1] != 0x25 && rom[datalocation + position - 1] != 0x26 && rom[datalocation + position+1] >= 1 && rom[datalocation + position + 1] <= 0xA)
							{
								text = true;
								japHints = true;
							}
							
							if(rom[datalocation + position] == 0xAB && rom[datalocation + position + 1] >= 0xFA)
							{
								text = true;
								japHints = true;
							}

							position += 1;
						}
						
						if(containsEnd && japHints)
							text = true;
						
						SectionLocations.put("0x08" + toHexString(datalocation), datalocation);
						if(!text && !containsMovement && datalocation < 0x1C68F3)
							SectionTypes.put(datalocation, PointerType.Script);
						else if(containsEnd && japHints)
							SectionTypes.put(datalocation, PointerType.JapText);
						else if(!containsMovement)
							SectionTypes.put(datalocation, PointerType.Text);
						else if(containsMovement)
							SectionTypes.put(datalocation, PointerType.Text);
					}
					if (datalocation >= enddata)
					{
						mainscriptdone = true;
						subscriptdone = true;
					}
					
				}
			}

			System.out.println("Actual Compile Run...");
			mainscriptdone = false;
			subscriptdone = false;
			i = origDataLocation-1;
			datalocation = origDataLocation;
			boolean isJapText = false;
			while (!completelydecompiled)
			{
				i++;
				String toWrite = "";

				if (mainscriptdone && subscriptdone)
				{
					completelydecompiled = true;
					break;
				}

				if (completelydecompiled)
					break;

				if (SectionLocations.containsValue(datalocation)) // Check if we have a data reference here...
				{
					String sectionType = "scrloc_";
					String extraComments = "";
					PointerType p = SectionTypes.get(datalocation);
					
					textMode = juststrings;	
					if (p == PointerType.Text | textMode)
					{
						sectionType = "scrmsg_";
						int position = 0;
						extraComments = " @ "; 
						while(rom[datalocation + position] != 0xFF)
						{
							extraComments += ddb.GetTextFromHex(rom[i + position]);
							position++;
						}
						textMode = true;
						currentData = PointerType.Text;
					}
					else if (p == PointerType.JapText | textMode)
					{
						sectionType = "scrmsg_";
						int position = 0;
						extraComments = " @ "; 
						while(rom[datalocation + position] != 0xFF)
						{
							extraComments += ddb.GetDesuFromHex(rom[i + position]);
							position++;
						}
						textMode = true;
						isJapText = true;
						currentData = PointerType.JapText;
					}
					else if(p == PointerType.Braille)
					{
						sectionType = "scrbraille_";
						int position = 0;
						extraComments = " @ "; 
						while(rom[datalocation + position] != 0xFF)
						{
							extraComments += ddb.GetTextFromBraille(rom[i + position]);
							position++;
						}
						textMode = true;
						currentData = PointerType.Braille;
					}
					else if(p == PointerType.Movement)
					{
						sectionType = "scrmovement_";
						textMode = true;
						currentData = p;
					}
					else if(p == PointerType.LevelScript)
					{
						sectionType = "levelscr_";
						currentData = p;
					}
					else if(p == PointerType.Mart)
					{
						sectionType = "scrmart_";
						currentData = p;
					}
					else if(p == PointerType.LevelScriptArray)
					{
						sectionType = "levelscrarray_";
						currentData = p;
					}
					else
						currentData = p;
						
					addLine(out, "\n" + sectionType + toDwordString(datalocation, true) + ":" + extraComments);
				}

				lastwasmsg = false;
				int instruction = rom[i];
				if(i >= 0x172F5C) //161653
					i = i+1-1;

				Command cmd = ddb.GetCommandInfo(instruction);
				if (cmd.Name.equalsIgnoreCase("error") | textMode)
				{
					toWrite += ".byte 0x" + toHexString(instruction) + " ";
					if(!textMode)
						System.out.println("Unknown bytecode " + toHexString(instruction) + " at " + toHexString(datalocation));
					//else
						//System.out.println(ddb.GetTextFromHex(instruction));
				}
				else if(currentData == PointerType.Script)
					toWrite += ".byte " + cmd.Name + " ";
				
				int b = rom[i+1];
				if(cmd.Name.equalsIgnoreCase("trainerbattle"))
				{
					switch(b)
					{
						case 1:
						case 2:
						case 4:
						case 7:
							cmd.ParamFormat = "122333";
							cmd.NumParams = cmd.ParamFormat.length();
							cmd.TotalSize = 1 + 1 + 2 + 2 + 4 + 4 + 4;
						break;
						
						case 3:
							cmd.ParamFormat = "1223";
							cmd.NumParams = cmd.ParamFormat.length();
							cmd.TotalSize = 1 + 1 + 2 + 2 + 4;
						break;
						
						case 6:
						case 8:
							cmd.ParamFormat = "1223333";
							cmd.NumParams = cmd.ParamFormat.length();
							cmd.TotalSize = 1 + 1 + 2 + 2 + 4 + 4 + 4 + 4;
						break;
						
						case 0:
						case 5:
						case 9:
						default:
							cmd.ParamFormat = "12233";
							cmd.NumParams = cmd.ParamFormat.length();
							cmd.TotalSize = 1 + 1 + 2 + 2 + 4 + 4;
						break;
							
					}
				}
				
				if(cmd.HexCode <= 3) //nop, nop1, return, end
				{
					/*int by = rom[i+4];
					if(by != 0x8)
					{*/
						cmd.NumParams = 0;
						cmd.TotalSize = 1;
						cmd.ParamFormat = "";
					/*}
					else
					{
						by = rom[i+5];
						if(by != 0x0)
						{
							cmd.NumParams = 1;
							cmd.TotalSize = 5;
							cmd.ParamFormat = "3";
						}
						else
						{
							cmd.NumParams = 2;
							cmd.TotalSize = 6;
							cmd.ParamFormat = "31";
						}
					}*/
				}
				
				int args = 1;
				String extraComments = "";
				
				if(currentData == PointerType.LevelScript)
				{
					String dword = "0x" + byteToStringNoZero(rom[i + args + 3] - (rom[i + args + 3] == 8 | rom[i + args + 3] == 9 ? 8 : 0)) + toHexString(rom[i + args + 2], true) + toHexString(rom[i + args + 1], true) + toHexString(rom[i + args], true);
					int dwint = 0;
					try
					{
						dwint = Integer.parseInt(dword.replace("0x", "").trim(), 16);
					}
					catch(Exception e){}
					switch(instruction)
					{
						case 0:
							toWrite += ".byte end_levelscript\n";
							break;
						case 1:
							toWrite += ".byte lv_setmaptile\n";
							toWrite += ".long " + dword.replace("0x", "scrloc_");
							SectionLocations.put(dword, dwint);
							SectionTypes.put(dwint, PointerType.Script);
							break;
						case 3:
							toWrite += ".byte entermap\n";
							toWrite += ".long " + dword.replace("0x", "scrloc_");
							SectionLocations.put(dword, dwint);
							SectionTypes.put(dwint, PointerType.Script);
							break;
						case 5:
							toWrite += ".byte entermap_or_menu\n";
							toWrite += ".long " + dword.replace("0x", "scrloc_");
							SectionLocations.put(dword, dwint);
							SectionTypes.put(dwint, PointerType.Script);
							break;
						case 6:
							toWrite += ".byte 0x6\n";
							toWrite += ".long " + dword.replace("0x", "scrloc_");
							SectionLocations.put(dword, dwint);
							SectionTypes.put(dwint, PointerType.Script);
							break;
						case 7:
							toWrite += ".byte entermap_or_menu2\n";
							toWrite += ".long " + dword.replace("0x", "scrloc_");
							SectionLocations.put(dword, dwint);
							SectionTypes.put(dwint, PointerType.Script);
							break;
						case 2:
							toWrite += ".byte var_array_screnv1\n";
							toWrite += ".long " + dword.replace("0x", "levelscrarray_");
							SectionLocations.put(dword, dwint);
							SectionTypes.put(dwint, PointerType.LevelScriptArray);
							break;
						case 4:
							toWrite += ".byte var_array_screnv2\n";
							toWrite += ".long " + dword.replace("0x", "levelscrarray_");
							SectionLocations.put(dword, dwint);
							SectionTypes.put(dwint, PointerType.LevelScriptArray);
							break;
					}
					if(instruction != 0)
					{
						i += 5 - 1;
						datalocation += 5 - 1;
					}
				}
				else if(currentData == PointerType.LevelScriptArray)
				{
					int var = (rom[i + 1] << 8) + rom[i];
					int count = 0;
					while(var != 0)
					{
						toWrite += (count == 0 ? "" : "\n") + ".byte 0x" + toHexString(rom[i + count]) + " @0x" + toHexString(rom[i + count + 1], true) + toHexString(rom[i + count], true);
						toWrite += "\n.byte 0x" + toHexString(rom[i + count + 1]) + " ";
						count += 2;
						toWrite += "\n.byte 0x" + toHexString(rom[i + count]) + " @0x" + toHexString(rom[i + count + 1], true) + toHexString(rom[i + count], true);
						toWrite += "\n.byte 0x" + toHexString(rom[i + count + 1]) + " ";
						count += 2;
						
						String dword = "0x" + byteToStringNoZero(rom[i + count + 3] - (rom[i + count + 3] == 8 | rom[i + count + 3] == 9 ? 8 : 0)) + toHexString(rom[i + count + 2], true) + toHexString(rom[i + count + 1], true) + toHexString(rom[i + count], true);
						int dwint = 0;
						try
						{
							dwint = Integer.parseInt(dword.replace("0x", "").trim(), 16);
						}
						catch(Exception e){}
						toWrite += "\n.long " + dword.replace("0x", "scrloc_");
						SectionLocations.put(dword, dwint);
						SectionTypes.put(dwint, PointerType.Script);
						count += 4;
						
						var = (rom[i + count + 1] << 8) + rom[i + count];
					}
					toWrite += "\n.byte 0x" + toHexString(rom[i + count]) + " @0x" + toHexString(rom[i + count + 1], true) + toHexString(rom[i + count], true);
					toWrite += "\n.byte 0x" + toHexString(rom[i + count + 1]) + " ";
					args += count;
					i += args;
					datalocation += args;
				}
				else if(currentData == PointerType.Mart)
				{
					int var = (rom[i + 1] << 8) + rom[i];
					int count = 0;
					while(var != 0)
					{
						//TODO: ITEM NAMES
						toWrite += (count == 0 ? "" : "\n") + ".byte 0x" + toHexString(rom[i + count]) + " @0x" + toHexString(rom[i + count + 1], true) + toHexString(rom[i + count], true);
						toWrite += "\n.byte 0x" + toHexString(rom[i + count + 1]) + " ";
						count += 2;
						
						var = (rom[i + count + 1] << 8) + rom[i + count];
					}
					toWrite += "\n.byte 0x" + toHexString(rom[i + count]) + " @0x" + toHexString(rom[i + count + 1], true) + toHexString(rom[i + count], true);
					toWrite += "\n.byte 0x" + toHexString(rom[i + count + 1]) + " ";
					args += count;
					i += args;
					datalocation += args;
				}
				else if (!textMode)
				{
					for (int j = 0; j < cmd.NumParams; j++)
					{
						try
						{
							switch (cmd.ParamFormat.charAt(j))
							// Parse parameters
							{
								case '1':
									toWrite += "\n.byte 0x" + toHexString(rom[i + args]) + " ";
									args += 1;
									break;
								case '2':
									// toWrite += "\n.word 0x" + toHexString(rom[i + args + 1]) + toHexString(rom[i + args], true) + " "; //DARN YOU GCC AND YOUR CRAPPY ADDRESSING
									toWrite += "\n.byte 0x" + toHexString(rom[i + args]) + " @0x" + toHexString(rom[i + args + 1], true) + toHexString(rom[i + args], true);
									toWrite += "\n.byte 0x" + toHexString(rom[i + args + 1]) + " ";
									args += 2;
									break;
								case '3':
									extraComments = "";
									String dword = "0x" + byteToStringNoZero(rom[i + args + 3] - (rom[i + args + 3] == 8 | rom[i + args + 3] == 9 ? 8 : 0)) + toHexString(rom[i + args + 2], true) + toHexString(rom[i + args + 1], true) + toHexString(rom[i + args], true);
									int dwint = 0;
									boolean tooLarge = false;
									
									try
									{
										dwint = Integer.parseInt(dword.replace("0x", "").trim(), 16);
									}
									catch (Exception e)
									{
										tooLarge = true;
									}
									String sectionType = "scrloc_";
									boolean isString = false;

									if ((rom[i + cmd.TotalSize] == ddb.GetCommandInfo("callstd").HexCode || rom[i] == ddb.GetCommandInfo("braille").HexCode || rom[i] == ddb.GetCommandInfo("braille2").HexCode || rom[i] == ddb.GetCommandInfo("nop1").HexCode || rom[i] == ddb.GetCommandInfo("goto").HexCode || rom[i] == ddb.GetCommandInfo("return").HexCode || rom[i] == ddb.GetCommandInfo("call").HexCode || rom[i] == ddb.GetCommandInfo("if1").HexCode || rom[i] == ddb.GetCommandInfo("if2").HexCode) || rom[i] == ddb.GetCommandInfo("loadpointer").HexCode  || rom[i] == ddb.GetCommandInfo("trainerbattle").HexCode && !tooLarge)
									{
										if (dwint > 0xA00000)
											tooLarge = true;
									}
									if((rom[i + cmd.TotalSize] == ddb.GetCommandInfo("callstd").HexCode || (rom[i] == ddb.GetCommandInfo("trainerbattle").HexCode && (j < 5 || (b != 1 && b != 2)))) && !tooLarge && !SectionLocations.containsValue(datalocation) )
									{
										// If the next command is callstd, this is probably a message...
										SectionLocations.put(dword, dwint);
										SectionTypes.put(dwint, PointerType.Text);
										//System.out.println("Registered string at "+ dword + "\n0x" + toHexString(dwint));
									}
									else if ((rom[i] == ddb.GetCommandInfo("goto").HexCode || rom[i] == ddb.GetCommandInfo("end").HexCode || rom[i] == ddb.GetCommandInfo("return").HexCode || rom[i] == ddb.GetCommandInfo("nop1").HexCode || rom[i] == ddb.GetCommandInfo("call").HexCode || rom[i] == ddb.GetCommandInfo("if1").HexCode || rom[i] == ddb.GetCommandInfo("if2").HexCode || (rom[i] == ddb.GetCommandInfo("trainerbattle").HexCode && (j >= 5 && (b == 1 || b == 2)))) && !tooLarge && !SectionLocations.containsValue(datalocation))
									{
										SectionLocations.put(dword, dwint);
										SectionTypes.put(dwint, PointerType.Script);
									}
									else if (rom[i] == ddb.GetCommandInfo("applymovement").HexCode && !tooLarge && !SectionLocations.containsValue(datalocation))
									{
										SectionLocations.put(dword, dwint);
										SectionTypes.put(dwint, PointerType.Movement);
									}
									else if (rom[i] == ddb.GetCommandInfo("pokemart").HexCode && !tooLarge)
									{
										SectionLocations.put(dword, dwint);
										SectionTypes.put(dwint, PointerType.Mart);
									}
									else if(rom[i] == ddb.GetCommandInfo("braille").HexCode && !tooLarge && !SectionLocations.containsValue(datalocation))
									{
										SectionLocations.put(dword, dwint);
										SectionTypes.put(dwint, PointerType.Braille);
									}
									
									if ((SectionLocations.containsKey(dword) || isString) && !tooLarge)
									{
										PointerType p = SectionTypes.get(dwint);
										int position = 0;
										switch(p)
										{
											case Braille:
												sectionType = "scrbraille_";
												extraComments = " @ ";
												
												position = 0;
												while(rom[dwint + position] != 0xFF)
												{
													extraComments += ddb.GetTextFromBraille(rom[dwint + position]);
													position++;
												}
												
												isString = true;
												break;
											case Text:
												sectionType = "scrmsg_";
												extraComments = " @ ";
												
												position = 0;
												while(rom[dwint + position] != 0xFF)
												{
													extraComments += ddb.GetTextFromHex(rom[dwint + position]);
													position++;
												}
												
												isString = true;
												break;
											case Script:
												sectionType = "scrloc_";
												break;
											case Movement:
												sectionType = "scrmovement_";
												break;
											case Mart:
												sectionType = "scrmart_";
												break;
										}
										//System.out.println("Found section at " + dword + "! Type: " + sectionType.replace("_", ""));
										toWrite += "\n.long " + dword.replace("0x", sectionType) + extraComments;
									}
									else
										toWrite += "\n.long " + "0x" + byteToStringNoZero(rom[i + args + 3]) + toHexString(rom[i + args + 2], true) + toHexString(rom[i + args + 1], true) + toHexString(rom[i + args], true) + " " + extraComments;

									args += 4;
									break;
								case 0:
								default:
									break;
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}

					if (cmd.TotalSize > 0)
					{
						i += args - 1;
						datalocation += args - 1;
					}
				}

				datalocation++;
				addLine(out, toWrite.trim());

				if ((!textMode && currentData == PointerType.Script && cmd.Name.equals("end")) | ((currentData == PointerType.Text || currentData == PointerType.JapText) && toHexString(instruction).equalsIgnoreCase("FF")) | (currentData == PointerType.Movement && toHexString(instruction).equalsIgnoreCase("FE")) | (currentData == PointerType.LevelScript && instruction == 0) | currentData == PointerType.LevelScriptArray | currentData == PointerType.Mart | SectionLocations.containsValue(datalocation))
				{
					textMode = false;
					currentSubroutine = datalocation;
					
					if(datalocation >= 0x17313D)
						Integer.parseInt("0");

					if(!SectionLocations.containsValue(datalocation))
					{
						int position = 0;
						boolean text = false;
						boolean containsEnd = false;
						boolean japHints = false;
						boolean containsMovement = false;
						
						while(rom[datalocation + position] != 0xFF)//ddb.GetCommandInfo("end").HexCode)
						{						
							if(SectionTypes.get(datalocation+position) == PointerType.Text)
								text = true;
							
							if(SectionTypes.get(datalocation+position) == PointerType.Movement)
								containsMovement = true;
							
							if(rom[datalocation + position] == 0x2 || rom[datalocation + position] == 0x3)
							{
								containsEnd = true;
							}
							
							//Look for |FD| strings
							if(rom[datalocation + position] == 0xFD && rom[datalocation + position - 1] != 0x25 && rom[datalocation + position - 1] != 0x26 && rom[datalocation + position+1] >= 1 && rom[datalocation + position + 1] <= 0xA)
							{
								text = true;
								japHints = true;
							}
							
							if(rom[datalocation + position] == 0xAB && rom[datalocation + position + 1] >= 0xFA)
							{
								text = true;
								japHints = true;
							}

							position += 1;
						}
						
						if(containsEnd && japHints)
							text = true;
						
						SectionLocations.put("0x08" + toHexString(datalocation), datalocation);
						if(!text && !containsMovement)
							SectionTypes.put(datalocation, PointerType.Script);
						//else if(!text && containsMovement)
							//SectionTypes.put(datalocation, PointerType.Movement)
						else if(containsEnd && japHints)
							SectionTypes.put(datalocation, PointerType.JapText);
						else
							SectionTypes.put(datalocation, PointerType.Text);
					}
					if (datalocation >= enddata)
					{
						mainscriptdone = true;
						subscriptdone = true;
						out.close();
					}
					
				}

			}
		}
		catch (IOException e)
		{
			// exception handling left as an exercise for the reader
		}
		
		return;
	}

	public void addLine(PrintWriter out, String line)
	{
		out.println(line);
	}

	public String toHexString(int b)
	{
		return toHexString(b, false);
	}

	public String toHexString(int b, boolean spacing)
	{
		if (spacing)
			return String.format("%02X", Math.abs(b)); // Use absolute value to prevent negative bytes
		else
			return String.format("%X", Math.abs(b));
	}

	public String toDwordString(int b, boolean spacing)
	{
		if (spacing)
			return String.format("%06X", Math.abs(b)); // Use absolute value to prevent negative bytes
		else
			return String.format("%X", Math.abs(b));
	}

	public String byteToStringNoZero(int b)
	{
		if (b != 0)
			return String.format("%X", Math.abs(b));
		else
			return "";
	}
}
