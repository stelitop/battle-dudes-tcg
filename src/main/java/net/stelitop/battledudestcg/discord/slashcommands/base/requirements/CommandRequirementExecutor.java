package net.stelitop.battledudestcg.discord.slashcommands.base.requirements;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;

public interface CommandRequirementExecutor {

    ConditionResult verify(ChatInputInteractionEvent event);
}
