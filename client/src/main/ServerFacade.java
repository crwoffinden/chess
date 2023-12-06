import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerFacade {
    public String getRequest(String urlString, String authtoken) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(5000);
        conn.setRequestMethod("GET");
        conn.setDoOutput(true);
        if (authtoken != null) conn.addRequestProperty("Authorization", authtoken);
        conn.connect();
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream responseBody = conn.getInputStream();
            String resData = readString(responseBody);
            conn.disconnect();
            return resData;
        }
        else {
            InputStream responseBody = conn.getErrorStream();
            String resData = readString(responseBody);
            conn.disconnect();
            return resData;
        }
    }

    public String postRequest(String urlString, String requestText, String authtoken) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(5000);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        if (authtoken != null) conn.addRequestProperty("Authorization", authtoken);
        OutputStream requestBody = conn.getOutputStream();
        requestBody.write(requestText.getBytes());
        requestBody.close();
        conn.connect();
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream responseBody = conn.getInputStream();
            String resData = readString(responseBody);
            conn.disconnect();
            return resData;
        }
        else {
            InputStream responseBody = conn.getErrorStream();
            String resData = readString(responseBody);
            conn.disconnect();
            return resData;
        }
    }

    public String deleteRequest(String urlString, String authtoken) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(5000);
        conn.setRequestMethod("DELETE");
        conn.setDoOutput(true);
        conn.addRequestProperty("Authorization", authtoken);
        conn.connect();
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream responseBody = conn.getInputStream();
            String resData = readString(responseBody);
            conn.disconnect();
            return resData;
        }
        else {
            InputStream responseBody = conn.getErrorStream();
            String resData = readString(responseBody);
            conn.disconnect();
            return resData;
        }
    }

    public String putRequest(String urlString, String requestText, String authtoken) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(5000);
        conn.setRequestMethod("PUT");
        conn.setDoOutput(true);
        conn.addRequestProperty("Authorization", authtoken);
        OutputStream requestBody = conn.getOutputStream();
        requestBody.write(requestText.getBytes());
        conn.connect();
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream responseBody = conn.getInputStream();
            String resData = readString(responseBody);
            conn.disconnect();
            return resData;
        }
        else {
            InputStream responseBody = conn.getErrorStream();
            String resData = readString(responseBody);
            conn.disconnect();
            return resData;
        }
    }

    //Reads a string from an inputStream
    private String readString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader sr = new InputStreamReader(is);
        char[] buf = new char[1024];
        int len;
        while ((len = sr.read(buf)) > 0) {
            sb.append(buf, 0, len);
        }
        return sb.toString();
    }
}
