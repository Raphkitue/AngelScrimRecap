package repository.rankings;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import model.rankings.Rankings;
import org.javatuples.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import reactor.util.Logger;
import reactor.util.Loggers;

public class JSONRankings implements IRankingsRepository {

    private static final Logger log = Loggers.getLogger(JSONRankings.class);
    Multimap<String, String> servRanks = ArrayListMultimap.create();
    Map<String, String> channelNames = new HashMap<>();
    Map<String, Rankings> ranks = new HashMap<>();
    Map<String, Rankings> dailyRanks = new HashMap<>();

    String fileName;

    public synchronized void updateServs(GatewayDiscordClient client) {
        log.info("updating servs");
        for (String e : this.ranks.keySet()) {
            if (!servRanks.containsValue(e)) {
                client.getChannelById(Snowflake.of(e))
                    .map(f -> ((GuildMessageChannel) f))
                    .map(f -> {
                        log.info("adding " + f.getGuildId().asString() + e);
                        this.servRanks.put(f.getGuildId().asString(), e);
                        this.channelNames.put(e, f.getName());
                        return f;
                    })
                    .block();
            }
        }
    }

    public JSONRankings(String filename) {
        this.fileName = filename;
        reload();
    }

    private synchronized void storeRankings() {
        try (FileWriter file = new FileWriter(fileName)) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("rankings", ranks.entrySet().stream().map(entry -> {
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put(entry.getKey(), entry.getValue().toJson());
                return jsonObject1;
            }).collect(Collectors.toList()));

            jsonObject.put("servs", servRanks.entries().stream().map(entry -> {
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put(entry.getKey(), String.join("DELIMITER", entry.getValue()));
                return jsonObject1;
            }).collect(Collectors.toList()));

            jsonObject.put("channs", channelNames.entrySet().stream().map(entry -> {
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put(entry.getKey(), entry.getValue());
                return jsonObject1;
            }).collect(Collectors.toList()));

            jsonObject.put("dailyRanks", dailyRanks.entrySet().stream().map(entry -> {
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put(entry.getKey(), entry.getValue().toJson());
                return jsonObject1;
            }).collect(Collectors.toList()));

            file.write(jsonObject.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized Rankings getRanking(String channelId) {
        return ranks.get(channelId);
    }

    @Override
    public synchronized Rankings getDayRanking(String channelId, Date date) {
        return dailyRanks.get(channelId + DateFormat.getDateInstance().format(date));
    }

    @Override
    public synchronized void updateDayRankings(Rankings rankings, Date date) {
        dailyRanks.put(rankings.getChannelId() + DateFormat.getDateInstance().format(date), rankings);
        storeRankings();
    }

    @Override
    public synchronized Collection<Rankings> getRankings() {
        return ranks.values();
    }

    @Override
    public synchronized List<Pair<String, String>> getRankingsForServer(String guildId) {
        return servRanks.get(guildId).stream().map(chan -> Pair.with(chan, channelNames.get(chan))).collect(Collectors.toList());
    }

    @Override
    public synchronized void updateRankings(Rankings rankings) {
        ranks.put(rankings.getChannelId(), rankings);
        storeRankings();
    }

    @Override
    public synchronized void addRankings(Rankings rankings, String serverId, String channelName) {
        servRanks.put(serverId, rankings.getChannelId());
        ranks.put(rankings.getChannelId(), rankings);
        channelNames.put(rankings.getChannelId(), channelName);
        storeRankings();
    }

    @Override
    public synchronized boolean rankingsExist(String channelId) {
        return ranks.containsKey(channelId);
    }

    @Override
    public synchronized void deleteRankings(String serverId, String channelId) {
        ranks.remove(channelId);
        channelNames.remove(channelId);
        servRanks.remove(serverId, channelId);
        storeRankings();
    }

    @Override
    public synchronized boolean nameExists(String name) {
        return channelNames.containsValue(name);
    }

    @Override
    public synchronized void renameRankings(String channelId, String name) {
        channelNames.put(channelId, name);
        storeRankings();
    }

    @Override
    public synchronized void reload() {

        ranks.clear();
        servRanks.clear();
        dailyRanks.clear();
        channelNames.clear();

        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader(fileName)) {

            //Read JSON file
            JSONObject obj = (JSONObject) jsonParser.parse(reader);

            JSONArray teams = (JSONArray) obj.get("rankings");
            JSONArray serv = (JSONArray) obj.get("servs");
            JSONArray channs = (JSONArray) obj.get("channs");
            JSONArray daily = (JSONArray) obj.get("dailyRanks");

            teams.forEach(object -> {
                JSONObject jsonObject = (JSONObject) object;
                String key = (String) jsonObject.keySet().stream().findFirst().get();
                this.ranks.put(key, (Rankings) new Rankings().fromJson((JSONObject) jsonObject.get(key)));
            });
            if (serv != null) {
                serv.forEach(object -> {
                    JSONObject jsonObject = (JSONObject) object;
                    String key = (String) jsonObject.keySet().stream().findFirst().get();
                    this.servRanks.putAll(key, Arrays.asList(((String) jsonObject.get(key)).split("DELIMITER")));
                });
                channs.forEach(object -> {
                    JSONObject jsonObject = (JSONObject) object;
                    String key = (String) jsonObject.keySet().stream().findFirst().get();
                    this.channelNames.put(key, (String) jsonObject.get(key));
                });
            }

            if (daily != null) {
                daily.forEach(object -> {
                    JSONObject jsonObject = (JSONObject) object;
                    String key = (String) jsonObject.keySet().stream().findFirst().get();
                    this.dailyRanks.put(key, (Rankings) new Rankings().fromJson((JSONObject) jsonObject.get(key)));
                });
            }


        } catch (Exception e) {
            ranks = new HashMap<>();
        }
    }
}
