package support.slashcommands;

import app.DependenciesContainer;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import model.rankings.Player;
import model.rankings.Rankings;
import model.rankings.Roles;
import net.owapi.IOWAPI;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;
import repository.rankings.IRankingsRepository;
import support.AngelCompetition;

import static controller.RankingsController.createTask;
import static controller.RankingsController.displayScores;
import static model.commands.commands.Rankings.*;
import static support.AngelCompetition.createCommands;
import static support.AngelCompetition.getSlashArgument;
import static util.LocaleUtils.getLocaleString;
import static util.MessageUtils.slashCommandMessage;

public class RankingsSlashHandlers {
    private static final Logger log = Loggers.getLogger(RankingsSlashHandlers.class);

    private static final IOWAPI owApi = DependenciesContainer.getInstance().getOwApi();
    private static final IRankingsRepository rankingsRepo = DependenciesContainer.getInstance().getRankingsRepo();

    public static Mono<Void> onEnroll(SlashCommandEvent event) {
        return slashCommandMessage(event, RANKINGS_ENROLL, (command, e) -> {
            String serverId = event.getInteraction().getGuildId().map(Snowflake::asString).get();
            String channel = getSlashArgument(e, "channel");
            String battletag = getSlashArgument(e, "battletag");
            String mainRole = getSlashArgument(e, "mainrole");

            if (Roles.from(mainRole) == null) {
                event.reply(getLocaleString(serverId, "invalid_role")).block();
                return;
            }

            if (!rankingsRepo.rankingsExist(channel)) {
                event.reply(getLocaleString(serverId, "ranking_system_not_created")).block();
                return;
            }

            //event.replyEphemeral("Adding player...").block();
            event.acknowledgeEphemeral().block();

            if (owApi.getPlayerElos(battletag) == null) {
                event.getInteractionResponse().createFollowupMessage(getLocaleString(serverId, "ranking_system_incorrect_profile")).block();
                event.getInteractionResponse().deleteInitialResponse();
                return;
            }

            Rankings rankings = rankingsRepo.getRanking(channel);
            Player player = new Player(battletag, mainRole, 0, 0, 0, 0, false);
            rankings.setRanking(player);

            rankingsRepo.updateRankings(rankings);

            createCommands(e.getClient().getRestClient(), event.getInteraction().getGuildId().get());

            event.getInteractionResponse().createFollowupMessage(getLocaleString(serverId, "ranking_system_player_enrolled", battletag)).block();
        });
    }

    public static Mono<Void> onStart(SlashCommandEvent event) {
        return slashCommandMessage(event, RANKINGS_START, (command, e) -> {
            String serverId = event.getInteraction().getGuildId().map(Snowflake::asString).get();
            GuildMessageChannel discordChannel = event.getOption("channel")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asChannel)
                .orElse(Mono.empty())
                .filter(channelMono -> channelMono instanceof GuildMessageChannel)
                .map(c -> (GuildMessageChannel) c)
                .block();

            String argumentName = getSlashArgument(event, "name");

            if (discordChannel == null) {
                event.reply("Channel should be text").block();
                return;
            }
            event.acknowledge().block();

            String channel = discordChannel.getId().asString();
            String ogChannelName = argumentName != null ? argumentName + "(#"+ discordChannel.getName() + ")" : discordChannel.getName();

            if (rankingsRepo.rankingsExist(channel)) {
                event.getInteractionResponse().createFollowupMessage(getLocaleString(serverId, "ranking_system_already_created")).block();
                return;
            }

            String channelName = ogChannelName;
            int i = 1;
            while(rankingsRepo.nameExists(channelName))
            {
                channelName = ogChannelName + "(" + i + ")";
            }
            Rankings rankings = new Rankings(channel, "", AngelCompetition.RankingsMode.MAIN_ROLE.getValue());
            rankingsRepo.addRankings(rankings, serverId, channelName);

            createTask(channel);

            event.getInteractionResponse().createFollowupMessage(getLocaleString(serverId, "ranking_system_enabled")).block();
            createCommands(e.getClient().getRestClient(), event.getInteraction().getGuildId().get());

        });
    }

    public static Mono<Void> onRemovePlayer(SlashCommandEvent event) {
        return slashCommandMessage(event, RANKINGS_DELETE, (command, e) -> {
            String serverId = event.getInteraction().getGuildId().map(Snowflake::asString).get();
            String channel = getSlashArgument(e, "channel");
            String battletag = getSlashArgument(e, "battletag");

            if (!rankingsRepo.rankingsExist(channel)) {
                event.replyEphemeral(getLocaleString(serverId, "ranking_system_not_created")).block();
                return;
            }

            event.acknowledgeEphemeral().block();

            Rankings rankings = rankingsRepo.getRanking(channel);

            rankings.removeRanking(battletag);

            rankingsRepo.updateRankings(rankings);

            createCommands(e.getClient().getRestClient(), event.getInteraction().getGuildId().get());

            event.getInteractionResponse().createFollowupMessage(getLocaleString(serverId, "ranking_system_player_removed", battletag)).block();
        });
    }

    public static Mono<Void> onRename(SlashCommandEvent event) {
        return slashCommandMessage(event, RANKINGS_RENAME, (command, e) -> {
            String serverId = event.getInteraction().getGuildId().map(Snowflake::asString).get();
            String channel = getSlashArgument(e, "channel");
            String name = getSlashArgument(e, "name");

            if (!rankingsRepo.rankingsExist(channel)) {
                event.replyEphemeral(getLocaleString(serverId, "ranking_system_not_created")).block();
                return;
            }

            event.acknowledgeEphemeral().block();

            rankingsRepo.renameRankings(channel, name);

            createCommands(e.getClient().getRestClient(), event.getInteraction().getGuildId().get());

            event.getInteractionResponse().createFollowupMessage(getLocaleString(serverId, "rename_success", name)).block();
        });
    }


    public static Mono<Void> onDelete(SlashCommandEvent event) {
        return slashCommandMessage(event, RANKINGS_REMOVE, (command, e) -> {
            String serverId = event.getInteraction().getGuildId().map(Snowflake::asString).get();
            String channel = getSlashArgument(e, "channel");

            log.info("channel value " + channel);
            if (!rankingsRepo.rankingsExist(channel)) {
                event.replyEphemeral(getLocaleString(serverId, "ranking_system_not_created")).block();
                return;
            }
            event.acknowledgeEphemeral().block();

            rankingsRepo.deleteRankings(serverId, channel);

            createCommands(e.getClient().getRestClient(), event.getInteraction().getGuildId().get());

            event.getInteractionResponse().createFollowupMessage("Ranking system deleted").block();
        });
    }

    public static Mono<Void> onUpdate(SlashCommandEvent event) {
        return slashCommandMessage(event, RANKINGS_UPDATE, (command, e) -> {
            String serverId = event.getInteraction().getGuildId().map(Snowflake::asString).get();
            String channel = getSlashArgument(e, "channel");

            if (!rankingsRepo.rankingsExist(channel)) {
                event.replyEphemeral(getLocaleString(serverId, "ranking_system_not_created")).block();
                return;
            }

            event.acknowledgeEphemeral().block();

            displayScores(serverId, channel);

            event.getInteractionResponse().createFollowupMessage("Ranking system updated").block();
        });
    }

}
