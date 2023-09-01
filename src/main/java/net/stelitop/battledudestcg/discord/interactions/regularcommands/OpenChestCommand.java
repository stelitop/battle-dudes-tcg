package net.stelitop.battledudestcg.discord.interactions.regularcommands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.User;
import discord4j.core.spec.MessageCreateSpec;
import net.stelitop.mad4j.DiscordEventsComponent;
import net.stelitop.mad4j.InteractionEvent;
import net.stelitop.mad4j.commands.CommandParam;
import net.stelitop.mad4j.commands.SlashCommand;
import net.stelitop.mad4j.convenience.EventUser;
import net.stelitop.battledudestcg.discord.interactions.autocomplete.OwnedChestAutocomplete;
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
            @EventUser User user,
            @CommandParam(
                    name = "name",
                    description = "The name of the chest",
                    autocomplete = OwnedChestAutocomplete.class
            ) String name
    ) {
        Optional<ChestOwnership> chestOwnershipOpt = chestOwnershipService.getChestOwnership(
                event.getInteraction().getUser().getId().asLong(), name);
        if (chestOwnershipOpt.isEmpty() || chestOwnershipOpt.get().getCount() == 0) {
            return event.reply("You don't have a chest in your collection with this name.")
                    .withEphemeral(true);
        }
        ChestOwnership chestOwnership = chestOwnershipOpt.get();

        MessageCreateSpec message = chestOpeningUI.getMessage(chestOwnership.getChest(), user);

        return event.reply()
                .withContent(message.content())
                .withEmbeds(message.embeds())
                .withComponents(message.components());
    }
}
