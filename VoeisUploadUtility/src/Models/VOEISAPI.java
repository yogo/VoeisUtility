/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Models;

import Classes.BypassApacheHttpAuthentication;
import Classes.BypassJavaHttpAuthentication;
import JSONClasses.JSONArray;
import JSONClasses.JSONParser;
import JSONClasses.JSONTokener;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;


/**
 *
 * @author afannin1
 */
public class VOEISAPI implements IModel{

    final private String devHost = "https://voeis-dev.msu.montana.edu/projects/";
    final private String devHost2 = "https://voeis-dev.msu.montana.edu";
    final private String projectID;
    final private String apiKey;
    Properties props = System.getProperties();  //To view HTTPS traffic via Fiddler

    
    public VOEISAPI(final String projectID, final String apiKey) {
        this.projectID = projectID;
        this.apiKey = apiKey;
        //props.put("https.proxyHost", "127.0.0.1");
        //props.put("https.proxyPort", "8888");
    }
    
    public Map<Object, String> get_project_sites() {
        Map<Object, String> siteMap = new HashMap<Object, String>();
        try {
            siteMap = JSONParser.getNameInformation(httpGetRequest("/apivs/get_project_sites.json?", 0));
        } catch (Exception ex) {
            Logger.getLogger(VOEISAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return siteMap;
    }
    
    public Map<Object, String> get_project_data_templates(final int siteID) {
        Map<Object, String> dataTemplateMap = new HashMap<Object, String>();
        try {
            dataTemplateMap = JSONParser.getNameInformation(httpGetRequest("/apivs/get_project_data_templates.json?", siteID));
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        
        return dataTemplateMap;
    }
    
    //Still getting 500 Server Error
    public void upload_data(File datafile, int data_template_id, int site_id, int start_line) throws Exception {     
        site_id = 2;    //This is just for debug when calling the upload_data method.
        HttpHost proxy = new HttpHost("127.0.0.1", 8888, "http");
        DefaultHttpClient client = new DefaultHttpClient();
        client = (DefaultHttpClient) BypassApacheHttpAuthentication.wrapClient(client);
        MultipartEntity entity;
        String url = devHost + projectID + "/apivs/upload_data.json?api_key=" + apiKey;
        HttpPost post;
        try {
            client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            //HttpHost target = new HttpHost(url, 8888, "https");
            post = new HttpPost(url);
            entity = new MultipartEntity();
            
            FileBody fileBody = new FileBody(datafile);
            entity.addPart("datafile", fileBody);
            entity.addPart("data_template_id", new StringBody(String.valueOf(data_template_id), Charset.forName("UTF-8")));
            entity.addPart("site_id", new StringBody(String.valueOf(site_id), Charset.forName("UTF-8")));
            entity.addPart("start_line", new StringBody(String.valueOf(start_line), Charset.forName("UTF-8")));

            post.setEntity(entity);
            
            HttpResponse response = client.execute(post);
            System.out.println(response.getStatusLine());
            
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        finally {
 
        }
    }

    private JSONArray httpGetRequest(String methodCall, final int ID) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        HttpHost proxy = new HttpHost("127.0.0.1", 8888, "http");
        DefaultHttpClient client = new DefaultHttpClient();
        client = (DefaultHttpClient) BypassApacheHttpAuthentication.wrapClient(client);
        HttpGet get;
        HttpResponse response;
        String target = devHost + projectID + methodCall;
        
        JSONTokener tokener;
        JSONArray parsedArray = null;

        BufferedReader reader = null;
        StringBuilder builder = null;
        String line = null;
          
          try {
              client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
              get = new HttpGet(target);
              response = client.execute(get);
              
              reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
              builder = new StringBuilder();
              
              while((line = reader.readLine()) != null) {
                  builder.append(line).append('\n');
              }
              tokener = new JSONTokener(builder.toString());
              parsedArray = new JSONArray(tokener);
              //parsedString = new JSONObject(tokener);
              JSONParser.printJSONString(parsedArray);
              //System.out.println(builder.toString());
          }
                  catch (Exception ex){
                      ex.printStackTrace();
                  }
          finally {
              reader = null;
              builder = null;
              return parsedArray;
          }
    }
    
    
    /*NOTE: This is how the GET request was executed using Java's libraries.
     *      This has been switched to a method that uses Apache's HTTPComponents
     *      but I kept this here for legacy purposes.
     *      A.F. 09 March 2012
     */
//    private JSONArray httpGetRequest(String methodCall, final int ID) throws IOException, NoSuchAlgorithmException, KeyManagementException {
//        BypassJavaHttpAuthentication trustAll = new BypassJavaHttpAuthentication();
//        SSLSocketFactory sslSocketFactory = trustAll.bypassCerts(); //DEV!!!
//        
//        JSONTokener tokener;
//        JSONArray parsedArray = null;
//
//          URL url;
//          HttpURLConnection connection = null;
//          String target = devHost + projectID + methodCall;
//          BufferedReader reader = null;
//          StringBuilder builder = null;
//          String line = null;
//          
//          try {
//              url = new URL(target);
//              connection = (HttpURLConnection)url.openConnection();
//              ((HttpsURLConnection) connection).setSSLSocketFactory(sslSocketFactory);
//              connection.setRequestMethod("GET");
//              if (ID > 0) {
//                  connection.setRequestProperty("id", String.valueOf(ID));
//              }
//              connection.setDoOutput(true);
//              connection.setReadTimeout(10000);
//              
//              connection.connect();
//              
//              reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//              builder = new StringBuilder();
//              
//              while((line = reader.readLine()) != null) {
//                  builder.append(line).append('\n');
//              }
//              tokener = new JSONTokener(builder.toString());
//              parsedArray = new JSONArray(tokener);
//              //parsedString = new JSONObject(tokener);
//              JSONParser.printJSONString(parsedArray);
//              //System.out.println(builder.toString());
//          }
//                  catch (Exception ex){
//                      ex.printStackTrace();
//                  }
//          finally {
//              connection.disconnect();
//              reader = null;
//              builder = null;
//              connection = null;
//              return parsedArray;
//          }
//    }
}
