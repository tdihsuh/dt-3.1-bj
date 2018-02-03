package com.hansight.kunlun.analysis.utils;


import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.util.Map;


public class HttpClientUtils {

    private String end = "\r\n";
    private String twoHyphens = "--";
    private String boundary = "*****";

    private static void initHttpsConnection() {
        SSLContext sc;
        TrustManager[] trustAllCerts = new TrustManager[]{new javax.net.ssl.X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
            }
        }};

        try {
            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }

        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
                return urlHostName.equals(session.getPeerHost());
            }
        };
        HttpsURLConnection.setDefaultHostnameVerifier(hv);

    }

    public static HttpURLConnection createConnection( String url, String method, Map<String, String> properties) throws IOException {
        initHttpsConnection();
        HttpURLConnection con = null;
        con = (HttpURLConnection) new URL(url).openConnection();
        /*if (ip == null) {
            con = (HttpURLConnection) new URL(url).openConnection();
        } else {
            String str[] = ip.split("\\.");
            byte[] b = new byte[str.length];
            for (int i = 0, len = str.length; i < len; i++) {
                b[i] = (byte) (Integer.parseInt(str[i], 10));
            }
            Proxy proxy = new Proxy(Proxy.Type.HTTP,
                    new InetSocketAddress(InetAddress.getByAddress(b), 80));  //b是绑定的ip，生成proxy代理对象，因为http底层是socket实现，
            con = (HttpURLConnection) new URL(url).openConnection(proxy);
        }*/

//		con.setDoInput(true); //默认值为 true
        con.setDoOutput(true); //默认值为 false
        con.setUseCaches(false);
        con.setRequestMethod(method);
        if (properties != null)
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                con.setRequestProperty(entry.getKey(), entry.getValue());
            }
//		con.setRequestProperty("Connection", "Keep-Alive");
        con.setRequestProperty("Charset", "UTF-8");

        return con;
    }

    private void writeFile(DataOutputStream dos, String name, String filePath) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append(twoHyphens).append(boundary).append(end);
        builder.append("Content-Disposition: form-data; name=\"").append(name).append("\"; filename=\"").append(filePath).append("\"").append(end);
        builder.append(end);
        dos.writeBytes(builder.toString());
        FileInputStream fis = new FileInputStream(filePath);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = fis.read(buffer)) != -1) {
            dos.write(buffer, 0, length);
        }
        dos.writeBytes(end);
    }

    public String assign(String url, String method, Map<String, String> parameters, String[]... files) throws IOException {
        this.initHttpsConnection();
        HttpURLConnection con = this.createConnection( url + "/" + method, "POST", null);
        DataOutputStream dos = new DataOutputStream(con.getOutputStream());
        dos.writeBytes(make(parameters));
        if (files != null) {
            for (String[] file : files) {
                if (file == null)
                    continue;
                this.writeFile(dos, file[0], file[1]);
            }
        }
        dos.flush();
        return response(con);
    }

    private String make(Map<String, String> parameters) throws IOException {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            builder.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8")).append("&");
        }
        return builder.toString();
    }

    private String response(HttpURLConnection con) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
        StringBuilder builder = new StringBuilder();
        String temp;
        while ((temp = reader.readLine()) != null) {
            builder.append(temp);
        }
        return builder.toString();
    }
}
