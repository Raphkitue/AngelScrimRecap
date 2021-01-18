package repository.rankings.recap;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import model.rankings.Ranking;
import model.rankings.Rankings;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import reactor.util.Logger;
import reactor.util.Loggers;

public class JSONRankings implements IRankingsRepository
{

    private static final Logger log = Loggers.getLogger(JSONRankings.class);
    Map<String, Rankings> ranks = new HashMap<>();

    String fileName;

    public JSONRankings(String filename)
    {
        this.fileName = filename;

        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader(fileName))
        {

            //Read JSON file
            JSONObject obj = (JSONObject) jsonParser.parse(reader);

            JSONArray teams = (JSONArray) obj.get("rankings");

            teams.forEach(object -> {
                JSONObject jsonObject = (JSONObject) object;
                String key = (String) jsonObject.keySet().stream().findFirst().get();
                this.ranks.put(key, (Rankings) new Rankings().fromJson((JSONObject) jsonObject.get(key)));
            });


        } catch (Exception e)
        {
            ranks = new HashMap<>();
        }
    }

    private void storeRankings()
    {
        try (FileWriter file = new FileWriter(fileName))
        {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("rankings", ranks.entrySet().stream().map(entry -> {
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put(entry.getKey(), entry.getValue().toJson());
                return jsonObject1;
            }).collect(Collectors.toList()));

            file.write(jsonObject.toJSONString());
            file.flush();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public Rankings getRankingsForServer(String serverId)
    {
        return ranks.get(serverId);
    }

    @Override
    public Collection<Rankings> getRankings()
    {
        return ranks.values();
    }

    @Override
    public Ranking getRankingsForServerPlayer(String serverId, String playername)
    {
        return ranks.get(serverId).getPlayerRanks(playername).orElse(new Ranking());
    }

    @Override
    public void updateRankings(Rankings rankings)
    {
        ranks.put(rankings.getServerId(), rankings);
        storeRankings();
    }

    @Override
    public boolean rankingsExist(String serverid)
    {
        return ranks.containsKey(serverid);
    }

    @Override
    public void deleteRankings(String serverId)
    {
        ranks.remove(serverId);
        storeRankings();
    }

}
