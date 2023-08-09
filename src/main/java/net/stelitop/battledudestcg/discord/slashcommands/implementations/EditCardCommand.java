package net.stelitop.battledudestcg.discord.slashcommands.implementations;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import net.stelitop.battledudestcg.discord.slashcommands.base.definition.CommandComponent;
import net.stelitop.battledudestcg.discord.slashcommands.base.definition.CommandEvent;
import net.stelitop.battledudestcg.discord.slashcommands.base.definition.SlashCommand;
import net.stelitop.battledudestcg.discord.slashcommands.base.definition.params.CommandParam;
import net.stelitop.battledudestcg.discord.slashcommands.implementations.requirements.RequireAdmin;
import net.stelitop.battledudestcg.discord.ui.CardInfoUI;
import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import net.stelitop.battledudestcg.game.database.repositories.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.util.Optional;

@CommandComponent
public class EditCardCommand {

    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private CardInfoUI cardInfoUI;

    @RequireAdmin
    @SlashCommand(
            name = "edit card",
            description = "Admin command for editing the information on a card."
    )
    public Mono<Void> editCardCommand(
            @CommandEvent ChatInputInteractionEvent event,
            @CommandParam(name = "name", description = "The name of the card") String cardName
    ) {
        Optional<Card> cardOpt = cardRepository.findByNameIgnoreCase(cardName);
        if (cardOpt.isEmpty()) {
            return event.reply("No card by the name " + cardName + " found")
                    .withEphemeral(true);

        }
        Card card = cardOpt.get();
        var cardMessage = cardInfoUI.getCardInfoEmbed(card);
        return event.reply()
                .withContent(cardMessage.content())
                .withEmbeds(cardMessage.embeds())
                .withEphemeral(true);
    }
}