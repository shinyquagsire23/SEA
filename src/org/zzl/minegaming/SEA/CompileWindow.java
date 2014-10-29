package org.zzl.minegaming.SEA;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.BevelBorder;
import javax.swing.JScrollPane;
import javax.swing.JButton;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CompileWindow extends JFrame {

	public static String LogOutput = "CODE OUTPUT BANANAS";
	public static String ByteCode = "BYTECODE PINEAPPLES";
	public static JTextPane tbLog = new JTextPane();
	public static JTextPane tbBytes = new JTextPane();
	
	public CompileWindow()
	{
		setTitle("Compiler Output");
		setResizable(false);
		
		JPanel panel = new JPanel();
		panel.setLayout(null);
		
		JTextPane textPane = new JTextPane();
		textPane.setText("");
		textPane.setBounds(98, 49, 6, 21);
		panel.add(textPane);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 12, 290, 230);
		panel.add(scrollPane);
		
		tbLog.setText(LogOutput);
		tbLog.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		scrollPane.setViewportView(tbLog);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(12, 248, 290, 77);
		panel.add(scrollPane_1);
		
		
		tbBytes.setText(ByteCode);
		tbBytes.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		scrollPane_1.setViewportView(tbBytes);
		
		JButton btnSaveLog = new JButton("Save Log");
		btnSaveLog.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				FileDialog fd = new FileDialog(new Frame(), "Load a ROM...", FileDialog.SAVE);
				fd.setFilenameFilter(new FilenameFilter()
				{
				    public boolean accept(File dir, String name)
				    {
				      return (name.toLowerCase().endsWith(".log") || name.toLowerCase().endsWith(".txt"));
				    }
				 });
				DateFormat dateFormat = new SimpleDateFormat("MMddyy-HH.mm.ss");
				Date date = new Date();
				
				fd.setDirectory(GlobalVars.LastDir);
				fd.setFile(dateFormat.format(date) + ".log");
				fd.show();
				GlobalVars.FileLoc = fd.getDirectory() + fd.getFile();
				//Path location = Paths.get(fd.getDirectory() + fd.getFile());
				String s = "LOG OUTPUT:\n-----\n\n" + LogOutput + "\n" + ByteCode + "\n\nCONSOLE OUTPUT:\n-----\n\n" + Main.lpsOut.buf + "\n\nERRORS:\n-----\n\n" + Main.lpsErr.buf + "\n\n" + "SCRIPT:\n-----" + "\n\n" + Main.scriptEditor.getText();
				byte[] b = new byte[s.length()];
				for(int i = 0; i < s.length(); i++)
				{
					b[i] = (byte)s.charAt(i);
				}
				try
				{
					//Files.write(location, b);
				}
				catch(Exception ex)
				{
					String error = "Couldn't write Log!\nStacktrace:\n" + ex.getCause();
					ex.printStackTrace();
					System.out.println(fd.getDirectory() + fd.getFile());
					JOptionPane.showMessageDialog(new Frame(),
							error,
					    	"'Tis an Error!",
					    	JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		btnSaveLog.setBounds(12, 337, 98, 25);
		panel.add(btnSaveLog);
		
		JButton btnClose = new JButton("Close");
		final JFrame window = this;
		btnClose.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0) 
			{
				window.dispose();
			}
		});
		btnClose.setBounds(229, 337, 73, 25);
		panel.add(btnClose);
		
		getContentPane().add(panel);
		
		JButton btnViewLog = new JButton("View Log");
		btnViewLog.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0) 
			{
				//Path location = Paths.get(GlobalVars.SettingsDir + File.separator + "temp.log");
				String s = "LOG OUTPUT:\n-----\n\n" + LogOutput + "\n" + ByteCode + "\n\nCONSOLE OUTPUT:\n-----\n\n" + Main.lpsOut.buf + "\n\nERRORS:\n-----\n\n" + Main.lpsErr.buf + "\n\n" + "SCRIPT:\n-----" + "\n\n" + Main.scriptEditor.getText();
				byte[] b = new byte[s.length()];
				for(int i = 0; i < s.length(); i++)
				{
					b[i] = (byte)s.charAt(i);
				}
				try
				{
					//Files.delete(location);
					//Files.createFile(location);
					//Files.write(location, b);
					Desktop.getDesktop().open(new File(GlobalVars.SettingsDir + "/temp.log"));
				}
				catch(Exception ex)
				{
					String error = "Couldn't write Log to location '" + GlobalVars.SettingsDir + "temp.log" + "'!\nStacktrace:\n" + ex.getCause();
					ex.printStackTrace();
					System.out.println(GlobalVars.SettingsDir + "/temp.log");
					JOptionPane.showMessageDialog(new Frame(),
							error,
					    	"'Tis an Error!",
					    	JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		btnViewLog.setBounds(119, 337, 98, 25);
		panel.add(btnViewLog);
		this.setSize(322,395);
		this.setVisible(true);
	}
	
	public static void UpdateText()
	{
		tbLog.setText(LogOutput);
		tbBytes.setText(ByteCode);
	}
}
