package support;

import static Util.LocaleUtils.getLocaleString;
import static Util.MessageUtils.commandMessage;
import static Util.MessageUtils.sendMessage;
import static model.commands.commands.Recap.RECAP_ADD_LINE;
import static model.commands.commands.Recap.RECAP_ADD_REPLAY;
import static model.commands.commands.Recap.RECAP_FINISH;
import static model.commands.commands.Recap.RECAP_START;
import static support.AngelTeam.getTeamFromArgument;

import app.DependenciesContainer;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.Reaction;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.retriever.EntityRetrievalStrategy;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import model.commands.Commander;
import model.scrims.Recap;
import model.scrims.Replay;
import model.scrims.Team;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;
import repository.installs.IInstallsRepository;
import repository.recap.IRecapRepository;
import repository.teams.ITeamsRepository;

public class AngelRecap
{

    private static final List<ReactionEmoji> voteEmojis = Arrays.asList(
        ReactionEmoji.unicode("1️⃣"),
        ReactionEmoji.unicode("2️⃣"),
        ReactionEmoji.unicode("3️⃣"),
        ReactionEmoji.unicode("4️⃣"),
        ReactionEmoji.unicode("5️⃣"),
        ReactionEmoji.unicode("6️⃣"),
        ReactionEmoji.unicode("7️⃣⃣"),
        ReactionEmoji.unicode("8️⃣"),
        ReactionEmoji.unicode("9️⃣")
    );

    private static final IInstallsRepository installsRepo = DependenciesContainer.getInstance().getInstallsRepo();
    private static final ITeamsRepository teamsRepository = DependenciesContainer.getInstance().getTeamsRepo();
    private static final IRecapRepository recapsRepo = DependenciesContainer.getInstance().getRecapsRepo();


    private static final Map<String, Recap> currentRecaps = new HashMap<>();

    private static final Logger log = Loggers.getLogger(AngelRecap.class);

    public static Mono<Void> onRecapStart(MessageCreateEvent event)
    {
        return commandMessage(event, RECAP_START, (command, e) -> {
            String serverId = e.getGuildId().get().asString();

            Team team = getTeamFromArgument(command, serverId, e.getMessage());

            Date date = new Date();
            Locale locale = new Locale("en", "FR");
            String dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, locale).format(date);

            if (currentRecaps.containsKey(team.getId()))
            {
                sendMessage(e.getMessage().getChannel(), serverId, "recap_running");
                return;
            }

            Recap recap = new Recap(team.getId(), dateFormat, serverId);
            recapsRepo.updateRecap(recap);
            currentRecaps.put(team.getId(), recap);

            StringBuilder sb = new StringBuilder();

            Arrays.stream(model.commands.commands.Recap.values())
                .forEach(f -> {
                    sb.append(" - ")
                        .append(f.getCommand());
                    f.getArguments().forEach(g -> sb.append(" ").append(g.toString()));
                    sb.append(": ")
                        .append(getLocaleString(serverId, command.getName()))
                        .append('\n');
                });

            sendMessage(event.getMessage().getChannel(), serverId, "scrim_created_intro", dateFormat, sb.toString());

        });
    }

    public static Mono<Void> onRecapAddReplay(MessageCreateEvent event)
    {
        return commandMessage(event, RECAP_ADD_REPLAY, (command, e) -> {
            String serverId = e.getGuildId().get().asString();

            String mapname = Commander.getMandatoryArgument(command, e.getMessage(), "mapname");
            String replaycode = Commander.getMandatoryArgument(command, e.getMessage(), "replaycode");

            Team team = getTeamFromArgument(command, serverId, e.getMessage());
            Recap recap = currentRecaps.get(team.getId());
            if (recap == null)
            {
                sendMessage(e.getMessage().getChannel(), serverId, "recap_not_running");
                return;
            }

            recap.getMapsPlayed().add(new Replay(mapname, replaycode));

            recapsRepo.updateRecap(recap);

            e.getMessage().addReaction(ReactionEmoji.unicode("✅")).block();
        });
    }

    public static Mono<Void> onRecapAddLine(MessageCreateEvent event)
    {
        return commandMessage(event, RECAP_ADD_LINE, (command, e) -> {
            String serverId = e.getGuildId().get().asString();

            Team team = getTeamFromArgument(command, serverId, e.getMessage());
            Recap recap = currentRecaps.get(team.getId());
            if (recap == null)
            {
                sendMessage(e.getMessage().getChannel(), serverId, "recap_not_running");
                return;
            }

            String userid = Commander.getArgument(command, e.getMessage().getContent(), "@username")
                .filter(s -> teamsRepository.getTeamById(recap.getTeamId())
                    .getMembers().stream()
                    .filter(user -> user.getUserId().equals(e.getMessage().getAuthor().get().getId().asString()))
                    .anyMatch(user -> user.getRole().equals("captain"))
                ).orElseGet(() -> e.getMessage().getAuthor()
                    .map(User::getId)
                    .map(Snowflake::asString)
                    .orElse("")
                );

            recap.getReviews().put(event.getGuild()
                .flatMap(g -> g.getMemberById(Snowflake.of(userid)))
                .block()
                .getNicknameMention(), e.getMessage().getId().asString());

            recapsRepo.updateRecap(recap);

            e.getMessage().addReaction(ReactionEmoji.unicode("✅")).block();
        });
    }

    public static Mono<Void> onRecapFinish(MessageCreateEvent event)
    {
        return commandMessage(event, RECAP_FINISH, (command, e) -> {
            String serverId = e.getGuildId().get().asString();

            Team team = getTeamFromArgument(command, serverId, e.getMessage());
            Recap recap = currentRecaps.get(team.getId());
            if (recap == null)
            {
                sendMessage(e.getMessage().getChannel(), serverId, "recap_not_running");
                return;
            }

            recap.getReviews().keySet().forEach(nickname -> {
                String reviewMessageNumber = recap.getReviews().get(nickname);
                e.getMessage().getChannel()
                    .flatMap(chann -> chann.getMessageById(Snowflake.of(reviewMessageNumber)))
                    .blockOptional()
                    .ifPresent(msg -> recap
                        .getReviews()
                        .replace(nickname, Commander.getText(RECAP_ADD_LINE, msg.getContent())));
            });
            //Schedule vote for favorite replay
            //Display scrim recap in total
            //

            String recapId;
            if (team.getChannelId() == null || team.getChannelId().isEmpty())
            { recapId = e.getMessage().getChannelId().asString(); }
            else
            { recapId = team.getChannelId(); }

            MessageChannel messageChannel = (MessageChannel) e.getGuild().flatMap(guild -> guild.getChannelById(Snowflake.of(recapId))).block();

            displayRecap(messageChannel, recap);

            Map<ReactionEmoji, Replay> mapping = new HashMap<>();

            Message message = displayVote(messageChannel, recap, mapping);

            new Timer().schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    handleVotes(team, recap, message, mapping);
                }
            }, (long) 20 * 60 * 1000);

            currentRecaps.remove(team.getId());

        });
    }

    private static void handleVotes(Team team, Recap recap, Message voteMessage, Map<ReactionEmoji, Replay> mapping)
    {
        String vodId;
        if (team.getVodId() == null || team.getVodId().isEmpty())
        { vodId = voteMessage.getChannelId().asString(); }
        else
        { vodId = team.getVodId(); }

        MessageChannel messageChannel = (MessageChannel) voteMessage.getGuild().flatMap(guild -> guild.getChannelById(Snowflake.of(vodId))).block();

        displayResults(messageChannel, voteMessage, recap, mapping);
    }

    private static void displayResults(MessageChannel vodChannel, Message msg, Recap recap, Map<ReactionEmoji, Replay> mapping)
    {

        Message votes = msg.getChannel().flatMap(channel1 -> channel1.getMessageById(msg.getId(), EntityRetrievalStrategy.REST)).block();

        StringBuilder sb = new StringBuilder();

        votes.getReactions().stream()
            .sorted(Comparator.comparingInt(Reaction::getCount).reversed())
            .forEach(
                reaction -> sb.append(mapping.get(reaction.getEmoji()).getMap())
                    .append(": ")
                    .append(mapping.get(reaction.getEmoji()).getCode())
                    .append(" (")
                    .append(reaction.getCount() - 1)
                    .append(" votes)\n")
            );

        sendMessage(vodChannel, msg.getGuildId().orElse(Snowflake.of(recap.getServerId())).asString(), "votes_display", recap.getDate(), sb.toString());

    }

    private static Message displayVote(MessageChannel channel, Recap recap, Map<ReactionEmoji, Replay> mapping)
    {
        AtomicInteger i = new AtomicInteger();
        i.set(0);
        Message message = sendMessage(channel, recap.getServerId(), "poll_display"
            , recap.getMapsPlayed().stream()
                .peek(replay -> mapping.put(voteEmojis.get(i.get()), replay))
                .map(replay -> (i.getAndIncrement() + 1) + ": " + replay.getMap() + " " + replay.getCode())
                .collect(Collectors.joining("\n"))
        );

        IntStream.rangeClosed(0, i.get() - 1)
            .forEach(j -> message.addReaction(voteEmojis.get(j)).block());
        return message;

    }

    private static void displayRecap(MessageChannel channel, Recap recap)
    {
        Date date = new Date();
        Locale locale = new Locale("en", "FR");
        String dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, locale).format(date);

        sendMessage(channel, recap.getServerId(), "recap_display", dateFormat,
            teamsRepository.getTeamById(recap.getTeamId()).getName() + ":\n"
                + recap.getReviews().entrySet().stream().map(entry -> entry.getKey() + ": " + entry.getValue()).collect(Collectors.joining("\n"))

        );
    }

}
