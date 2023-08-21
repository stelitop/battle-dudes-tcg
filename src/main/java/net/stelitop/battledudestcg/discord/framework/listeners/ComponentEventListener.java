package net.stelitop.battledudestcg.discord.framework.listeners;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ComponentInteractionEvent;
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import lombok.Builder;
import lombok.ToString;
import net.stelitop.battledudestcg.discord.framework.components.ComponentInteraction;
import net.stelitop.battledudestcg.discord.framework.DiscordEventsComponent;
import net.stelitop.battledudestcg.discord.framework.InteractionEvent;
import net.stelitop.battledudestcg.discord.framework.convenience.EventUser;
import net.stelitop.battledudestcg.discord.framework.convenience.EventUserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ComponentEventListener implements ApplicationRunner {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private GatewayDiscordClient client;
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Loaded data for the classes that can contain the implementation.
     */
    private Map<Class<? extends ComponentInteractionEvent>, List<ImplementationEntry>> methods;

    @Builder
    @ToString
    private static class ImplementationEntry {
        Object bean;
        String regex;
        Method method;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        loadBeans();
        client.on(ButtonInteractionEvent.class, this::mapEvent).subscribe();
        client.on(SelectMenuInteractionEvent.class, this::mapEvent).subscribe();
        client.on(ModalSubmitInteractionEvent.class, this::mapEvent).subscribe();
    }

    private void loadBeans() {
        this.methods = new HashMap<>();
        this.methods.put(ButtonInteractionEvent.class, new ArrayList<>());
        this.methods.put(SelectMenuInteractionEvent.class, new ArrayList<>());
        this.methods.put(ModalSubmitInteractionEvent.class, new ArrayList<>());

        Collection<Object> beans = applicationContext.getBeansWithAnnotation(DiscordEventsComponent.class).values();
        for (var bean : beans) {
            List<Method> methods = Arrays.stream(bean.getClass().getMethods())
                    .filter(x -> x.isAnnotationPresent(ComponentInteraction.class))
                    .toList();

            for (var method : methods) {
                ComponentInteraction annotation = method.getAnnotation(ComponentInteraction.class);
                this.methods.get(annotation.event()).add(ImplementationEntry.builder()
                        .bean(bean)
                        .method(method)
                        .regex(annotation.regex())
                        .build());
            }
        }
    }

    private Mono<Void> mapEvent(ComponentInteractionEvent event) {
        List<ImplementationEntry> possibleEntries = methods.getOrDefault(event.getClass(), null);
        if (possibleEntries == null) {
            LOGGER.error("Event type " + event.getClass().getName() + " not supported!");
            return Mono.empty();
        }
        String eventId = event.getCustomId();
        List<ImplementationEntry> matches = possibleEntries.stream()
                .filter(x -> eventId.matches(x.regex))
                .toList();
        if (matches.isEmpty()) {
            LOGGER.error("No declared interaction matched id \"" + eventId + "\" of event type " + event.getClass() + "!");
            return Mono.empty();
        }
        if (matches.size() > 1) {
            String errorMsg = matches.stream()
                    .map(x -> "Method \"" + x.method.getName() + "\" in class \"" + x.bean.getClass().getName() + "\"!")
                    .collect(Collectors.joining("\n"));
            LOGGER.error("Multiple interactions match the id \"" + eventId + "\"!\n" + errorMsg);
            return Mono.empty();
        }

        return executeEvent(event, matches.get(0));
    }

    private Mono<Void> executeEvent(ComponentInteractionEvent event, ImplementationEntry imp) {
        Parameter[] parameters = imp.method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            args[i] = mapParam(event, imp, parameters[i]);
        }

        try {
            Object result = imp.method.invoke(imp.bean, args);
            return (Mono<Void>) result;
        } catch (IllegalAccessException | InvocationTargetException e) {
            String name = "Method \"" + imp.method.getName() + "\" in class \"" + imp.bean.getClass().getName() + "\"";
            LOGGER.error(name + " had a problem during invoking!");
            throw new RuntimeException(e);
        } catch (ClassCastException e) {
            String name = "Method \"" + imp.method.getName() + "\" in class \"" + imp.bean.getClass().getName() + "\"";
            LOGGER.error(name + "'s result could not be cast to Mono<Void>. Check method signature.");
            return event.reply("Could not cast result of slash command.")
                    .withEphemeral(true);
        }
    }

    private Object mapParam(ComponentInteractionEvent event, ImplementationEntry imp, Parameter param) {
        if (param.isAnnotationPresent(InteractionEvent.class)) return event;
        if (param.isAnnotationPresent(EventUser.class)) return event.getInteraction().getUser();
        if (param.isAnnotationPresent(EventUserId.class)) return event.getInteraction().getUser().getId().asLong();
        return null;
    }
}
