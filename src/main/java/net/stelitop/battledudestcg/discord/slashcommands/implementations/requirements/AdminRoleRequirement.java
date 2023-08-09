package net.stelitop.battledudestcg.discord.slashcommands.implementations.requirements;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import net.stelitop.battledudestcg.discord.DiscordBotSettings;
import net.stelitop.battledudestcg.discord.slashcommands.base.requirements.CommandRequirementExecutor;
import net.stelitop.battledudestcg.discord.slashcommands.base.requirements.ConditionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class AdminRoleRequirement implements CommandRequirementExecutor {

    @Autowired
    private DiscordBotSettings discordBotSettings;

    @Override
    public ConditionResult verify(ChatInputInteractionEvent event) {
        long userId = event.getInteraction().getUser().getId().asLong();
        boolean isAdmin = Arrays.stream(discordBotSettings.getAdminUsers()).anyMatch(x -> x == userId);
        if (isAdmin) return ConditionResult.success();
        else return ConditionResult.fail("You must be an admin to use this command!");
    }
}
