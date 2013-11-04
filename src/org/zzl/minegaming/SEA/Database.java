package org.zzl.minegaming.SEA;
import java.io.File;
import java.io.Reader;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Database 
{
	Connection conn;
	Statement stat;
	public static HashMap<String, Command> commands = new HashMap<String, Command>();
	public static HashMap<Integer, String> pokeString = new HashMap<Integer, String>();
	public Database()
	{
		try 
		{
			Class.forName("org.sqlite.JDBC");
		conn =
		DriverManager.getConnection("jdbc:sqlite:Commands");
		stat = conn.createStatement();
		AddAliases();
		ResultSet rs = stat.executeQuery("select * from commands;");
		while (rs.next()) 
		{
			Command c = new Command(rs.getString("name"),rs.getInt("hex"),rs.getString("desc"), rs.getInt("length"), rs.getInt("args"),rs.getString("arglengths"));
			commands.put(rs.getString("name"), c);
		}
		rs = stat.executeQuery("select * from pokestring;");
		while (rs.next()) 
		{
			pokeString.put(rs.getInt("hex"), rs.getString("char"));
		}
		rs.close();
		//conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Command GetCommandInfoDDB(String cmd)
	{
		try 
		{
			conn = DriverManager.getConnection("jdbc:sqlite:Commands");
			stat = conn.createStatement();
		ResultSet rs = stat.executeQuery("select * from commands where name='" + cmd + "'");
		rs.next();
		Command c = new Command(rs.getString("name"),rs.getInt("hex"),rs.getString("desc"), rs.getInt("length"), rs.getInt("args"),rs.getString("arglengths"));
		rs.close();
		return c;
		} catch (SQLException e) 
		{
			if(cmd.equalsIgnoreCase("msgbox"))
				return new Command("msgbox",-2,"Prints text to a messagebox.\nEquivalent to:\n\nloadpointer 0x0 <msg-dword>\ncallstd <type-byte>", 8, 2, "30");
			return new Command("error", -1, "error",-1,-1, "");
		}
	}
	
	public Command GetCommandInfo(String cmd)
	{
		try
		{
			return commands.get(cmd);
		}
		catch(Exception e)
		{
			if(cmd.equalsIgnoreCase("msgbox"))
				return new Command("msgbox",-2,"Prints text to a messagebox.\nEquivalent to:\n\nloadpointer 0x0 <msg-dword>\ncallstd <type-byte>", 8, 2, "30");
			return new Command("error", -1, "error",-1,-1, "");
		}
	}
	
	public Command GetCommandInfo(int cmd)
	{
		try 
		{
			conn = DriverManager.getConnection("jdbc:sqlite:Commands");
			stat = conn.createStatement();
		ResultSet rs = stat.executeQuery("select * from commands where hex=" + cmd);
		rs.next();
		Command c = new Command(rs.getString("name"),rs.getInt("hex"),rs.getString("desc"), rs.getInt("length"), rs.getInt("args"),rs.getString("arglengths"));
		rs.close();
		return c;
		} catch (SQLException e) 
		{
			e.printStackTrace();
			//if(cmd.equalsIgnoreCase("msgbox"))
				//return new Command("msgbox",-2,"Prints text to a messagebox.\nEquivalent to:\n\nloadpointer 0x0 <msg-dword>\ncallstd <type-byte>", 8, 2, "30");
			return new Command("error", -1, "error",-1,-1, "");
		}
	}
	
	private void AddAliases() throws SQLException
	{
		commands.put("msgbox",new Command("msgbox",-2,"Prints text to a messagebox.\nEquivalent to:\n\nloadpointer 0x0 <msg-dword>\ncallstd <type-byte>", 8, 2, "30"));
		//commands.add("if");
	}
	
	public int GetHexFromChar(char c)
	{
		int data = 0;
		try {
		ResultSet rs = stat.executeQuery("select * from pokestring where char='" + c + "'");
		rs.next();
			data = rs.getInt("hex");
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
		return data;
	}
	
	public String GetTextFromHexDDB(int hex)
	{
		String c;
		try {
			ResultSet rs = stat.executeQuery("select * from pokestring where hex='" + hex + "'");
			rs.next();
				c = rs.getString("char");
			} catch (SQLException e) {
				e.printStackTrace();
				return "";
			}
		return c;
	}
	
	public char GetCharFromHexDDB(int hex)
	{
		char c;
		try {
			ResultSet rs = stat.executeQuery("select * from pokestring where hex='" + hex + "'");
			rs.next();
				c = rs.getString("char").toCharArray()[0];
			} catch (SQLException e) {
				e.printStackTrace();
				return ' ';
			}
		return c;
	}
	
	public String GetTextFromHex(int hex)
	{
		String c;
		try 
		{
			c = pokeString.get(hex);
			if(c == null)
				c = " ";
		} 
		catch (Exception e) 
		{
			c = "\\h" + String.format("X2", hex);
		}
		return c;
	}
	
	public char GetCharFromHex(int hex)
	{
		char c;
		try 
		{
			c = pokeString.get(hex).toCharArray()[0];
			if(hex == 0)
				c = ' ';
		} 
		catch (Exception e) 
		{
			c = ' ';
		}
		return c;
	}
	
	private void AddCommand(String name, int hex, String desc, int length, int args, String arglengths) throws SQLException
	{
		PreparedStatement prep = conn.prepareStatement(
				"insert into commands values (?, ?, ?, ?, ?, ?);");
		prep.setString(1, name);
		prep.setInt(2, hex);
		prep.setString(3, desc);
		prep.setInt(4, length);
		prep.setInt(5, args);
		prep.setString(6, arglengths);
		prep.addBatch();
		conn.setAutoCommit(false);
		prep.executeBatch();
		conn.setAutoCommit(true);
		commands.put(name, new Command(name,hex,desc,length,args,arglengths));
	}
	
	/*private void WriteAllCommands() throws SQLException
	{
		stat.executeUpdate("drop table if exists commands;");
		stat.executeUpdate("create table commands (name, hex, desc, length, args);");
		PreparedStatement prep = conn.prepareStatement(
		"insert into commands values (?, ?, ?, ?, ?);");

		AddCommand("nop",0x0,"Does nothing at all.",1,0);
		AddCommand("nop1",0x1,"Does nothing at all.",1,0);
		AddCommand("end",0x2,"Terminates execution of the script",1,0);
		AddCommand("return",0x3,"Pops back to the last calling command used",1,0);
		AddCommand("call",0x4,"Continues script execution from another point. Can be returned to.",5,1);
		AddCommand("goto",0x5,"Continues script execution from another point.",5,1);
		AddCommand("if1",0x6,"In the last comparison returned a value, jumps to another script",6,2);
		AddCommand("if2",0x7,"Calling version of if command",6,2);
		AddCommand("gotostd",0x8,"Jumps to a built-in function",2,1);
		AddCommand("callstd",0x9,"Calls a built-in function",2,1);
		AddCommand("gotostdif",0xA,"Jumps to a built-in function, conditional",3,2);
		AddCommand("callstdif",0xB,"Jump to a built-in function, conditional",3,2);
		AddCommand("jumpram",0xC,"Jumps to a default RAM location, executing the script stored there.",1,0);
		AddCommand("killscript",0xD,"Kills the script and resets the script RAM.",1,0);
		AddCommand("setbyte",0xE,"Sets a predefined address to the specified byte value.",2,1);
		AddCommand("loadpointer",0xF,"Loads a pointer into the script RAM so other commands can use it.",6,2);
		AddCommand("setbyte2",0x10,"Sets a memory bank to the specified byte.",3,2);
		AddCommand("writebytetooffset",0x11,"Sets the byte at the specified offset to a certain value.",6,2);
		AddCommand("loadbytefrompointer",0x12,"Loads the byte found at the pointer into the script RAM so other commands can use it.",6,2);
		AddCommand("setfarbyte",0x13,"Sets the byte into a specified pointer.",6,2);
		AddCommand("copyscriptbanks",0x14,"Copies one script bank to another.",3,2);
		AddCommand("copybyte",0x15,"Copies a byte value from one place to another.",9,2);
		AddCommand("setvar",0x16,"Sets variable A to any value.",5,2);
		AddCommand("addvar",0x17,"Adds any value to variable A.",5,2);
		AddCommand("subvar",0x18,"Subtracts any value from variable A.",5,2);
		AddCommand("copyvar",0x19,"Copies variable B to A.",5,2);
		AddCommand("copyvarifnotzero",0x1A,"Sets variable B to A, but only if B is higher than zero.",5,2);
		AddCommand("comparebanks",0x1B,"Compares two banks.",5,2);
		AddCommand("comparebanktobyte",0x1C,"Compares a variable stored in a buffer to a byte value.",3,2);
		AddCommand("comparebanktofarbyte",0x1D,"Compares a bank with a byte at some location",6,2);
		AddCommand("comparefarbytetobank",0x1E,"Compares a byte at some location to a buffered variable. The reverse of comparebanktofatbyte",6,2);
		AddCommand("comparefarbytetobyte",0x1F,"Compares a byte at some location to a byte value.",6,2);
		AddCommand("comparefarbytes",0x20,"Compares a byte at some location to a byte at another location",9,2);
		AddCommand("compare",0x21,"Compares variable A to a value",5,2);
		AddCommand("comparevars",0x22,"Compares two variables",5,2);
		AddCommand("callasm",0x23,"Calls a custom ASM routine",5,1);
		AddCommand("cmd24",0x24,"Unknown Command",5,1);
		AddCommand("special",0x25,"Calls a special event (ASM)",3,1);
		AddCommand("special2",0x26,"Calls a special event and returns a value (ASM)",5,2);
		AddCommand("waitstate",0x27,"Sets the script to a wait state, useful for some specials and commands.",1,0);
		AddCommand("pause",0x28,"Pauses script execution for a short amount of time.",3,1);
		AddCommand("setflag",0x29,"Sets a flag for later use.",3,1);
		AddCommand("clearflag",0x2A,"Clears the value of a flag.",3,1);
		AddCommand("checkflag",0x2B,"Checks the value of a flag.",3,1);
		AddCommand("cmd2c",0x2C,"",5,2);
		AddCommand("checkdailyflags",0x2D,"Checks the daily flags to see if any of them have been set already, but only if they were set previously. Then it clears those flags. R/S/E only.",1,0);
		AddCommand("resetvars",0x2E,"Resets the value of variables 0x8000, 0x8001, and 0x8002.",1,0);
		AddCommand("sound",0x2F,"Plays a sound.",3,1);
		AddCommand("checksound",0x30,"Checks if a sound/fanfare/song is currently being played.",1,0);
	}*/
}
