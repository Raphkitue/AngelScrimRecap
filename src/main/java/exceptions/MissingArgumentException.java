package exceptions;

public class MissingArgumentException extends Exception
{
    private String channelId;
    private String serverId;
    private String argname;

    public MissingArgumentException(String message, String argname, String channelId, String serverId)
    {
        super(message);
        this.channelId = channelId;
        this.argname = argname;
        this.serverId = serverId;
    }

    public String getChannelId()
    {
        return channelId;
    }

    public String getServerId()
    {
        return serverId;
    }

    public String getArgname()
    {
        return argname;
    }
}
