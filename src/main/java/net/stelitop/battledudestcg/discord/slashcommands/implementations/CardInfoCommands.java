package net.stelitop.battledudestcg.discord.slashcommands.implementations;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.MessageCreateSpec;
import net.stelitop.battledudestcg.discord.slashcommands.framework.definition.CommandComponent;
import net.stelitop.battledudestcg.discord.slashcommands.framework.definition.CommandEvent;
import net.stelitop.battledudestcg.discord.slashcommands.framework.definition.SlashCommand;
import net.stelitop.battledudestcg.discord.slashcommands.framework.definition.CommandParam;
import net.stelitop.battledudestcg.discord.ui.CardInfoUI;
import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import net.stelitop.battledudestcg.game.database.repositories.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.util.Optional;

@CommandComponent
public class CardInfoCommands {

    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private CardInfoUI cardInfoUI;

//    @SlashCommand(
//            name = "dude",
//            description = "Displays information about a dude."
//    )
//    public Mono<Void> dudeInfo(
//            @CommandEvent ChatInputInteractionEvent event,
//            @CommandParam(name = "name", description = "The name of the dude.") String name
//    ) {
//        Optional<Card> dudeOpt = cardRepository.findByNameIgnoreCase(name);
//        if (dudeOpt.isEmpty() || !(dudeOpt.get() instanceof DudeCard dude)) {
//            return event.reply()
//                    .withContent("No matching dudes!")
//                    .withEphemeral(true);
//        }
//
//        EmbedCreateSpec embed = getDudeInfoEmbed(dude);
//        return event.reply()
//                .withEmbeds(embed);
//    }

    @SlashCommand(
            name = "info card",
            description = "Displays information about a card."
    )
    public Mono<Void> cardInfo(
            @CommandEvent ChatInputInteractionEvent event,
            @CommandParam(name = "name", description = "The name of the card.") String name
    ) {
        Optional<Card> cardOpt = cardRepository.findByNameIgnoreCase(name);
        if (cardOpt.isEmpty()) {
            return event.reply("No matching cards!")
                    .withEphemeral(true);
        }
        Card card = cardOpt.get();
        MessageCreateSpec message = cardInfoUI.getCardInfoMessage(card);
        return event.reply()
                .withContent(message.content())
                .withEmbeds(message.embeds())
                .withComponents(message.components());
    }
}
