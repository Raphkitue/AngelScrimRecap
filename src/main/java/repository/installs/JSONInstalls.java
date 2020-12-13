package repository.installs;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import model.Install;
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

            JSONObject installs = (JSONObject) obj.get("installs");
            JSONObject vods = (JSONObject) obj.get("vod");
            JSONObject recaps = (JSONObject) obj.get("recaps");

            this.installs.putAll(installs);
            this.vods.putAll(vods);
            this.recaps.putAll(recaps);

        } catch (Exception e) {
            installs = new HashMap<>();
            vods = new HashMap<>();
            recaps = new HashMap<>();
        }
    }

    @Override
    public void updateInstall(Install install)
    {
        super.updateInstall(install);
        try (FileWriter file = new FileWriter(fileName)) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("installs", installs);
            jsonObject.put("vod", vods);
            jsonObject.put("recaps", recaps);
            file.write(jsonObject.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
