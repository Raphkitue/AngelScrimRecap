package app;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.rankings.Rankings;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import reactor.util.Logger;
import reactor.util.Loggers;
import repository.rankings.IRankingsRepository;
import repository.rankings.JSONRankings;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

public class APIHandler

{
    private final String authToken;
    private static final Logger log = Loggers.getLogger(APIHandler.class);

    private static final IRankingsRepository rankingsRepo = DependenciesContainer.getInstance().getRankingsRepo();

    public APIHandler()
    {
        String fileName = System.getenv("PERSISTENCE_ROOT") + "securityToken";
        String authToken = "";
        try (FileReader reader = new FileReader(fileName)) {
            authToken = new BufferedReader(reader).readLine();
        } catch (IOException e)
        {
            try (FileWriter file = new FileWriter(fileName)) {
                authToken = UUID.randomUUID().toString();
                file.write(authToken);
                file.flush();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        this.authToken = authToken;
    }

    private boolean validateAuth(String token)
    {
        return authToken.equalsIgnoreCase(token);
    }

    public HttpHandler getBaseHandler()
    {
        return (httpExchange) -> {

            OutputStream outputStream = httpExchange.getResponseBody();

            // encode HTML content

            String htmlResponse = "<html>"
                + "<body>"
                + "<h1>"
                + "Hello "
                + "</h1>"
                + "</body>"
                + "</html>"
                // encode HTML content
                ;

            // this line is a must

            httpExchange.sendResponseHeaders(200, htmlResponse.length());

            outputStream.write(htmlResponse.getBytes());

            outputStream.flush();

            outputStream.close();
        };
    }

    public HttpHandler getPostRankingsFileHandler()
    {
        return httpExchange -> {
            log.info("cool");
            if(!httpExchange.getRequestMethod().equals("POST") || !validateAuth(httpExchange.getRequestHeaders().getFirst("auth-token")))
            {
                httpExchange.sendResponseHeaders(400, 0);
                httpExchange.close();
                return;
            }

            String fileName = System.getenv("PERSISTENCE_ROOT") + "rankings.json";
            try (FileWriter file = new FileWriter(fileName)) {
                file.write(new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                file.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            httpExchange.sendResponseHeaders(200, 0);
            httpExchange.close();

            log.info("reloading");
            rankingsRepo.reload();
        };
    }

    public HttpHandler getGetRankingsFileHandler()
    {
        return httpExchange -> {
            log.info("Yes");
            if(!httpExchange.getRequestMethod().equals("GET") || !validateAuth(httpExchange.getRequestHeaders().getFirst("auth-token")))
            {
                httpExchange.sendResponseHeaders(400, 0);
                httpExchange.close();
                return;
            }

            String fileName = System.getenv("PERSISTENCE_ROOT") + "rankings.json";
            try (FileReader reader = new FileReader(fileName)) {
                BufferedReader bufferedReader = new BufferedReader(reader);
                String collect = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
                httpExchange.sendResponseHeaders(200, collect.getBytes(StandardCharsets.UTF_8).length);
                OutputStream os = httpExchange.getResponseBody();
                os.write(collect.getBytes(StandardCharsets.UTF_8));
                os.close();
            } catch (Exception e) {

            }
            httpExchange.close();
        };
    }


}

