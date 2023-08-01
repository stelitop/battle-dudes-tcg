package net.stelitop.battledudestcg.discord.slashcommands.implementations;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.User;
import net.stelitop.battledudestcg.discord.slashcommands.annotations.*;
import net.stelitop.battledudestcg.game.services.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.util.Optional;

@CommandComponent
public class CollectionCommands {

    @Autowired
    private UserProfileService userProfileService;

    @SlashCommand(
            name = "coins",
            description = "Gets the coins a user has. By default it shows your coins."
    )
    public Mono<Void> getUserCoins(
            @CommandEvent ChatInputInteractionEvent event,
            @OptionalCommandParam(
                    name = "user",
                    description = "User to check the coins of. Defaults to self",
                    type = User.class
            ) Optional<User> userOpt
    ) {
        User user = userOpt.orElse(event.getInteraction().getUser());
        String username = user.getUsername();
        int coins = userProfileService.getProfile(user.getId().asLong()).getUserCollection().getCoins();
        return event.reply()
                .withContent(username + " has " + coins + " coins!");
    }

    @SlashCommand(
            name = "test choicecommand",
            description = "Boaty"
    )
    public Mono<Void> testOptionsCommand(
            @CommandEvent ChatInputInteractionEvent event,
            @OptionalCommandParam(
                    name = "param",
                    description = "Test param",
                    type = Long.class,
                    choices = {
                            @CommandParamChoice(name = "Frog", value = "1"),
                            @CommandParamChoice(name = "Bear", value = "2")
                    }
            ) Optional<Long> myParam
    ) {
        return event.reply()
                .withContent(myParam.orElse(-123L).toString())
                .withEphemeral(true);
    }
}
