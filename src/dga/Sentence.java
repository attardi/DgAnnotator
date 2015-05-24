///
/// Copyright (c) 2005, Giuseppe Attardi (attardi@di.unipi.it).
///
package dga;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Locale;
import org.w3c.dom.Node;

/**
 * Represents the dependency tree of a sentence.
 * @author attardi
 *
 */
class SentenceTree {
  SentenceTree() {}
  
  SentenceTree(Vector<Integer> parentV, Vector<String> depV) {
    int n = parentV.size();
    parents = new int[n];
    deps = new String[n];
    for (int i = 0; i < n; i++) {
      parents[i] = parentV.get(i);
      deps[i] = depV.get(i);
    }
  }
  /**
   * offsets of each term to its head word: 0 means no link, Integer.MAX_VALUE
   * means root link
   */
  public int parents[];
  
  public String[] deps;	///< corresponding dependency type
}

/**
 * Representation of dependency parse tree for a sentence.
 * @author attardi
 *
 */
public class Sentence extends SentenceTree {
  
  public String id;		///< sentence identifier
  public String[] forms;	///< sentence terms, including punctuations
  //public HashMap<String, String[]> attributes; ///< 
  public String[] lemmas;		///< corresponding lemmas
  public String[] tags;			///< corresponding POS tags
  public String[] coarseTags;	///< corresponding coarse grain POS tags
  public String[] morphos;		///< corresponding morphological features
  public String[] pheads;		///< corresponding projective heads
  public String[] pdeprels;		///< corresponding projective deprel
  public Vector<Node> extras;	///< extra XML elements
  public Node context = null;	///< the context, e.g. containing document
  
  static final String defaultLanguage = "en";
  static final String defaultTagger = "http://medialab.di.unipi.it/cgi-bin/tagger?";
  static final String defaultEncoding = "UTF-8";
  
  Sentence(String s, Locale locale) {
    ResourceBundle props = ResourceBundle.getBundle("dga.DGA", locale);
    if (props == null)
      props = ResourceBundle.getBundle("dga.DGA");
    String language = props.getString("language");
    if (language == null) language = defaultLanguage;
    String tagger = props.getString("tagger");
    if (tagger == null) tagger = defaultTagger;
    String encoding = defaultEncoding;
    try {
      String enc = props.getString("encoding");
      if (enc != null) encoding = enc;
    } catch (MissingResourceException e) { }    
    Vector<String> tagged = PosTags(s, language, tagger, encoding);
    int n = tagged.size();
    forms = new String[n];
    lemmas = new String[n];
    tags = new String[n];
    morphos = new String[n];
    parents = new int[n];
    deps = new String[n];
    for (int i = 0; i < n; i++) {
      String token = tagged.elementAt(i);
      String[] parts = token.split("\t");
      forms[i] = parts[0];
      lemmas[i] = parts.length > 2 ? parts[2] : "";
      tags[i] = parts.length > 1 ? parts[1] : "_";
      morphos[i] = "";
      parents[i] = 0;
      deps[i] = "";
    }
  }
  
  Sentence(String sid, Vector<String> wordV, Vector<String> lemmaV,
      Vector<String> ctagV, Vector<String> tagV, Vector<String> featsV,
      Vector<Integer> parentV, Vector<String> depV,
      Vector<String> pheadV, Vector<String> pdeprelV,
      Vector<Node> extras, Node context) {
    super(parentV, depV);
    this.id = sid;
    this.extras = extras;
    this.context = context;
    int n = wordV.size();
    forms = new String[n];
    lemmas = new String[n];
    coarseTags = new String[n];
    tags = new String[n];
    morphos = new String[n];
    pheads = new String[n];
    pdeprels = new String[n];
    for (int i = 0; i < n; i++) {
      forms[i] = wordV.get(i);
      lemmas[i] = lemmaV.get(i);
      coarseTags[i] = ctagV.get(i);
      tags[i] = tagV.get(i);
      morphos[i] = featsV.get(i);
      parents[i] = parentV.get(i);
      deps[i] = depV.get(i);
      pheads[i] = (pheadV == null) ? "" : pheadV.get(i);
      pdeprels[i] = (pdeprelV == null) ? "" : pdeprelV.get(i);
    }
  }
  
  public int length() {
    return (forms == null) ? 0 : forms.length;
  }
  
  public String toString() {
    String indent0 = "\n";
    String indent1 = "\n  ";
    //String indent2 = "\n    ";
    StringBuffer s = new StringBuffer("<s");
    if (id != null)
      s.append(" id=\"" + id + "\"");
    s.append(">");
    for (int i = 0; i < forms.length; i++) {
      s.append(indent0 + "<tok id=\"" + (i + 1) + "\">");
      if (forms[i] != null)
	s.append(indent1 + "<orth>" + HtmlEncoder.encode(forms[i]) + "</orth>");
      if (lemmas[i] != null && lemmas[i].length() > 0)
	s.append(indent1 + "<lemma>" + HtmlEncoder.encode(lemmas[i]) + "</lemma>");
      if (coarseTags[i] != null && coarseTags[i].length() > 0)
	s.append(indent1 + "<cpos>" + HtmlEncoder.encode(coarseTags[i]) + "</cpos>");
      if (tags[i] != null && tags[i].length() > 0)
	s.append(indent1 + "<pos>" + HtmlEncoder.encode(tags[i]) + "</pos>");
      if (morphos[i] != null && !morphos[i].isEmpty())
	s.append(indent1 + "<morph>" + HtmlEncoder.encode(morphos[i]) + "</morph>");
      if (parents[i] != 0) {
	int head = parents[i] == Integer.MAX_VALUE ? 0 : parents[i] + i + 1;
	/* Old version
	 s.append(indent1 + "<syn head=\"" + head + "\">");
	 if (deps[i] != null)
	 s.append(indent2 + "<reltype>" + HtmlEncoder.encode(deps[i]) + "</reltype>");
	 s.append(indent1 + "</syn>");
	 */
	s.append(indent1 + "<dep head=\"" + head + '"');
	if (deps[i] != null)
	  s.append(" type=\"" + HtmlEncoder.encode(deps[i]) + '"');
	s.append(" />");
      }
      if (extras != null) {
	Node node = extras.get(i);
	if (node != null) {
	  s.append(XmlPrinter.NodeToString(node, indent1));
	}
      }
      s.append(indent0 + "</tok>");
    }
    s.append(indent0 + "</s>");
    return s.toString();
  }
  
  /**
   * Serialize to CoNLL (or CoNLL-U) tab separated format
   *	id	form	lemma	cpostag	postag	feats	head	phead	pdeprel	
   * @return a line terminated string.
   */
  public String toTab() {
    StringBuffer s = new StringBuffer();
    if (context != null)
      s.append(context.getAttributes().getNamedItem("meta").getTextContent());
    int mws = 0, mwe = 0;
    String mwv = "";
    int mwi = 0;
    for (int i = 0; i < forms.length; i++) {
      // check for multiword
      if (mws < i+1 && mwi < extras.size()) {
	  Node node = extras.get(mwi);
	  mws = Integer.parseInt(node.getAttributes().getNamedItem("start").getNodeValue());
	  mwe = Integer.parseInt(node.getAttributes().getNamedItem("end").getNodeValue());
	  mwv = node.getAttributes().getNamedItem("value").getNodeValue();
	  mwi += 1;
      }
      if (mws == i+1)	// numbering from 1
	s.append(String.format("%d-%d\t%s\n", mws, mwe, mwv));
      s.append((i + 1) + "\t");
      s.append(forms[i] == null || forms[i].isEmpty() ? "_" : forms[i]); s.append("\t");
      s.append(lemmas[i] == null || lemmas[i].isEmpty() ? "_" : lemmas[i]); s.append("\t");
      s.append(coarseTags[i] == null || coarseTags[i].isEmpty() ? "_" : coarseTags[i]); s.append("\t");
      s.append(tags[i] == null || tags[i].isEmpty() ? "_" : tags[i]); s.append("\t");
      s.append(morphos[i] == null || morphos[i].isEmpty() ? "_" : morphos[i]); s.append("\t");
      int head = (parents[i] == Integer.MAX_VALUE) ? 0 : parents[i] + i + 1;
      s.append(head); s.append("\t");
      s.append(deps[i] == null || deps[i].isEmpty() ? "_" : deps[i]); s.append("\t");
      s.append(pheads[i] == null || pheads[i].isEmpty() ? "_" : pheads[i]); s.append("\t");
      s.append(pdeprels[i] == null || pdeprels[i].isEmpty() ? "_" : pdeprels[i]); s.append("\n");
    }
    s.append("\n");
    return s.toString();
  }
  
  private boolean changed = false;
  public void changed(boolean v) { changed = v; }
  public boolean changed() { return changed; }
  
  /**
   * Invoke POS tagger service to tag sentence
   * 
   * @param s
   *            the sentence to be tagged.
   * @param lang
   *            the language of the sentence.
   * @return a vector of strings, one per word in the sentence, in the format:
   *         Word\tPOS\tLemma. Returns an empty vector is tagging failed.
   */
  Vector<String> PosTags(String s, String lang, String tagger, String encoding) {
    Vector<String> res = new Vector<String>();
    try {
      // Construct params
      String params = "p=" + HtmlEncoder.encode(s) +
      "&lang=" + HtmlEncoder.encode(lang);
      
      // Send request
      URL url = new URL(tagger + params);
      URLConnection conn = url.openConnection();
      // ((HttpURLConnection)conn).setRequestProperty("Content-Type",
      // "application/x-www-form-urlencoded; charset=ISO-8859-1");
      
      // Get the response
      BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      String line;
      while ((line = rd.readLine()) != null) {
	res.add(line);
      }
      rd.close();
    } catch (Exception e) {
      // failed connection, return just tokenized sentence
      for (String token : s.split(" "))
	res.add(token);
    }
    return res;
  }
}
