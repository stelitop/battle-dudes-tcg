package net.stelitop.battledudestcg.discord.listeners.buttons;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import net.stelitop.battledudestcg.commons.utils.RandomUtils;
import net.stelitop.battledudestcg.discord.utils.ColorUtils;
import net.stelitop.battledudestcg.game.database.entities.chests.Chest;
import net.stelitop.battledudestcg.game.database.entities.profile.collection.ChestOwnership;
import net.stelitop.battledudestcg.game.database.entities.profile.collection.UserCollectionChestKey;
import net.stelitop.battledudestcg.game.database.repositories.ChestOwnershipRepository;
import net.stelitop.battledudestcg.game.database.repositories.ChestRepository;
import net.stelitop.battledudestcg.game.pojo.ChestReward;
import net.stelitop.battledudestcg.game.services.CollectionService;
import net.stelitop.battledudestcg.game.services.UserProfileService;
import net.stelitop.battledudestcg.game.utils.ChestRewardUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Handles the user clicking on either the open chest button or the
 * add to collection button.
 */
@Component
public class ChestOpenButtonListener implements ApplicationRunner {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private GatewayDiscordClient client;
    @Autowired
    private ChestRepository chestRepository;
    @Autowired
    private RandomUtils randomUtils;
    @Autowired
    private CollectionService collectionService;
    @Autowired
    private UserProfileService userProfileService;
    @Autowired
    private ChestOwnershipRepository chestOwnershipRepository;
    @Autowired
    private ColorUtils colorUtils;
    @Autowired
    private ChestRewardUtils chestRewardUtils;

    @Override
    public void run(ApplicationArguments args) {
        client.on(ButtonInteractionEvent.class, this::handle).subscribe();
    }

    private Mono<Void> handle(ButtonInteractionEvent event) {

        Message message = event.getInteraction().getMessage().get();
        System.out.println(message);

        String buttonId = event.getCustomId();
        String[] parts = buttonId.split("\\|");
        // check for correct button
        if (parts.length != 3 || (!parts[0].equals("keepchest") && !parts[0].equals("openchest"))) {
            return Mono.empty();
        }
        // check for correct user
        if (!event.getInteraction().getUser().getId().asString().equals(parts[1])) {
            return event.reply()
                    .withContent("This is not your chest!")
                    .withEphemeral(true);
        }
        long userId = event.getInteraction().getUser().getId().asLong();
        long chestId = Long.parseLong(parts[2]);
        Optional<Chest> chestOpt = chestRepository.findById(chestId);
        if (chestOpt.isEmpty()) {
            LOGGER.error("Chest " + parts[2] + " was not found in the database when user "
                    + event.getInteraction().getUser().getUsername() + " tried opening one!");
            return event.reply()
                    .withContent("There was an error opening this chest. Missing chest.")
                    .withEphemeral(true);
        }
        Chest chest = chestOpt.get();

        if (parts[0].equals("openchest")) {
            return openChest(event, userId, chest);
        } else {
            return keepChest(event, userId, chest);
        }
    }

    private Mono<Void> keepChest(ButtonInteractionEvent event, long userId, Chest chest) {
        String username = event.getInteraction().getUser().getUsername();
        return event.edit().withEmbeds(EmbedCreateSpec.builder()
                .title(username + " found a " + chest.getName() + "!")
                .description("The chest has been added to your collection!")
                .color(colorUtils.getChestEmbedColor())
                .thumbnail(chest.getIconUrl())
                .build())
                .withComponents(new ArrayList<>());
    }

    private Mono<Void> openChest(ButtonInteractionEvent event, long userId, Chest chest) {
        var collection = userProfileService.getProfile(userId).getUserCollection();
        var chestOwnershipKey = new UserCollectionChestKey(collection.getCollectionId(), chest.getChestId());
        Optional<ChestOwnership> chestOwnershipOpt = chestOwnershipRepository.findById(chestOwnershipKey);

        if (chestOwnershipOpt.isEmpty() || chestOwnershipOpt.get().getCount() == 0) {
            return event.reply()
                    .withContent("You've already opened this chest!")
                    .withEphemeral(true);
        }
        var chestOwnership = chestOwnershipOpt.get();
        chestOwnership.setCount(chestOwnership.getCount() - 1);
        chestOwnershipRepository.save(chestOwnership);

        String username = event.getInteraction().getUser().getUsername();
        List<ChestReward> rewards = chest.rollChest(randomUtils);
        rewards = chestRewardUtils.transformExtraRewards(rewards, userId);
        collectionService.awardRewards(userId, rewards);

        String description = "## Chest Rewards\n\n" + rewards.stream()
                .map(x -> "> " + x.toString())
                .collect(Collectors.joining("\n> \u200B\n"));

        return event.edit().withEmbeds(EmbedCreateSpec.builder()
                .title(username + " found a " + chest.getName() + "!")
                //.addField("Chest Rewards", description, false)
                .description(description)
                .color(colorUtils.getChestEmbedColor())
                .thumbnail(chest.getIconUrl())
                .build())
                .withComponents(new ArrayList<>());
    }
}
