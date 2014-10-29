package org.zzl.minegaming.SEA;
import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.FlowLayout;
import java.awt.Rectangle;

import javax.swing.border.BevelBorder;

import org.zzl.minegaming.SEA.TextEdit.ScriptEditor;
import org.zzl.minegaming.SEA.TextEdit.TextLineNumber;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Insets;

import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class Main extends JFrame
{
	public static Database Commands = new Database();
	private JTextField txtOffset;
	private JTextField textField_1;
	public static int[] hex = new int[] { 0x6a, 0x5a, 0x0f, 0x00, 0x0c, 0x00, 0x80, 0x08, 0x09, 0x02, 0x6c, 0x02, 0xc2, 0xd9, 0xe0, 0xe0, 0xe3, 0xb8, 0x00, 0xd1, 0xe3, 0xe6, 0xe0, 0xd8, 0xab, 0xff};
	public static int scriptStart = 0x800000;
	public static final ScriptEditor scriptEditor = new ScriptEditor();
	final JToolBar toolBar = new JToolBar();
	public Main() 
	{
		setTitle("Script Editor Advanced");
		this.setSize(new Dimension(996, 596));
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		panel_1.setPreferredSize(new Dimension(10, 24));
		panel_1.setMinimumSize(new Dimension(100, 20));
		panel.add(panel_1, BorderLayout.SOUTH);
		
		final JSplitPane splitPane = new JSplitPane();
		splitPane.setEnabled(false);
		splitPane.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		float percent = (float)(1f - (220f / (float)this.getWidth()));
		splitPane.setDividerLocation((int)(this.getWidth() * (float)percent));
		System.out.println(percent);
		panel.add(splitPane, BorderLayout.CENTER);
		//scrollPane.setViewportView(splitPane);
		
		JSplitPane splitPane_1 = new JSplitPane();
		splitPane_1.setLastDividerLocation(50);
		splitPane_1.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		splitPane_1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane_1.setDividerLocation((int)(this.getHeight() / 2.4));
		splitPane.setRightComponent(splitPane_1);
		
		JScrollPane panel_4 = new JScrollPane();
		panel_4.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128)), "Notes", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		splitPane_1.setRightComponent(panel_4);
		
		JTextPane txtpnInsertYourNotes = new JTextPane();
		txtpnInsertYourNotes.setText("Insert Your Notes Here...");
		panel_4.setViewportView(txtpnInsertYourNotes);
		
		JPanel panel_3 = new JPanel();
		splitPane_1.setLeftComponent(panel_3);
		panel_3.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_6 = new JPanel();
		panel_6.setMaximumSize(new Dimension(50, 32767));
		panel_6.setLayout(null);
		panel_6.setSize(new Dimension(100, 100));
		panel_6.setBorder(new TitledBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128)), "Calculator", TitledBorder.LEADING, TitledBorder.TOP, null, null), "Calculator", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_3.add(panel_6, BorderLayout.CENTER);
		
		textField_1 = new JTextField();
		textField_1.setText("0");
		textField_1.setColumns(10);
		textField_1.setBounds(12, 24, 159, 27);
		panel_6.add(textField_1);
		
		JRadioButton rdbtDec = new JRadioButton("Dec");
		rdbtDec.setSelected(true);
		rdbtDec.setPreferredSize(new Dimension(20, 23));
		rdbtDec.setFont(new Font("Dialog", Font.BOLD, 10));
		rdbtDec.setBounds(12, 56, 47, 18);
		panel_6.add(rdbtDec);
		
		JButton button_6 = new JButton("CE");
		button_6.setFont(new Font("Dialog", Font.BOLD, 8));
		button_6.setBounds(111, 56, 28, 21);
		panel_6.add(button_6);
		
		JButton button_7 = new JButton("C");
		button_7.setFont(new Font("Dialog", Font.BOLD, 8));
		button_7.setBounds(143, 56, 28, 21);
		panel_6.add(button_7);
		
		JButton button_8 = new JButton("C");
		button_8.setFont(new Font("Dialog", Font.BOLD, 8));
		button_8.setBounds(15, 83, 28, 21);
		panel_6.add(button_8);
		
		JButton button_9 = new JButton("D");
		button_9.setFont(new Font("Dialog", Font.BOLD, 8));
		button_9.setBounds(47, 83, 28, 21);
		panel_6.add(button_9);
		
		JButton button_10 = new JButton("E");
		button_10.setFont(new Font("Dialog", Font.BOLD, 8));
		button_10.setBounds(79, 83, 28, 21);
		panel_6.add(button_10);
		
		JButton button_11 = new JButton("F");
		button_11.setFont(new Font("Dialog", Font.BOLD, 8));
		button_11.setBounds(111, 83, 28, 21);
		panel_6.add(button_11);
		
		JButton button_12 = new JButton("/");
		button_12.setFont(new Font("Dialog", Font.BOLD, 8));
		button_12.setBounds(143, 83, 28, 21);
		panel_6.add(button_12);
		
		JButton button_13 = new JButton("8");
		button_13.setFont(new Font("Dialog", Font.BOLD, 8));
		button_13.setBounds(15, 109, 28, 21);
		panel_6.add(button_13);
		
		JButton button_14 = new JButton("9");
		button_14.setFont(new Font("Dialog", Font.BOLD, 8));
		button_14.setBounds(47, 109, 28, 21);
		panel_6.add(button_14);
		
		JButton button_15 = new JButton("A");
		button_15.setFont(new Font("Dialog", Font.BOLD, 8));
		button_15.setBounds(79, 109, 28, 21);
		panel_6.add(button_15);
		
		JButton button_16 = new JButton("B");
		button_16.setFont(new Font("Dialog", Font.BOLD, 8));
		button_16.setBounds(111, 109, 28, 21);
		panel_6.add(button_16);
		
		JButton button_17 = new JButton("X");
		button_17.setFont(new Font("Dialog", Font.BOLD, 8));
		button_17.setBounds(143, 109, 28, 21);
		panel_6.add(button_17);
		
		JButton button_18 = new JButton("0");
		button_18.setFont(new Font("Dialog", Font.BOLD, 8));
		button_18.setBounds(15, 161, 28, 21);
		panel_6.add(button_18);
		
		JButton button_19 = new JButton("4");
		button_19.setFont(new Font("Dialog", Font.BOLD, 8));
		button_19.setBounds(15, 135, 28, 21);
		panel_6.add(button_19);
		
		JButton button_20 = new JButton("5");
		button_20.setFont(new Font("Dialog", Font.BOLD, 8));
		button_20.setBounds(47, 135, 28, 21);
		panel_6.add(button_20);
		
		JButton button_21 = new JButton("1");
		button_21.setFont(new Font("Dialog", Font.BOLD, 8));
		button_21.setBounds(47, 161, 28, 21);
		panel_6.add(button_21);
		
		JButton button_22 = new JButton("6");
		button_22.setFont(new Font("Dialog", Font.BOLD, 8));
		button_22.setBounds(79, 135, 28, 21);
		panel_6.add(button_22);
		
		JButton button_23 = new JButton("2");
		button_23.setFont(new Font("Dialog", Font.BOLD, 8));
		button_23.setBounds(79, 161, 28, 21);
		panel_6.add(button_23);
		
		JButton button_24 = new JButton("3");
		button_24.setFont(new Font("Dialog", Font.BOLD, 8));
		button_24.setBounds(111, 161, 28, 21);
		panel_6.add(button_24);
		
		JButton button_25 = new JButton("7");
		button_25.setFont(new Font("Dialog", Font.BOLD, 8));
		button_25.setBounds(111, 135, 28, 21);
		panel_6.add(button_25);
		
		JButton button_26 = new JButton("-");
		button_26.setFont(new Font("Dialog", Font.BOLD, 8));
		button_26.setBounds(143, 135, 28, 21);
		panel_6.add(button_26);
		
		JButton button_27 = new JButton("+");
		button_27.setFont(new Font("Dialog", Font.BOLD, 8));
		button_27.setBounds(143, 161, 28, 21);
		panel_6.add(button_27);
		
		JButton button_28 = new JButton("0");
		button_28.setFont(new Font("Dialog", Font.BOLD, 8));
		button_28.setBounds(15, 213, 28, 21);
		panel_6.add(button_28);
		
		JButton button_29 = new JButton("4");
		button_29.setFont(new Font("Dialog", Font.BOLD, 8));
		button_29.setBounds(15, 187, 28, 21);
		panel_6.add(button_29);
		
		JButton button_30 = new JButton("5");
		button_30.setFont(new Font("Dialog", Font.BOLD, 8));
		button_30.setBounds(47, 187, 28, 21);
		panel_6.add(button_30);
		
		JButton button_31 = new JButton("1");
		button_31.setFont(new Font("Dialog", Font.BOLD, 8));
		button_31.setBounds(47, 213, 28, 21);
		panel_6.add(button_31);
		
		JButton button_32 = new JButton("2");
		button_32.setFont(new Font("Dialog", Font.BOLD, 8));
		button_32.setBounds(79, 213, 28, 21);
		panel_6.add(button_32);
		
		JButton button_33 = new JButton("6");
		button_33.setFont(new Font("Dialog", Font.BOLD, 8));
		button_33.setBounds(79, 187, 28, 21);
		panel_6.add(button_33);
		
		JButton button_34 = new JButton("7");
		button_34.setFont(new Font("Dialog", Font.BOLD, 8));
		button_34.setBounds(111, 187, 28, 21);
		panel_6.add(button_34);
		
		JButton button_35 = new JButton("3");
		button_35.setFont(new Font("Dialog", Font.BOLD, 8));
		button_35.setBounds(111, 213, 28, 21);
		panel_6.add(button_35);
		
		JButton button_36 = new JButton("+");
		button_36.setFont(new Font("Dialog", Font.BOLD, 8));
		button_36.setBounds(143, 213, 28, 21);
		panel_6.add(button_36);
		
		JButton button_37 = new JButton("-");
		button_37.setFont(new Font("Dialog", Font.BOLD, 8));
		button_37.setBounds(143, 187, 28, 21);
		panel_6.add(button_37);
		
		JRadioButton rdbtHex = new JRadioButton("Hex");
		rdbtHex.setFont(new Font("Dialog", Font.BOLD, 10));
		rdbtHex.setBounds(60, 56, 51, 18);
		panel_6.add(rdbtHex);
		
		ButtonGroup hexdec = new ButtonGroup();
		hexdec.add(rdbtHex);
		hexdec.add(rdbtDec);
		//----------------------------
		JPanel panel_7 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_7.getLayout();
		flowLayout.setHgap(10);
		panel_3.add(panel_7, BorderLayout.WEST);
		
		JPanel panel_5 = new JPanel();
		splitPane.setLeftComponent(panel_5);
		panel_5.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_2 = new JPanel();
		panel_5.add(panel_2, BorderLayout.NORTH);
		panel_2.setPreferredSize(new Dimension(1030, 36));
		panel_2.setLayout(new BorderLayout(0, 0));
		panel_2.add(toolBar, BorderLayout.CENTER);
		toolBar.setMargin(new Insets(2, 5, 2, 5));
		
		JLabel lblFile = new JLabel("File: ");
		toolBar.add(lblFile);
		
		JPanel panel_8 = new JPanel();
		panel_8.setMaximumSize(new Dimension(200, 20));
		toolBar.add(panel_8);
		panel_8.setLayout(new BorderLayout(0, 0));
		
		final JComboBox cbFile = new JComboBox();
		cbFile.setPreferredSize(new Dimension(250, 30));
		panel_8.add(cbFile);
		cbFile.setEditable(true);
		cbFile.setMinimumSize(new Dimension(200, 20));
		cbFile.setMaximumSize(new Dimension(200, 20));
		
		scriptEditor.setText(";0x800000\n//Script Editor Advanced (SEA) Testing Release!\n\n:start\nlock\nfaceplayer\nmsgbox(msg 2)\nrelease\nend\n\n:msg (Hello, World!)");
		scriptEditor.setFont(new Font("Monospaced", Font.PLAIN, 12));
		scriptEditor.setBorder(new LineBorder(new Color(0, 0, 0)));
		
		TextLineNumber tln = new TextLineNumber(scriptEditor);
		
		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		horizontalStrut_1.setPreferredSize(new Dimension(3, 0));
		horizontalStrut_1.setMaximumSize(new Dimension(3, 32767));
		toolBar.add(horizontalStrut_1);
		
		JButton btnOpen = new JButton("");
		btnOpen.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0) 
			{
				FileDialog fd = new FileDialog(new Frame(), "Load a ROM...", FileDialog.LOAD);
				fd.setFilenameFilter(new FilenameFilter()
				{
				    public boolean accept(File dir, String name)
				    {
				      return (name.toLowerCase().endsWith(".gba") || name.toLowerCase().endsWith(".bin") || name.toLowerCase().endsWith(".rbc") || name.toLowerCase().endsWith(".rbh"));
				    }
				 });
				
				fd.setDirectory(GlobalVars.LastDir);
				fd.show();
				GlobalVars.FileLoc = fd.getDirectory() + fd.getFile();
				Path location = Paths.get(fd.getDirectory() + fd.getFile());
				try
				{
					GlobalVars.ROM = Files.readAllBytes(location);
					GlobalVars.NewROM = Files.readAllBytes(location);
					GlobalVars.FileOpened = true;
					cbFile.addItem(fd.getDirectory() + fd.getFile());
					cbFile.setMinimumSize(new Dimension(200, 20));
					cbFile.setMaximumSize(new Dimension(200, 20));
					cbFile.setMaximumSize( cbFile.getPreferredSize() );
				}
				catch(Exception e)
				{
					if(fd.getFile() != null)
					{
						String error = "Couldn't load ROM!\nStacktrace:\n" + e.getCause();
						e.printStackTrace();
						System.out.println(fd.getDirectory() + fd.getFile());
						JOptionPane.showMessageDialog(new Frame(),
								error,
						    	"'Tis an Error!",
						    	JOptionPane.WARNING_MESSAGE);
						GlobalVars.FileOpened = false;
					}
				}
			}
		});
		btnOpen.setIcon(new ImageIcon(Main.class.getResource("/org/zzl/minegaming/SEA/resources/Open.png")));
		btnOpen.setMinimumSize(new Dimension(32, 32));
		btnOpen.setFont(new Font("Dialog", Font.BOLD, 8));
		toolBar.add(btnOpen);
		
		Component horizontalStrut = Box.createHorizontalStrut(20);
		horizontalStrut.setPreferredSize(new Dimension(3, 0));
		horizontalStrut.setMaximumSize(new Dimension(3, 32767));
		toolBar.add(horizontalStrut);
		
		JButton btnSave = new JButton("");
		btnSave.setIcon(new ImageIcon(Main.class.getResource("/org/zzl/minegaming/SEA/resources/Save.png")));
		toolBar.add(btnSave);
		
		toolBar.addSeparator();
		
		JButton btnCompile = new JButton("");
		btnCompile.setIcon(new ImageIcon(Main.class.getResource("/org/zzl/minegaming/SEA/resources/Compile.png")));
		btnCompile.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0) 
			{
				new Thread()
				{
					public void run()
					{
						CompileWindow c = new CompileWindow();
						c.setVisible(true);
					}
				}.start();
				
				ScriptCompiler sc = new ScriptCompiler(scriptEditor.getText());
				new Thread(sc).start(); //Use threading to prevent lag between the Log Window and compiling.
			}
		});
		toolBar.add(btnCompile);
		
		Component horizontalStrut_2 = Box.createHorizontalStrut(20);
		horizontalStrut_2.setPreferredSize(new Dimension(3, 0));
		horizontalStrut_2.setMaximumSize(new Dimension(3, 32767));
		toolBar.add(horizontalStrut_2);
		
		Component horizontalStrut_3 = Box.createHorizontalStrut(20);
		horizontalStrut_3.setPreferredSize(new Dimension(3, 0));
		horizontalStrut_3.setMaximumSize(new Dimension(3, 32767));
		toolBar.add(horizontalStrut_3);
		
		JButton btnLog = new JButton("");
		btnLog.setIcon(new ImageIcon(Main.class.getResource("/org/zzl/minegaming/SEA/resources/Log.png")));
		toolBar.add(btnLog);
		
		toolBar.addSeparator();
		
		JLabel lblOffset = new JLabel("Offset: ");
		lblOffset.setFont(new Font("Dialog", Font.BOLD, 10));
		toolBar.add(lblOffset);
		
		txtOffset = new JTextField();
		txtOffset.setText("0000000");
		txtOffset.setSize(new Dimension(58, 19));
		txtOffset.setMaximumSize(new Dimension(70, 20));
		txtOffset.setColumns(10);
		toolBar.add(txtOffset);
		
		Component horizontalStrut_5 = Box.createHorizontalStrut(20);
		horizontalStrut_5.setPreferredSize(new Dimension(3, 0));
		horizontalStrut_5.setMaximumSize(new Dimension(3, 32767));
		toolBar.add(horizontalStrut_5);
		
		JButton btnDecompile = new JButton("");
		
		btnDecompile.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0) 
			{
				scriptEditor.setText(new ScriptDecompilerASM().decompile(GlobalVars.NewROM, 0x16047C));
			}
		});
		btnDecompile.setIcon(new ImageIcon(Main.class.getResource("/org/zzl/minegaming/SEA/resources/Decompile.png")));
		toolBar.add(btnDecompile);
		
		Component horizontalStrut_4 = Box.createHorizontalStrut(20);
		horizontalStrut_4.setPreferredSize(new Dimension(3, 0));
		horizontalStrut_4.setMaximumSize(new Dimension(3, 32767));
		toolBar.add(horizontalStrut_4);
		
		JButton btnLevelScript = new JButton("");
		btnLevelScript.setIcon(new ImageIcon(Main.class.getResource("/org/zzl/minegaming/SEA/resources/Level Script.png")));
		toolBar.add(btnLevelScript);
		
		Component horizontalStrut_6 = Box.createHorizontalStrut(20);
		horizontalStrut_6.setPreferredSize(new Dimension(3, 0));
		horizontalStrut_6.setMaximumSize(new Dimension(3, 32767));
		toolBar.add(horizontalStrut_6);
		
		JButton btnDebug = new JButton("");
		btnDebug.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0) 
			{
				//TODO Start emulator
				try
				{
					//Files.write(Paths.get(GlobalVars.MainDir + "/temp.gba"), GlobalVars.NewROM);
					Desktop.getDesktop().open(new File(GlobalVars.MainDir + "/temp.gba"));
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		btnDebug.setIcon(new ImageIcon(Main.class.getResource("/org/zzl/minegaming/SEA/resources/DebugGame.png")));
		toolBar.add(btnDebug);
		
		JScrollPane scrollPane = new JScrollPane();
		panel_5.add(scrollPane, BorderLayout.CENTER);
		
		scrollPane.setViewportView(scriptEditor);
		if(started)
			scrollPane.setRowHeaderView(tln);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenu mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);
		
		JMenu mnTools = new JMenu("Tools");
		menuBar.add(mnTools);
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		//------------------------------
		this.addComponentListener(new ComponentListener() 
		{  
	        public void componentResized(ComponentEvent evt) {
	            Component c = (Component)evt.getSource();
	    		float percent = (float)(1f - (220f / (float)c.getWidth()));
	    		splitPane.setDividerLocation((int)(c.getWidth() * (float)percent));
	            System.out.println((1 - (220 / c.getWidth())));
	            Rectangle r = toolBar.getBounds();
	            r.width = c.getWidth() - 240;
	            toolBar.setBounds(r);
	        }

			@Override
			public void componentHidden(ComponentEvent arg0) {	
			}

			@Override
			public void componentMoved(ComponentEvent arg0) {
			}

			@Override
			public void componentShown(ComponentEvent arg0) {			
			}
	});
		this.setVisible(true);
	}
	
	static boolean started = false;
	public static LoggedPrintStream lpsOut = LoggedPrintStream.create(System.out);
	public static LoggedPrintStream lpsErr = LoggedPrintStream.create(System.err);
	public static void main(String[] args) throws IOException
	{
		int argNum = 0;
		ArrayList<Integer> forceScripts = new ArrayList<Integer>();
		ArrayList<Integer> forceLvScripts = new ArrayList<Integer>();
		ArrayList<Integer> forceStrings = new ArrayList<Integer>();
		ArrayList<Integer> forceJapStrings = new ArrayList<Integer>();
		ArrayList<Integer> forceMovement = new ArrayList<Integer>();
		for(String s : args)
		{
			if(s.equalsIgnoreCase("d") | s.equalsIgnoreCase("ds"))
			{
				String file = args[argNum + 1];
				GlobalVars.FileLoc = file;
				Path location = Paths.get(file);
				try
				{
					GlobalVars.ROM = Files.readAllBytes(location);
					GlobalVars.NewROM = Files.readAllBytes(location);
					GlobalVars.FileOpened = true;
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				
				new ScriptDecompilerASMSequential().decompile(GlobalVars.NewROM, Integer.parseInt(args[argNum + 2], 16), Integer.parseInt(args[argNum + 3], 16), forceScripts, forceLvScripts, forceStrings, forceJapStrings, forceMovement, s.equalsIgnoreCase("ds"));
				return;
			}
			else if(s.equalsIgnoreCase("h"))
			{
				String header = "";
				for(int i = 0; i < 0x100; i++)
				{
					Command c = Commands.GetCommandInfo(i);
					header += ".equ\t" + c.Name + ","+ (c.Name.length() < 16 ? "\t" : "") + (c.Name.length() < 8 ? "\t" : "") + (c.Name.length() < 4 ? "\t" : "") + "\t0x" + String.format("%X", c.HexCode) + "\n";
				}
				try
				{
					Files.write(Paths.get("./scr_names.asm"), header.getBytes());
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				return;
			}
			else if(s.equalsIgnoreCase("s"))
			{
				List<String> offsets = Files.readAllLines(Paths.get(args[argNum + 1]), Charset.defaultCharset());
				for(String str : offsets)
					forceScripts.add(Integer.parseInt(str,16));
			}
			else if(s.equalsIgnoreCase("lv"))
			{
				List<String> offsets = Files.readAllLines(Paths.get(args[argNum + 1]), Charset.defaultCharset());
				for(String str : offsets)
					forceLvScripts.add(Integer.parseInt(str,16));
			}
			else if(s.equalsIgnoreCase("str"))
			{
				List<String> offsets = Files.readAllLines(Paths.get(args[argNum + 1]), Charset.defaultCharset());
				for(String str : offsets)
					forceStrings.add(Integer.parseInt(str,16));
			}
			else if(s.equalsIgnoreCase("strj"))
			{
				List<String> offsets = Files.readAllLines(Paths.get(args[argNum + 1]), Charset.defaultCharset());
				for(String str : offsets)
					forceJapStrings.add(Integer.parseInt(str,16));
			}
			else if(s.equalsIgnoreCase("mov"))
			{
				List<String> offsets = Files.readAllLines(Paths.get(args[argNum + 1]), Charset.defaultCharset());
				for(String str : offsets)
					forceMovement.add(Integer.parseInt(str,16));
			}
			argNum++;
		}
		System.setOut(lpsOut);
		System.setErr(lpsErr);
		
		String OS = System.getProperty("os.name");
		started = true;
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e){}
		for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) 
		{
		    if ("com.sun.java.swing.plaf.gtk.GTKLookAndFeel".equals(info.getClassName())) 
		    {   
		    	try
		    	{
		    		UIManager.setLookAndFeel(info.getClassName());
		    		break;
		    	}
		    	catch(Exception e){}
		    }
		    else if("com.sun.java.swing.plaf.windows.WindowsLookAndFeel".equals(info.getClassName()) && OS.equalsIgnoreCase("Windows"))
		    {
		    	try
		    	{
		    		UIManager.setLookAndFeel(info.getClassName());
		    		break;
		    	}
		    	catch(Exception e){}
		    }
		}
		File d = new File(GlobalVars.SettingsDir);
		d.mkdirs();
		System.out.println("Set working directory to " + GlobalVars.MainDir);
		new Main();
	}
}
