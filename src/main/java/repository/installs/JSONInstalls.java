package repository.installs;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.stream.Collectors;
import model.Install;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class JSONInstalls extends InMemoryInstalls
{

    String fileName;

    public JSONInstalls(String filename)
    {
        this.fileName = filename;

        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader(fileName))
        {

            //Read JSON file
            JSONObject obj = (JSONObject) jsonParser.parse(reader);

            JSONArray installs = (JSONArray) obj.get("installs");

            installs.forEach(object -> {
                JSONObject jsonObject = (JSONObject) object;
                String key = (String) jsonObject.keySet().stream().filter(e -> !((String) e).isEmpty()).findFirst().get();
                this.installs.put(key, (Install) new Install().fromJson((JSONObject) jsonObject.get(key)));
            });


        } catch (Exception e)
        {
            installs = new HashMap<>();

        }
    }

    @Override
    public void updateInstall(Install install)
    {
        super.updateInstall(install);
        try (FileWriter file = new FileWriter(fileName))
        {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("installs", installs.entrySet().stream().map(entry -> {
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
