package dataAccess.webAPI;

import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dataAccess.DAO.MemoryDatabase;
import dataAccess.request.CreateGameRequest;
import dataAccess.request.JoinGameRequest;
import dataAccess.result.CreateGameResult;
import dataAccess.result.JoinGameResult;
import dataAccess.result.ListGamesResult;
import dataAccess.service.CreateGameService;
import dataAccess.service.JoinGameService;
import dataAccess.service.ListGamesService;

import java.io.*;
import java.net.HttpURLConnection;

public class GamesHandler implements HttpHandler {
    //FIXME memory implementation adjust when adding actual database

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Gson gson = new Gson();
        boolean success = false;
        try {
            //Checks the request method, and only accepts GET methods
            if (exchange.getRequestMethod().toLowerCase().equals("get")) {
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

                    //Have the list games service kust all games in the database and return a response
                    ListGamesService service = new ListGamesService();
                    ListGamesResult result = service.listGames(authToken);
                    success = result.isSuccess();

                    //Everything works, send an OK header
                    if (success) exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                        //If the error was an internal server error, sends a server error response header
                    else if (result.serverError()) {
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_SERVER_ERROR, 0);
                    }
                    else if (result.getMessage().toLowerCase().equals("error: unauthorized")) {
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_UNAUTHORIZED, 0);
                    }
                    //Something went wrong with the request, send a bad request header
                    else exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
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
            }
            else if (exchange.getRequestMethod().toLowerCase().equals("post")) {
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

                    //Have the create game service create a new game and return a response
                    CreateGameRequest request = (CreateGameRequest) gson.fromJson(reqData, CreateGameRequest.class);

                    CreateGameService service = new CreateGameService();
                    CreateGameResult result = service.createGame(request, authToken);
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
            }
            else if (exchange.getRequestMethod().toLowerCase().equals("put")) {
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

                    //Have the join game service add the user to the game and return a response
                    JoinGameRequest request = (JoinGameRequest) gson.fromJson(reqData, JoinGameRequest.class);

                    JoinGameService service = new JoinGameService();
                    JoinGameResult result = service.joinGame(request, authToken);
                    success = result.isSuccess();

                    //Everything works, send an OK header
                    if (success) exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                        //If the error was an internal server error, sends a server error response header
                    else if (result.serverError()) {
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_SERVER_ERROR, 0);
                    }
                    else if (result.getMessage().toLowerCase().equals("error: unauthorized")) {
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_UNAUTHORIZED, 0);
                    }
                    else if (result.getMessage().toLowerCase().equals("error: already taken")) {
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, 0);
                    }
                    //Something went wrong with the request, send a bad request header
                    else exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
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
            }
            else {
                //Sends bad request response if the request was not a GET request
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

