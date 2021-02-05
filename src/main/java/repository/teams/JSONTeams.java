package repository.teams;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.stream.Collectors;
import model.scrims.Team;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import reactor.util.Logger;
import reactor.util.Loggers;

public class JSONTeams extends InMemoryTeams
{

    private static final Logger log = Loggers.getLogger(JSONTeams.class);

    String fileName;

    public JSONTeams(String filename)
    {
        this.fileName = filename;

        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader(fileName))
        {

            //Read JSON file
            JSONObject obj = (JSONObject) jsonParser.parse(reader);

            JSONArray teams = (JSONArray) obj.get("teams");

            teams.forEach(object -> {
                JSONObject jsonObject = (JSONObject) object;
                String key = (String) jsonObject.keySet().stream().filter(e -> !((String)e).isEmpty()).findFirst().get();
                this.teams.put(key, (Team) new Team().fromJson((JSONObject) jsonObject.get(key)));
            });


        } catch (Exception e)
        {
            teams = new HashMap<>();
        }
    }


    @Override
    public void updateTeam(Team team)
    {
        super.updateTeam(team);

        storeTeams();

    }

    @Override
    public boolean createTeam(Team team)
    {
        boolean b = super.createTeam(team);

        storeTeams();
        return b;
    }

    private void storeTeams()
    {
        try (FileWriter file = new FileWriter(fileName))
        {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("teams", teams.entrySet().stream().map(entry -> {
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
}
