package org.zzl.minegaming.SEA;
import java.awt.Color;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ScriptCompiler extends Thread
{
	final int MAX_ELEMENTS = 1000;
	private String script; 
	private int freespaceSearchStart = -1;
	private int scriptStart = 0;
	private int currentByte = 0;
	private int oldByte = 0;
	private int linenumber = 0;
	private boolean error = false;
	private String header = "";
	String errorString = "";
	private HashMap<String,Integer> SectionLocations = new HashMap<String,Integer>();
	private HashMap<String,Integer> StringSections = new HashMap<String,Integer>();
	private HashMap<String,Long> VariableValues = new HashMap<String,Long>();
	private List<Integer> WriteList = new ArrayList<Integer>(Collections.<Integer>nCopies(0x2000000, null));
	private List<String> StringList = new ArrayList<String>();
	private HashMap<String,String> AliasCommands = new HashMap<String,String>();
	Database ddb = Main.Commands;
	private String errorReport = "";
	
	public ScriptCompiler(String text)
	{
		script = text;
	}
	
	public void run()
	{
		try
		{
			CompileWindow.tbLog.setForeground(Color.black);
			long begin = System.currentTimeMillis();
			CompileWindow.ByteCode = "";
			CompileWindow.LogOutput = "";
			String[] lines = script.split("\n");
			header += HeaderHelper.GetHeader("<AliasCommands.rbh>");

			preCompile(lines);
			String[] headers = header.split("\n");
			
			//Now to compile the headers...
			currentByte = oldByte;
			preCompile(headers);
			
			linenumber = 0;
			currentByte = 0;
			
			compileScript(lines);
			compileScript(headers);
			
			if(error)
			{
				CompileWindow.tbLog.setForeground(Color.red);
				print(errorString);
				CompileWindow.UpdateText();
			}
			else
			{
				writeStrings(lines);
				writeStrings(headers);
				//currentByte = oldByte;
				int[] array = new int[currentByte];
				for(int i = 0; i < currentByte; i++)
				{
					if(WriteList.get(i) == null)
					{
						array[i] = 0;
						continue;
					}
					array[i] = WriteList.get(i);
				}
				Main.hex = array.clone();
				Main.scriptStart = scriptStart;
				print("Script length in bytes (Decimal): " + currentByte);
				printBytecode("Compiled Dump:");
				for(int i = 0; i < currentByte ; i++)
				{
					int b = array[i];
					String hex = String.format("%02x", b);
					printByte(hex + " ");
				}

				for(int i = 0; i < currentByte; i++)
				{
					int b = array[i];
					GlobalVars.NewROM[scriptStart + i] = (byte)b;
				}

				long end = System.currentTimeMillis();

				long dt = end - begin;
				CompileWindow.LogOutput = "Compiled in " + (float)(dt / 1000f) + "s.\n" + CompileWindow.LogOutput;
				CompileWindow.UpdateText();
			}
		}
		catch(Exception e)
		{
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			e.printStackTrace();
			CompileWindow.tbLog.setForeground(Color.red);
			CompileWindow.LogOutput += 	"JAVA EXCEPTION WHILE COMPILING!\n---------------\n" + sw.toString(); // stack trace as a string
			CompileWindow.LogOutput += 	"\n at line " + linenumber;
			CompileWindow.UpdateText();
		}
	}


	private void SetFreespaceStart(String space)
	{
		freespaceSearchStart = Integer.parseInt(space, 16);
		print("Set script search start to " + freespaceSearchStart + " (0x" + space + ")");
		print("");
		//TODO Freespace Finding
		int freespace = FreeSpaceHelper.find(oldByte, freespaceSearchStart);
		SetScriptStart(String.format("%x", freespace));
	}

	private void SetScriptStart(String space)
	{
		scriptStart = Integer.parseInt(space, 16);
		print("Set script start to 0x" + space);
	}

	private boolean AddStringToSectionTable(String section)
	{
		String message = "";
		//If it's a data location
		if(section.contains("(") && section.contains(")"))
		{
			String orig = section;
			String[] split = section.split(" ");
			section = split[0];
			message = orig.replaceFirst(section, "").replaceFirst(" ", "").replace("(", "").replace(")", "");
			stringEscapes = 0;
			if(!WriteString(message))
				return false;
		}
		if(scriptStart != -1)
		{
			print("Added section: " + section + " at 0x" + Integer.toHexString(scriptStart + (currentByte - (message.length() - stringEscapes + 1))));
			if(!message.equals(""))
			{
				print("Section contains message: " + message);
				//Add corrected address during test run so that the final run will contain the real value.
				SectionLocations.put(section, scriptStart + (currentByte - (message.length() - stringEscapes + 1)));
				StringSections.put(section, message.length() - stringEscapes + 1);
			}
			print("");
			return true;
		}
		else 
		{
			errorReport = "Cannot Allocate Space for Routine! No offset to begin search for free space, nor a static address was defined!";
			return false;
		}
	}
	
	private boolean AddStringToSectionTableTest(String section)
	{
		String message = "";
		//If it's a data location
		if(section.contains("(") && section.contains(")"))
		{
			String orig = section;
			String[] split = section.split(" ");
			section = split[0];
			message = orig.replaceFirst(section, "").replaceFirst(" ", "").replace("(", "").replace(")", "");
			stringEscapes = 0;
			if(!WriteStringTest(message))
				return false;
		}
		if(scriptStart != -1)
		{
			if(message.length() == 0)
			{
				SectionLocations.put(section, scriptStart + (currentByte));
				print("Added section: " + section + " at 0x" + Integer.toHexString(scriptStart + (currentByte - (message.length() - stringEscapes))));
			}
			if(!message.equals(""))
			{
				//Add message to section table to make the test run happy.
				SectionLocations.put(section, scriptStart + (currentByte - (message.length() - stringEscapes + 1)));
				StringSections.put(section, message.length() - stringEscapes + 1);
			}
			print("");
			return true;
		}
		else 
		{
			errorReport = "Cannot Allocate Space for Routine! No offset to begin search for free space, nor a static address was defined!";
			return false;
		}
	}

	int stringEscapes = 0;
	private boolean WriteString(String string)
	{
		string = decompose(string);
		char[] array = string.toCharArray();
		for(int i = 0; i < array.length; i++)
		{
			if(array[i] == '\\')
			{
				stringEscapes++;
				char modifier = array[i + 1];
				switch(modifier)
				{
				case 'n':
					WriteByte(0xFE);
					stringEscapes--;
					break;
				case 'p':
					WriteByte(0xFB);
					break;
				case 'l':
					WriteByte(0xFA);
					break;
				case 'c':
					WriteByte(0xFC);
					break;
				case 'v':
					WriteByte(0xFD);
					break;
				case 'h': //WHY CAN'T I FRIGGIN APPEND A CHARACTER TO A STRING EASILY???
					char a = (char)array[i + 2];
					char b = (char)array[i + 3];
					String c = "12";
					c.replace('1', a);
					c.replace('2', b);
					byte towrite = 0x0;
					try
					{
						towrite = (byte)Integer.parseInt(c.toLowerCase(), 16);
					}
					catch(Exception e)
					{
						errorReport = "String Conversion Error! Cannot parse bytecode '0x" + c.toLowerCase() + "'!";
						return false;
					}
					WriteByte(towrite);
					stringEscapes += 2;
					i += 2;
					break;
				default:
					int bb = ddb.GetHexFromChar(array[i]);
					WriteByte(bb);
					stringEscapes--;
					break;
				}
				i++;
			}
			else
			{
				int b = ddb.GetHexFromChar(array[i]);
				WriteByte(b);
			}
			currentByte++;
		}
		WriteByte(0xFF); //End teh Stringz!
		currentByte += 1;
		return true;
	}
	
	private boolean WriteStringTest(String string)
	{
		string = decompose(string);
		char[] array = string.toCharArray();
		for(int i = 0; i < array.length; i++)
		{
			if(array[i] == '\\')
			{
				stringEscapes++;
				char modifier = array[i + 1];
				switch(modifier)
				{
				case 'n':
					stringEscapes--;
					break;
				
				case 'h': //WHY CAN'T I FRIGGIN APPEND A CHARACTER TO A STRING EASILY???
					stringEscapes += 2;
					i += 2;
					break;
				default:
					stringEscapes--;
					break;
				}
				i++;
			}
			else
			{
				int b = ddb.GetHexFromChar(array[i]);
			}
			currentByte++;
		}
		currentByte += 1;
		return true;
	}

	private void WriteByte(long writez)
	{
		int write = (int)(writez & 0x000000FF);
		WriteList.set(currentByte, write);
	}

	private void WriteWord(long wordz)
	{
		int word = (int)(wordz & 0x0000FFFF);
		int[] array = new int[2];
		String s = toWordString(word);
		array[0] = Integer.parseInt(s.substring(2, 4), 16);
		array[1] = Integer.parseInt(s.substring(0, 2), 16);
		WriteList.set(currentByte, array[0]);
		WriteList.set(currentByte + 1, array[1]);
	}

	public String toWordString(int b)
	{
		return String.format("%04X", Math.abs(b)); //Use absolute value to prevent negative bytes
	}

	public void WriteDWord(long dword)
	{
		byte extendingbyte = 8;
		if(dword < 0x00001000) //Chances are if the value is below that, it's not a pointer
		{
			extendingbyte = 0;
		}
		if(dword >= 0x1000000 && dword < 0x2000000)
		{
			dword = dword & 0x00FFFFFF;
			extendingbyte = 9;
		}
		else if(dword >= 0x2000000)
		{
			extendingbyte = (byte)((dword & 0xFF000000) >> (8 * 3));
			dword = dword & 0x00FFFFFF;
		}

		ByteBuffer b = ByteBuffer.allocate(4);
		b.order(ByteOrder.LITTLE_ENDIAN);
		b.putInt((int)dword);
		byte[] array = b.array();
		if(array[3] == 0)
		{
			array[3] = extendingbyte;
		}
		for(int i = 0; i < array.length; i++)
		{
			int j = (int)(array[i] & 0x000000FF);
			WriteList.set(currentByte, j);
			currentByte++;
		}
	}

	public Boolean isHex(String arg)
	{
		if(arg.startsWith("0x"))
			arg.replace("0x", "");
		if(!containsAF(arg))
			return false;
		try
		{
			@SuppressWarnings("unused") //It's nice that you warned me and all, but I need this dude.
			int i = Integer.parseInt(arg.toLowerCase(),16);
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}

	public Boolean containsAF(String arg)
	{
		return (arg.contains("a") || arg.contains("b") || arg.contains("c") || arg.contains("d") || arg.contains("e") || arg.contains("f") || arg.contains("A") || arg.contains("B") || arg.contains("C") || arg.contains("D") || arg.contains("E") || arg.contains("F"));
	}

	public static String decompose(String s) {
		return java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+","");
	}

	public void print(String s)
	{
		System.out.println(s);
		CompileWindow.LogOutput += s + "\n";
	}

	public void printBytecode(String s)
	{
		System.out.println(s);
		CompileWindow.ByteCode += s + "\n";
	}

	public void printByte(String s)
	{
		System.out.print(s);
		CompileWindow.ByteCode += s;
	}
	
	public void compileScript(String[] lines)
	{
		compileScript(lines,true);
	}
	
	public void compileScript(String[] lines, boolean addlines)
	{
		for(String s : lines)
		{
			if(addlines)
				linenumber++;

			if(error)
				break;
			if(s.startsWith("#"))
				continue;
			else if(s.startsWith(";"))
				continue;
			else if(s.startsWith(":"))
				continue;
			else if(s.trim().equals(""))		//Whitespace
				continue;
			else if(s.startsWith("//")) //Comment
				continue;
			else if(s.startsWith("'")) //Comment
				continue;
			else if(s.contains("=")) //Variables
				continue;
			else if(s.startsWith("*"))
				continue;
			else if(s.toLowerCase().startsWith("import")) //Headers
				continue;
			String filter = s.replace('(', ' ');
			filter = filter.replace(')', ' ');
			String[] split = filter.split(" ");
			String command = split[0];
			List<String> args = new ArrayList<String>();

			for(int i = 1; i < split.length; i++)
			{
				args.add(split[i]);
			}

			Command cmd = ddb.GetCommandInfo(command);
			if(cmd == null)
				cmd = new Command("error", -1, "error",-1,-1, "");
			if(cmd.HexCode == -1)
			{
				if(AliasCommands.containsKey(command))
				{
					cmd.HexCode = -27;
					cmd.Name = command;
					cmd.Description = "Custom Alias";
					cmd.NumParams = 0;
					cmd.ParamFormat = "";
					cmd.TotalSize = 1;
				}
				else
				{
					error = true;
					errorString = "Command not found: " + command + " on line " + linenumber;
					break;
				}
			}
			if(cmd.NumParams < args.size() && cmd.HexCode != -27)
			{
				error = true;
				errorString = "Too many args for command " + command + " on line " + linenumber + ". Correct amount: " + cmd.NumParams;
				break;
			}
			else if(cmd.NumParams > args.size() && cmd.HexCode != -27)
			{
				error = true;
				errorString = "Not enough args for command " + command + " on line " + linenumber + ". Correct amount: " + cmd.NumParams;
				break;
			}
			for(int i = 0; i < args.size(); i++)
			{
				char[] paramformat = new char[args.size()];
				cmd.ParamFormat.getChars(0, cmd.ParamFormat.length(), paramformat, 0);
				if(cmd.NumParams == 0)
					break;
				String argument = args.get(i);
				try
				{
					long arg;
					if(argument.startsWith("0x") || isHex(argument))
						arg = Long.parseLong(argument.replace("0x", "").trim(), 16);
					else
					{
						arg = Long.parseLong(argument.trim());
					}
					if(arg > 255)
					{
						if(paramformat[i] == '1')
						{
							error = true;
							errorString = "Parameter Mismatch! Parameter " + i + " of " + cmd.Name + " is not a word value on line " + linenumber;
							break;
						}
					}
				}
				catch(Exception e)
				{
					if(!VariableValues.containsKey(argument.trim()) && !SectionLocations.containsKey(args.get(0)))
					{
						if(!argument.equalsIgnoreCase("goto") || !argument.equalsIgnoreCase("call"))
						{

						}
						else
						{
							error = true;
							errorString = "Variable \"" + argument + "\" is not in the variable index on line " + linenumber;
							break;
						}
					}
					else
					{
						try
						{
							@SuppressWarnings("unused") //Sorry, I need him too.
							long arg;
							if(argument.startsWith("0x"))
								arg = Long.parseLong(argument.replace("0x", "").trim(), 16);
							else
								arg = Long.parseLong(argument.trim());
							if(paramformat[i] != '3')
							{
								error = true;
								errorString = "Parameter Mismatch! Parameter " + i + " of " + cmd.Name + " is not a dword value on line " + linenumber;
								break;
							}
						}
						catch(Exception e2)
						{
							if(!argument.equalsIgnoreCase("goto") || !argument.equalsIgnoreCase("call"))
							{

							}
							else if(!SectionLocations.containsKey(argument))
							{
								error = true;
								errorString = "Section name \"" + argument + "\" is not in the section index on line " + linenumber;
								break;
							}
						}
					}
				}
			}
			if(error)
				break;

			boolean alias = false;
			switch(cmd.HexCode)
			{
			case -2: //msgbox negative bytecode
				alias = true;
				WriteByte(ddb.GetCommandInfo("preparemsg").HexCode);
				currentByte++;
				if(args.get(0).startsWith("0x"))
				{
					WriteDWord(Long.parseLong(args.get(0), 16));
				}
				else
				{
					if(!SectionLocations.containsKey(args.get(0)))
					{
						error = true;
						errorString = "Section name \"" + args.get(0) + "\" is not in the section index on line " + linenumber;
						break;
					}
					else
						WriteDWord(SectionLocations.get(args.get(0)));
				}
				String msg = SectionLocations.get(args.get(0)).toString();
				WriteByte(ddb.GetCommandInfo("callstd").HexCode);
				currentByte++;
				WriteByte(Long.parseLong(args.get(1).replace("0x", ""), 16));
				currentByte++;
				break;
			case -3: //if negative bytecode -- alternative to using if[1/2]
				if(args.size() > 2)
					alias = true;
				else
					break;
				if(args.get(1).equalsIgnoreCase("goto") || args.get(1).equalsIgnoreCase("jump"))
					WriteByte(ddb.GetCommandInfo("if1").HexCode); //If your friends jumped off a cliff, would you jump too?
				else if(args.get(1).equalsIgnoreCase("call"))
					WriteByte(ddb.GetCommandInfo("if2").HexCode); //... So call me maybe!
				currentByte++;
				WriteByte((byte)Integer.parseInt(args.get(0).replace("0x", ""),16)); //Write the equal, not equal, etc.
				currentByte++;
				if(args.get(2).startsWith("0x"))
				{
					WriteDWord(Long.parseLong(args.get(2).replace("0x", ""), 16));
				}
				else
					WriteDWord(SectionLocations.get(args.get(2)));
				break;
			case -27:
				alias = true;
				String commandLine = AliasCommands.get(command);
				while(commandLine.contains("$"))
				{
					int index = commandLine.indexOf("$") + 1;
					int argument = Integer.parseInt(commandLine.toCharArray()[index] + "");
					commandLine = commandLine.replace("$" + argument, args.get(argument - 1) + "");
				}
				compileScript(commandLine.split("\n"));
				break;
			default:
				break;
			}

			if(!alias)
			{
				//Write command bytecode
				WriteByte(cmd.HexCode);
				currentByte++;

				//Write args
				int counter = -1;
				for(String arg : args)
				{
					counter++;
					if(arg.equals(""))
						break;
					long arglength = 0;
					char argtype = cmd.ParamFormat.toCharArray()[counter];
					if((arg.startsWith("0x") || isHex(arg)) && !VariableValues.containsKey(arg.trim()))
					{
						arglength = Long.parseLong(arg.replace("0x", ""), 16);
					}
					else if(!VariableValues.containsKey(arg.trim()))
					{
						try
						{
							arglength = Long.parseLong(arg);
						}
						catch(Exception e)
						{
							arg = arg.trim();
							if(argtype >= 3)
								break;
							if(VariableValues.containsKey(arg))
							{
								long pointer = VariableValues.get(arg);
								WriteDWord(pointer);
								currentByte += 4;
							}
							else
							{
								int pointer = SectionLocations.get(arg);
								WriteDWord(pointer);
								currentByte += 4;
							}
							continue;
						}
					}
					if(argtype == '3') //Word Value
					{
						if(VariableValues.containsKey(arg))
						{
							long pointer = VariableValues.get(arg);
							WriteDWord(pointer);
							currentByte += 4;
						}
						else
						{
							WriteDWord(arglength);
						}
						continue;
					}
					if(argtype == '2')
					{
						if(VariableValues.containsKey(arg))
						{
							long pointer = VariableValues.get(arg);
							WriteWord(pointer & 0xFFFF);
							currentByte += 2;
						}
						else
						{
							WriteWord(arglength);
							currentByte += 2;
						}
						continue;
					}
					if(argtype == '1')
					{
						if(VariableValues.containsKey(arg))
						{
							long pointer = VariableValues.get(arg);
							WriteByte(pointer & 0xFF);
							currentByte += 1;
						}
						else
						{
							WriteByte(arglength);
							currentByte++;
						}
						continue;
					}
				}
				continue;
			}
		}
	}
	
	public void writeStrings(String[] lines)
	{
		for(String s : lines) //Write strings to ROM
		{
			if(s.startsWith(":"))
			{
				String orig = s;
				String message = "";
				String[] split = s.split(" ");
				s = split[0];
				message = orig.replaceFirst(s, "").replaceFirst(" ", "").replace("(", "").replace(")", "");
				s = orig;
				if(!StringList.contains(message))
					continue;
				if(!AddStringToSectionTable(s.replace(":", "")))
				{
					error = true;
					errorString = errorReport;
					break;
				}
			}
		}
	}
	
	public void preCompile(String[] lines)
	{
		int startLineNum = linenumber;
		int startByte = currentByte;
		for(String s : lines)
		{
			linenumber++;
			if(s.startsWith("#"))
				continue;
			if(s.startsWith(";"))
				continue;
			if(s.startsWith(":"))
			{
				if(s.contains("(") && s.contains(")"))
				{
					String orig = s;
					String message = "";
					String[] split = s.split(" ");
					s = split[0];
					message = orig.replaceFirst(s, "").replaceFirst(" ", "").replace("(", "").replace(")", "");
					StringList.add(message);
				}
				continue;
			}
			else if(s.toLowerCase().startsWith("import"))
			{
				String h = HeaderHelper.GetHeader(s.split(" ")[1].toString());
				if(h.contains("ERROR!"))
				{
					error = true;
					errorString = "Header Error! Could not find file " + h.split(" ")[1];
					continue;
				}
				header += h;
			}
			else if(s.equals(""))		//Whitespace
				continue;
			else if(s.startsWith("//")) //Comment
				continue;
			else if(s.startsWith("'")) //Comment
				continue;
			else if(s.startsWith("*"))
			{
				String alias = s.split(" ")[0].replace("*", "");
				String commands = s.split("\\(",2)[1].replaceFirst("\\)", "").replace(";", "\n");
				AliasCommands.put(alias,commands);
			}
			else if(s.contains("=")) //Variables
			{
				String[] args = s.split("=");
				String name = args[0].trim();
				long value = 0;
				String arg = args[1].replace("0x", "").trim();
				if(args[1].contains("0x"))
					value = Long.parseLong(arg,16);
				else if(isHex(arg))
					value = Long.parseLong(arg,16);
				else 
					value = Long.parseLong(arg);
				VariableValues.put(name,value);
			}
			else
			{
				String filter = s.replace('(', ' ');
				filter = filter.replace(')', ' ');
				String[] split = filter.split(" ");
				String command = split[0];
				List<String> args = new ArrayList<String>();

				for(int i = 1; i < split.length; i++)
				{
					args.add(split[i]);
				}
				
				if(AliasCommands.containsKey(command))
				{
					String commandLine = AliasCommands.get(command);
					while(commandLine.contains("$"))
					{
						int index = commandLine.indexOf("$") + 1;
						int argument = Integer.parseInt(commandLine.toCharArray()[index] + "");
						commandLine = commandLine.replace("$" + argument, args.get(argument - 1) + "");
					}
					preCompile(commandLine.split("\n"));
					continue;
				}
				
				if(command.equals("msgbox"))
				{
					currentByte++;
					currentByte += 4;
					currentByte++;
					currentByte++;
				}
				else
				{
					Command cmd = ddb.GetCommandInfo(command);
					if(cmd == null)
						new Command("error", -1, "error",-1,-1, "");
					if(cmd.HexCode == -1)
					{
						if(AliasCommands.containsKey(command))
						{
							cmd.HexCode = -27;
							cmd.Name = command;
							cmd.Description = "Custom Alias";
							cmd.NumParams = 0;
							cmd.ParamFormat = "";
							cmd.TotalSize = 0;
							preCompile(AliasCommands.get(command).split("\n"));
						}
						else
						{
							error = true;
							errorString = "Command not found: " + command + " on line " + linenumber;
							break;
						}
					}
					currentByte += cmd.TotalSize;
				}
			}
		}

		
		for(String s : lines) //Get string's pointers for compiling.
		{
			oldByte = currentByte;
			if(s.startsWith("#"))
			{
				SetScriptStart(s.replace("#0x", ""));
				continue;
			}
			else if(s.startsWith(";"))
			{
				SetFreespaceStart(s.replace(";0x", ""));
				continue;
			}
			else if(s.startsWith(":"))
			{
				if(s.contains("(") && s.contains(")"))
				{
					String orig = s;
					String message = "";
					String[] split = s.split(" ");
					s = split[0];
					message = orig.replaceFirst(s, "").replaceFirst(" ", "").replace("(", "").replace(")", "");
					s = orig;
					if(!StringList.contains(message))
						continue;
					if(!AddStringToSectionTableTest(s.replace(":", "")))
					{
						error = true;
						errorString = errorReport;
						break;
					}
				}
			}
		}
		
		linenumber = startLineNum;
		oldByte = currentByte;
		currentByte = startByte;
		for(String s : lines) //One more time, this time to get sections. All this looping is making be feel like a bad programmer. :P
		{
			if(s.startsWith(":"))
			{
				if(!s.contains("(") && !s.contains(")"))
				{
					if(!AddStringToSectionTableTest(s.replace(":", ""))) //Sections we want to go in normally.
					{
						error = true;
						errorString = errorReport;
						break;
					}
				}
			}
		}
	}
}
