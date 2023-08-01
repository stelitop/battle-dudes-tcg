package net.stelitop.battledudestcg.discord.slashcommands;

import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import lombok.AllArgsConstructor;
import lombok.ToString;
import net.stelitop.battledudestcg.discord.slashcommands.annotations.*;
import org.reflections.Reflections;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class SlashCommandRegistrar implements ApplicationRunner {

    private final String PACKAGE_NAME = "net.stelitop.battledudestcg.discord.slashcommands";
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RestClient restClient;

    @Override
    public void run(ApplicationArguments args) {
        var reflections = new Reflections(PACKAGE_NAME);
        var slashCommandClasses = reflections.getTypesAnnotatedWith(CommandComponent.class);
        var slashCommandMethods = slashCommandClasses.stream()
                .map(Class::getMethods)
                .flatMap(Arrays::stream)
                .filter(m -> m.isAnnotationPresent(SlashCommand.class))
                .toList();

        String missingCommandEventAnnotation = slashCommandMethods.stream()
                .filter(x -> Arrays.stream(x.getParameters()).noneMatch(y -> y.isAnnotationPresent(CommandEvent.class)))
                .map(Method::getName)
                .collect(Collectors.joining(", "));
        if (!missingCommandEventAnnotation.isBlank()) {
            throw new RuntimeException("Following commands don't have a command event: " + missingCommandEventAnnotation);
        }

        var slashCommandRequests = createCommandRequestsFromMethods(slashCommandMethods);

        var applicationService = restClient.getApplicationService();
        long applicationId = restClient.getApplicationId().block();

        LOGGER.info("Started registering global commands...");
        applicationService.bulkOverwriteGlobalApplicationCommand(applicationId, slashCommandRequests)
                .doOnNext(c -> LOGGER.info("Successfully registered command " + c.name() + "."))
                .doOnError(e -> LOGGER.error("Failed to register global commands.", e))
                .doOnComplete(() -> LOGGER.info("Finished registering global commands."))
                .subscribe();
    }

    /**
     * Creates the application command requests to send to discord for creating the
     * blueprints of the slash commands. They are created from taking all methods
     * annotated with {@link SlashCommand} and reading their content. From the names
     * of the commands a tree is created to group commands that have the same first
     * names.
     *
     * @param methods List of methods annotated with {@link SlashCommand}.
     * @return A list of application command requests. Every request is about a
     *     different command.
     */
    private List<ApplicationCommandRequest> createCommandRequestsFromMethods(List<Method> methods) {

        List<CommandTreeNode> trees = createCommandNameTrees(methods);

        return trees.stream()
                .map(this::processSlashCommandTree)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Creates a tree hierarchy from slash command methods. The names of the
     * slash commands are space-separated strings, each being a different
     * level of nesting in the final command.
     *
     * @param methods The methods of the commands, each annotated with
     *     {@link SlashCommand}.
     * @return A list of command tree nodes, where each node has a different
     *     first command name.
     */
    private List<CommandTreeNode> createCommandNameTrees(List<Method> methods) {
        CommandTreeNode shadowRoot = new CommandTreeNode("", new ArrayList<>(), null);
        for (var method : methods) {
            SlashCommand scAnnotation = method.getAnnotation(SlashCommand.class);
            if (scAnnotation == null) {
                continue;
            }
            String[] parts = scAnnotation.name().toLowerCase().split(" ");
            if (parts.length == 0) {
                continue;
            }

            CommandTreeNode currentNode = shadowRoot;
            for (String partName : parts) {
                if (currentNode.children.stream().noneMatch(x -> x.name.equals(partName))) {
                    currentNode.children.add(new CommandTreeNode(partName, new ArrayList<>(), null));
                }
                currentNode = currentNode.children.stream().filter(x -> x.name.equals(partName)).findFirst().get();
            }
            currentNode.method = method;
        }

        return shadowRoot.children;
    }

    /**
     * Helper class to represent a node in the tree structure of commands.
     */
    @AllArgsConstructor
    @ToString
    private static class CommandTreeNode {
        public String name;
        public List<CommandTreeNode> children;
        public Method method;
    }

    /**
     * Transforms a tree of commands into a single application command request. The
     * tree is recursively parsed to explore all branches.
     *
     * @param tree The slash command tree.
     * @return An application command request that can be directly sent.
     */
    private ApplicationCommandRequest processSlashCommandTree(CommandTreeNode tree) {
        var requestBuilder = ApplicationCommandRequest.builder();
        requestBuilder.name(tree.name);
        if (tree.method != null) {
            var annotation = tree.method.getAnnotation(SlashCommand.class);
            if (annotation == null) {
                return null;
            }
            requestBuilder.description(annotation.description());
            requestBuilder.addAllOptions(getOptionsFromMethod(tree.method));
            return requestBuilder.build();
        }
        requestBuilder.description("Description for " + tree.name);
        tree.children.forEach(child -> requestBuilder.addOption(createOptionFromTreeNode(child)));
        return requestBuilder.build();
    }

    /**
     * Parses the annotations present on a method's signature and transforms them into
     * {@link ApplicationCommandOptionData} objects for the slash command request.
     *
     * @param method Method to parse.
     * @return A list of {@link ApplicationCommandOptionData} for the annotated methods.
     */
    private List<ApplicationCommandOptionData> getOptionsFromMethod(Method method) {
        List<ApplicationCommandOptionData> ret = new ArrayList<>();
        var parameters = Arrays.stream(method.getParameters())
                .filter(x -> x.isAnnotationPresent(CommandParam.class) || x.isAnnotationPresent(OptionalCommandParam.class))
                .toList();

        for (var parameter : parameters) {
            var acodBuilder = ApplicationCommandOptionData.builder();

            if (parameter.isAnnotationPresent(CommandParam.class)) {
                CommandParam paramAnnotation = parameter.getAnnotation(CommandParam.class);
                acodBuilder.name(paramAnnotation.name().toLowerCase());
                acodBuilder.description(paramAnnotation.description());
                Class<?> paramType = parameter.getType();
                acodBuilder.required(true);
                acodBuilder.type(OptionType.getCodeOfClass(paramType));

                if (paramAnnotation.choices().length > 0) {
                    acodBuilder.addAllChoices(Arrays.stream(paramAnnotation.choices())
                            .map(x -> ApplicationCommandOptionChoiceData.builder()
                                    .name(x.name())
                                    .value(x.value())
                                    .build())
                            .map(x -> (ApplicationCommandOptionChoiceData)x)
                            .toList());
                }

            } else if (parameter.isAnnotationPresent(OptionalCommandParam.class)) {
                OptionalCommandParam paramAnnotation = parameter.getAnnotation(OptionalCommandParam.class);
                acodBuilder.name(paramAnnotation.name().toLowerCase());
                acodBuilder.description(paramAnnotation.description());

                if (!parameter.getType().equals(Optional.class)) {
                    throw new RuntimeException("Not an optional!");
                }
                Class<?> paramType = paramAnnotation.type();
                acodBuilder.required(false);
                acodBuilder.type(OptionType.getCodeOfClass(paramType));

                if (paramAnnotation.choices().length > 0) {
                    acodBuilder.addAllChoices(Arrays.stream(paramAnnotation.choices())
                            .map(x -> ApplicationCommandOptionChoiceData.builder()
                                    .name(x.name())
                                    .value(x.value())
                                    .build())
                            .map(x -> (ApplicationCommandOptionChoiceData)x)
                            .toList());
                }
            }
            ret.add(acodBuilder.build());
        }
        return ret;
    }

    private ApplicationCommandOptionData createOptionFromTreeNode(CommandTreeNode node) {
        // this is a method
        var acodBuilder = ApplicationCommandOptionData.builder();
        acodBuilder.name(node.name);
        if (node.method != null) {
            var annotation = node.method.getAnnotation(SlashCommand.class);
            if (annotation == null) {
                return ApplicationCommandOptionData.builder().build();
            }
            acodBuilder.description(annotation.description());
            acodBuilder.type(OptionType.SUB_COMMAND);
            acodBuilder.addAllOptions(getOptionsFromMethod(node.method));
            return acodBuilder.build();
        }
        acodBuilder.description("Description for " + node.name);
        acodBuilder.type(OptionType.SUB_COMMAND_GROUP);
        node.children.forEach(child -> acodBuilder.addOption(createOptionFromTreeNode(child)));
        return acodBuilder.build();
    }
}
