package org.zzl.minegaming.SEA.TextEdit;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class ScriptViewFactory extends Object implements ViewFactory {
    
	/**
     * @see javax.swing.text.ViewFactory#create(javax.swing.text.Element)
     */
	@Override
	public View create(Element element) {
		// TODO Auto-generated method stub
		return new ScriptView(element);
	}
 
}