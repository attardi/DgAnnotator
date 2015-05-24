///
/// Copyright (c) 2006, Giuseppe Attardi (attardi@di.unipi.it).
///
package dga;

import java.awt.Cursor;
//import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;

/**
 * This class represents a pan interactor.
 * 
 * FIXJAVA: JDK does not handle horizontal wheel scroll.
 * @see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4419271
 *
 * @see http://svn.apache.org/viewcvs.cgi/xmlgraphics/batik/trunk/sources/org/apache/batik/swing/gvt/
 * @author <a href="mailto:attardi@di.unipi.it">Giuseppe Attardi</a>
 * @version $Id: AbstractPanInteractor.java,v 1.4 2008/12/26 21:49:33 attardi Exp $
 */
public abstract class AbstractPanInteractor implements //KeyListener,
MouseListener,
MouseMotionListener,
MouseWheelListener {
  
  enum Mode { pan, zoom };
  /**
   * Which navigation mode to use.
   */
  Mode mode = Mode.pan;
  public void setMode(Mode m) { this.mode = m; }
  
  static final double zoomFactor = 100.0;
  
  /**
   * The cursor for panning.
   */
  public final static Cursor PAN_CURSOR = new Cursor(Cursor.MOVE_CURSOR);
  public final static Cursor ZOOM_CURSOR = new Cursor(Cursor.HAND_CURSOR);
  
  /**
   * The mouse x start position.
   */
  protected int xStart;
  
  /**
   * The mouse y start position.
   */
  protected int yStart;
  
  /**
   * Cumulative x scroll distance.
   */
  protected int xScroll = 0;
  
  /**
   * Cumulative y scroll distance.
   */
  protected int yScroll = 0;
  
  /**
   * To store the previous cursor.
   */
  protected Cursor previousCursor;
  
  /**
   * To store the previous TransPanel transform.
   */
  protected AffineTransform previousTransform;
  
  // MouseListener ///////////////////////////////////////////////////////
  
  /**
   * Invoked when a mouse button has been pressed on a component.
   */
  public void mousePressed(MouseEvent e) {
    if (previousTransform == null) {
      xStart = e.getX();
      yStart = e.getY();
      TransPanel c = (TransPanel)e.getSource();
      previousTransform = c.getTransform();
      previousCursor = c.getCursor();
      c.setCursor(mode == Mode.pan ? PAN_CURSOR : ZOOM_CURSOR);
    }
  }
  
  /**
   * Invoked when a mouse button has been released on a component.
   */
  public void mouseReleased(MouseEvent e) {
    mouseExited(e);
  }
  
  /**
   * Invoked when the mouse enters a component.
   */
  public void mouseEntered(MouseEvent e) { }
  
  /**
   * Invoked when the mouse exits a component.
   */
  public void mouseExited(MouseEvent e) {
    previousTransform = null;
    TransPanel c = (TransPanel)e.getSource();
    c.setCursor(previousCursor);
  }
  // MouseMotionListener /////////////////////////////////////////////////
  
  /**
   * Invoked when a mouse button is pressed on a component and then 
   * dragged.  Mouse drag events will continue to be delivered to
   * the component where the first originated until the mouse button is
   * released (regardless of whether the mouse position is within the
   * bounds of the component).
   */
  public void mouseDragged(MouseEvent e) {
    if (previousTransform == null)	// if mouse exited window
      return;
    TransPanel c = (TransPanel)e.getSource();
    
    AffineTransform at;
    if (mode == Mode.pan)
      at = AffineTransform.getTranslateInstance(e.getX() - xStart,
	  e.getY() - yStart);
    else {
      double delta = (e.getY() - yStart) / zoomFactor;
      double scale = (delta > 0) ? 1.0 + delta : 1 / (1 - delta);
      // to maintain the mouse position fixed, we must traslate it back
      at = AffineTransform.getTranslateInstance(xStart - xStart * scale,
	  yStart - yStart * scale);
      at.concatenate(AffineTransform.getScaleInstance(scale, scale));
      at.concatenate(AffineTransform.getTranslateInstance(e.getX() - xStart, 0));
    }
    
    at.concatenate(previousTransform);
    if (at.getScaleX() > 0.0 && at.getScaleY() > 0) {
      // avoid reflection
      c.setTransform(at);
      //c.validate();
      c.repaint();
    }
  }
  /** Invoked when the mouse cursor has been moved onto a component
   * 	but no buttons have been pushed. */
  public void mouseMoved(MouseEvent e) { }
  
  // MouseWheelListener
  public void mouseWheelMoved(MouseWheelEvent e) {
    if (previousTransform == null) {
      mousePressed(e);
      xScroll = 0;
      yScroll = 0;
    }
    TransPanel c = (TransPanel)e.getSource();
    yScroll += e.getScrollAmount() * e.getWheelRotation();
    AffineTransform at;
    if (mode == Mode.pan)
      at = AffineTransform.getTranslateInstance(xScroll, yScroll);
    else {
      double delta = yScroll / zoomFactor;
      double scale = (delta > 0) ? 1.0 + delta : 1 / (1 - delta);
      // to maintain the mouse position fixed, we must traslate it back
      at = AffineTransform.getTranslateInstance(xStart - xStart * scale,
	  yStart - yStart * scale);
      at.concatenate(AffineTransform.getScaleInstance(scale, scale));
    }
    at.concatenate(previousTransform);
    if (at.getScaleX() > 0.0 && at.getScaleY() > 0) {
      // avoid reflection
      c.setTransform(at);
      c.validate();
      c.repaint();
    }
  }
}
