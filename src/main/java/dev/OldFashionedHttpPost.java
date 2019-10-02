package dev;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class OldFashionedHttpPost {
    private ObjectMapper MAPPER = new ObjectMapper();


    public void post(String urlBasePath){
        HttpURLConnection urlConnection;
        String url;
        String result = null;
        try {
            String username ="thomas.nameofsomething@gmail.com";
            String password = "12345678";

            String auth =new String(username + ":" + password);
            byte[] data1 = auth.getBytes("UTF-8");
            String base64 = Base64.getEncoder().encodeToString(data1);
            //Connect
            urlConnection = (HttpURLConnection) ((new URL(urlBasePath).openConnection()));
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Authorization", "Basic "+base64);
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestMethod("POST");
            urlConnection.setConnectTimeout(10000);
            urlConnection.connect();

            Map<String,Object> obj = new HashMap<>();
            obj.put("MobileNumber", "+97333746934");
            obj.put("EmailAddress", "danish.hussain@mee.com");
            obj.put("FirstName", "Danish");
            obj.put("LastName", "Hussain");
            obj.put("Country", "BH");
            obj.put("Language", "EN");

            String json = MAPPER.writeValueAsString(obj);
            //Write
            OutputStream outputStream = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write(json);
            writer.close();
            outputStream.close();
            int responseCode=urlConnection.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                //Read
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                String line = null;
                StringBuilder sb = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }
                bufferedReader.close();
                result = sb.toString();
            }else {
                //    return new String("false : "+responseCode);
                new String("false : "+responseCode);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void get(String urlBasePath){
        HttpURLConnection urlConnection;
        String url;
        String result = null;
        try {
            String username ="thomas.nameofsomething@gmail.com";
            String password = "12345678";

            String auth =new String(username + ":" + password);
            byte[] data1 = auth.getBytes("UTF-8");
            String base64 = Base64.getEncoder().encodeToString(data1);
            //Connect
            urlConnection = (HttpURLConnection) ((new URL(urlBasePath).openConnection()));
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Authorization", "Basic "+base64);
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(10000);
            urlConnection.connect();

            //Write
            OutputStream outputStream = urlConnection.getOutputStream();
            outputStream.close();
            int responseCode=urlConnection.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                //Read
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                String line = null;
                StringBuilder sb = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }
                bufferedReader.close();
                result = sb.toString();
                System.out.println(result);
            }else {
                //    return new String("false : "+responseCode);
                new String("false : "+responseCode);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public static void main(String[] args) {
        OldFashionedHttpPost pgm = new OldFashionedHttpPost();
        pgm.get("http://fanto.se");
    }


}
