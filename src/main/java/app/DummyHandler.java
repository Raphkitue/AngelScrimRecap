package app;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;

public class DummyHandler implements HttpHandler

{

    @Override
    public void handle(HttpExchange httpExchange) throws IOException
    {

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
    }




}

