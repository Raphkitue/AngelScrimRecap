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
import support.AngelCompetition.RankingsMode;

public class Rankings implements Jsonable
{

    private static final Logger log = Loggers.getLogger(Rankings.class);

    private String lastMessageId;
    private String mode;
    private String channelId;
    private Map<String, Player> playersRanks = new HashMap<>();

    public Rankings()
    {
    }

    public Rankings(String channelId, String lastMessageId, String mode)
    {
        this.channelId = channelId;
        this.lastMessageId = lastMessageId;
        this.mode = mode;
    }

    public Optional<Player> getPlayerRanks(String playername)
    {
        return Optional.ofNullable(playersRanks.get(playername));
    }

    public Map<String, Player> getServerRanks()
    {
        return playersRanks;
    }

    public void setServerRanks(Map<String, Player> playersRanks)
    {
        this.playersRanks = playersRanks;
    }

    public Collection<Player> getRanks()
    {
        return playersRanks.values();
    }

    public String getChannelId()
    {
        return channelId;
    }

    public void setChannelId(String channelId)
    {
        this.channelId = channelId;
    }

    public String getLastMessageId()
    {
        return lastMessageId;
    }

    public void setLastMessageId(String lastMessageId)
    {
        this.lastMessageId = lastMessageId;
    }

    public void setRanking(Player player)
    {
        playersRanks.put(player.getBattletag(), player);
    }

    public void setMode(String mode)
    {
        this.mode = mode;
    }

    public String getMode()
    {
        return mode;
    }

    public void removeRanking(String battletag) {
        playersRanks.remove(battletag);
    }

    @Override
    public JSONObject toJson()
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mode", mode);
        jsonObject.put("channelId", channelId);
        jsonObject.put("lastMessageId", lastMessageId);
        JSONObject players = new JSONObject();
        playersRanks.values().forEach(player -> players.put(player.getBattletag(), player.toJson()));
        jsonObject.put("players", players);
        return jsonObject;

    }

    @Override
    public Jsonable fromJson(JSONObject jsonObject)
    {
        mode = (String) jsonObject.getOrDefault("mode", RankingsMode.MAIN_ROLE.getValue());
        channelId = (String) jsonObject.getOrDefault("channelId", "");
        lastMessageId = (String) jsonObject.getOrDefault("lastMessageId", "");
        HashMap<String, JSONObject> players = (JSONObject) jsonObject.get("players");

        playersRanks = players.values().stream()
            .map(j -> (Player) new Player().fromJson(j))
            .collect(Collectors.toMap(Player::getBattletag, e -> e));

        return this;
    }

    public Rankings deepClone()
    {
        Rankings rankings = new Rankings(channelId, lastMessageId, mode);

        rankings.setServerRanks(
            playersRanks.values().stream()
            .map(player -> new Player(player.getBattletag(), player.getMainRole(), player.getTankElo(), player.getDamageElo(), player.getSupportElo(), player.getOpenQElo(), player.isPrivate()))
            .collect(Collectors.toMap(Player::getBattletag, r -> r))
        );
        return rankings;
    }
}
