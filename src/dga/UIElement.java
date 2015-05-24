///
/// Copyright (c) 2005, Giuseppe Attardi (attardi@di.unipi.it).
///
package dga;

import javax.swing.JComponent;

/**
 * Interface that describes a UI data element, consisting of a name (title), an
 * optional description (usually used as a tooltip) and a visual component.
 * This class allows an object to be 
 * associated with a corresponding visual that works with UIElement implementations.
 * @author Phil Herold
 */
public interface UIElement {
  /**
   * Gets the item for the UI element; the toString() method on the item is normally
   * used to show the name of the item in the UI
   * @return Object	 
   */
  Object getItem();
  
  /**
   * Gets a description of the UI element; this method may return null
   * @return String
   */
  String getDescription();
  
  /**
   * Gets the visual component associated with the UI element; this method must <em>not</em> return
   * null
   * @return JComponent
   */
  JComponent getComponent();
  
}
