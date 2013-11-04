package org.zzl.minegaming.SEA.TextEdit;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.ViewFactory;


public class ScriptEditorKit extends StyledEditorKit {
	private static final long serialVersionUID = 2969179649596107757L;
    private ViewFactory scriptViewFactory;
 
    public ScriptEditorKit() {
        scriptViewFactory = new ScriptViewFactory();
    }
     
    @Override
    public ViewFactory getViewFactory() {
        return scriptViewFactory;
    }
 
    @Override
    public String getContentType() {
        return "text/rbc";
    }
}
