package dga;

import java.io.*;
import java.net.*;
import java.util.*;

public class XMLResourceBundleControl extends ResourceBundle.Control {
  private static String XML = "xml";
  
  public List<String> getFormats(String baseName) {
    return Collections.singletonList(XML);
  }
  
  public ResourceBundle newBundle(String baseName, Locale locale,
		  String format, ClassLoader loader, boolean reload)
				  throws IllegalAccessException, InstantiationException, IOException {
	  if ((baseName == null) || (locale == null) || (format == null)
			  || (loader == null)) {
		  throw new NullPointerException();
	  }
	  ResourceBundle bundle = null;
	  if (format.equals(XML)) {
		  // dont use toBundleName since we need just the language
		  String bundleName = baseName;
		  String lang = locale.getLanguage();
		  if (lang != null && !lang.isEmpty())
			  bundleName += '_' + lang;
		  String resourceName = toResourceName(bundleName, format);
		  URL url = loader.getResource(resourceName);
		  if (url != null) {
			  URLConnection connection = url.openConnection();
			  if (connection != null) {
				  if (reload)
					  connection.setUseCaches(false);
				  InputStream stream = connection.getInputStream();
				  if (stream != null) {
					  BufferedInputStream bis = new BufferedInputStream(stream);
					  bundle = new XMLResourceBundle(bis);
					  bis.close();
				  }
			  }
		  }
	  }
	  return bundle;
  }
  
  public static class XMLResourceBundle extends ResourceBundle {
    private Properties props;
    
    XMLResourceBundle(InputStream stream) throws IOException {
      props = new Properties();
      try {
	props.loadFromXML(stream);
      } catch (InvalidPropertiesFormatException e) {
	System.err.println(e.getMessage());
      }
    }
    
    protected String handleGetObject(String key) {
      return props.getProperty(key);
    }
    
    public Enumeration<String> getKeys() {
      Set<String> handleKeys = props.stringPropertyNames();
      return Collections.enumeration(handleKeys);
    }
  }
}
