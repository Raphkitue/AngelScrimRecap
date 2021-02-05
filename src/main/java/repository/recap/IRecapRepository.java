package repository.recap;

import java.util.List;
import model.scrims.Recap;
import model.scrims.User;

public interface IRecapRepository
{
    List<Recap> getRecapsForTeam(String teamId);

    Recap getRecapById(String recapId);
    Recap getRecapByServerDate(String serverId, String date);

    void updateRecap(Recap recap);

    boolean recapExists(String recapId);

    void deleteUser(String userId);
}
