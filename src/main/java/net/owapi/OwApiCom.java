package net.owapi;

import java.io.IOException;
import java.util.ArrayList;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.javatuples.Quartet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import reactor.util.Logger;
import reactor.util.Loggers;

public class OwApiCom implements IOWAPI
{

    private static final Logger log = Loggers.getLogger(OwApiCom.class);

    public Quartet<Long, Long, Long, Long> getPlayerElos(String battletag)
    {
        OkHttpClient client = new OkHttpClient();

        log.info(battletag.replace('#', '-'));

        Request request = new Request.Builder()
            .url("https://ow-api.com/v1/stats/pc/eu/" + battletag.replace('#', '-') + "/profile")
            .build(); // defaults to GET

        Response response;
        try
        {
            response = client.newCall(request).execute();

            JSONParser jsonParser = new JSONParser();
            String string = response.body().string();
            //Read JSON file
            JSONObject obj = (JSONObject) jsonParser.parse(string);

            if ((boolean) obj.get("private"))
            {
                return null;
            }

            Long openQ = (Long) obj.getOrDefault("rating", 0L);
            Quartet<Long, Long, Long, Long> with = Quartet.with(
                0L,
                0L,
                0L,
                openQ
            );

            ArrayList<JSONObject> competitive = (JSONArray) obj.get("ratings");
            log.info(string);

            return competitive.stream()
                .reduce(with, (quartet, o) -> {
                    switch ((String) o.get("role"))
                    {
                        case "tank":
                            return quartet.setAt0((Long) o.getOrDefault("level", 0L));
                        case "damage":
                            return quartet.setAt1((Long) o.getOrDefault("level", 0L));
                        case "support":
                            return quartet.setAt2((Long) o.getOrDefault("level", 0L));
                        default:
                            return quartet;
                    }
                }, (prev, next) -> next);



        } catch (IOException | ParseException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
