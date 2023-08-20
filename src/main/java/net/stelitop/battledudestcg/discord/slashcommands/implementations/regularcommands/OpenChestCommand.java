package net.stelitop.battledudestcg.discord.slashcommands.implementations.regularcommands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.MessageCreateSpec;
import net.stelitop.battledudestcg.discord.framework.definition.DiscordEventsComponent;
import net.stelitop.battledudestcg.discord.framework.definition.InteractionEvent;
import net.stelitop.battledudestcg.discord.framework.definition.CommandParam;
import net.stelitop.battledudestcg.discord.framework.definition.SlashCommand;
import net.stelitop.battledudestcg.discord.slashcommands.implementations.autocomplete.OwnedChestAutocomplete;
import net.stelitop.battledudestcg.discord.ui.ChestOpeningUI;
import net.stelitop.battledudestcg.game.database.entities.collection.ChestOwnership;
import net.stelitop.battledudestcg.game.services.ChestOwnershipService;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.util.Optional;

@DiscordEventsComponent
public class OpenChestCommand {

    @Autowired
    private ChestOwnershipService chestOwnershipService;
    @Autowired
    private ChestOpeningUI chestOpeningUI;

    @SlashCommand(
            name = "chest open",
            description = "Opens a chest from your collection."
    )
    public Mono<Void> openChest(
            @InteractionEvent ChatInputInteractionEvent event,
            @CommandParam(
                    name = "name",
                    description = "The name of the chest",
                    autocomplete = OwnedChestAutocomplete.class
            ) String name
    ) {
        Optional<ChestOwnership> chestOwnershipOpt = chestOwnershipService.getChestOwnership(
                event.getInteraction().getUser().getId().asLong(),
                name
        );
        if (chestOwnershipOpt.isEmpty()) {
            return event.reply("There is no chest with this name!")
                    .withEphemeral(true);
        }
        ChestOwnership chestOwnership = chestOwnershipOpt.get();
        if (chestOwnership.getCount() == 0) {
            return event.reply("You don't have any more of these chests!")
                    .withEphemeral(true);
        }

        MessageCreateSpec message = chestOpeningUI.getMessage(chestOwnership.getChest(), event.getInteraction().getUser());

        return event.reply()
                .withContent(message.content())
                .withEmbeds(message.embeds())
                .withComponents(message.components());
    }
}
