package Util;

import org.json.simple.JSONObject;

public interface Jsonable
{
    JSONObject toJson();
    Jsonable fromJson(JSONObject jsonString);
}
