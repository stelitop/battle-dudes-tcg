package net.stelitop.battledudestcg.discord.interactions.devcommands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import net.stelitop.mad4j.DiscordEventsComponent;
import net.stelitop.mad4j.InteractionEvent;
import net.stelitop.mad4j.commands.SlashCommand;
import net.stelitop.mad4j.commands.CommandParam;
import net.stelitop.battledudestcg.discord.interactions.autocomplete.CardNameAutocomplete;
import net.stelitop.battledudestcg.discord.interactions.requirements.RequireAdmin;
import net.stelitop.battledudestcg.discord.ui.EditCardUI;
import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import net.stelitop.battledudestcg.game.database.repositories.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.util.Optional;

@DiscordEventsComponent
public class EditCardCommand {

    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private EditCardUI editCardUI;

    @RequireAdmin
    @SlashCommand(
            name = "dev edit card",
            description = "Admin command for editing the information on a card."
    )
    public Mono<Void> editCardCommand(
            @InteractionEvent ChatInputInteractionEvent event,
            @CommandParam(
                    name = "name",
                    description = "The name of the card. Allows for name queries.",
                    autocomplete = CardNameAutocomplete.class
            ) String cardName
    ) {
        Optional<Card> cardOpt = cardRepository.findByNameIgnoreCase(cardName);
        if (cardOpt.isEmpty()) {
            return event.reply("No card by the name " + cardName + " found")
                    .withEphemeral(true);

        }
        Card card = cardOpt.get();
        var cardMessage = editCardUI.getEditCardMessage(card);
        return event.reply()
                .withContent(cardMessage.content())
                .withEmbeds(cardMessage.embeds())
                .withComponents(cardMessage.components())
                .withEphemeral(true);
    }
}
