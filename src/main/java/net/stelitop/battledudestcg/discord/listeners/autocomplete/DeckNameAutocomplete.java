package net.stelitop.battledudestcg.discord.listeners.autocomplete;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.channel.Channel;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import net.stelitop.battledudestcg.discord.slashcommands.OptionType;
import net.stelitop.battledudestcg.game.database.entities.profile.collection.decks.CardDeck;
import net.stelitop.battledudestcg.game.services.DeckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component
public class DeckNameAutocomplete implements ApplicationRunner {

    @Autowired
    private GatewayDiscordClient client;
    @Autowired
    private DeckService deckService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        client.on(ChatInputAutoCompleteEvent.class, this::handle).subscribe();
    }

    private Mono<Void> handle(ChatInputAutoCompleteEvent event) {
        if (!event.getCommandName().equals("deck")
                || (!event.getOptions().get(0).getName().equals("view") &&
                    !event.getOptions().get(0).getName().equals("edit"))) return Mono.empty();

        var defaultVal = new ApplicationCommandInteractionOptionValue(client, null, OptionType.STRING, "");
        var userInput = event.getFocusedOption().getValue().orElse(defaultVal).asString().toLowerCase();

        List<ApplicationCommandOptionChoiceData> suggestions = new ArrayList<>();

        var decks = deckService.getDecksOfUser(event.getInteraction().getUser().getId().asLong());
        decks.stream()
                .map(CardDeck::getName)
                .filter(x -> x.toLowerCase().contains(userInput))
                .map(x -> ApplicationCommandOptionChoiceData.builder().name(x).value(x).build())
                .limit(25)
                .forEach(suggestions::add);

        return event.respondWithSuggestions(suggestions);
    }
}
