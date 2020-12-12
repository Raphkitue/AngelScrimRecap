package repository.installs;

import model.Install;

public interface IInstallsRepository
{
    Install getInstallForServer(String serverId);

    boolean installExists(String serverId);

    void updateInstall(Install install);
}
