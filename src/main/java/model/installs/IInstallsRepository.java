package model.installs;

public interface IInstallsRepository
{
    Install getInstallForServer(String serverId);

    boolean installExists(String serverId);

    void updateInstall(Install install);
}
