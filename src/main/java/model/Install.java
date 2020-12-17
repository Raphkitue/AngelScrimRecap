package model;


import Util.Jsonable;
import org.json.simple.JSONObject;
import reactor.util.annotation.NonNull;

public class Install implements Jsonable
{
    private String serverId;
    private String channelId;
    private String vodId;
    private String recapsId;
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
    public Install(String serverId, String channelId, String vodId, String recapsId, @NonNull String lang, @NonNull String voteDelay)
    {
        this.serverId = serverId;
        this.recapsId = recapsId;
        this.channelId = channelId;
        this.vodId = vodId;
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

    public String getVodId()
    {
        return vodId;
    }

    public String getRecapsId()
    {
        return recapsId;
    }

    public void setChannelId(String channelId)
    {
        this.channelId = channelId;
    }

    public void setRecapsId(String recapsId)
    {
        this.recapsId = recapsId;
    }

    public void setVodId(String vodId)
    {
        this.vodId = vodId;
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
        jsonObject.put("vodId", vodId);
        jsonObject.put("recapsId", recapsId);
        jsonObject.put("lang", lang);
        jsonObject.put("voteDelay", voteDelay);
        return jsonObject;
    }

    @Override
    public Jsonable fromJson(JSONObject jsonString)
    {
        serverId = (String) jsonString.getOrDefault("serverId", "");
        vodId = (String) jsonString.getOrDefault("vodId", "");
        recapsId = (String) jsonString.getOrDefault("recapsId", "");
        lang = (String) jsonString.getOrDefault("lang", "");
        voteDelay = (String) jsonString.getOrDefault("voteDelay", "");

        return this;
    }
}
