package repository.teams;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import model.Install;
import model.scrims.Team;
import model.scrims.User;
import org.graalvm.compiler.lir.LIRInstruction;
import org.graalvm.compiler.lir.LIRInstruction.Use;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import reactor.util.Logger;
import reactor.util.Loggers;
import repository.installs.InMemoryInstalls;
import support.AngelBot;

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
                String key = (String) jsonObject.keySet().stream().findFirst().get();
                this.teams.put(key, teamFromJson((JSONObject) jsonObject.get(key)));
            });


        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private Team teamFromJson(JSONObject teamObject)
    {
        return new Team(
            (String) teamObject.get("serverId"),
            (String) teamObject.get("name"),
            ((List<JSONObject>) teamObject.get("members")).stream().map(j -> new User((String) j.get("id"), (String) j.get("name"), (String) j.get("role"))).collect(Collectors.toSet())
        );
    }

    private JSONObject teamToJson(Team team)
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("serverId", team.getServerId());
        jsonObject.put("name", team.getName());
        jsonObject.put("members", team.getMembers().stream().map(this::userToJson).collect(Collectors.toList()));
        return jsonObject;
    }

    private JSONObject userToJson(User user)
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", user.getUserId());
        jsonObject.put("name", user.getName());
        jsonObject.put("role", user.getRole());
        return jsonObject;
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
                jsonObject1.put(entry.getKey(), teamToJson(entry.getValue()));
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
