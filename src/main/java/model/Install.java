package model;


import Util.Jsonable;
import org.json.simple.JSONObject;
import reactor.util.annotation.NonNull;

public class Install implements Jsonable
{
    private String serverId;
    private String channelId;
    private String vodId;
    private String rankingsId;
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

    public Install(String serverId, String channelId, String vodId, String recapsId, @NonNull String lang, @NonNull String voteDelay, String rankingsId)
    {
        this.serverId = serverId;
        this.recapsId = recapsId;
        this.channelId = channelId;
        this.vodId = vodId;
        this.lang = lang;
        this.voteDelay = voteDelay;
        this.rankingsId = rankingsId;
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

    public String getRankingsId()
    {
        return rankingsId;
    }

    public void setRankingsId(String rankingsId)
    {
        this.rankingsId = rankingsId;
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
        jsonObject.put("channelId", channelId);
        jsonObject.put("recapsId", recapsId);
        jsonObject.put("lang", lang);
        jsonObject.put("voteDelay", voteDelay);
        jsonObject.put("rankingsId", rankingsId);
        return jsonObject;
    }

    @Override
    public Jsonable fromJson(JSONObject jsonString)
    {
        serverId = (String) jsonString.getOrDefault("serverId", "");
        channelId = (String) jsonString.getOrDefault("channelId", "");
        vodId = (String) jsonString.getOrDefault("vodId", "");
        recapsId = (String) jsonString.getOrDefault("recapsId", "");
        lang = (String) jsonString.getOrDefault("lang", "");
        voteDelay = (String) jsonString.getOrDefault("voteDelay", "");
        rankingsId = (String) jsonString.getOrDefault("rankingsId", "");

        return this;
    }
}
