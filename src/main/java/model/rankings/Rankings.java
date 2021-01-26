package model.rankings;

import Util.Jsonable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.json.simple.JSONObject;
import reactor.util.Logger;
import reactor.util.Loggers;

public class Rankings implements Jsonable
{

    private static final Logger log = Loggers.getLogger(Rankings.class);

    private String serverId;
    private Map<String, Ranking> playersRanks = new HashMap<>();

    public Rankings()
    {
    }

    public Rankings(String serverId)
    {
        this.serverId = serverId;
    }

    public Optional<Ranking> getPlayerRanks(String playername)
    {
        return Optional.ofNullable(playersRanks.get(playername));
    }

    public Map<String, Ranking> getServerRanks()
    {
        return playersRanks;
    }

    public void setServerRanks(Map<String, Ranking> playersRanks)
    {
        this.playersRanks = playersRanks;
    }

    public String getServerId()
    {
        return serverId;
    }

    public Collection<Ranking> getRanks()
    {
        return playersRanks.values();
    }

    public void setRanking(Ranking ranking)
    {
        playersRanks.put(ranking.getBattletag(), ranking);
    }

    public String getUniqueStats()
    {
        return playersRanks.values().stream().map(ranking -> ranking.getBattletag()+ ranking.getSupportElo() + ranking.getDamageElo() + ranking.getTankElo()).collect(Collectors.joining());
    }

    @Override
    public JSONObject toJson()
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("serverId", serverId);
        JSONObject players = new JSONObject();
        playersRanks.values().forEach(ranking -> players.put(ranking.getBattletag(), ranking.toJson()));
        jsonObject.put("players", players);
        return jsonObject;

    }

    @Override
    public Jsonable fromJson(JSONObject jsonObject)
    {

        serverId = (String) jsonObject.get("serverId");
        HashMap<String, JSONObject> players = (JSONObject) jsonObject.get("players");

        playersRanks = players.values().stream()
            .map(j -> (Ranking) new Ranking().fromJson(j))
            .collect(Collectors.toMap(Ranking::getBattletag, e -> e));

        return this;
    }

    public Rankings deepClone()
    {
        Rankings rankings = new Rankings(serverId);

        rankings.setServerRanks(
            playersRanks.values().stream()
            .map(ranking -> new Ranking(ranking.getBattletag(), ranking.getMainRole(), ranking.getTankElo(), ranking.getDamageElo(), ranking.getSupportElo(), ranking.getOpenQElo(), ranking.isPrivate()))
            .collect(Collectors.toMap(Ranking::getBattletag, r -> r))
        );
        return rankings;
    }
}
