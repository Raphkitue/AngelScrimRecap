package support;

import static Util.LocaleUtils.getLocaleString;
import static Util.MessageUtils.commandMessage;
import static Util.MessageUtils.sendMessage;
import static controller.RankingsController.createTask;
import static controller.RankingsController.displayScores;
import static model.commands.commands.Rankings.RANKINGS_CONF;
import static model.commands.commands.Rankings.RANKINGS_DELETE;
import static model.commands.commands.Rankings.RANKINGS_ENROLL;
import static model.commands.commands.Rankings.RANKINGS_REMOVE;
import static model.commands.commands.Rankings.RANKINGS_START;
import static model.commands.commands.Rankings.RANKINGS_UPDATE;

import app.DependenciesContainer;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.reaction.ReactionEmoji;
import java.util.Arrays;
import model.commands.Commander;
import model.rankings.Player;
import model.rankings.Rankings;
import model.rankings.Roles;
import net.owapi.IOWAPI;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;
import repository.rankings.recap.IRankingsRepository;

public class AngelCompetition
{

    public enum RankingsMode
    {
        MAIN_ROLE("main_role"),
        MEAN_ROLES("mean_role"),
        MAIN_ROLE_AND_OPEN_Q("main_role_open_q"),
        MEAN_ROLES_AND_OPEN_Q("mean_role_open_q");

        private final String value;

        RankingsMode(String value)
        {
            this.value = value;
        }

        public String getValue()
        {
            return value;
        }
    }

    private static final Logger log = Loggers.getLogger(AngelCompetition.class);

    private static final IOWAPI owApi = DependenciesContainer.getInstance().getOwApi();
    private static final IRankingsRepository rankingsRepo = DependenciesContainer.getInstance().getRankingsRepo();


    public static Mono<Void> onStartRankings(MessageCreateEvent event)
    {
        return commandMessage(event, RANKINGS_START, (command, e) -> {
            String serverId = e.getGuildId().get().asString();
            String channel = Commander.getMandatoryArgument(command, e.getMessage(), "#channel");

            //Start task for weekly summary + task to check for changes

            if (rankingsRepo.rankingsExist(channel))
            {
                sendMessage(e.getMessage().getChannel(), serverId, "ranking_system_already_created");
            }

            Rankings rankings = new Rankings(channel, "", RankingsMode.MAIN_ROLE.value);
            rankingsRepo.updateRankings(rankings);

            createTask(channel);

            StringBuilder sb = new StringBuilder();

            Arrays.stream(model.commands.commands.Rankings.values())
                .forEach(f -> {
                    sb.append(" - ")
                        .append(f.getCommand());
                    f.getArguments().forEach(g -> sb.append(" ").append(g.toString()));
                    sb.append(": ")
                        .append(getLocaleString(serverId, command.getName()))
                        .append('\n');
                });

            sendMessage(event.getMessage().getChannel(), serverId, "ranking_system_enabled", sb.toString());

        });
    }

    public static Mono<Void> onRankingsEnroll(MessageCreateEvent event)
    {
        return commandMessage(event, RANKINGS_ENROLL, (command, e) -> {
            String serverId = e.getGuildId().get().asString();
            String channel = Commander.getMandatoryArgument(command, e.getMessage(), "#channel");
            String battletag = Commander.getMandatoryArgument(command, e.getMessage(), "battletag");
            String mainRole = Commander.getMandatoryArgument(command, e.getMessage(), "mainrole");

            if (Roles.from(mainRole) == null)
            {
                sendMessage(e.getMessage().getChannel(), serverId, "invalid_role");
                return;
            }

            //Start task for weekly summary + task to check for changes
            if (!rankingsRepo.rankingsExist(channel))
            {
                sendMessage(e.getMessage().getChannel(), serverId, "ranking_system_not_created");
                return;
            }

            if (owApi.getPlayerElos(battletag) == null)
            {
                sendMessage(e.getMessage().getChannel(), serverId, "ranking_system_incorrect_profile");
                return;
            }

            Rankings rankings = rankingsRepo.getRanking(channel);
            Player player = new Player(battletag, mainRole, 0, 0, 0, 0, false);
            rankings.setRanking(player);

            rankingsRepo.updateRankings(rankings);

            sendMessage(event.getMessage().getChannel(), serverId, "ranking_system_player_enrolled", battletag);

        });
    }

    public static Mono<Void> onRankingsDelete(MessageCreateEvent event)
    {
        return commandMessage(event, RANKINGS_DELETE, (command, e) -> {
            String serverId = e.getGuildId().get().asString();
            String channel = Commander.getMandatoryArgument(command, e.getMessage(), "#channel");
            String battletag = Commander.getMandatoryArgument(command, e.getMessage(), "battletag");

            //Start task for weekly summary + task to check for changes
            if (!rankingsRepo.rankingsExist(channel))
            {
                sendMessage(e.getMessage().getChannel(), serverId, "ranking_system_not_created");
                return;
            }

            Rankings rankings = rankingsRepo.getRanking(channel);

            rankings.removeRanking(battletag);

            rankingsRepo.updateRankings(rankings);

            sendMessage(event.getMessage().getChannel(), serverId, "ranking_system_player_removed", battletag);

        });
    }

    public static Mono<Void> onRankingsRemove(MessageCreateEvent event)
    {
        return commandMessage(event, RANKINGS_REMOVE, (command, e) -> {
            String serverId = e.getGuildId().get().asString();
            String channel = Commander.getMandatoryArgument(command, e.getMessage(), "#channel");

            //Start task for weekly summary + task to check for changes
            if (!rankingsRepo.rankingsExist(channel))
            {
                sendMessage(e.getMessage().getChannel(), serverId, "ranking_system_not_created");
                return;
            }

            rankingsRepo.deleteRankings(channel);

            e.getMessage().addReaction(ReactionEmoji.unicode("✅")).block();
        });
    }

    public static Mono<Void> onRankingsConf(MessageCreateEvent event)
    {
        return commandMessage(event, RANKINGS_CONF, (command, e) -> {
            String serverId = e.getGuildId().get().asString();
            String channel = Commander.getMandatoryArgument(command, e.getMessage(), "#channel");
            String confmode = Commander.getMandatoryArgument(command, e.getMessage(), "confmode");

            if (!rankingsRepo.rankingsExist(channel))
            {
                sendMessage(e.getMessage().getChannel(), serverId, "ranking_system_not_created");
                return;
            }

            Rankings rankings = rankingsRepo.getRanking(channel);

            boolean correctConf = Arrays.stream(RankingsMode.values())
                .map(RankingsMode::getValue)
                .anyMatch(confmode::equalsIgnoreCase);

            if (!correctConf)
            {
                sendMessage(e.getMessage().getChannel(), serverId, "ranking_incorrect_conf_mode");
                return;
            }

            rankings.setMode(confmode);

            rankingsRepo.updateRankings(rankings);

            e.getMessage().addReaction(ReactionEmoji.unicode("✅")).block();
        });
    }

    public static Mono<Void> onRankingsUpdate(MessageCreateEvent event)
    {
        return commandMessage(event, RANKINGS_UPDATE, (command, e) -> {
            String serverId = e.getGuildId().get().asString();
            String channel = Commander.getMandatoryArgument(command, e.getMessage(), "#channel");

            if (!rankingsRepo.rankingsExist(channel))
            {
                sendMessage(e.getMessage().getChannel(), serverId, "ranking_system_not_created");
                return;
            }

            //Request for new elos
            Rankings rankingsForServer = rankingsRepo.getRanking(channel);
            displayScores(serverId, rankingsForServer);
        });
    }


}
