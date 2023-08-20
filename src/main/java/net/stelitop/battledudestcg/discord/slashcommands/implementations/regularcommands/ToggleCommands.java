package net.stelitop.battledudestcg.discord.slashcommands.implementations.regularcommands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.User;
import net.stelitop.battledudestcg.discord.framework.definition.DiscordEventsComponent;
import net.stelitop.battledudestcg.discord.framework.definition.InteractionEvent;
import net.stelitop.battledudestcg.discord.framework.definition.CommandParam;
import net.stelitop.battledudestcg.discord.framework.definition.SlashCommand;
import net.stelitop.battledudestcg.game.database.entities.profile.UserProfile;
import net.stelitop.battledudestcg.game.services.UserProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

@DiscordEventsComponent
public class ToggleCommands {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserProfileService userProfileService;

    @SlashCommand(
            name = "istoggled",
            description = "Check whether or not a given user is currently opted in to the game."
    )
    public Mono<Void> isToggledCommand(
            @InteractionEvent ChatInputInteractionEvent event,
            @CommandParam(
                    name = "user",
                    description = "The user you want to check. Yourself by default.",
                    required = false
            ) User userOpt
    ) {
        User user = userOpt == null ? event.getInteraction().getUser() : userOpt;
        long userId = user.getId().asLong();
        String username = user.getUsername();
        boolean isParticipating = userProfileService.getProfile(userId).getUserSettings().isParticipating();

        if (isParticipating) {
            return event.reply(username + " is currently opted in to the game. They will randomly collect chests when sending messages in this server.");
        } else {
            return event.reply(username + " is currently opted out of the game. They will not collect chests when sending messages in this server.");
        }
    }

    @SlashCommand(
            name = "toggle",
            description = "Toggles whether you're participating in the game. Toggles getting drops and notifications."
    )
    public Mono<Void> togglePlayer(
            @InteractionEvent ChatInputInteractionEvent event
    ) {
        User user = event.getInteraction().getUser();
        long userId = user.getId().asLong();
        UserProfile profile = userProfileService.toggleParticipation(userId);
        boolean newToggledState = profile.getUserSettings().isParticipating();
        String message = newToggledState ?
                "You are now participating in the game! You will now randomly collect Dudes when sending messages in the server. If you want to stop participating, use this command again." :
                "You are no longer participating in the game! You will no longer collect Dudes when sending messages in the server. If you want to participate, use this command again.";
        LOGGER.debug(user.getUsername() + " has set their participation to: " + newToggledState);

        return event.reply(message)
                .withEphemeral(true);
    }
}
