package net.stelitop.battledudestcg.discord.listeners.autocomplete;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.AutoCompleteInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.command.ApplicationCommandOptionChoice;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ImmutableApplicationCommandInteractionOptionData;
import net.stelitop.battledudestcg.discord.slashcommands.OptionType;
import net.stelitop.battledudestcg.game.database.repositories.ChestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component
public class ChestAutocomplete implements ApplicationRunner {

    @Autowired
    private GatewayDiscordClient client;
    @Autowired
    private ChestRepository chestRepository;

    private final List<String> chestNames = new ArrayList<>();

    @Override
    public void run(ApplicationArguments args) throws Exception {
        fillPossibleChestNames();
        client.on(ChatInputAutoCompleteEvent.class, this::handle).subscribe();
    }

    private void fillPossibleChestNames() {
        chestNames.clear();
        chestNames.addAll(chestRepository.getAllChestNames());
    }

    public Mono<Void> handle(ChatInputAutoCompleteEvent event) {

        if (!event.getCommandName().equals("info")
            || !event.getOptions().get(0).getName().equals("chest")) return Mono.empty();

        var defaultVal = new ApplicationCommandInteractionOptionValue(client, null, OptionType.STRING, "");
        var userInput = event.getFocusedOption().getValue().orElse(defaultVal).asString().toLowerCase();

        List<ApplicationCommandOptionChoiceData> suggestions = new ArrayList<>();

        chestNames.stream()
                .filter(x -> x.toLowerCase().contains(userInput))
                .map(x -> ApplicationCommandOptionChoiceData.builder().name(x).value(x).build())
                .limit(25)
                .forEach(suggestions::add);

        return event.respondWithSuggestions(suggestions);
    }
}
