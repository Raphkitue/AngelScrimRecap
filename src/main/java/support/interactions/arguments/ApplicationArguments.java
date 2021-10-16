package support.interactions.arguments;

import app.DependenciesContainer;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.rest.util.ApplicationCommandOptionType;
import model.commands.Argument;
import model.commands.Commands;
import org.javatuples.Pair;
import reactor.util.Logger;
import reactor.util.Loggers;
import repository.rankings.IRankingsRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ApplicationArguments {

    private static final Logger log = Loggers.getLogger(ApplicationArguments.class);

    private static final IRankingsRepository rankingsRepo = DependenciesContainer.getInstance().getRankingsRepo();

    public static List<ApplicationCommandOptionData> getOptions(Commands command) {
        return getOptions(command, new HashMap<>());
    }

    public static List<ApplicationCommandOptionData> getOptions(Commands command, Map<Argument, Object> specificData) {
        return command.getArguments().stream()
            .map(arg -> ApplicationCommandOptionData.builder()
                .type(getArgumentType(arg.getArgumentType()))
                .name(arg.getName())
                .description(arg.getDescription())
                .choices(getArgumentOptions(arg.getArgumentType(), specificData.getOrDefault(arg, null)))
                .required(arg.getNecessity().isRequired())
                .build())
            .collect(Collectors.toList());
    }

    private static int getArgumentType(Argument.ArgumentType argumentType) {
        ApplicationCommandOptionType type;
        switch (argumentType) {
            case USERNAME:
                type = ApplicationCommandOptionType.MENTIONABLE;
                break;
            case CHANNEL:
                type = ApplicationCommandOptionType.CHANNEL;
                break;
            case STRING:
            case BATTLETAG:
            case SPECIFIC:
            case EXISTING_RANKING:
            default:
                type = ApplicationCommandOptionType.STRING;
                break;
        }
        return type.getValue();
    }

    private static List<ApplicationCommandOptionChoiceData> getArgumentOptions(Argument.ArgumentType argumentType, Object specificData) {
        switch (argumentType) {
            case EXISTING_RANKING:
                return getExistingLeaderboardsChoices((String) specificData);
            case SPECIFIC:
                return getSpecificChoices((List<Pair<String, String>>) specificData);
            case BATTLETAG:
            case USERNAME:
            case CHANNEL:
            case STRING:
            default:
                return new ArrayList<>();
        }
    }

    private static List<ApplicationCommandOptionChoiceData> getSpecificChoices(List<Pair<String,String>> choices) {
        return choices.stream()
            .map(choice -> ApplicationCommandOptionChoiceData.builder()
                .value(choice.getValue0())
                .name(choice.getValue1())
                .build())
            .collect(Collectors.toList());
    }

    private static List<ApplicationCommandOptionChoiceData> getExistingLeaderboardsChoices(String guildId) {
        return rankingsRepo.getRankingsForServer(guildId).stream()
            .map(chann -> ApplicationCommandOptionChoiceData.builder()
                .value(chann.getValue0())
                .name("#" + chann.getValue1())
                .build())
            .collect(Collectors.toList());
    }
}
