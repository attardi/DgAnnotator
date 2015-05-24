package dga;

import java.util.Hashtable;

/**
 * Converts a String to HTML by converting all disallowed characters to HTML-entities.
 * Can't use java.io.URLEncoder.encode() since it does not use entities.
 */

public class HtmlEncoder
{

    private static Hashtable<Character, String> entityTableEncode = new Hashtable<Character, String>();

    static {
        entityTableEncode.put('<', "&lt;"); 
        entityTableEncode.put('&', "&amp;"); 
        entityTableEncode.put('"', "&quot;"); 
    }

    /**
     * Converts a String to HTML by converting all special characters to HTML-entities.
     * 
     *  * According to XML 1.1, only characters '<&"' are not allowed in attribute values.
     * (@see http://www.w3.org/TR/2006/REC-xml11-20060816/#sec-common-syn)
     */
    public final static String encode(String s)
    {
        if (s == null)
            return "";
        StringBuffer sb = new StringBuffer(s.length() * 2);
        for (int i = 0; i < s.length(); ++i) {
        	char ch = s.charAt(i);
        	String chEnc = entityTableEncode.get(ch); 
        	if (chEnc != null) 
        		sb.append(chEnc); 
        	else
        		sb.append(ch);
        }
        return sb.toString(); 
    }
}