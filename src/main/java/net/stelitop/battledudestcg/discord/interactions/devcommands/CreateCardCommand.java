package net.stelitop.battledudestcg.discord.interactions.devcommands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.MessageCreateSpec;
import net.stelitop.battledudestcg.discord.framework.DiscordEventsComponent;
import net.stelitop.battledudestcg.discord.framework.InteractionEvent;
import net.stelitop.battledudestcg.discord.framework.commands.*;
import net.stelitop.battledudestcg.discord.interactions.requirements.RequireAdmin;
import net.stelitop.battledudestcg.discord.ui.EditCardUI;
import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import net.stelitop.battledudestcg.game.database.entities.cards.DudeCard;
import net.stelitop.battledudestcg.game.database.entities.cards.ItemCard;
import net.stelitop.battledudestcg.game.database.entities.cards.WarpCard;
import net.stelitop.battledudestcg.game.database.repositories.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

@DiscordEventsComponent
public class CreateCardCommand {

    public static final String DUDE_DEFAULT_ART = "https://i.imgur.com/X2ffUWK.png";
    public static final String ITEM_DEFAULT_ART = "https://i.imgur.com/MiV8igJ.png";
    public static final String WARP_DEFAULT_ART = "https://i.imgur.com/EF09Cxd.png";

    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private EditCardUI editCardUI;

    @RequireAdmin
    @SlashCommand(
            name = "dev create card",
            description = "Creates a new default card."
    )
    public Mono<Void> createCardCommand(
            @InteractionEvent ChatInputInteractionEvent event,
            @CommandParam(
                    name = "type",
                    description = "The card type of the card.",
                    choices = {
                            @CommandParamChoice(name = "Dude", value = "dude"),
                            @CommandParamChoice(name = "Item", value = "item"),
                            @CommandParamChoice(name = "Warp", value = "warp"),
                    }
            ) String cardType,
            @CommandParam(
                    name = "cardname",
                    description = "The name of the card"
            ) String cardName
    ) {
        if (cardRepository.findByNameIgnoreCase(cardName).isPresent()) {
            return event.reply("There already is a card with this name!")
                    .withEphemeral(true);
        }

        Card newCard = switch (cardType) {
            case "dude" -> DudeCard.builder().name(cardName).artUrl(DUDE_DEFAULT_ART).build();
            case "item" -> ItemCard.builder().name(cardName).artUrl(ITEM_DEFAULT_ART).build();
            case "warp" -> WarpCard.builder().name(cardName).artUrl(WARP_DEFAULT_ART).build();
            default -> null;
        };
        if (newCard == null) {
            return event.reply("Could not parse the card type!")
                    .withEphemeral(true);
        }
        Card savedCard = cardRepository.save(newCard);
        MessageCreateSpec msg = editCardUI.getEditCardMessage(savedCard);
        return event.reply()
                .withContent(msg.content())
                .withEmbeds(msg.embeds())
                .withComponents(msg.components())
                .withEphemeral(true);
    }
}
