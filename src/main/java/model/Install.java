package model;




public class Install
{
    private final String serverId;
    private final String channelId;
    private String vodId;
    private String recapsId;

    public Install(String serverId, String channelId)
    {
        this.serverId = serverId;
        this.channelId = channelId;
    }
    public Install(String serverId, String channelId, String vodId, String recapsId)
    {
        this.serverId = serverId;
        this.recapsId = recapsId;
        this.channelId = channelId;
        this.vodId = vodId;
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

    public void setRecapsId(String recapsId)
    {
        this.recapsId = recapsId;
    }

    public void setVodId(String vodId)
    {
        this.vodId = vodId;
    }
}
