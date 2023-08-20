package net.stelitop.battledudestcg.discord.framework.components;

import discord4j.core.event.domain.interaction.ComponentInteractionEvent;

import javax.annotation.RegEx;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentInteraction {
    Class<? extends ComponentInteractionEvent> event();
    @RegEx
    String regex();
}
