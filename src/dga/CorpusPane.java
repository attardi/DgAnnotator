///
/// Copyright (c) 2006, Giuseppe Attardi (attardi@di.unipi.it).
///
package dga;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.*;
import java.io.*;
import java.util.regex.*;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.JDialog;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.ImageIcon;

/**
 * Display the list of sentences for a corpus in a searchable text area.
 * 
 * @author Attardi
 *
 */
public class CorpusPane extends JScrollPane {
  
  private JEditorPane textArea = null;
  protected Corpus corpus;
  protected boolean hasChanged = false;
  protected SentenceView sentenceView;
  protected WordSearcher searcher = null;
  
  Corpus getCorpus() { return corpus; }
  
  /**
   * Corresponds to the corpus annotation scheme.
   */
  public void setScheme(String scheme) {
    corpus.scheme = scheme;
  }
  
  /**
   * Create pane with an empty corpus.
   *
   */
  public CorpusPane() {
    super();
    initialize();
    corpus = new Corpus();
  }
  
  public CorpusPane(Corpus corpus) {
    super();
    initialize();
    this.corpus = corpus;
    hasChanged = false; // undo change in setLocale()
    textArea.setText(corpus.toHTML());
    textArea.setCaretPosition(0);
    textArea.setEditable(false);
  }
  
  /**
   * reset the visualization after a change to the text.
   */
  public void reset() {
    int caret = textArea.getCaretPosition();
    textArea.setText(corpus.toHTML());
    textArea.setCaretPosition(caret);
  }

  /**
   * Create pane loading corpus from file.
   * @param file
   * @throws IOException
   */
  public CorpusPane(File file) throws IOException {
    initialize();
    corpus = new Corpus();
    textArea.read(new FileReader(file), null);
    textArea.setEditable(true);
  }
  
  /**
   * This method initializes this
   * 
   * @return void
   */
  private void initialize() {
    this.setSize(400, 200);
    this.setViewportView(getTextArea());
    this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    this.addComponentListener(new java.awt.event.ComponentAdapter() {
      public void componentHidden(java.awt.event.ComponentEvent e) {
	if (searchDialog != null)
	  searchDialog.setVisible(false);
      }
    });
    this.getSearchDialog();
  }
  
  /**
   * This method initializes textArea	
   * 	
   * @return javax.swing.JTextArea	
   */
  @SuppressWarnings("serial")
  private JEditorPane getTextArea() {
    if (textArea == null) {
      textArea = new JEditorPane("text/html", "") {
	protected void paintComponent(Graphics g) {
	  Graphics2D g2 = (Graphics2D)g;
	  g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	      RenderingHints.VALUE_ANTIALIAS_ON);
	  super.paintComponent(g);
	}
      };
      textArea.setMargin(new Insets(5, 5, 5, 5));
      textArea.addKeyListener(new KeyAdapter() {
	public void keyTyped(KeyEvent e) {
	  if (e.isControlDown()) {
	    if (e.getKeyChar() == 'F' - '@') { // C-f = 'F' - '@'
	      searchDialog.setLocationRelativeTo(textArea.getParent());
	      searchDialog.setVisible(true);
	      searcher.reset();
	      wordField.requestFocusInWindow();
	    }
	  }
	}
      });
      /* does not work:
       textArea.getInputMap().put(KeyStroke.getKeyStroke('f', InputEvent.CTRL_MASK), "find");
       textArea.getActionMap().put("find",
       new AbstractAction("find") {
       public void actionPerformed(ActionEvent evt) {
       searchDialog.setVisible(true);
       searcher.reset();
       wordField.requestFocusInWindow();
       }
       });
       */
    }
    return textArea;
  }
  
  public void addHyperlinkListener(HyperlinkListener hl) {
    textArea.addHyperlinkListener(hl);
  }
  
  public void addCaretListener(CaretListener cl) {
    textArea.addCaretListener(cl);
  }
  
  public SentenceView getSentenceView(int i) {
    sentenceView = new SentenceView(corpus.getSentence(i), corpus.scheme);
    return sentenceView;
  }
  
  public SentenceView getSentenceView() {
    return sentenceView;
  }
  
  public SentenceView getSelectedSentenceView() {
    String selectedText = textArea.getSelectedText();
    Sentence sentence = new Sentence(selectedText, corpus.scheme);
    corpus.add(sentence);
    sentenceView = new SentenceView(sentence, corpus.scheme);
    return sentenceView;
  }
  
  public boolean close() {
    if (hasChanged) {
      switch (JOptionPane.showConfirmDialog(
	  null,
	  "There are unsaved annotations.\nDo you want to save them?",
	  "Unsaved Annotations",
	  JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE)) {
	  case JOptionPane.YES_OPTION:
	    return corpus.save();
	  case JOptionPane.CANCEL_OPTION:
	    return false;
      }
    }
    return true;
  }
  
  /**
   * 
   * @param file
   * @return true if save was successful.
   */
  public boolean save(File file) {
    if (corpus.save(file)) {
      hasChanged = false;
      return true;
    }
    return false;
  }
  
  /**
   * 
   * @param file
   * @return true if save was successful.
   */
  public boolean appendTo(File file) {
    if (corpus.appendTo(file)) {
      hasChanged = false;
      return true;
    }
    return false;
  }
  
  private static final long serialVersionUID = 1L;
  private JDialog searchDialog = null;  //  @jve:decl-index=0:visual-constraint="437,17"
  private JPanel searchPane = null;
  private JTextField wordField = null;
  private JButton nextButton = null;
  private JButton previousButton = null;
  /**
   * This method initializes searchDialog	
   * 	
   * @return javax.swing.JDialog	
   */
  private JDialog getSearchDialog() {
    if (searchDialog == null) {
      searchDialog = new JDialog();
      searchDialog.setSize(new java.awt.Dimension(172,55));
      searchDialog.setResizable(false);
      searchDialog.setTitle("Search");
      searchDialog.setContentPane(getSearchPane());
      searcher = new WordSearcher(getTextArea());
    }
    return searchDialog;
  }
  
  /**
   * This method initializes searchPane	
   * 	
   * @return javax.swing.JPanel	
   */
  private JPanel getSearchPane() {
    if (searchPane == null) {
      searchPane = new JPanel();
      searchPane.setLayout(new BorderLayout());
      searchPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,0,0,0));
      searchPane.add(getWordField(), java.awt.BorderLayout.CENTER);
      searchPane.add(getPreviousButton(), java.awt.BorderLayout.WEST);
      searchPane.add(getNextButton(), java.awt.BorderLayout.EAST);
    }
    return searchPane;
  }
  
  /**
   * This method initializes wordField	
   * 	
   * @return javax.swing.JTextField	
   */
  private JTextField getWordField() {
    if (wordField == null) {
      wordField = new JTextField();
      wordField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,0,0,0));
      wordField.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 14));
    }
    return wordField;
  }
  
  /**
   * This method initializes nextButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getNextButton() {
    if (nextButton == null) {
      nextButton = new JButton();
      nextButton.setPreferredSize(new java.awt.Dimension(26,26));
      nextButton.setIcon(new ImageIcon(getClass().getResource("/dga/icons/next.png")));
      nextButton.setBackground(java.awt.SystemColor.control);
      nextButton.setToolTipText("Find next");
      nextButton.addActionListener(new java.awt.event.ActionListener() {
	public void actionPerformed(java.awt.event.ActionEvent e) {
	  String word = wordField.getText();
	  int offset = searcher.search(word, true);
	  if (offset != -1) {
	    try {
	      textArea.scrollRectToVisible(textArea.modelToView(offset));
	    } catch (BadLocationException ex) {
	    }
	  }
	}
      });
    }
    return nextButton;
  }
  
  /**
   * This method initializes backSearchButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getPreviousButton() {
    if (previousButton == null) {
      previousButton = new JButton();
      previousButton.setPreferredSize(new java.awt.Dimension(26,26));
      previousButton.setIcon(new ImageIcon(getClass().getResource("/dga/icons/previous.png")));
      previousButton.setBackground(java.awt.SystemColor.control);
      previousButton.setToolTipText("Find previous");
      previousButton.addActionListener(new java.awt.event.ActionListener() {
	public void actionPerformed(java.awt.event.ActionEvent e) {
	  String word = wordField.getText().trim();
	  int offset = searcher.search(word, false);
	  if (offset != -1) {
	    try {
	      textArea.scrollRectToVisible(textArea.modelToView(offset));
	    } catch (BadLocationException ex) {
	    }
	  }
	}
      });
    }
    return previousButton;
  }
}

/**
 *	A simple class that searches for a word in
 *	a document and highlights occurrences of that word
 */
class WordSearcher {
  public WordSearcher(JTextComponent comp) {
    this.comp = comp;
    this.painter = DefaultHighlighter.DefaultPainter;
  }
  int lastIndex = 0;
  /**
   * Prepare for new search
   *
   */
  public void reset() {
    lastIndex = 0;
  }
  
  //	Search for a word and return the offset of the first occurrence.
  //  Highlights are added for all occurrences found.
  public int search(String word, boolean forward) {
    Highlighter highlighter = comp.getHighlighter();
    
    // Remove any existing highlights for last word
    Highlighter.Highlight[] highlights = highlighter.getHighlights();
    for (Highlighter.Highlight h : highlights) {
      highlighter.removeHighlight(h);
    }
    if (word == null || word.equals("")) {
      return -1;
    }
    // Look for the word we are given - insensitive search
    String content = null;
    int length;
    try {
      Document d = comp.getDocument();
      length = d.getLength();
      content = d.getText(0, length);
    } catch (BadLocationException e) {
      // Cannot happen
      return -1;
    }
    Pattern p = Pattern.compile(word, Pattern.CASE_INSENSITIVE);
    Matcher matcher = p.matcher(content);
    if (forward) {
      matcher.region(lastIndex, content.length());
      if (matcher.find(lastIndex)) {
	int nextIndex = matcher.start();
	int endIndex = matcher.end();
	try {
	  highlighter.addHighlight(nextIndex, endIndex, painter);
	} catch (BadLocationException e) {
	}
	lastIndex = endIndex;
	return lastIndex;
      }
    } else {
      if (lastIndex == 0)
	lastIndex = length;
      //matcher.region(0, lastIndex); does not work
      int nextIndex = 0;
      int endIndex = 0;
      while (matcher.find(endIndex)) {
	if (matcher.end() >= lastIndex)
	  break;
	nextIndex = matcher.start();
	endIndex = matcher.end();
      }
      try {
	highlighter.addHighlight(nextIndex, endIndex, painter);
      } catch (BadLocationException e) {
      }
      lastIndex = nextIndex;
      return lastIndex;
    }
    return -1;
  }
  
  protected JTextComponent comp;
  
  protected Highlighter.HighlightPainter painter;
}
