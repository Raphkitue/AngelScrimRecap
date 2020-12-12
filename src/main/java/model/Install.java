package model;




public class Install
{
    private final String serverId;
    private final String channelId;

    public Install(String serverId, String channelId)
    {
        this.serverId = serverId;
        this.channelId = channelId;
    }

    public String getServerId()
    {
        return serverId;
    }

    public String getChannelId()
    {
        return channelId;
    }
}
