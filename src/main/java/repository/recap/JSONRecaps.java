package repository.recap;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import model.scrims.Recap;
import model.scrims.Team;
import model.scrims.User;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class JSONRecaps implements IRecapRepository
{

    Map<String, Recap> recaps = new HashMap<>();

    String fileName;

    public JSONRecaps(String filename)
    {
        this.fileName = filename;

        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader(fileName))
        {

            //Read JSON file
            JSONObject obj = (JSONObject) jsonParser.parse(reader);

            JSONArray teams = (JSONArray) obj.get("recaps");

            teams.forEach(object -> {
                JSONObject jsonObject = (JSONObject) object;
                String key = (String) jsonObject.keySet().stream().findFirst().get();
                this.recaps.put(key, (Recap) new Recap().fromJson((JSONObject) jsonObject.get(key)));
            });


        } catch (Exception e)
        {
            recaps = new HashMap<>();
        }
    }

    private void storeRecaps()
    {
        try (FileWriter file = new FileWriter(fileName))
        {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("recaps", recaps.entrySet().stream().map(entry -> {
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
    public List<Recap> getRecapsForTeam(String teamId)
    {
        return recaps.values().stream().filter(recap -> recap.getTeamId().equals(teamId)).collect(Collectors.toList());
    }

    @Override
    public boolean recapExists(String recapId)
    {
        return recaps.containsKey(recapId);
    }

    @Override
    public Recap getRecapById(String recapId)
    {
        return recaps.get(recapId);
    }

    @Override
    public Recap getRecapByServerDate(String server, String date)
    {
        return recaps.values().stream().filter(recap -> recap.getServerId().equals(server)).filter(recap -> recap.getDate().equals(date)).findFirst().orElse(null);
    }

    @Override
    public void updateRecap(Recap recap)
    {
        recaps.put(recap.getRecapId(), recap);
        storeRecaps();
    }

    @Override
    public void deleteUser(String recapId)
    {
        recaps.remove(recapId);
    }
}
