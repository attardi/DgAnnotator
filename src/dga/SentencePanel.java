///
/// Copyright (c) 2005, Giuseppe Attardi (attardi@di.unipi.it).
///
package dga;

import java.io.*;

public class SentencePanel extends AnnotateCanvas {
  
  public SentencePanel(String sent) {
    try {
      Corpus corpusXML = new Corpus(new ByteArrayInputStream(sent.getBytes()));
      annotate(new SentenceView(corpusXML.getSentence(1), null));
    } catch (Exception e) {}
  }
  
  private static final long serialVersionUID = 1L;
}
