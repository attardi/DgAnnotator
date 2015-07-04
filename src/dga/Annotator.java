///
/// Copyright (c) 2005, Giuseppe Attardi (attardi@di.unipi.it).
///
package dga;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ResourceBundle;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JLabel;

import java.awt.CardLayout;

public class Annotator extends JPanel {
  
  private JPanel leftPanel = null;
  private JPanel depsPanel = null;
  private JLabel relTitle = null;
  private JScrollPane depsScroll = null;
  private JList<Object> depsList = null;
  
  private JPanel posPanel = null;
  private JLabel posTitle = null;
  private JScrollPane posScroll = null;
  private JList<Object> posList = null;
  
  private JScrollPane scrollCanvas = null;
  private AnnotateCanvas annotateCanvas = null;
  
  /**
   * Corpus annotation scheme.
   */
  private String scheme = "UD";
  
  /**
   * Sensitive deps
   */
  private ArrayListModel<Tag> depsModel = null;
  
  /**
   * Sensitive pos
   */
  private ArrayListModel<Tag> posModel = null;
  
  /**
   * This method initializes depsList	
   * 	
   * @return javax.swing.JList	
   */
  @SuppressWarnings("serial")
  private JList<Object> getDepsList() {
    if (depsList == null) {
      depsList = new JList<Object>() {
	// This method is called as the cursor moves within the list.
	public String getToolTipText(MouseEvent e) {
	  int index = locationToIndex(e.getPoint());
	  UIElement element = (UIElement)getModel().getElementAt(index);
	  return element.getDescription();
	}					
      };
      depsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
      depsList.setBackground(java.awt.SystemColor.control);
      depsList.setModel(getDepsModel());
      depsList.addMouseListener(new java.awt.event.MouseAdapter() {
	public void mouseClicked(java.awt.event.MouseEvent e) {
	  annotateCanvas.setDependency(depsList.getSelectedValue().toString());
	}
      });
    }
    return depsList;
  }
  
  /**
   * This method initializes posList.	
   * 	
   * @return javax.swing.JList	
   */
  @SuppressWarnings("serial")
  private JList<Object> getPosList() {
    if (posList == null) {
      posList = new JList<Object>() {
	// This method is called as the cursor moves within the list.
	public String getToolTipText(MouseEvent e) {
	  int index = locationToIndex(e.getPoint());
	  UIElement element = (UIElement)getModel().getElementAt(index);
	  return element.getDescription();
	}					
      };
      posList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
      posList.setBackground(java.awt.SystemColor.control);
      posList.setModel(getPosModel());
      posList.addMouseListener(new java.awt.event.MouseAdapter() {
	public void mouseClicked(java.awt.event.MouseEvent e) {
	  annotateCanvas.setTag(posList.getSelectedValue().toString());
	}
      });
    }
    return posList;
  }
  
  /**
   * This method initializes annotateCanvas	
   * 	
   * @return dga.AnnotateCanvas	
   */
  private AnnotateCanvas getAnnotateCanvas() {
    if (annotateCanvas == null) {
      annotateCanvas = new AnnotateCanvas();
      annotateCanvas.setAnnotator(this);
      annotateCanvas.setPreferredSize(new java.awt.Dimension(400,200));
      annotateCanvas.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,0,0,0));
    }
    return annotateCanvas;
  }
  
  /**
   * This method creates depsModel, from property file, according to a given corpus.	
   * 	
   * @return dga.ArrayListModel
   */
  private ArrayListModel<Tag> getDepsModel() {
    depsModel = new ArrayListModel<Tag>();
    setDepsModel(scheme);
    return depsModel;
  }

  /**
   * Update dependency tags according to new @param scheme.
   * @return
   */
  private ArrayListModel<Tag> setDepsModel(String scheme) {
    depsModel.clear();
    // FIXME: XMLResourceBundleControl is needed here in the first call, while is not needed in setPosMoel
    // which gets called later. Why?
    ResourceBundle bundle = ResourceBundle.getBundle("dga.DGA_" + scheme, new XMLResourceBundleControl());
    if (bundle == null)
      bundle = ResourceBundle.getBundle("dga.DGA", new XMLResourceBundleControl());
    String deprels = bundle.getString("deprels");
    String[] deps = deprels.split("\n");
    for (String dep : deps) {
      String[] pair = dep.split("\\|");
      String descr = pair.length > 1 ? pair[1] : "";
      depsModel.add(new Tag(pair[0], descr, ""));
    }
    return depsModel;
  }
  
  /**
   * This method initializes posModel	
   * 	
   * @return dga.ArrayListModel
   */
  private ArrayListModel<Tag> getPosModel() {
    posModel = new ArrayListModel<Tag>();
    setPosModel(scheme);
    return posModel;
  }

  /**
   * Update POS tags according to new @param scheme.
   * @return
   */
  private ArrayListModel<Tag> setPosModel(String scheme) {
    posModel.clear();
    ResourceBundle props = ResourceBundle.getBundle("dga.DGA_" + scheme);
    if (props == null)
      props = ResourceBundle.getBundle("dga.DGA");
    String deprels = props.getString("pos");
    if (deprels == null)
      deprels = ResourceBundle.getBundle("dga.DGA").getString("pos");
    String[] deps = deprels.split("\n");
    for (String dep : deps) {
      // FIXME: does not handle presence of ':' in string
      String[] pair = dep.split("\\|");
      if (pair[0] != "") {
	String descr = pair.length > 1 ? pair[1] : "";
	posModel.add(new Tag(pair[0], descr, ""));
      }
    }
    return posModel;
  }
  
  /**
   * This method initializes relTitle	
   * 	
   * @return javax.swing.JLabel	
   */
  private JLabel getRelTitle() {
    if (relTitle == null) {
      relTitle = new JLabel();
      relTitle.setText("Relations");
      relTitle.setForeground(java.awt.Color.white);
      relTitle.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
      relTitle.setBackground(java.awt.SystemColor.controlDkShadow);
      relTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    }
    return relTitle;
  }
    
  /**
   * This method initializes posTitle	
   * 	
   * @return javax.swing.JLabel	
   */
  private JLabel getPosTitle() {
    if (posTitle == null) {
      posTitle = new JLabel();
      posTitle.setText("POS");
      posTitle.setForeground(java.awt.Color.white);
      posTitle.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
      posTitle.setBackground(java.awt.SystemColor.controlDkShadow);
      posTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    }
    return posTitle;
  }
  
  /**
   * This method initializes depsPanel	
   * 	
   * @return javax.swing.JPanel
   */
  private JPanel getRelationsPanel() {
    if (depsPanel == null) {
      depsPanel = new JPanel();
      depsPanel.setLayout(new BorderLayout());
      depsPanel.setPreferredSize(new java.awt.Dimension(110,100));
      depsPanel.setBackground(java.awt.SystemColor.controlShadow);
      depsPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.SoftBevelBorder.RAISED));
      depsPanel.setName("depsPanel");
      depsPanel.add(getRelTitle(), java.awt.BorderLayout.NORTH);
      depsPanel.add(getDepsScroll(), java.awt.BorderLayout.CENTER);
    }
    return depsPanel;
  }
  
  /**
   * This method initializes depsPanel	
   * 	
   * @return javax.swing.JPanel
   */
  private JPanel getPosPanel() {
    if (posPanel == null) {
      posPanel = new JPanel();
      posPanel.setLayout(new BorderLayout());
      posPanel.setPreferredSize(new java.awt.Dimension(110,100));
      posPanel.setBackground(java.awt.SystemColor.controlShadow);
      posPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.SoftBevelBorder.RAISED));
      posPanel.setName("posPanel");
      posPanel.add(getPosTitle(), java.awt.BorderLayout.NORTH);
      posPanel.add(getPosScroll(), java.awt.BorderLayout.CENTER);
    }
    return posPanel;
  }
  
  /**
   * This method initializes scrollCanvas	
   * 	
   * @return javax.swing.JScrollPane	
   */
  private JScrollPane getScrollCanvas() {
    if (scrollCanvas == null) {
      scrollCanvas = new JScrollPane();
      scrollCanvas.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,0,0,0));
      scrollCanvas.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_NEVER);
      scrollCanvas.setViewportView(getAnnotateCanvas());
    }
    return scrollCanvas;
  }
  
  /**
   * This method initializes depsScroll	
   * 	
   * @return javax.swing.JScrollPane	
   */
  private JScrollPane getDepsScroll() {
    if (depsScroll == null) {
      depsScroll = new JScrollPane();
      depsScroll.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,0,0,0));
      depsScroll.setViewportView(getDepsList());
    }
    return depsScroll;
  }
  
  /**
   * This method initializes posList	
   * 	
   * @return javax.swing.JScrollPane	
   */
  private JScrollPane getPosScroll() {
    if (posScroll == null) {
      posScroll = new JScrollPane();
      posScroll.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,0,0,0));
      posScroll.setViewportView(getPosList());
    }
    return posScroll;
  }
  
  /**
   * This method initializes leftPanel	
   * 	
   * @return javax.swing.JPanel	
   */
  private JPanel getLeftPanel() {
    if (leftPanel == null) {
      leftPanel = new JPanel();
      leftPanel.setLayout(new CardLayout());
      leftPanel.add(getRelationsPanel(), getRelationsPanel().getName());
      leftPanel.add(getPosPanel(), getPosPanel().getName());
    }
    return leftPanel;
  }
  
  /**
   * Select which list to show in the left panel.
   * @param name
   */
  public void showLeft(String name) {
    if (name.equals("POS"))
      ((CardLayout)leftPanel.getLayout()).last(leftPanel);
    else
      ((CardLayout)leftPanel.getLayout()).first(leftPanel);
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    Annotator frame = new Annotator(null);
    frame.setVisible(true);
  }
  
  /**
   * This is the default constructor
   */
  public Annotator(DGA dga) {
    super();
    initialize();
    annotateCanvas.frame = dga;
  }
  
  /**
   * This method initializes this
   * 
   * @return void
   */
  private void initialize() {
    this.setSize(509, 250);
    setLayout(new BorderLayout());
    this.setPreferredSize(new java.awt.Dimension(600,250));
    this.add(getLeftPanel(), java.awt.BorderLayout.WEST);
    this.add(getScrollCanvas(), java.awt.BorderLayout.CENTER);
  }
  
  /**
   * Change the corpus annotation schemes
   * @param scheme
   */
  public void setScheme(String scheme) {
    if (!scheme.equals(this.scheme)) {
      // update depsModel
      setDepsModel(scheme);
      depsList.setModel(depsModel);
      // create posModel
      setPosModel(scheme);
      posList.setModel(posModel);
    }
  }
  
  /**
   * Annotate sentence.
   * @param sentenceModel
   */
  public void sentence(SentenceView sentenceView) {
    if (sentenceView == null) {
      annotateCanvas.clear();
      return;
    }
    // set the scheme from the sentence
    setScheme(sentenceView.scheme);
    annotateCanvas.annotate(sentenceView);
  }
  
  public void viewPOS(boolean coarse) {
    annotateCanvas.viewPOS(coarse);
  }
  
  /**
   * Show differences with sentences from other corpus.
   * @param sentences
   */
  public void compareWith(Sentence sentence) {
    annotateCanvas.compareWith(sentence);
  }
  
  public void EnablePanning() { annotateCanvas.EnablePanning(); }
  public void EnableZooming() { annotateCanvas.EnableZooming(); }
  
  public void undo() {
    annotateCanvas.undo();
  }
  
  public void redo() {
    annotateCanvas.redo();
  }
  
  public boolean saveImage(File file) {
    BufferedImage bufferedImage = annotateCanvas.getImage();
    try {
      ImageIO.write(bufferedImage, "png", file);
    } catch (Exception e) {
      return false;
    }
    return true;
  }
  
  private static final long serialVersionUID = 1L;
}  //  @jve:decl-index=0:visual-constraint="10,10"
