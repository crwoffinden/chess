package dataAccess.webAPI;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dataAccess.DAO.MemoryDatabase;
import dataAccess.result.ClearApplicationResult;
import dataAccess.service.ClearApplicationService;

import java.io.*;
import java.net.HttpURLConnection;

public class ClearApplicationHandler implements HttpHandler {

    @Override
    //FIXME memory implementation, adjust when adding the actual database
    public void handle(HttpExchange exchange) throws IOException {
        Gson gson = new Gson();
        boolean success = false;
        try {
            //Checks the request method and only allows DELETE methods
            if (exchange.getRequestMethod().toLowerCase().equals("delete")) {
                // Extract the JSON string from the HTTP request body

                // Get the request body input stream
                InputStream reqBody = exchange.getRequestBody();

                // Read JSON string from the input stream
                String reqData = readString(reqBody);

                // Display/log the request JSON data
                System.out.println(reqData);

                //Have the clear service clear the database and return the response
                ClearApplicationService service = new ClearApplicationService();
                ClearApplicationResult result = service.clear();
                success = result.isSuccess();

                //Everything works, send an OK header
                if (success) exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                    //If the error was an internal server error, sends a server error response header
                else if (result.serverError()) {
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_SERVER_ERROR, 0);
                }
                //Something went wrong with the request, send a bad request header
                else exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
                //Write the response as a JSON object and write it in an output stream
                OutputStream resBody = exchange.getResponseBody();
                OutputStreamWriter writer = new OutputStreamWriter(resBody);
                gson.toJson(result, writer);
                writer.close();
                resBody.close();
            }
            else {
                //Sends bad request response if the request was not a DELETE request
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

    //Reads a string from an input stream
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