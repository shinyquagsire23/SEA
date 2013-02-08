package org.zzl.minegaming.SEA.TextEdit;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.PlainView;
import javax.swing.text.Segment;
import javax.swing.text.Utilities;

import org.zzl.minegaming.SEA.Database;
 
public class ScriptView extends PlainView {
 
    private static HashMap<Pattern, Color> patternColors;
    private static String TAG_MULTI_COMMENT = "//.*|(\"(?:\\\\[^\"]|\\\\\"|.)*?\")|(?s)/\\*.*?\\*/";
    private static String TAG_FREESPACE_BEGIN = "[;#]0[xX][1-9a-fA-F].*";
    private static String TAG_SECTION_STRING = ":([\u0000-\uFFFF]*?)[\\(\\s]";
    private static String TAG_SECTION = ":.*";
    private static String TAG_TEH_JUICY_STUFF = "(\\()+.*(\\))$.*";
 
    static {
        // NOTE: the order is important!
        patternColors = new HashMap<Pattern, Color>();

        patternColors.put(Pattern.compile(TAG_MULTI_COMMENT), new Color(63, 127, 95));
        patternColors.put(Pattern.compile(TAG_FREESPACE_BEGIN), Color.RED);
        for(String s : Database.commands.keySet())
        {
        	patternColors.put(Pattern.compile("^" + s + ""), Color.BLUE);
        	patternColors.put(Pattern.compile(";" + s + ""), Color.BLUE);
        }
        patternColors.put(Pattern.compile(TAG_TEH_JUICY_STUFF), Color.MAGENTA);
        patternColors.put(Pattern.compile(TAG_SECTION_STRING), new Color(100,0,200));
    }
 
    public ScriptView(Element element) {
 
        super(element);
 
        // Set tabsize to 4 (instead of the default 8)
        //getDocument().putProperty(PlainDocument.tabSizeAttribute, 4);
    }
 
    Font mono = new Font("Monospaced", Font.PLAIN, 12);
    Font monoBold = new Font("Monospaced", Font.BOLD, mono.getSize());
    @Override
    protected int drawUnselectedText(Graphics graphics, int x, int y, int p0,
            int p1) throws BadLocationException {
        return drawSelectedText(graphics,x,y,p0,p1);
    }
 
    @Override
    protected int drawSelectedText(Graphics graphics, int x, int y, int p0,
            int p1) throws BadLocationException {
 
        Document doc = getDocument();
        String text = doc.getText(p0, p1 - p0);
 
        Segment segment = getLineBuffer();
 
        SortedMap<Integer, Integer> startMap = new TreeMap<Integer, Integer>();
        SortedMap<Integer, Color> colorMap = new TreeMap<Integer, Color>();
 
        // Match all regexes on this snippet, store positions
        for (Map.Entry<Pattern, Color> entry : patternColors.entrySet()) {
 
            Matcher matcher = entry.getKey().matcher(text);
 
            while (matcher.find()) {
                startMap.put(matcher.start(0), matcher.end());
                colorMap.put(matcher.start(0), entry.getValue());
            }
        }
 
        int start = 0;
        boolean overlap = false;
        // TODO: check the map for overlapping parts
        int[] taken = new int[text.length()];
        try
        {
        for (Map.Entry<Integer, Integer> entry : startMap.entrySet()) 
        {
        	if(overlap)
        	{
        		start = entry.getKey();
        		int endy = entry.getValue();
        		Color c = colorMap.get(start);
        		colorMap.remove(entry.getKey());
        		startMap.entrySet().remove(entry);
        		
        		if(taken[start] == 0)
        		{
        			int safe = 0;
        			for(int i = start; i < endy; i++)
        			{
        				if(taken[i] == 1)
        				{
        					safe = i - 1;
        					break;
        				}
        			}
        			startMap.put(start, safe);
                    colorMap.put(start, c);
        		}
        		continue;
        	}
        	overlap = false;
            start = entry.getKey();
            int end = entry.getValue();
            for(int i = start; i < end; i++)
            {
            	if(taken[i] == 0)
            		taken[i] = 1;
            	else
            	{
            		overlap = true;
            	}
            }
        }
        }
        catch(Exception e){}
         
        int i = 0;
        start = 0;
        // Color the parts
        for (Map.Entry<Integer, Integer> entry : startMap.entrySet()) 
        {
            start = entry.getKey();
            int end = entry.getValue();
 
            if (i < start) {
                graphics.setColor(Color.black);
                graphics.setFont(mono);
                doc.getText(p0 + i, start - i, segment);
                x = Utilities.drawTabbedText(segment, x, y, graphics, this, i);
            }
 
            graphics.setColor(colorMap.get(start));
            if(colorMap.get(start).equals(new Color(100,0,200)))
            {
            	graphics.setFont(monoBold);
            	end--;
            }
            else if(colorMap.get(start).equals(new Color(100,0,201)))
            {
            	graphics.setFont(monoBold);
            }
            else if(text.startsWith("//"))
            {
            	if(!colorMap.get(start).equals(new Color(63, 127, 95)))
            		continue;
            }
            else
            	graphics.setFont(mono);
            i = end;
            doc.getText(p0 + start, i - start, segment);
            x = Utilities.drawTabbedText(segment, x, y, graphics, this, start);
        }
        //x = Utilities.drawTabbedText(segment, x, y, graphics, this, start);
        // Paint possible remaining text black
        if (i < text.length()) {
            graphics.setColor(Color.black);
            doc.getText(p0 + i, text.length() - i, segment);
            x = Utilities.drawTabbedText(segment, x, y, graphics, this, i);
        }
        return x;
    }
}