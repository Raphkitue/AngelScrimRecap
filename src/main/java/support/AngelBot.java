package support;

import static util.LocaleUtils.getLocaleString;
import static util.MessageUtils.commandMessage;
import static util.MessageUtils.sendMessage;
import static model.commands.commands.Setup.HELP;
import static model.commands.commands.Setup.SETUP;
import static model.commands.commands.Setup.SETUP_DELAY;
import static model.commands.commands.Setup.SETUP_LANGUAGE;

import app.DependenciesContainer;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import model.Install;
import model.commands.Commander;
import model.commands.Commands;
import model.commands.commands.Rankings;
import model.commands.commands.Recap;
import model.commands.commands.Setup;
import model.commands.commands.Team;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;
import repository.installs.IInstallsRepository;

public class AngelBot
{

    private static final IInstallsRepository installsRepo = DependenciesContainer.getInstance().getInstallsRepo();

    private static final Logger log = Loggers.getLogger(AngelBot.class);

    public static Mono<Void> readyHandler(GatewayDiscordClient client)
    {
        return client.on(ReadyEvent.class)
            .doOnNext(ready -> log.info("Logged in as {}", ready.getSelf().getUsername()))
            .then();
    }

    public static Mono<Void> logMessages(MessageCreateEvent event)
    {
        log.info(event.getMessage().getContent());
        return Mono.empty();
    }

    public static Mono<Void> onSetupMessage(MessageCreateEvent event)
    {
        return commandMessage(event, SETUP, (command, ev) -> {

            log.info("Creating channel for server");
            String serverId = event.getGuildId().get().asString();

            String channelId = Commander.getArgument(command, ev.getMessage().getContent(), "channel").orElse(null);
            if (channelId != null)
            {
                GuildChannel channel = ev.getGuild().flatMap(guild -> guild.getChannelById(Snowflake.of(channelId))).block();
                log.info("Created set to ", channel.getName());
                Install installForServer = installsRepo.getInstallForServer(serverId);

                if (installForServer != null)
                {
                    installForServer.setChannelId(channel.getId().asString());
                    installsRepo.updateInstall(installForServer);
                }
                else
                { installsRepo.updateInstall(new Install(serverId, channel.getId().asString())); }

                sendMessage(ev.getMessage().getChannel(), serverId, "channel_set_success", channel.getName());
                return;
            }

            if (installsRepo.installExists(serverId))
            {
                sendMessage(ev.getMessage().getChannel(), serverId, "channel_set_fail");
                return;
            }

            event.getMessage().getGuild().flatMap(e -> {
                log.info("Created channel for server {}", e.getId().asString());
                return e.createTextChannel(spec ->
                    spec.setTopic("Scrim recaps")
                        .setName("angel-scrim-recap")
                        .setReason("New channel for Scrim")
                );
            })
                .flatMap(e -> {
                    log.info(getLocaleString(serverId, "channel_set_success", e.getId().asString()));
                    installsRepo.updateInstall(new Install(event.getGuildId().get().asString(), e.getId().asString()));
                    return Mono.empty();
                })
                .block();

        });
    }

    public static Mono<Void> onSetupLang(MessageCreateEvent event)
    {
        return setupSetting(event, SETUP_LANGUAGE, "lang", (Install::setLang));
    }

    public static Mono<Void> onSetupDelay(MessageCreateEvent event)
    {
        return setupSetting(event, SETUP_DELAY, "delay", (Install::setVoteDelay));
    }

    private static Mono<Void> setupSetting(MessageCreateEvent event, Commands comm, String argname, BiConsumer<Install, String> action)
    {
        return commandMessage(event, comm, (command, e) -> {
            String serverId = e.getGuildId().get().asString();
            String argument = Commander.getMandatoryArgument(command, e.getMessage(), argname);

            Install installForServer = installsRepo.getInstallForServer(serverId);
            if (installForServer == null)
            {
                sendMessage(e.getMessage().getChannel(), null, "server_not_setup");
                return;
            }

            action.accept(installForServer, argument);

            installsRepo.updateInstall(installForServer);
            e.getMessage().addReaction(ReactionEmoji.unicode("✅")).block();
        });
    }

    public static Mono<Void> onHelp(MessageCreateEvent event)
    {
        return commandMessage(event, HELP, (command, ev) -> {
            StringBuilder sb = new StringBuilder();
            String serverId = ev.getGuildId().get().asString();
            Optional<String> category = Commander.getArgument(command, ev.getMessage().getContent(), "category");
            sb.append("\n");

            if (category.isEmpty() || category.get().equals("setup"))
            {
                sb.append(getLocaleString(serverId, "setup_category"));
                getCommandsDescription(Arrays.asList(Setup.values()), serverId, sb);
            }
            if (category.isEmpty() || category.get().equals("team"))
            {
                sb.append(getLocaleString(serverId, "team_category"));
                getCommandsDescription(Arrays.asList(Team.values()), serverId, sb);
            }
            if (category.isEmpty() || category.get().equals("recap"))
            {
                sb.append(getLocaleString(serverId, "recap_category"));
                getCommandsDescription(Arrays.asList(Recap.values()), serverId, sb);
            }
            if (category.isEmpty() || category.get().equals("rankings"))
            {
                sb.append(getLocaleString(serverId, "rankings_category"));
                getCommandsDescription(Arrays.asList(Rankings.values()), serverId, sb);
            }

            sendMessage(event.getMessage().getChannel(), serverId, "help_intro", sb.toString());
        });

    }

    private static void getCommandsDescription(List<Commands> commands, String serverId, StringBuilder sb)
    {
        commands.forEach(e -> {
            sb.append(" - ")
                .append(e.getCommand());
            e.getArguments().forEach(f -> sb.append(" ").append(f.toString()));
            sb.append(": ")
                .append(getLocaleString(serverId, e.getName()))
                .append('\n');
        });
    }


}
