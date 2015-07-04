package dga;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

class MultipartPost extends HttpPost {

  MultipartEntityBuilder builder = MultipartEntityBuilder.create(); 

  public MultipartPost(String url, String authorization) {
    super(url);
    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
    builder.setBoundary("HdKTBlvOJu3FRglX5wx67cU5im88g94");
    addHeader("Authorization", authorization);
    addHeader("Content-type", "multipart/form-data; boundary=HdKTBlvOJu3FRglX5wx67cU5im88g94");
    addHeader("Accept", "text/html");
  }
  
  public MultipartEntityBuilder addPart(String key, ContentBody content) {
    return builder.addPart(key, content);
  }
  
  public CloseableHttpResponse send() throws ClientProtocolException, IOException {
    CloseableHttpClient httpclient = HttpClients.createDefault();
    setEntity(builder.build());
    CloseableHttpResponse response = httpclient.execute(this);
    //httpclient.close();
    return response;
  }
}

public class Service {
  static final String defaultLanguage = "en";
  static final String defaultTagger = "http://tanl.di.unipi.it/en/api";

  public Service(String url, String authProvider, String token, String email) {
    this.url = url == null ? defaultTagger : url;
    this.token = token;
    this.authProvider = authProvider;
    this.email = email;
  }
  
  /**
   * 
   * @param params extra parameters to be posted: pairs key/value.
   * @return the connection from which to read response.
   * @throws IOException
   */
  public CloseableHttpResponse post(String[] params) throws IOException {
    MultipartPost post = new MultipartPost(url, token);
    post.builder.addTextBody("authentication", authProvider);
    post.builder.addTextBody("token", token);
    post.builder.addTextBody("email", email);
    for (int i = 0; i < params.length - 1; i += 2)
      post.builder.addTextBody(params[i], new String(params[i+1].getBytes("UTF-8")));
    return post.send();
  }

  /**
   * 
   * @param params extra parameters to be posted: key/value pairs.
   * @param file filename to be sent.
   * @return the connection from which to read response.
   * @throws IOException
   */
  public HttpResponse post(String[] params, String file) throws IOException {
    MultipartPost post = new MultipartPost(url, token);		// add "Bearer"
    post.builder.addTextBody("authentication", authProvider);
    post.builder.addTextBody("token", token);
    post.builder.addTextBody("email", email);
    post.addPart("file", new FileBody(new File(file)));
    return post.send();
  }

  public String url;
  public String authProvider;
  public String token;
  public String email;
}
