package support;

import static Util.LocaleUtils.getLocaleString;
import static Util.MessageUtils.commandMessage;
import static Util.MessageUtils.sendMessage;

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
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import model.Install;
import model.commands.Command;
import model.scrims.Recap;
import model.scrims.Replay;
import model.scrims.Team;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;
import repository.installs.IInstallsRepository;
import repository.recap.IRecapRepository;
import repository.teams.ITeamsRepository;

public class AngelScrim
{
    private static final List<ReactionEmoji> voteEmojis= Arrays.asList(
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

    private static final long VOTING_TIME = 1000 * 20 * 60;


    private static final Map<String, Recap> currentRecaps = new HashMap<>();

    private static final Logger log = Loggers.getLogger(AngelScrim.class);

    private static Team getTeamFromArgument(Command command, String serverId, String message)
    {
        Team team;
        Optional<String> teamname = command.getArgument(message, "teamname");

        if (teamname.isPresent())
        { team = teamsRepository.getTeamById(Team.getTeamId(teamname.get(), serverId)); } else
        {
            List<Team> teams = teamsRepository.getTeamsForServer(serverId);
            if (teams.size() != 1)
            { team = null; } else
            { team = teams.get(0); }
        }
        return team;
    }

    public static Mono<Void> onScrimStart(MessageCreateEvent event)
    {
        return commandMessage(event, Command.RECAP_START, (command, e) -> {
            String serverId = e.getGuildId().get().asString();

            Team team = getTeamFromArgument(command, serverId, e.getMessage().getContent());
            if (team == null)
            {
                sendMessage(e.getMessage().getChannel(), serverId , "team_not_found");
                return;
            }

            Date date = new Date();
            Locale locale = new Locale("en", "FR");
            String dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, locale).format(date);

            if (currentRecaps.containsKey(serverId))
            {
                sendMessage(e.getMessage().getChannel(), serverId, "recap_running");
                return;
            }

            Recap recap = new Recap(team.getId(), dateFormat, serverId);
            recapsRepo.updateRecap(recap);
            currentRecaps.put(serverId, recap);

            StringBuilder sb = new StringBuilder();

            Arrays.stream(Command.values())
                .filter(f -> f.getCommand().contains("recap"))
                .filter(f -> !f.equals(Command.RECAP_START))
                .forEach(f -> {
                    sb.append(" - ")
                        .append(f.getCommand());
                    f.getArguments().forEach(g -> sb.append(" ").append(g.toString()));
                    sb.append(": ")
                        .append(f.getDescription(serverId))
                        .append('\n');
                });

            sendMessage(event.getMessage().getChannel(), serverId, "scrim_created_intro", dateFormat, sb.toString());

        });
    }

    public static Mono<Void> onRecapAddReplay(MessageCreateEvent event)
    {
        return commandMessage(event, Command.RECAP_ADD_REPLAY, (command, e) -> {
            String serverId = e.getGuildId().get().asString();

            String mapname = command.getMandatoryArgument(e.getMessage().getContent(), "mapname");
            String replaycode = command.getMandatoryArgument(e.getMessage().getContent(), "replaycode");

            Recap recap = currentRecaps.get(serverId);
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
        return commandMessage(event, Command.RECAP_ADD_LINE, (command, e) -> {
            String serverId = e.getGuildId().get().asString();

            Recap recap = currentRecaps.get(serverId);
            if (recap == null)
            {
                sendMessage(e.getMessage().getChannel(), serverId,  "recap_not_running");
                return;
            }

            String userid = command.getArgument(e.getMessage().getContent(), "@username")
                .filter(s -> teamsRepository.getTeamById(recap.getTeamId())
                    .getMembers().stream()
                    .filter(user -> user.getUserId().equals(e.getMessage().getAuthor().get().getId().asString()))
                    .anyMatch(user -> user.getRole().equals("captain"))
                ).orElseGet(() -> e.getMessage().getAuthor()
                    .map(User::getId)
                    .map(Snowflake::asString)
                    .orElse("")
                );


            String review = command.getText(e.getMessage().getContent());
            recap.getReviews().put(event.getGuild()
                .flatMap(g -> g.getMemberById(Snowflake.of(userid)))
                .block()
                .getNicknameMention(), review);

            recapsRepo.updateRecap(recap);

            e.getMessage().addReaction(ReactionEmoji.unicode("✅")).block();
            log.info("things " + recap.getReviews());
        });
    }

    public static Mono<Void> onRecapFinish(MessageCreateEvent event)
    {
        return commandMessage(event, Command.RECAP_FINISH, (command, e) -> {
            String serverId = e.getGuildId().get().asString();

            Recap recap = currentRecaps.get(serverId);
            if (recap == null)
            {
                sendMessage(e.getMessage().getChannel(), serverId, "recap_not_running");
                return;
            }

            //Schedule vote for favorite replay
            //Display scrim recap in total
            //

            Install install = installsRepo.getInstallForServer(serverId);
            String recapId;
            if (install.getRecapsId() == null || install.getRecapsId().isEmpty())
            { recapId = e.getMessage().getChannelId().asString(); } else
            { recapId = install.getRecapsId(); }

            MessageChannel messageChannel = (MessageChannel) e.getGuild().flatMap(guild -> guild.getChannelById(Snowflake.of(recapId))).block();

            displayRecap(messageChannel, recap);

            Map<ReactionEmoji, Replay> mapping = new HashMap<>();

            Message message = displayVote(messageChannel, recap, mapping);

            new Timer().schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    handleVotes(install, recap, message, mapping);
                }
            }, VOTING_TIME);

            currentRecaps.remove(serverId);

        });
    }

    private static void handleVotes(Install install, Recap recap, Message voteMessage, Map<ReactionEmoji, Replay> mapping)
    {
        String vodId;
        if (install.getVodId() == null || install.getVodId().isEmpty())
        { vodId = voteMessage.getChannelId().asString(); } else
        { vodId = install.getVodId(); }

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

        sendMessage(vodChannel, votes.getGuildId().get().asString(), "votes_display", recap.getDate(), sb.toString());

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
