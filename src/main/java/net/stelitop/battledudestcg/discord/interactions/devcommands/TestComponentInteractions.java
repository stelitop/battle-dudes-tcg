package net.stelitop.battledudestcg.discord.interactions.devcommands;

import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import net.stelitop.battledudestcg.discord.framework.components.ComponentInteraction;
import net.stelitop.battledudestcg.discord.framework.DiscordEventsComponent;
import net.stelitop.battledudestcg.discord.framework.InteractionEvent;
import net.stelitop.battledudestcg.discord.framework.commands.SlashCommand;
import net.stelitop.battledudestcg.discord.interactions.requirements.RequireAdmin;
import reactor.core.publisher.Mono;

@DiscordEventsComponent
public class TestComponentInteractions {

    @RequireAdmin
    @SlashCommand(
            name = "test command",
            description = "Test."
    ) public Mono<Void> testCommnad(
            @InteractionEvent ChatInputInteractionEvent event
    ) {
        return event.reply("hello")
                .withEphemeral(true)
                .withComponents(ActionRow.of(Button.primary("test|test", "Click me!")));
    }

    @ComponentInteraction(
            event = ButtonInteractionEvent.class,
            regex = "test\\|test"
    ) public Mono<Void> testInteraction(
            @InteractionEvent ButtonInteractionEvent event
    ) {
        return event.edit()
                .withContent("Amogus!");
    }
}
