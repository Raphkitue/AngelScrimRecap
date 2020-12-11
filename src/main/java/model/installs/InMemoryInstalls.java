package model.installs;

import java.util.HashMap;
import java.util.Map;

public class InMemoryInstalls implements IInstallsRepository
{

    private final Map<String, String> installs = new HashMap<>();

    @Override
    public Install getInstallForServer(String serverId)
    {
        if (installExists(serverId))
        { return new Install(serverId, installs.get(serverId)); }
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
    }
}
