package net.stelitop.battledudestcg.discord.slashcommands.implementations;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import net.stelitop.battledudestcg.discord.slashcommands.framework.definition.CommandComponent;
import net.stelitop.battledudestcg.discord.slashcommands.framework.definition.CommandEvent;
import net.stelitop.battledudestcg.discord.slashcommands.framework.definition.CommandParam;
import net.stelitop.battledudestcg.discord.slashcommands.framework.definition.SlashCommand;
import net.stelitop.battledudestcg.discord.slashcommands.implementations.autocomplete.ChestNameAutocomplete;
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

        String description = "";
        if (chest instanceof ChannelChest c) {
            Optional<Long> channelId = chestService.getChannelIdOfChest(c);
            if (channelId.isPresent()) {
                description += "Found in <#" + channelId.get() + ">\n\n";
            }
        }
        description += "*" + chest.getDescription() + "*";
        return event.reply()
                .withEmbeds(EmbedCreateSpec.builder()
                        .title(chest.getName())
                        .description(description)
                        .thumbnail(chest.getIconUrl())
                        .color(colorUtils.getChestEmbedColor())
                        .build());
    }
}
