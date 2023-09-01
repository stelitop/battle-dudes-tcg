package net.stelitop.battledudestcg.discord.interactions.requirements;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import net.stelitop.battledudestcg.discord.DiscordBotSettings;
import net.stelitop.mad4j.requirements.CommandRequirementExecutor;
import net.stelitop.mad4j.utils.ActionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class AdminUserRequirement implements CommandRequirementExecutor {

    @Autowired
    private DiscordBotSettings discordBotSettings;

    @Override
    public ActionResult<Void> verify(ChatInputInteractionEvent event) {
        long userId = event.getInteraction().getUser().getId().asLong();
        boolean isAdmin = Arrays.stream(discordBotSettings.getAdminUsers()).anyMatch(x -> x == userId);
        if (isAdmin) return ActionResult.success();
        else return ActionResult.fail("You must be an admin to use this command!");
    }
}
