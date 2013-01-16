package org.zzl.minegaming.SEA.TextEdit;
import javax.swing.JEditorPane;

public class ScriptEditor extends JEditorPane {
 
    private static final long serialVersionUID = 6270183148379328084L;
 
    public ScriptEditor() {
         
        // Set editor kit
        this.setEditorKitForContentType("text/rbc", new ScriptEditorKit());
        this.setContentType("text/rbc");
    }
     
}