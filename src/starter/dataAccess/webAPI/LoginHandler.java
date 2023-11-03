package dataAccess.webAPI;

import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dataAccess.DAO.Database;
import dataAccess.request.LoginRequest;
import dataAccess.result.LoginResult;
import dataAccess.result.LogoutResult;
import dataAccess.service.LoginService;
import dataAccess.service.LogoutService;

import java.io.*;
import java.net.HttpURLConnection;

public class LoginHandler implements HttpHandler {
    //FIXME memory implementation adjust when adding actual database
    private Database db;

    public LoginHandler(Database db) {
        this.db = db;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Gson gson = new Gson();
        boolean success = false;
        try {
            //Checks request method and only accepts POST requests
            if (exchange.getRequestMethod().toLowerCase().equals("post")) {
                // Extract the JSON string from the HTTP request body

                // Get the request body input stream
                InputStream reqBody = exchange.getRequestBody();

                // Read JSON string from the input stream
                String reqData = readString(reqBody);

                // Display/log the request JSON data
                System.out.println(reqData);

                //Have the login service use the request to log the user in and return the response
                LoginRequest request = (LoginRequest) gson.fromJson(reqData, LoginRequest.class);

                LoginService service = new LoginService();
                LoginResult result = service.login(request, db);
                success = result.isSuccess();

                //Everything worked, we send an OK response header
                if (success) exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                    //If the error was an internal server error, sends a server error response header
                else if (result.serverError()) {
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_SERVER_ERROR, 0);
                }
                //Something was wrong with the request, send an unauthorized header
                else exchange.sendResponseHeaders(HttpURLConnection.HTTP_UNAUTHORIZED, 0);
                //Write the response as a JSON object and write it in an output stream
                OutputStream resBody = exchange.getResponseBody();
                OutputStreamWriter writer = new OutputStreamWriter(resBody);
                gson.toJson(result, writer);
                writer.close();
                resBody.close();
            }
            else if (exchange.getRequestMethod().toLowerCase().equals("delete")) {
                // Get the HTTP request headers
                Headers reqHeaders = exchange.getRequestHeaders();

                // Check to see if an "Authorization" header is present
                if (reqHeaders.containsKey("Authorization")) {

                    // Extract the auth token from the "Authorization" header
                    String authToken = reqHeaders.getFirst("Authorization");

                    // Extract the JSON string from the HTTP request body

                    // Get the request body input stream
                    InputStream reqBody = exchange.getRequestBody();

                    // Read JSON string from the input stream
                    String reqData = readString(reqBody);

                    // Display/log the request JSON data
                    System.out.println(reqData);

                    //Have the Logout service log the user out and return a response
                    LogoutService service = new LogoutService();
                    LogoutResult result = service.logout(authToken, db);
                    success = result.isSuccess();

                    //Everything works, send an OK header
                    if (success) exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                        //If the error was an internal server error, sends a server error response header
                    else if (result.serverError()) {
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_SERVER_ERROR, 0);
                    }
                    //Something went wrong with the request, send a bad request header
                    else exchange.sendResponseHeaders(HttpURLConnection.HTTP_UNAUTHORIZED, 0);
                    //Write the response as a JSON object and write it in an output stream
                    OutputStream resBody = exchange.getResponseBody();
                    OutputStreamWriter writer = new OutputStreamWriter(resBody);
                    gson.toJson(result, writer);
                    writer.close();
                    resBody.close();
                } else {
                    //Sends bad request response if the request did not contain the "Authorization" header
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_UNAUTHORIZED, 0);
                    exchange.getResponseBody().close();
                }
            } else { //Sends bad request response if the request was not a POST request
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
                exchange.getResponseBody().close();
            }
        } catch (Exception e) {
            // Some kind of internal error has occurred inside the server (not the
            // client's fault), so we return an "internal server error" status code
            // to the client.
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_SERVER_ERROR, 0);
            exchange.getResponseBody().close();

            // Display/log the stack trace
            e.printStackTrace();
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
