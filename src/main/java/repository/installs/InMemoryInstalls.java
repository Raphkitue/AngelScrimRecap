package repository.installs;

import java.util.HashMap;
import java.util.Map;
import model.Install;

public class InMemoryInstalls implements IInstallsRepository
{

    protected Map<String, String> installs = new HashMap<>();
    protected Map<String, String> vods = new HashMap<>();
    protected Map<String, String> recaps = new HashMap<>();

    @Override
    public Install getInstallForServer(String serverId)
    {
        if (installExists(serverId))
        { return new Install(serverId, installs.get(serverId), vods.get(serverId), recaps.get(serverId)); }
        return null;
    }

    @Override
    public boolean installExists(String serverId)
    {
        return installs.containsKey(serverId);
    }

    @Override
    public void updateInstall(Install install)
    {
        installs.put(install.getServerId(), install.getChannelId());
        vods.put(install.getServerId(), install.getVodId());
        recaps.put(install.getServerId(), install.getRecapsId());
    }
}
