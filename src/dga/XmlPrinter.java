package dga;

import java.io.*;
import org.w3c.dom.*;

public class XmlPrinter {
  private PrintWriter out;
  
  public XmlPrinter(PrintWriter out) {
    this.out = out;
  }
  
  public void printTree(Node node, String indent) {
    switch (node.getNodeType()) {
    case Node.DOCUMENT_NODE:
      // print the contents of the Document node
      out.print("<?xml version=\"1.0\"?>");
      NodeList nodes = node.getChildNodes();
      if (nodes != null) {
	for (int i = 0; i < nodes.getLength(); i++) {
	  printTree(nodes.item(i), indent);
	}
      }
      break;
      
    case Node.ELEMENT_NODE:
      // print element and atributes
      String name = node.getNodeName();
      out.print(indent + '<' + name);
      // print attributes
      NamedNodeMap attributes = node.getAttributes();
      for (int i = 0; i < attributes.getLength(); i++) {
	Node current = attributes.item(i);
	out.print(" " + current.getNodeName() + "=\"" + htmlEncode(current.getNodeValue()) +  "\"");
      }
      // recurse on children
      NodeList children = node.getChildNodes();
      if (children == null)
	out.print(" />");
      else {
	out.print(">");
	for (int i = 0; i < children.getLength(); i++) {
	  Node child = children.item(i);
	  printTree(child, indent + "  ");
	}
	// FIXME: should indent when last printed child is ELEMENT_NODE
	out.print("</" + name + ">");
      }
      break;
      
    case Node.TEXT_NODE:
    case Node.CDATA_SECTION_NODE:
      // print textual data
      if (node.getNodeValue().trim() != "") {
	out.print(htmlEncode(node.getNodeValue()));
      }
      break;
      
    case Node.PROCESSING_INSTRUCTION_NODE:
    case Node.ENTITY_REFERENCE_NODE:
    case Node.DOCUMENT_TYPE_NODE:
    default:
      break;
    }
  }
  
  public static String htmlEncode(String v) {
    return(v.trim().replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;"));
  }
  
  // Converts the contents of a node to a string
  public static String NodeToString(Node n, String indent) {
    try {
      StringWriter tmpout = new StringWriter();
      PrintWriter pw = new PrintWriter(tmpout);
      XmlPrinter xmp = new XmlPrinter(pw);
      xmp.printTree(n, indent);
      pw.close();
      tmpout.close();
      return tmpout.toString();
    } catch (IOException ex) {
      return("");
    }
  }
}
