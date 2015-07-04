///
/// Copyright (c) 2006, Giuseppe Attardi (attardi@di.unipi.it).
///
package dga;

import java.awt.geom.*;

import javax.swing.undo.UndoManager;

/**
 * This class maintains the status of a Sentence being edited.
 * @author attardi
 *
 */
public class SentenceView {
  
  Sentence sentence;
  protected UndoManager undoManager;
  protected AffineTransform transform;
  /**
   * The corpus scheme
   */
  protected String scheme;
  /**
   * Determine whether to show POS or coarse POS.
   */
  public boolean viewPOS = true;
  
  public SentenceView(Sentence sentence, String scheme) {
    this.sentence = sentence;
    this.scheme = scheme;
    undoManager = new UndoManager();
    transform = new AffineTransform();
    //transform.setToIdentity();
  }
  
}
