package net.stelitop.battledudestcg.discord.slashcommands.implementations.requirements;

import net.stelitop.battledudestcg.discord.slashcommands.base.requirements.CommandRequirement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@CommandRequirement(implementation = AdminRoleRequirement.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAdmin {

}
