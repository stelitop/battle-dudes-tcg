package net.stelitop.battledudestcg.discord.listeners;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.stelitop.battledudestcg.discord.slashcommands.OptionType;
import net.stelitop.battledudestcg.discord.slashcommands.annotations.*;
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
public class SlashCommandListener implements ApplicationRunner {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private GatewayDiscordClient client;

    private List<SlashCommandEntry> slashCommands;

    @Override
    public void run(ApplicationArguments args) {
        Collection<Object> commandBeans = applicationContext.getBeansWithAnnotation(CommandComponent.class).values();

        slashCommands = new ArrayList<>();
        for (var bean : commandBeans) {
            var slashCommandMethods = Arrays.stream(bean.getClass().getMethods())
                    .filter(x -> x.isAnnotationPresent(SlashCommand.class))
                    .toList();

            for (var method : slashCommandMethods) {
                var newEntry = new SlashCommandEntry();
                SlashCommand slashCommandAnnotation = method.getAnnotation(SlashCommand.class);
                newEntry.method = method;
                newEntry.name = slashCommandAnnotation.name().toLowerCase();
                newEntry.sourceBean = bean;
                slashCommands.add(newEntry);
            }
        }

        client.on(ChatInputInteractionEvent.class, this::handle).subscribe();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    private static class SlashCommandEntry {
        private Object sourceBean;
        private Method method;
        private String name;
    }

    private Mono<Void> handle(ChatInputInteractionEvent event) {
        StringBuilder commandNameBuilder = new StringBuilder(event.getCommandName().toLowerCase());
        List<ApplicationCommandInteractionOption> options = event.getOptions();
        while (options.size() == 1
                && (options.get(0).getType().getValue() == OptionType.SUB_COMMAND
                || options.get(0).getType().getValue() == OptionType.SUB_COMMAND_GROUP)) {

            commandNameBuilder.append(" ").append(options.get(0).getName().toLowerCase());
            options = options.get(0).getOptions();
        }
        String commandName = commandNameBuilder.toString();

        Map<String, ApplicationCommandInteractionOption> optionsMap = options.stream()
                .collect(Collectors.toMap(x -> x.getName().toLowerCase(), x -> x));

        Optional<SlashCommandEntry> command = this.slashCommands.stream()
                .filter(x -> x.name.equalsIgnoreCase(commandName))
                .findFirst();

        if (command.isEmpty()) {
            return event.reply("Could not resolve command '" + commandName + "'.")
                    .withEphemeral(true);
        }
        return invokeSlashCommand(event, optionsMap, command.get());
    }

    private Mono<Void> invokeSlashCommand(
            ChatInputInteractionEvent event,
            Map<String, ApplicationCommandInteractionOption> options,
            SlashCommandEntry command
    ) {
        Parameter[] parameters = command.method.getParameters();
        List<Object> invocationParams = Arrays.stream(parameters)
                .map(p -> mapCommandParamToMethodParam(event, options, p))
                .toList();

        try {
            Object result = command.method.invoke(command.sourceBean, invocationParams.toArray());
            return (Mono<Void>) result;
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.error(command.name + " had a problem during invoking.");
            e.printStackTrace();
            return event.reply("An error occurred invoking the slash command!")
                    .withEphemeral(true);
        } catch (ClassCastException e) {
            LOGGER.error(command.name + "'s result could not be cast to Mono<Void>. Check method signature.");
            return event.reply("Could not cast result of slash command.")
                    .withEphemeral(true);
        }
    }

    private Object mapCommandParamToMethodParam(
            ChatInputInteractionEvent event,
            Map<String, ApplicationCommandInteractionOption> options,
            Parameter param
    ) {
        if (param.isAnnotationPresent(CommandEvent.class)) {
            return event;
        }
        if (param.isAnnotationPresent(CommandParam.class)) {
            CommandParam annotation = param.getAnnotation(CommandParam.class);
            if (!options.containsKey(annotation.name().toLowerCase())) {
                return null;
            }
            ApplicationCommandInteractionOption option = options.get(annotation.name().toLowerCase());
            return getValueFromOption(option);
        }
        if (param.isAnnotationPresent(OptionalCommandParam.class)) {
            OptionalCommandParam annotation = param.getAnnotation(OptionalCommandParam.class);
            if (!options.containsKey(annotation.name().toLowerCase())) {
                return Optional.empty();
            }
            ApplicationCommandInteractionOption option = options.get(annotation.name().toLowerCase());
            Object value = getValueFromOption(option);
            if (value == null) {
                return Optional.empty();
            } else {
                return Optional.of(value);
            }
        }

        return null;
    }

    private Object getValueFromOption(ApplicationCommandInteractionOption option) {
        if (option.getValue().isEmpty()) {
            return null;
        }
        ApplicationCommandInteractionOptionValue value = option.getValue().get();

        return switch (option.getType()) {
            case BOOLEAN -> value.asBoolean();
            case INTEGER -> value.asLong();
            case STRING -> value.asString();
            case NUMBER -> value.asDouble();
            case CHANNEL -> value.asChannel().block();
            case USER -> value.asUser().block();
            case ROLE -> value.asRole().block();
            default -> null;
        };
    }

}
