///
/// Copyright (c) 2005, Giuseppe Attardi (attardi@di.unipi.it).
///
package dga;

import java.awt.*;
import java.io.*;
import java.net.URL;
//import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.event.*;
import javax.swing.JMenuItem;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JTabbedPane;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import org.wonderly.swing.tabs.*;

public class DGA extends JFrame implements HyperlinkListener {

  private static final long serialVersionUID    = 1L;

  private JPanel	jContentPane	= null;
  private JMenuBar	menuBar	     = null;
  private JMenu		fileMenu	    = null;
  private JMenuItem	openMenuItem	= null;
  private JMenuItem	saveMenuItem	= null;
  private JMenuItem	appendToMenuItem    = null;
  private JMenuItem	compareWithMenuItem = null;
  private JMenuItem	exitMenuItem	    = null;
  private JMenu	     	helpMenu	    = null;
  private JMenuItem	helpMenuItem	    = null;
  private JMenu	     	configMenu	    = null;
  private JToolBar	toolBar		    = null;

  private JButton	openAction	  = null;
  private JButton	saveAction	  = null;
  private JButton	annotateAction      = null;

  protected Annotator	annotator;
  protected boolean	hasChanged	  = false;
  protected CorpusPane	corpusPane;		 // @jve:decl-index=0:visual-constraint="815,68"
  protected Corpus	compareCorpus;
  /**
   * Determine whether to show POS or coarse POS.
   */
  public boolean	    viewPOS	     = true;

  public void modelChanged() {
    hasChanged = true;
    corpusPane.hasChanged = true;
    appendToMenuItem.setEnabled(true);
    saveMenuItem.setEnabled(true);
    if (corpusPane.getCorpus().docFile != null) saveAction.setEnabled(true);
  }

  /**
   * This method initializes menuBar
   * 
   * @return javax.swing.JMenuBar
   */
  public JMenuBar getJMenuBar() {
    if (menuBar == null) {
      menuBar = new JMenuBar();
      // menuBar.setBackground(java.awt.SystemColor.control);
      menuBar.add(getFileMenu());
      menuBar.add(getConfigMenu());
      menuBar.add(getHelpMenu());
    }
    return menuBar;
  }

  /**
   * This method initializes fileMenu
   * 
   * @return javax.swing.JMenu
   */
  private JMenu getFileMenu() {
    if (fileMenu == null) {
      fileMenu = new JMenu();
      fileMenu.setText("File");
      fileMenu.setMnemonic(java.awt.event.KeyEvent.VK_F);
      fileMenu.add(getOpenMenuItem());
      fileMenu.add(getSaveMenuItem());
      fileMenu.add(getAppendToMenuItem());
      fileMenu.add(getCompareWithMenuItem());
      fileMenu.add(getSaveImageMenuItem());
      fileMenu.add(getExitMenuItem());
    }
    return fileMenu;
  }

  /**
   * This method initializes exitMenuItem
   * 
   * @return javax.swing.JMenuItem
   */
  private JMenuItem getExitMenuItem() {
    if (exitMenuItem == null) {
      exitMenuItem = new JMenuItem();
      exitMenuItem.setText("Exit");
      exitMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_X);
      exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
	public void actionPerformed(java.awt.event.ActionEvent e) {
	  doExit();
	}
      });
    }
    return exitMenuItem;
  }

  /**
   * This method initializes openMenuItem
   * 
   * @return javax.swing.JMenuItem
   */
  private JMenuItem getOpenMenuItem() {
    if (openMenuItem == null) {
      openMenuItem = new JMenuItem();
      openMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_O);
      openMenuItem.setIcon(new ImageIcon(getClass().getResource(
	  "/dga/icons/open.gif")));
      openMenuItem.setText("Open");
      openMenuItem.addActionListener(new java.awt.event.ActionListener() {
	public void actionPerformed(java.awt.event.ActionEvent e) {
	  open();
	}
      });
    }
    return openMenuItem;
  }

  /**
   * This method initializes saveMenuItem
   * 
   * @return javax.swing.JMenuItem
   */
  private JMenuItem getSaveMenuItem() {
    if (saveMenuItem == null) {
      saveMenuItem = new JMenuItem();
      saveMenuItem.setText("Save As");
      saveMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_S);
      saveMenuItem.setIcon(new ImageIcon(getClass().getResource(
	  "/dga/icons/save.gif")));
      saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
	public void actionPerformed(java.awt.event.ActionEvent e) {
	  saveAs();
	}
      });
    }
    return saveMenuItem;
  }

  /**
   * This method initializes helpMenu
   * 
   * @return javax.swing.JMenu
   */
  private JMenu getHelpMenu() {
    if (helpMenu == null) {
      helpMenu = new JMenu();
      helpMenu.setText("Help");
      helpMenu.setMnemonic(java.awt.event.KeyEvent.VK_HELP);
      helpMenu.add(getHelpMenuItem());
      helpMenu.add(getAboutItem());
    }
    return helpMenu;
  }

  /**
   * This method initializes configMenu
   * 
   * @return javax.swing.JMenu
   */
  private JMenu getConfigMenu() {
    if (configMenu == null) {
      configMenu = new JMenu();
      configMenu.setText("Configure");
      configMenu.setMnemonic(java.awt.event.KeyEvent.VK_C);
      configMenu.add(getCorpusMenu());
      configMenu.add(getPosMenuItem());
    }
    return configMenu;
  }

  /**
   * This method initializes toolBar
   * 
   * @return javax.swing.JToolBar
   */
  private JToolBar getToolBar() {
    if (toolBar == null) {
      toolBar = new JToolBar();
      // toolBar.setBorder(new
      // javax.swing.border.SoftBevelBorder(javax.swing.border.SoftBevelBorder.RAISED));
      toolBar.setBackground(java.awt.SystemColor.control);
      toolBar.setFloatable(false);
      toolBar.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
      toolBar.add(getOpenAction());
      toolBar.add(getSaveAction());
      toolBar.add(getSaveImageAction());
      toolBar.add(getPanAction());
      toolBar.add(getZoomAction());
      toolBar.addSeparator();
      toolBar.add(getUndoAction());
      toolBar.add(getRedoAction());
      toolBar.addSeparator();
      toolBar.add(getAnnotateAction());
      toolBar.addSeparator();
    }
    return toolBar;
  }

  /**
   * This method initializes openAction
   * 
   * @return javax.swing.JButton
   */
  private JButton getOpenAction() {
    if (openAction == null) {
      openAction = new JButton();
      openAction.setIcon(new ImageIcon(getClass().getResource(
	  "/dga/icons/open.gif")));
      openAction.setToolTipText("Open file");
      openAction.setPreferredSize(new java.awt.Dimension(30, 30));
      openAction.setBorderPainted(false);
      openAction.setBackground(java.awt.SystemColor.control);
      openAction.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
      openAction.setMnemonic(java.awt.event.KeyEvent.VK_O);
      openAction.addActionListener(new java.awt.event.ActionListener() {
	public void actionPerformed(java.awt.event.ActionEvent e) {
	  open();
	}
      });
    }
    return openAction;
  }

  /**
   * Directory from which to start open/save
   */
  String	    currentDir	= System.getProperty("user.dir");

  private JMenuItem saveImageMenuItem = null;

  private JButton   saveImageAction   = null;

  private CorpusPane open(File file) throws Exception {
    CorpusPane cp = new CorpusPane(new Corpus(file));
    corpusTabs.addTab(file.getName(), null, cp, null);
    corpusTabs.setSelectedComponent(cp);
    cp.addHyperlinkListener(this);
    corpusPane = cp;
    saveAction.setEnabled(false);
    compareCorpus = null;
    return cp;
  }

  private boolean open() {
    if (hasChanged) {
      switch (JOptionPane.showConfirmDialog(null,
	  "There are unsaved annotations.\nDo you want to save them?",
	  "Unsaved Annotations", JOptionPane.YES_NO_CANCEL_OPTION,
	  JOptionPane.INFORMATION_MESSAGE)) {
      case JOptionPane.YES_OPTION:
	save();
	break;
      case JOptionPane.CANCEL_OPTION:
	return false;
      }
    }
    JFileChooser fileChooser = new JFileChooser(currentDir);
    fileChooser.addChoosableFileFilter(new XmlFileFilter());
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      File selected = fileChooser.getSelectedFile();
      currentDir = selected.getParent();
      try {
	CorpusPane cp;
	if (selected.getPath().endsWith(".xml")
	    || selected.getPath().endsWith(".tab")
	    || selected.getPath().endsWith(".conll")
	    || selected.getPath().endsWith(".conllu")) {
	  cp = open(selected);
	  compareWithMenuItem.setEnabled(true);
	} else {
	  cp = open(selected);
	  cp.setLocale(annotator.getLocale());
	  // get notified about changes in text selection
	  cp.addCaretListener(caretListener);
	  appendToMenuItem.setEnabled(false);
	  compareWithMenuItem.setEnabled(false);
	}
	return true;
      } catch (Exception e) {
	JOptionPane.showMessageDialog(this, "Error reading file: " + selected
	    + "\n" + e.getMessage(), "Read Error", JOptionPane.ERROR_MESSAGE);
      }
    }
    return false;
  }

  /**
   * Save corpus of currently open Tab
   *
   */
  private void save() {
    corpusPane.getCorpus().save();
  }

  private boolean saveAs() {
    JFileChooser fileChooser = new JFileChooser(currentDir);
    fileChooser.addChoosableFileFilter(new XmlFileFilter());
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
      File selected = fileChooser.getSelectedFile();
      currentDir = selected.getParent();
      if (corpusPane.save(selected)) {
	saveAction.setEnabled(false);
	appendToMenuItem.setEnabled(false);
	return true;
      }
    }
    return false;
  }

  /**
   * Append current tagged sentence to corpus file.
   *
   */
  private void appendTo() {
    JFileChooser fileChooser = new JFileChooser(currentDir);
    fileChooser.addChoosableFileFilter(new XmlFileFilter());
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
      File selected = fileChooser.getSelectedFile();
      currentDir = selected.getParent();
      if (corpusPane.appendTo(selected)) {
	saveAction.setEnabled(false);
	appendToMenuItem.setEnabled(false);
      }
    }
  }

  private boolean compareWith() {
    JFileChooser fileChooser = new JFileChooser(currentDir);
    fileChooser.addChoosableFileFilter(new XmlFileFilter());
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      File selected = fileChooser.getSelectedFile();
      currentDir = selected.getParent();
      try {
	if (selected.getPath().endsWith(".xml")
	    || selected.getPath().endsWith(".tab")
	    || selected.getPath().endsWith(".conll")
	    || selected.getPath().endsWith(".conllu")) {
	  compareCorpus = new Corpus(selected);
	  // check that corpus contains same sentences
	  Corpus corpus = corpusPane.corpus;
	  if (corpus.sentences.size() != compareCorpus.sentences.size()) return false;
	  for (int i = 0; i < corpus.sentences.size(); i++) {
	    String[] words1 = corpus.sentences.get(i).forms;
	    String[] words2 = compareCorpus.sentences.get(i).forms;
	    if (words1.length != words2.length) return false;
	    for (int j = 0; j < words1.length; j++)
	      if (!words1[j].equals(words2[j])) return false;
	  }
	}
	return true;
      } catch (Exception e) {
      }
    }
    return false;
  }

  private boolean saveImage() {
    JFileChooser fileChooser = new JFileChooser(currentDir);
    fileChooser.addChoosableFileFilter(new ImageFileFilter());
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
      File selected = fileChooser.getSelectedFile();
      currentDir = selected.getParent();
      return annotator.saveImage(selected);
    }
    return false;
  }

  /**
   * This method initializes annotator
   * 
   * @return dga.annotator
   */
  private Annotator getAnnotator() {
    if (annotator == null) {
      annotator = new Annotator(this);
      annotator.setPreferredSize(new java.awt.Dimension(250, 250));
      annotator.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0,
	  0));
    }
    return annotator;
  }

  /**
   * This method initializes saveAction
   * 
   * @return javax.swing.JButton
   */
  private JButton getSaveAction() {
    if (saveAction == null) {
      saveAction = new JButton();
      saveAction.setMnemonic(java.awt.event.KeyEvent.VK_S);
      saveAction.setIcon(new ImageIcon(getClass().getResource(
	  "/dga/icons/save.gif")));
      saveAction.setToolTipText("Save to file");
      saveAction.setPreferredSize(new java.awt.Dimension(30, 30));
      saveAction.setBorderPainted(false);
      saveAction.setBackground(java.awt.SystemColor.control);
      saveAction.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
      saveAction.setEnabled(false);
      saveAction.addActionListener(new java.awt.event.ActionListener() {
	public void actionPerformed(java.awt.event.ActionEvent e) {
	  save();
	}
      });
    }
    return saveAction;
  }

  static final String helpFile     = "/Doc/index.htm";

  private JButton     panAction    = null;

  private JButton     zoomAction   = null;

  private JMenu       corpusMenu   = null;

  /**
   * This method initializes helpMenuItem
   * 
   * @return javax.swing.JMenuItem
   */
  private JMenuItem getHelpMenuItem() {
    if (helpMenuItem == null) {
      helpMenuItem = new JMenuItem();
      helpMenuItem.setText("Manual");
      helpMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_M);
      com.Ostermiller.util.Browser.init();
      helpMenuItem.addActionListener(new java.awt.event.ActionListener() {
	public void actionPerformed(java.awt.event.ActionEvent e) {
	  try {
	    com.Ostermiller.util.Browser.displayURL("file:///" + currentDir
		+ helpFile);
	  } catch (Exception exc) {
	  }
	}
      });
    }
    return helpMenuItem;
  }

  /**
   * This method initializes annotateAction
   * 
   * @return javax.swing.JButton
   */
  private JButton getAnnotateAction() {
    if (annotateAction == null) {
      annotateAction = new JButton();
      annotateAction.setIcon(new ImageIcon(getClass().getResource(
	  "/dga/icons/pencil.gif")));
      annotateAction.setToolTipText("Annotate selected text");
      annotateAction.setPreferredSize(new java.awt.Dimension(30, 30));
      annotateAction.setBorderPainted(false);
      annotateAction.setBackground(java.awt.SystemColor.control);
      annotateAction.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
      annotateAction.setEnabled(false);
      annotateAction.addActionListener(new java.awt.event.ActionListener() {
	public void actionPerformed(java.awt.event.ActionEvent e) {
	  annotate();
	}
      });
    }
    return annotateAction;
  }

  private void annotate() {
    if (corpusPane.hasChanged) {
      switch (JOptionPane.showConfirmDialog(null,
	  "There are unsaved annotations.\nDo you want to save them?",
	  "Unsaved Annotations", JOptionPane.YES_NO_CANCEL_OPTION,
	  JOptionPane.INFORMATION_MESSAGE)) {
      case JOptionPane.YES_OPTION:
	appendTo();
	break;
      case JOptionPane.CANCEL_OPTION:
	return;
      }
    }
    annotator.sentence(corpusPane.getSelectedSentenceView());
    saveImageMenuItem.setEnabled(true);
    saveImageAction.setEnabled(true);
  }

  public void hyperlinkUpdate(HyperlinkEvent event) {
    if (event.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
      int id = 0;
      try {
	id = Integer.parseInt(event.getDescription());
      } catch (Exception exception) {
	return;
      }
      SentenceView sv = corpusPane.getSentenceView(id);
      sv.viewPOS = viewPOS;
      annotator.sentence(sv);
      if (compareCorpus != null)
	annotator.compareWith(compareCorpus.getSentence(id));
      annotateAction.setEnabled(false);
      saveImageMenuItem.setEnabled(true);
      saveImageAction.setEnabled(true);
    }
  }

  /**
   * This method initializes appendToMenuItem
   * 
   * @return javax.swing.JMenuItem
   */
  private JMenuItem getAppendToMenuItem() {
    if (appendToMenuItem == null) {
      appendToMenuItem = new JMenuItem();
      appendToMenuItem.setText("Append To...");
      appendToMenuItem.setEnabled(false);
      appendToMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_A);
      appendToMenuItem.setToolTipText("Append tagged sentence to corpus file");
      appendToMenuItem.addActionListener(new java.awt.event.ActionListener() {
	public void actionPerformed(java.awt.event.ActionEvent e) {
	  appendTo();
	}
      });
    }
    return appendToMenuItem;
  }

  /**
   * This method initializes compareToMenuItem
   * 
   * @return javax.swing.JMenuItem
   */
  private JMenuItem getCompareWithMenuItem() {
    if (compareWithMenuItem == null) {
      compareWithMenuItem = new JMenuItem();
      compareWithMenuItem.setEnabled(false);
      compareWithMenuItem.setToolTipText("Load other corpus to compare with");
      compareWithMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_C);
      compareWithMenuItem.setText("Compare With ...");
      compareWithMenuItem
	  .addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent e) {
	      compareWith();
	    }
	  });
    }
    return compareWithMenuItem;
  }

  /**
   * This method initializes saveImageMenuItem
   * 
   * @return javax.swing.JMenuItem
   */
  private JMenuItem getSaveImageMenuItem() {
    if (saveImageMenuItem == null) {
      saveImageMenuItem = new JMenuItem();
      saveImageMenuItem.setText("Save image");
      saveImageMenuItem.setActionCommand("saveImage");
      saveImageMenuItem.setToolTipText("Save to PNG file");
      saveImageMenuItem.setIcon(new ImageIcon(getClass().getResource(
	  "/dga/icons/snapshot.gif")));
      saveImageMenuItem.setEnabled(false);
      saveImageMenuItem.addActionListener(new java.awt.event.ActionListener() {
	public void actionPerformed(java.awt.event.ActionEvent e) {
	  saveImage();
	}
      });
    }
    return saveImageMenuItem;
  }

  /**
   * This method initializes imageAction
   * 
   * @return javax.swing.JButton
   */
  private JButton getSaveImageAction() {
    if (saveImageAction == null) {
      saveImageAction = new JButton();
      saveImageAction.setToolTipText("Save PNG image");
      saveImageAction.setIcon(new ImageIcon(getClass().getResource(
	  "/dga/icons/snapshot.gif")));
      saveImageAction.setPreferredSize(new java.awt.Dimension(30, 30));
      saveImageAction.setBorderPainted(false);
      saveImageAction.setBackground(java.awt.SystemColor.control);
      saveImageAction.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
      saveImageAction.setEnabled(false);
      saveImageAction.addActionListener(new java.awt.event.ActionListener() {
	public void actionPerformed(java.awt.event.ActionEvent e) {
	  saveImage();
	}
      });
    }
    return saveImageAction;
  }

  /**
   * This method initializes panAction
   * 
   * @return javax.swing.JButton
   */
  private JButton getPanAction() {
    if (panAction == null) {
      panAction = new JButton();
      panAction.setIcon(new ImageIcon(getClass().getResource(
	  "/dga/icons/panPressed.gif")));
      panAction.setIconTextGap(0);
      panAction.setPreferredSize(new java.awt.Dimension(30, 30));
      panAction.setBorderPainted(false);
      panAction.setBackground(java.awt.SystemColor.control);
      panAction.setSelected(true);
      panAction.setPressedIcon(new ImageIcon(getClass().getResource(
	  "/dga/icons/pan.gif")));
      panAction.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
      panAction.setToolTipText("Enable panning");
      panAction.addActionListener(new java.awt.event.ActionListener() {
	public void actionPerformed(java.awt.event.ActionEvent e) {
	  switchMode();
	  annotator.EnablePanning();
	}
      });
    }
    return panAction;
  }

  /**
   * This method initializes zoomAction
   * 
   * @return javax.swing.JButton
   */
  private JButton getZoomAction() {
    if (zoomAction == null) {
      zoomAction = new JButton();
      zoomAction.setPreferredSize(new java.awt.Dimension(30, 30));
      zoomAction.setIcon(new ImageIcon(getClass().getResource(
	  "/dga/icons/zoom.gif")));
      zoomAction.setBorderPainted(false);
      zoomAction.setBackground(java.awt.SystemColor.control);
      zoomAction.setPressedIcon(new ImageIcon(getClass().getResource(
	  "/dga/icons/zoomPressed.gif")));
      zoomAction.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
      zoomAction.setToolTipText("Enable Zoom");
      zoomAction.addActionListener(new java.awt.event.ActionListener() {
	public void actionPerformed(java.awt.event.ActionEvent e) {
	  switchMode();
	  annotator.EnableZooming();
	}
      });
    }
    return zoomAction;
  }

  void switchMode() {
    Icon tmp = panAction.getIcon();
    panAction.setIcon(panAction.getPressedIcon());
    panAction.setPressedIcon(tmp);
    tmp = zoomAction.getIcon();
    zoomAction.setIcon(zoomAction.getPressedIcon());
    zoomAction.setPressedIcon(tmp);
  }

  @SuppressWarnings("serial")
  /**
   * Action when a Corpus is selected
   * @author Attardi
   *
   */
  class CorpusListener extends AbstractAction {
    String corpus;

    CorpusListener(String corpus) {
      this.corpus = corpus;
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
      corpusPane.setScheme(corpus);
      annotator.setScheme(corpus);
    }
  }

  class PatternFilter implements FilenameFilter {
    public PatternFilter(String p) {
      re = p;
    }

    public boolean accept(File dir, String s) {
      return s.matches(re);
    }

    private String re;
  }

  // Descriptions of corpora:
  private PatternFilter filter = new PatternFilter("DGA_.*\\.xml");
  
  /**
   * Get list of pairs (corpus, language) from all resource files
   * @return
   */
  private Vector<String[]> getCorpora() {
    File dir = null;
    try {
      URL url = getClass().getResource("DGA.xml");
      String path = url.getPath();
      if (url.getProtocol().equals("jar")) {
	// A JAR path, strip "file:" and "!/dga/DGA.xml"
	path = path.substring(5, path.indexOf('!'));
	path = URLDecoder.decode(path, "UTF-8");	// e.g. spaces
	// remove extra slash in front of Windows path:
	dir = new File(path).getCanonicalFile();
        // resources should be in directory "dga" relative to jar file
	dir = dir.toPath().resolveSibling("dga").toFile();
      } else {
	path = URLDecoder.decode(path, "UTF-8");
	dir = new File(path).getParentFile();
      }
    } catch (Exception e) {
    }
    Vector<String[]> pairs = new Vector<String[]>();
    for (String f : dir.list(filter)) {
      try {
	FileInputStream fis = new FileInputStream(new File(dir, f));
	ResourceBundle bundle = new XMLResourceBundleControl.XMLResourceBundle(fis);
	pairs.add(new String[] { bundle.getString("corpus"),
	    bundle.getString("language") });
      } catch (IOException e) {
	System.err.println("Failed loading: " + f);
      }
    }
    return pairs;
  }

  /**
   * This method initializes corpusMenu
   * 
   * @return javax.swing.JMenu
   */
  private JMenu getCorpusMenu() {
    if (corpusMenu == null) {
      Vector<String[]> corpora = getCorpora();
      corpusMenu = new JMenu();
      corpusMenu.setText("Corpus...");
      for (String[] pair : corpora) {
	JMenuItem item = new JMenuItem();
	item.setText(pair[0]);
	item.addActionListener(new CorpusListener(pair[1]));
	corpusMenu.add(item);
      }
    }
    return corpusMenu;
  }

  private JButton	     undoAction = null;

  private JButton	     redoAction = null;

  private CloseableTabbedPane corpusTabs = null;

  /**
   * This method initializes undoAction
   * 
   * @return javax.swing.JButton
   */
  private JButton getUndoAction() {
    if (undoAction == null) {
      undoAction = new JButton();
      undoAction.setToolTipText("Undo last edit");
      undoAction.setIcon(new ImageIcon(getClass().getResource(
	  "/dga/icons/undo.png")));
      undoAction.setBorderPainted(false);
      undoAction.setPreferredSize(new java.awt.Dimension(30, 30));
      undoAction.setEnabled(false);
      undoAction.setBackground(java.awt.SystemColor.control);
      undoAction.setRolloverIcon(new ImageIcon(getClass().getResource(
	  "/dga/icons/undoHover.png")));
      undoAction.addActionListener(new java.awt.event.ActionListener() {
	public void actionPerformed(java.awt.event.ActionEvent e) {
	  annotator.undo();
	}
      });
    }
    return undoAction;
  }

  public void enableUndo(boolean e) {
    undoAction.setEnabled(e);
  }

  /**
   * This method initializes redoAction
   * 
   * @return javax.swing.JButton
   */
  private JButton getRedoAction() {
    if (redoAction == null) {
      redoAction = new JButton();
      redoAction.setIcon(new ImageIcon(getClass().getResource(
	  "/dga/icons/redo.png")));
      redoAction.setSize(new java.awt.Dimension(30, 30));
      redoAction.setBorderPainted(false);
      redoAction.setPreferredSize(new java.awt.Dimension(30, 30));
      redoAction.setEnabled(false);
      redoAction.setBackground(java.awt.SystemColor.control);
      redoAction.setToolTipText("Redo last undone action");
      redoAction.setRolloverIcon(new ImageIcon(getClass().getResource(
	  "/dga/icons/redoHover.png")));
      redoAction.addActionListener(new java.awt.event.ActionListener() {
	public void actionPerformed(java.awt.event.ActionEvent e) {
	  annotator.redo();
	}
      });
    }
    return redoAction;
  }

  public void enableRedo(boolean e) {
    redoAction.setEnabled(e);
  }

  /**
   * This method initializes corpusTabs
   * 
   * @return javax.swing.JTabbedPane
   */
  private JTabbedPane getCorpusTabs() {
    if (corpusTabs == null) {
      corpusTabs = new CloseableTabbedPane();
      corpusTabs.setPreferredSize(new java.awt.Dimension(600, 250));
      corpusTabs.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0,
	  0));
      corpusTabs.addTab("Scratch", getCorpusPane());
      corpusTabs.setUnclosableTab(0);
      corpusTabs.addChangeListener(new javax.swing.event.ChangeListener() {
	public void stateChanged(javax.swing.event.ChangeEvent e) {
	  corpusPane = (CorpusPane) corpusTabs.getSelectedComponent();
	  annotator.sentence(corpusPane.getSentenceView());
	}
      });
      corpusTabs.addTabCloseListener(new TabCloseListener() {
	public void tabClosed(TabCloseEvent e) {
	  int idx = e.getClosedTab();
	  CorpusPane cp = (CorpusPane) corpusTabs.getComponentAt(idx);
	  // assert (cp == corpusPane) because stateChanged() has been called.
	  if (cp.close()) {
	    corpusTabs.remove(cp);
	  }
	  // closing when not selected, selects it first. Hence restore
	  // selected.
	  corpusPane = (CorpusPane) corpusTabs.getSelectedComponent();
	  if (cp != corpusPane) {
	    // restore AnnotateCanvas, changed by stateChanged()
	    annotator.sentence(corpusPane.getSentenceView());
	  }
	}
      });
    }
    return corpusTabs;
  }

  /**
   * Listener to get notifications about changes in selection
   */
  CaretListener       caretListener      = new CaretListener() {
					   public void caretUpdate(CaretEvent e) {
					     annotateAction.setEnabled(e
						 .getDot() != e.getMark());
					   }
					 };

  private JOptionPane aboutPane	  = null;

  private JMenuItem   aboutItem	  = null;

  private JDialog     aboutDialog	= null; // @jve:decl-index=0:visual-constraint="10,613"

  private JPanel      aboutDialogContent = null;

  private JMenuItem   posMenuItem	= null;

  /**
   * This method initializes corpusPane
   * 
   * @return javax.swing.JTabbedPane
   */
  private CorpusPane getCorpusPane() {
    if (corpusPane == null) {
      corpusPane = new CorpusPane();
      // get notified about changes in selection
      corpusPane.addCaretListener(caretListener);
    }
    return corpusPane;
  }

  /**
   * This method initializes aboutPane
   * 
   * @return javax.swing.JOptionPane
   */
  private JOptionPane getAboutPane() {
    if (aboutPane == null) {
      aboutPane = new JOptionPane("DgAnnotator Version 1.1\n"
	  + "    by Giuseppe Attardi\n" + "    attardi@di.unipi.it",
	  JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION,
	  new ImageIcon(getClass().getResource("/dga/icons/Dga.png")));
      aboutPane.setPreferredSize(new java.awt.Dimension(262, 130));

      aboutPane.addPropertyChangeListener(new PropertyChangeListener() {
	public void propertyChange(PropertyChangeEvent e) {
	  // Reset the JOptionPane's value.
	  // If you don't do this, then if the user
	  // presses the same button next time, no
	  // property change event will be fired.
	  aboutPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
	  if (aboutDialog.isVisible()) aboutDialog.setVisible(false);
	}
      });
    }
    return aboutPane;
  }

  /**
   * This method initializes aboutItem
   * 
   * @return javax.swing.JMenuItem
   */
  private JMenuItem getAboutItem() {
    if (aboutItem == null) {
      aboutItem = new JMenuItem();
      aboutItem.setText("About");
      aboutItem.addActionListener(new java.awt.event.ActionListener() {
	public void actionPerformed(java.awt.event.ActionEvent e) {
	  getAboutDialog().setVisible(true);
	}
      });
    }
    return aboutItem;
  }

  /**
   * This method initializes aboutDialog
   * 
   * @return javax.swing.JDialog
   */
  private JDialog getAboutDialog() {
    if (aboutDialog == null) {
      aboutDialog = new JDialog();
      aboutDialog.setSize(new java.awt.Dimension(256, 159));
      aboutDialog.setResizable(false);
      aboutDialog.setModal(true);
      aboutDialog.setPreferredSize(new java.awt.Dimension(268, 150));
      aboutDialog.setContentPane(getAboutDialogContent());
      aboutDialog.setLocationRelativeTo(null);
    }
    return aboutDialog;
  }

  /**
   * This method initializes aboutDialogContent
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getAboutDialogContent() {
    if (aboutDialogContent == null) {
      aboutDialogContent = new JPanel();
      aboutDialogContent.setLayout(new BorderLayout());
      aboutDialogContent.add(getAboutPane(), java.awt.BorderLayout.NORTH);
    }
    return aboutDialogContent;
  }

  /**
   * This method initializes posMenuItem
   * 
   * @return javax.swing.JMenuItem
   */
  private JMenuItem getPosMenuItem() {
    if (posMenuItem == null) {
      posMenuItem = new JMenuItem();
      posMenuItem.setText("View POS");
      posMenuItem.addActionListener(new java.awt.event.ActionListener() {
	public void actionPerformed(java.awt.event.ActionEvent e) {
	  viewPOS = !viewPOS;
	  annotator.viewPOS(viewPOS);
	}
      });
    }
    return posMenuItem;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    String laf = UIManager.getCrossPlatformLookAndFeelClassName();
    // String laf = UIManager.getSystemLookAndFeelClassName();
    // String laf = "org.jvnet.substance.SubstanceLookAndFeel";
    // String laf = "com.birosoft.liquid.LiquidLookAndFeel";
    // String laf = "com.jgoodies.looks.windows.WindowsLookAndFeel";
    try {
      UIManager.setLookAndFeel(laf);
    } catch (Exception exception) {
      System.err.println("Can't set " + laf);
    }
    DGA dga = new DGA(args);
    dga.setVisible(true);
  }

  /**
   * This is the default constructor
   */
  public DGA(String[] args) {
    super();
    initialize();
    for (String file : args) {
      try {
	open(new File(file));
      } catch (Exception e) {
      }
    }
  }

  /**
   * This method initializes this
   * 
   * @return void
   */
  private void initialize() {
    this.setSize(new java.awt.Dimension(800, 600));
    this.setLocationRelativeTo(null);
    this.setJMenuBar(getJMenuBar());
    this.setContentPane(getJContentPane());
    this.setTitle("DG Annotator");
    this.addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent e) {
	doExit();
      }
    });
  }

  private void doExit() {
    if (hasChanged) {
      switch (JOptionPane.showConfirmDialog(null,
	  "There are unsaved annotations.\nDo you want to save them?",
	  "Unsaved Annotations", JOptionPane.YES_NO_CANCEL_OPTION,
	  JOptionPane.INFORMATION_MESSAGE)) {
      case JOptionPane.YES_OPTION:
	save();
	break;
      case JOptionPane.CANCEL_OPTION:
	return;
      }
    }
    System.exit(0);
  }

  /**
   * This method initializes jContentPane
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getJContentPane() {
    if (jContentPane == null) {
      jContentPane = new JPanel();
      jContentPane.setLayout(new BorderLayout());
      jContentPane.add(getToolBar(), BorderLayout.NORTH);
      jContentPane.add(getAnnotator(), BorderLayout.CENTER);
      jContentPane.add(getCorpusTabs(), BorderLayout.SOUTH);
    }
    return jContentPane;
  }

  /*
   * SentenceView sentenceView;
   * 
   * static final String test = "<DGA><s><tok id='1'><orth>il</orth>" +
   * "<syn head='2'><reltype>det</reltype></syn></tok>" +
   * "<tok id='2'><orth>Governo</orth></tok>" + "</s></DGA>";
   * 
   * private SentenceView getSentenceView() { if (sentenceView == null) {
   * sentenceView = new SentenceView(test); sentenceView.setPreferredSize(new
   * Dimension(100, 30)); } return sentenceView; }
   */

  class XmlFileFilter extends FileFilter {

    public boolean accept(File file) {
      if (file.isDirectory()) return true;
      String s = file.getName();
      int i = s.lastIndexOf('.');
      if (i > 0 && i < s.length() - 1) s = s.substring(i + 1);
      return s != null
	  && (s.equalsIgnoreCase("xml") || s.equalsIgnoreCase("tsv") ||
	      s.equalsIgnoreCase("conll") || s.equalsIgnoreCase("conllu"));
    }

    public String getDescription() {
      return "XML, CoNLL file";
    }
  }

  class ImageFileFilter extends FileFilter {

    public boolean accept(File file) {
      if (file.isDirectory()) return true;
      String s = file.getName();
      int i = s.lastIndexOf('.');
      if (i > 0 && i < s.length() - 1) s = s.substring(i + 1);
      return s != null && (s.equalsIgnoreCase("png"));
    }

    public String getDescription() {
      return "PNG image file";
    }
  }

} // @jve:decl-index=0:visual-constraint="10,10"
