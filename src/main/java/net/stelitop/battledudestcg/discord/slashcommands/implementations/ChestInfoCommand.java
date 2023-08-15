package net.stelitop.battledudestcg.discord.slashcommands.implementations;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import net.stelitop.battledudestcg.discord.slashcommands.framework.definition.CommandComponent;
import net.stelitop.battledudestcg.discord.slashcommands.framework.definition.CommandEvent;
import net.stelitop.battledudestcg.discord.slashcommands.framework.definition.CommandParam;
import net.stelitop.battledudestcg.discord.slashcommands.framework.definition.SlashCommand;
import net.stelitop.battledudestcg.discord.slashcommands.implementations.autocomplete.ChestNameAutocomplete;
import net.stelitop.battledudestcg.discord.ui.ChestInfoUI;
import net.stelitop.battledudestcg.discord.utils.ColorUtils;
import net.stelitop.battledudestcg.game.database.entities.chests.ChannelChest;
import net.stelitop.battledudestcg.game.database.entities.chests.Chest;
import net.stelitop.battledudestcg.game.services.ChestService;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.util.Optional;

@CommandComponent
public class ChestInfoCommand {

    @Autowired
    private ChestService chestService;
    @Autowired
    private ColorUtils colorUtils;
    @Autowired
    private ChestInfoUI chestInfoUI;

    // TODO: Add chest names autofill.
    @SlashCommand(
        name = "info chest",
        description = "Displays info about a chest."
    )
    public Mono<Void> chestInfoCommand(
            @CommandEvent ChatInputInteractionEvent event,
            @CommandParam(
                    name = "name",
                    description = "The name of the chest.",
                    autocomplete = ChestNameAutocomplete.class
            ) String name
    ) {
        Chest chest = chestService.getChest(name);
        if (chest == null) {
            return event.reply("There is no chests of this name!")
                    .withEphemeral(true);
        }

        var msg = chestInfoUI.getChestInfoMessage(ChestInfoUI.Model.builder()
                .page(1)
                .chestId(chest.getChestId())
                .userId(event.getInteraction().getUser().getId().asLong())
                .build());

        return event.reply()
                .withContent(msg.content())
                .withEmbeds(msg.embeds())
                .withComponents(msg.components());
    }
}
