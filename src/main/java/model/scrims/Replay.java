package model.scrims;

import util.Jsonable;
import org.json.simple.JSONObject;

public class Replay implements Jsonable
{
    private String map;
    private String code;

    public Replay(String map, String code)
    {
        this.map = map;
        this.code = code;
    }

    public Replay()
    {
    }

    public String getMap()
    {
        return map;
    }

    public String getCode()
    {
        return code;
    }

    @Override
    public JSONObject toJson()
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("map", map);
        jsonObject.put("code", code);
        return jsonObject;

    }

    @Override
    public Jsonable fromJson(JSONObject jsonObject)
    {
        map = (String) jsonObject.get("map");
        code = (String) jsonObject.get("code");
        return this;
    }
}
