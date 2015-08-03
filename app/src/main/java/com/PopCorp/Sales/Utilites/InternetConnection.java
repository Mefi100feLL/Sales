package com.PopCorp.Sales.Utilites;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class InternetConnection {

    public static final String REQUEST_METHOD_GET = "GET";
    public static final String REQUEST_METHOD_POST = "POST";

    private URL url;
    private HttpURLConnection connection;
    private String requestMethod = REQUEST_METHOD_GET;

    public InternetConnection(String page) throws MalformedURLException {
        url = new URL(page);
    }

    public InternetConnection(String page, String requestMethod) throws MalformedURLException {
        url = new URL(page);
        this.requestMethod = requestMethod;
    }

    private InputStream getStream() throws IOException {
        connection = (HttpURLConnection) url.openConnection();
        int connectTimeout = 10000;
        connection.setConnectTimeout(connectTimeout);
        int readTimeout = 10000;
        connection.setReadTimeout(readTimeout);
        connection.setRequestMethod(requestMethod);
        connection.setRequestProperty("Accept-Encoding", "gzip");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setDoInput(true);
        connection.connect();
        return connection.getInputStream();
    }

    /*public String getPage() throws IOException {
        InputStream stream = getStream();
        InputStreamReader streamReader = new InputStreamReader(stream);
        StringBuilder pageInStringBuilder = new StringBuilder();
        int n = 0;
        connection.getHeaderFields();
        int lenght =  connection.getContentLength();
        char[] buffer = new char[lenght];
        while (n >= 0) {
            n = streamReader.read(buffer, 0, buffer.length);
            if (n > 0) {
                pageInStringBuilder.append(buffer, 0, n);
            }
        }
        stream.close();
        streamReader.close();
        connection.disconnect();
        return pageInStringBuilder.toString();
    }*/

    public String getPageFromGzip() throws IOException {
        InputStream stream = getStream();
        InputStream responseStream = new BufferedInputStream(stream);

        BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(responseStream), "utf-8"));
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = responseStreamReader.readLine()) != null)
        {
            stringBuilder.append(line).append("\n");
        }
        responseStreamReader.close();
        responseStream.close();
        connection.disconnect();
        return stringBuilder.toString();
    }

    public void disconnect(){
        if (connection!=null){
            connection.disconnect();
        }
    }

}
