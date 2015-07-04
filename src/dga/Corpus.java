///
/// Copyright (c) 2005, Giuseppe Attardi (attardi@di.unipi.it).
///
package dga;

import java.io.*;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.regex.*;
import java.text.ParseException;

import javax.xml.parsers.*;
import javax.xml.validation.*;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.w3c.dom.*;

/**
 * A collection of sentences with a common language and annotation scheme.
 * 
 * @author Attardi
 *
 */
public class Corpus {

  DocumentBuilderFactory docFactory;
  static final String    schemaFile = "/dga.xsd";

  public Corpus() {
    sentences = new Vector<Sentence>();
  }

  public Corpus(InputStream is) throws Exception {
    if (docFactory == null) {
      docFactory = DocumentBuilderFactory.newInstance();
      // create a SchemaFactory capable of understanding WXS schemas
      SchemaFactory factory = SchemaFactory
	  .newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
      // load a WXS schema, represented by a Schema instance
      // Schema schema = factory.newSchema(new File(schemaFile));
      java.net.URL url = Corpus.class.getResource(schemaFile);
      Schema schema = factory.newSchema(url);
      docFactory.setSchema(schema);
    }
    DocumentBuilder parser = docFactory.newDocumentBuilder();
    getSentences(parser.parse(is));
  }

  public Corpus(File file) throws Exception {
    docFile = file;
    sentences = open(file);
  }

  /**
   * Language code for the corpus
   */
  protected String language = Locale.getDefault().getLanguage();

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String l) {
    this.language = l;
  }

  /**
   * Annotation scheme.
   */
  public String scheme = "UD";
  
  /**
   * Read corpus file.
   * 
   * @param file
   * @throws Exception
   */
  public Vector<Sentence> open(File file) throws Exception {
    if (file.getPath().endsWith(".xml")) {
      if (docFactory == null) {
	docFactory = DocumentBuilderFactory.newInstance();
	// create a SchemaFactory capable of understanding WXS schemas
	SchemaFactory factory = SchemaFactory
	    .newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
	// load a WXS schema, represented by a Schema instance
	// Schema schema = factory.newSchema(new File(schemaFile));
	java.net.URL url = Corpus.class.getResource(schemaFile);
	Schema schema = factory.newSchema(url);
	docFactory.setSchema(schema);
      }
      DocumentBuilder parser = docFactory.newDocumentBuilder();
      // DocumentBuilder interns XML names, so we can use == for string comparisons
      Document dom = parser.parse(file);
      return getSentences(dom);
    } else if (file.getPath().endsWith(".txt"))
      return parseText(file);
    return parseUD(file);	// subsumes parseConll
  }
  
  /**
   * Read corpus file in text format and submit it for parsing to the Web Service associated
   * to the currently selected Corpus type.
   * 
   * @param file
   * @return parsed sentences from corpus
   * @throws Exception
   */
  public Vector<Sentence> parseText(File file) throws Exception {
    FileInputStream fis = new FileInputStream(file);
    BufferedReader in = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
    String line;
    StringBuilder text = new StringBuilder();
    while ((line = in.readLine()) != null)
      text.append(line);
    in.close();
    return parseText(text.toString());
  }

  /**
   * Invoke parsing service to parse a text.
   * 
   * @param text the text to be parsed.
   * @return a vector of Sentence.
   */
  Vector<Sentence> parseText(String text) throws Exception {
    ResourceBundle props;
    try {
      props = ResourceBundle.getBundle("dga.DGA_" + scheme);
    } catch (Exception e) {
      props = ResourceBundle.getBundle("dga.DGA");
    }
    String tagger = props.getString("tagger");
    String authProvider = "DgAnnotator";
    String authToken = ""; //props.getString("token");
    String email = "";
    Service service = new Service(tagger, authProvider, authToken, email);
    String[] params = {"service", "parser",
	"format", "plain",
	"text", text};
    // submit request
    CloseableHttpResponse response = service.post(params);
    if (response.getStatusLine().getStatusCode() == 200) {
      // read response
      HttpEntity entity = response.getEntity();
      InputStream is = entity.getContent();
      Vector<Sentence> sentences = parseConll(is);
      response.close();
      return sentences;
    }
    return null;
  }

  // pattern for analyzing token line in CoNLL-X format:
  // id form lemma cpostag postag feats head deprel phead pdeprel
  static Pattern reCoNLL_X   = Pattern.compile("(\\d+?)\t(.+?)\t(.+?)\t(.+?)\t(.+?)\t(.+?)\t(\\d+?)\t(.+?)\t(.+?)\t(.*)");
  // id form lemma gpos ppos splitForm splitLemma pposs head deprel pred arg+
  static Pattern reCoNLL_XII = Pattern.compile("(\\d+?)\t(.+?)\t(.+?)\t(.+?)\t(.+?)\t(.+?)\t(.+?)\t(.+?)\t(\\d+?)\t(.+?)\t(.+?)\t(.*)");
  // Optional doc element
  static Pattern reDoc       = Pattern.compile("</?doc((\\s+?)id=(['\"])(.*?)\\3(\\s+?)url=(['\"])(.*?)\\6)?(\\s*?)>");

  /**
   * Read corpus file in CoNLL tab separated format: one token per line,
   * sentence terminated by blank line. Optionally sentences are grouped into
   * documents, wrapped in element <doc id="" url=""> ... </doc>
   * 
   * @param file
   * @return parsed sentences from corpus
   * @throws Exception
   */
  public Vector<Sentence> parseConll(File file) throws Exception {
    FileInputStream fis = new FileInputStream(file);
    return parseConll(fis);
  }
  
  public Vector<Sentence> parseConll(InputStream is) throws Exception {
    BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
    Vector<Sentence> sentences = new Vector<Sentence>();
    Vector<String> words = new Vector<String>();
    Vector<String> lemmas = new Vector<String>();
    Vector<String> ctags = new Vector<String>();
    Vector<String> tags = new Vector<String>();
    Vector<String> morphos = new Vector<String>();
    Vector<Integer> parents = new Vector<Integer>();
    Vector<String> deps = new Vector<String>();
    Vector<String> pheads = new Vector<String>();
    Vector<String> pdeprels = new Vector<String>();
    Vector<Node> extras = null;
    Element doc = null; // /< the current document
    docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = docFactory.newDocumentBuilder();
    Document document = builder.newDocument();
    String line;
    int count = 0;
    while ((line = in.readLine()) != null) {
      Matcher d = reDoc.matcher(line);
      if (d.matches()) {
	if (line.charAt(1) == '/') doc = null;
	else {
	  doc = document.createElement("doc");
	  doc.setAttribute("id", d.group(4));
	  doc.setAttribute("url", d.group(7));
	}
	continue;
      }
      if (line.isEmpty()) {
	String sid = Integer.toString(count++);
	sentences.add(new Sentence(sid, words, lemmas, ctags, tags, morphos,
	    parents, deps, pheads, pdeprels, extras, doc));
	words.clear();
	lemmas.clear();
	ctags.clear();
	tags.clear();
	morphos.clear();
	parents.clear();
	deps.clear();
	pheads.clear();
	pdeprels.clear();
	continue;
      }
      Matcher m = reCoNLL_X.matcher(line);
      if (!m.matches()) continue;
      int id = Integer.parseInt(m.group(1));
      if (id != words.size() + 1) continue;
      // add orth
      words.add(m.group(2));
      // add lemma
      lemmas.add(m.group(3));
      // add cpos
      ctags.add(m.group(4));
      // add pos
      tags.add(m.group(5));
      // add feats
      morphos.add(m.group(6));
      // add dependency
      int head = Integer.parseInt(m.group(7));
      if (head == 0) head = Integer.MAX_VALUE;
      else head -= id;
      parents.add(head);
      deps.add(m.group(8));
      // add pheads
      pheads.add(m.group(9));
      // add pdeprels
      pdeprels.add(m.group(10));
    }
    if (words.size() > 0) // leftover
    sentences.add(new Sentence(Integer.toString(count), words, lemmas, ctags,
	tags, morphos, parents, deps, pheads, pdeprels, extras, doc));
    in.close();
    return sentences;
  }

  // id form lemma cpostag postag feats head deprel deps misc
  static Pattern reCoNLL_U    = Pattern.compile("(\\d+?)\t(.+?)\t(.+?)\t(.+?)\t(.+?)\t(.+?)\t(\\d+?)\t(.+?)\t(.+?)\t(.*)");
  static Pattern reCoNLL_U_MW = Pattern.compile("(\\d+?)-(\\d+?)\t(.*)");

  /**
   * Read corpus file in CoNLL-U (Universal Dependency) format:
   * 
   * @see http://universaldependencies.github.io/docs/format.html sentence
   *      terminated by blank line. Optionally sentences are preceded by comment
   *      lines starting with # # sent_id 1
   * 
   * @param file
   * @return parsed sentences from corpus
   * @throws Exception
   */
  public Vector<Sentence> parseUD(File file) throws Exception {
    FileInputStream fis = new FileInputStream(file);
    return parseUD(fis);
  }

  public Vector<Sentence> parseUD(InputStream is) throws Exception {
    BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
    Vector<Sentence> sentences = new Vector<Sentence>();
    Vector<String> words = new Vector<String>();
    Vector<String> lemmas = new Vector<String>();
    Vector<String> ctags = new Vector<String>();
    Vector<String> tags = new Vector<String>();
    Vector<String> morphos = new Vector<String>();
    Vector<Integer> parents = new Vector<Integer>();
    Vector<String> deprels = new Vector<String>();
    Vector<String> depss = new Vector<String>();
    Vector<String> miscs = new Vector<String>();
    Vector<Node> multiwords = new Vector<Node>();
    docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = docFactory.newDocumentBuilder();
    Document document = builder.newDocument();
    Element sent = document.createElement("sent"); ///< the current sentence
    String line;
    int count = 0;	// sentences
    int ln = 0;		// line number
    while ((line = in.readLine()) != null) {
      ln++;
      if (line.isEmpty()) {
	String sid = Integer.toString(count++);
	sentences.add(new Sentence(sid, words, lemmas, ctags, tags, morphos,
	    parents, deprels, depss, miscs, multiwords, sent));
	words.clear();
	lemmas.clear();
	ctags.clear();
	tags.clear();
	morphos.clear();
	parents.clear();
	deprels.clear();
	depss.clear();
	miscs.clear();
	multiwords = new Vector<Node>();
	sent = document.createElement("sent");
	continue;
      }
      if (line.charAt(0) == '#') {
	sent.setAttribute("meta", sent.getAttribute("meta") + line + "\n");
	continue;
      }
      Matcher m = reCoNLL_U_MW.matcher(line);
      if (m.matches()) {
	// build multiword
	Element mw = document.createElement("MultiWord");
	mw.setAttribute("start", m.group(1));
	mw.setAttribute("end", m.group(2));
	mw.setAttribute("value", m.group(3));
	multiwords.add(mw);
	continue;
      }
      m = reCoNLL_U.matcher(line);
      if (!m.matches())
	throw new ParseException("malformed input file at line: " + ln, ln);
      int id = Integer.parseInt(m.group(1));
      if (id != words.size() + 1) continue;
      // add orth
      words.add(m.group(2));
      // add lemma
      lemmas.add(m.group(3));
      // add cpos
      ctags.add(m.group(4));
      // add pos
      tags.add(m.group(5));
      // add feats
      morphos.add(m.group(6));
      // add dependency
      int head = Integer.parseInt(m.group(7));
      if (head == 0) head = Integer.MAX_VALUE;
      else head -= id;
      parents.add(head);
      deprels.add(m.group(8));
      // add depss
      depss.add(m.group(9));
      // add miscs
      miscs.add(m.group(10));
    }
    if (words.size() > 0) // leftover
      sentences.add(new Sentence(Integer.toString(count), words, lemmas, ctags,
	  tags, morphos, parents, deprels, depss, miscs, multiwords, sent));
    in.close();
    return sentences;
  }

  static final String evenLineColor = "#DCDCDC";
  static final String oddLineColor  = "#F8F8F8";

  /**
   * Generate HTML with links for each sentence to corresponding corpus item.
   * 
   * @return
   */
  public String toHTML() {
    StringBuffer s = new StringBuffer("<HTML>\n<HEAD>\n");
    if (docFile != null) s.append("<TITLE>\n" + docFile.getName()
	+ "\n</TITLE>\n");
    s.append("<style TYPE='text/css'> A {text-decoration: none; color: #000000 } </STYLE>");
    s.append("</HEAD>\n<BODY>\n");
    int i = 0;
    for (Sentence sent : sentences) {
      s.append("<DIV bgcolor=" + (i % 2 == 0 ? evenLineColor : oddLineColor)
	  + "><A href=\"" + i++ + "\">");
      for (String word : sent.forms)
	s.append(word + " ");
      s.append("</A></DIV>\n");
    }
    s.append("</BODY>\n</HTML>\n");
    return s.toString();
  }

  /**
   * Get the i-th sentence from corpus.
   * 
   * @param id
   * @param relTypes
   */
  public Sentence getSentence(int id) {
    return (id < sentences.size()) ? sentences.get(id) : null;
  }

  public void add(Sentence sentence) {
    sentences.add(sentence);
  }

  /**
   * Turn the DOM into a corpus.
   * 
   * @param dom
   * @return
   */
  Vector<Sentence> getSentences(Document dom) {
    Vector<Sentence> sentences = new Vector<Sentence>();
    Element element = dom.getDocumentElement();
    Node lang = element.getAttributes().getNamedItem("language");
    if (lang != null) language = lang.getNodeValue();
    for (Node node = element.getFirstChild(); node != null; node = node.getNextSibling()) {
      if (node.getNodeName() != "s") continue;
      String sid = null;
      Node idn = node.getAttributes().getNamedItem("id");
      if (idn != null) sid = idn.getNodeValue();
      Vector<String> words = new Vector<String>();
      Vector<String> lemmas = new Vector<String>();
      Vector<String> cposTags = new Vector<String>();
      Vector<String> posTags = new Vector<String>();
      Vector<String> morphos = new Vector<String>();
      Vector<Integer> depIndex = new Vector<Integer>();
      Vector<String> relTypes = new Vector<String>();
      Vector<Node> extras = new Vector<Node>();
      Node context = null;
      int tok = -1;
      for (Node token = node.getFirstChild(); token != null; token = token.getNextSibling()) {
	if (token.getNodeName() == "tok") {
	  tok++;
	  words.add("");
	  lemmas.add("");
	  cposTags.add("");
	  posTags.add("");
	  morphos.add("");
	  depIndex.add(0);
	  relTypes.add("");
	  extras.add(null);
	  String gender = "";
	  String number = "";
	  String person = "";
	  String feats = "";
	  for (Node child = token.getFirstChild(); child != null; child = child
	      .getNextSibling()) {
	    String childName = child.getNodeName();
	    if (childName == "orth") {
	      words.set(tok, child.getFirstChild().getNodeValue());
	    } else if (childName == "lemma") {
	      lemmas.set(tok, child.getFirstChild().getNodeValue());
	    } else if (childName == "cpos") {
	      String cposTag = child.getFirstChild().getNodeValue();
	      cposTags.set(tok, cposTag);
	    } else if (childName == "pos") {
	      String posTag = child.getFirstChild().getNodeValue();
	      posTags.set(tok, posTag);
	    } else if (childName == "gender") {
	      gender = child.getFirstChild().getNodeValue();
	    } else if (childName == "number") {
	      number = child.getFirstChild().getNodeValue();
	    } else if (childName == "person") {
	      person = child.getFirstChild().getNodeValue();
	    } else if (childName == "feats") { // back compatibility
	      feats = child.getFirstChild().getNodeValue();
	    } else if (childName == "morph") {
	      feats = child.getFirstChild().getNodeValue();
	    } else if (childName == "dep") {
	      Node head = child.getAttributes().getNamedItem("head");
	      if (head != null) {
		int index = 0;
		try {
		  index = Integer.parseInt(head.getNodeValue());
		} catch (Exception exception) {
		}
		// Integer.MAX_VALUE means root link
		depIndex.set(tok, (index == 0) ? Integer.MAX_VALUE : index
		    - tok - 1);
	      }
	      Node type = child.getAttributes().getNamedItem("type");
	      if (type != null) {
		relTypes.set(tok, type.getNodeValue());
	      }
	    } else if (childName == "syn") {
	      // backward compatibility
	      Node head = child.getAttributes().getNamedItem("head");
	      if (head != null) {
		int index = 0;
		try {
		  index = Integer.parseInt(head.getNodeValue());
		} catch (Exception exception) {
		}
		// Integer.MAX_VALUE means root link
		depIndex.set(tok, (index == 0) ? Integer.MAX_VALUE : index
		    - tok - 1);
	      }
	      for (Node attr = child.getFirstChild(); attr != null; attr = attr
		  .getNextSibling()) {
		if (attr.getNodeName() == "reltype") {
		  String reltype = attr.getFirstChild().getNodeValue();
		  relTypes.set(tok, reltype);
		}
	      }
	    } else if (childName == "extra") {
	      // save additional elements
	      extras.set(tok, child.cloneNode(true));
	    }
	  }
	  morphos.set(tok, (feats == "" ? gender + number + person : feats));
	}
      }
      sentences.add(new Sentence(sid, words, lemmas, cposTags, posTags,
	  morphos, depIndex, relTypes, null, null, extras, context));
    }
    return sentences;
  }

  /**
   * Updates the i-th sentence
   * 
   * @param sentence
   * @param i
   * @param sentence
   * @return true if DOM was changed
   */
  boolean setSentence(int i, Sentence sentence) {
    if (i < sentences.size()) {
      sentences.set(i, sentence);
      return true;
    }
    return false;
  }

  boolean save() {
    return (docFile != null) && save(docFile);
  }

  boolean save(File file) {
    try {
      FileOutputStream fw = new FileOutputStream(file);
      BufferedWriter bw = new BufferedWriter(
	  new OutputStreamWriter(fw, "UTF-8"));
      if (file.getName().endsWith("xml")) {
	bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	bw.write("<DGA");
	if (language != "") bw.write(" language=\"" + language + "\"");
	bw.write(">\n");
	for (Sentence sent : sentences) {
	  bw.write(sent.toString());
	  bw.write("\n");
	}
	bw.write("</DGA>\n");
      } else {
	boolean conllu = file.getName().endsWith("conllu");
	Node doc = null;
	boolean first = true;
	for (Sentence sent : sentences) {
	  if (doc != sent.context) {
	    doc = sent.context;
	    if (doc != null && !conllu) {
	      if (first) first = false;
	      else bw.write("</doc>\n");
	      NamedNodeMap attrs = doc.getAttributes();
	      bw.write("<doc " + attrs.getNamedItem("id") + " "
		  + attrs.getNamedItem("url") + ">\n");
	    }
	  }
	  bw.write(sent.toTab());
	}
	if (doc != null && !conllu) bw.write("</doc>\n");
      }
      bw.close();
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  /**
   * 
   * @param file
   * @return true if save was successful.
   */
  public boolean appendTo(File file) {
    try {
      BufferedWriter bw;
      if (file.getName().endsWith("xml")) {
	if (file.exists()) {
	  // copy old file to tempFile up to ending tag, in case of XML.
	  File tempFile = File.createTempFile("DGA", "tmp");
	  FileWriter fw = new FileWriter(tempFile);
	  FileReader fileReader = new FileReader(file);
	  BufferedReader br = new BufferedReader(fileReader);
	  while (true) {
	    String line = br.readLine();
	    if (line == null) break;
	    int len = line.indexOf("</DGA>");
	    if (len < 0) fw.write(line + "\n");
	    else {
	      fw.write(line, 0, len);
	      break;
	    }
	  }
	  br.close();
	  fw.close();
	  file.delete();
	  tempFile.renameTo(file);
	  FileOutputStream fos = new FileOutputStream(file, true);
	  bw = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
	} else {
	  FileOutputStream fos = new FileOutputStream(file);
	  bw = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
	  bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	  bw.write("<DGA");
	  if (language != "") bw.write(" language=\"" + language + "\"");
	  bw.write(">\n");
	}
	for (Sentence sent : sentences)
	  bw.write(sent.toString());
	bw.write("</DGA>\n");
	bw.close();
	return true;
      } else {
	// write to TAB format
	if (file.exists()) {
	  File tempFile = File.createTempFile("DGA", "tmp");
	  FileOutputStream fos = new FileOutputStream(tempFile);
	  bw = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
	  FileInputStream fis = new FileInputStream(file);
	  BufferedReader br = new BufferedReader(new InputStreamReader(fis,
	      "UTF-8"));
	  // copy old file.
	  while (true) {
	    String line = br.readLine();
	    if (line == null) break;
	    bw.write(line + "\n");
	  }
	  br.close();
	  bw.close();
	  file.delete();
	  tempFile.renameTo(file);
	}
	FileOutputStream fos = new FileOutputStream(file, true);
	bw = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
	for (Sentence sent : sentences)
	  bw.write(sent.toTab());
	bw.close();
	return true;
      }
    } catch (Exception e) {
    }
    return false;
  }

  Vector<Sentence> sentences;

  File	     docFile;
}