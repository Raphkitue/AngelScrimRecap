package model.scrims;

import util.Jsonable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.json.simple.JSONObject;

public class Recap implements Jsonable
{
    private String recapId;
    private String teamId;
    private String serverId;
    private Map<String, String> reviews;
    private Set<Replay> mapsPlayed;
    private String date;


    public Recap(String teamId, String serverId, Map<String, String> reviews, Set<Replay> mapsPlayed, String date)
    {
        this.serverId = serverId;
        this.teamId = teamId;
        this.reviews = reviews;
        this.mapsPlayed = mapsPlayed;
        this.date = date;

        this.recapId = getRecapId(teamId, date);
    }

    public Recap(String teamId, String date, String serverId)
    {
        this.serverId = serverId;
        this.teamId = teamId;
        this.reviews = new HashMap<>();
        this.mapsPlayed = new HashSet<>();
        this.date = date;

        this.recapId = getRecapId(teamId, date);
    }

    public Recap()
    {
    }

    public String getRecapId()
    {
        return recapId;
    }

    public String getServerId()
    {
        return serverId;
    }

    public String getDate()
    {
        return date;
    }

    public String getTeamId()
    {
        return teamId;
    }

    public Map<String, String> getReviews()
    {
        return reviews;
    }

    public Set<Replay> getMapsPlayed()
    {
        return mapsPlayed;
    }

    @Override
    public JSONObject toJson()
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("recapId", recapId);
        jsonObject.put("serverId", serverId);
        jsonObject.put("date", date);
        jsonObject.put("teamId", teamId);
        jsonObject.put("reviews", new JSONObject(reviews));
        jsonObject.put("mapsPlayed", mapsPlayed.stream().map(Replay::toJson).collect(Collectors.toList()));
        return jsonObject;

    }

    @Override
    public Jsonable fromJson(JSONObject jsonObject)
    {
        recapId = (String) jsonObject.get("recapId");
        serverId = (String) jsonObject.get("serverId");
        date = (String) jsonObject.get("date");
        teamId = (String) jsonObject.get("teamId");
        reviews = (Map<String, String>) jsonObject.get("reviews");
        mapsPlayed = ((List<JSONObject>) jsonObject.get("mapsPlayed")).stream().map(j -> (Replay) new Replay().fromJson(j)).collect(Collectors.toSet());
        return this;
    }

    public static String getRecapId(String teamId, String date)
    {return teamId + "-" + date;}

}
