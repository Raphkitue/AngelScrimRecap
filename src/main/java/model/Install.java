package model;


import util.Jsonable;
import org.json.simple.JSONObject;
import reactor.util.annotation.NonNull;

public class Install implements Jsonable
{
    private String serverId;
    private String channelId;
    @NonNull
    private String lang = "en";
    @NonNull
    private String voteDelay = "20";

    public Install(String serverId, String channelId)
    {
        this.serverId = serverId;
        this.channelId = channelId;
    }
    public Install()
    {
    }

    public Install(String serverId, String channelId, @NonNull String lang, @NonNull String voteDelay)
    {
        this.serverId = serverId;
        this.channelId = channelId;
        this.lang = lang;
        this.voteDelay = voteDelay;
    }

    public String getServerId()
    {
        return serverId;
    }

    public String getChannelId()
    {
        return channelId;
    }

    public void setChannelId(String channelId)
    {
        this.channelId = channelId;
    }

    public String getLang()
    {
        return lang;
    }

    public void setLang(@NonNull String lang)
    {
        this.lang = lang;
    }

    public String getVoteDelay()
    {
        return voteDelay;
    }

    public void setVoteDelay(String voteDelay)
    {
        this.voteDelay = voteDelay;
    }

    @Override
    public JSONObject toJson()
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("serverId", serverId);
        jsonObject.put("channelId", channelId);
        jsonObject.put("lang", lang);
        jsonObject.put("voteDelay", voteDelay);
        return jsonObject;
    }

    @Override
    public Jsonable fromJson(JSONObject jsonString)
    {
        serverId = (String) jsonString.getOrDefault("serverId", "");
        channelId = (String) jsonString.getOrDefault("channelId", "");
        lang = (String) jsonString.getOrDefault("lang", "");
        voteDelay = (String) jsonString.getOrDefault("voteDelay", "");

        return this;
    }
}
