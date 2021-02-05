package repository.installs;

import java.util.HashMap;
import java.util.Map;
import model.Install;

public class InMemoryInstalls implements IInstallsRepository
{

    protected Map<String, Install> installs = new HashMap<>();


    @Override
    public Install getInstallForServer(String serverId)
    {
        return installs.get(serverId);
    }

    @Override
    public boolean installExists(String serverId)
    {
        return installs.containsKey(serverId);
    }

    @Override
    public void updateInstall(Install install)
    {
        installs.put(install.getServerId(), install);
    }
}
