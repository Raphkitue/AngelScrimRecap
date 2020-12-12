package repository.installs;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
            Object obj = jsonParser.parse(reader);

            JSONObject installs = (JSONObject) obj;

            this.installs.putAll(installs);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateInstall(Install install)
    {
        super.updateInstall(install);
        try (FileWriter file = new FileWriter(fileName)) {
            file.write(new JSONObject(installs).toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
