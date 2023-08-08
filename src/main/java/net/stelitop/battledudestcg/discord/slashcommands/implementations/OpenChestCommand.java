package net.stelitop.battledudestcg.discord.slashcommands.implementations;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.MessageCreateSpec;
import net.stelitop.battledudestcg.discord.slashcommands.annotations.CommandComponent;
import net.stelitop.battledudestcg.discord.slashcommands.annotations.CommandEvent;
import net.stelitop.battledudestcg.discord.slashcommands.annotations.CommandParam;
import net.stelitop.battledudestcg.discord.slashcommands.annotations.SlashCommand;
import net.stelitop.battledudestcg.discord.ui.ChestOpeningUI;
import net.stelitop.battledudestcg.game.database.entities.chests.Chest;
import net.stelitop.battledudestcg.game.database.entities.profile.collection.ChestOwnership;
import net.stelitop.battledudestcg.game.database.repositories.ChestOwnershipRepository;
import net.stelitop.battledudestcg.game.database.repositories.ChestRepository;
import net.stelitop.battledudestcg.game.services.ChestOwnershipService;
import net.stelitop.battledudestcg.game.services.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.util.Optional;

@CommandComponent
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
            @CommandEvent ChatInputInteractionEvent event,
            @CommandParam(name = "name", description = "The name of the chest") String name
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
