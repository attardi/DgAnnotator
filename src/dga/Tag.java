///
/// Copyright (c) 2005, Giuseppe Attardi (attardi@di.unipi.it).
///
package dga;

import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * This class shows a Tag. 
 */
@SuppressWarnings("serial")
public class Tag extends JLabel implements UIElement {

	private String label;
	private String description;
	//private SentencePanel sentenceView;

	public Tag(String label, String description, String sentence) {
		super(label);
		this.description = description;
		this.label = label;
		//this.sentenceView = null; //new SentenceView(sentence);
	}
	public Object getItem() {
		return this;
	}
	
	public String getDescription() {
		return description;
	}
	
	public JComponent getComponent() {
		return this;
	}
	
	public String toString() {
		return label;
	}
}
