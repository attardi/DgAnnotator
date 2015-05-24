///
/// Copyright (c) 2006, Giuseppe Attardi (attardi@di.unipi.it).
///
package dga;

import java.awt.geom.*;
import javax.swing.undo.UndoManager;
import java.util.Locale;

/**
 * This class maintains the status of a Sentence being edited.
 * @author attardi
 *
 */
public class SentenceView {
  
  Sentence sentence;
  protected UndoManager undoManager;
  protected AffineTransform transform;
  protected Locale locale;
  /**
   * Determine whether to show POS or coarse POS.
   */
  public boolean viewPOS = true;
  
  public SentenceView(Sentence sentence, Locale locale) {
    this.sentence = sentence;
    this.locale = locale == null ? Locale.getDefault() : locale;
    undoManager = new UndoManager();
    transform = new AffineTransform();
    transform.setToIdentity();
  }
  
}
