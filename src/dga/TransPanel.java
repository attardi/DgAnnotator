package dga;

import java.awt.geom.*;
import javax.swing.*;

class TransPanel extends JPanel {
  
  AffineTransform at = new AffineTransform();
  
  TransPanel () {
    at.setToIdentity();
  }
  
  /** Gets the AffineTransform for drawing in the panel. */
  public AffineTransform getTransform() {
    return at;
  }
  /** Sets the AffineTransform for drawing in the panel. */
  public void setTransform(AffineTransform at) {
    this.at = at;
  }
  
  private static final long serialVersionUID = 1L;
}
