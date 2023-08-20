package net.stelitop.battledudestcg.discord.framework.requirements;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import net.stelitop.battledudestcg.commons.pojos.ActionResult;

public interface CommandRequirementExecutor {

    ActionResult<Void> verify(ChatInputInteractionEvent event);
}
