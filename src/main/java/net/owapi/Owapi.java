package net.owapi;

import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.javatuples.Quartet;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import reactor.util.Logger;
import reactor.util.Loggers;

public class Owapi implements IOWAPI
{

    private static final Logger log = Loggers.getLogger(Owapi.class);

    public Quartet<Long, Long, Long, Long> getPlayerElos(String battletag)
    {
        OkHttpClient client = new OkHttpClient();

        log.info(battletag.replace('#', '-'));

        Request request = new Request.Builder()
            .url("https://owapi.io/profile/pc/eu/" + battletag.replace('#', '-'))
            .build(); // defaults to GET

        Response response;
        try
        {
            response = client.newCall(request).execute();

            JSONParser jsonParser = new JSONParser();
            String string = response.body().string();
            //Read JSON file
            JSONObject obj = (JSONObject) jsonParser.parse(string);

            if ((boolean) obj.get("private")){
                return null;
            }

            JSONObject competitive = (JSONObject) obj.get("competitive");
            log.info(string);

            Long tank = (Long) ((JSONObject) competitive.get("tank")).getOrDefault("rank", 0L);
            Long damage = (Long) ((JSONObject) competitive.get("damage")).getOrDefault("rank", 0L);
            Long support = (Long) ((JSONObject) competitive.get("support")).getOrDefault("rank", 0L);

            return Quartet.with(
                tank == null ? 0L : tank,
                damage == null ? 0L : damage,
                support == null ? 0L : support,
                0L
            );


        } catch (IOException | ParseException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
