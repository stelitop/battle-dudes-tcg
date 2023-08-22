package net.stelitop.battledudestcg.discord.listeners.buttons;

import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ComponentInteractionEvent;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import net.stelitop.battledudestcg.commons.pojos.ActionResult;
import net.stelitop.battledudestcg.commons.utils.RandomUtils;
import net.stelitop.battledudestcg.discord.framework.components.ComponentInteraction;
import net.stelitop.battledudestcg.discord.framework.DiscordEventsComponent;
import net.stelitop.battledudestcg.discord.framework.InteractionEvent;
import net.stelitop.battledudestcg.discord.utils.ColorUtils;
import net.stelitop.battledudestcg.game.database.entities.chests.Chest;
import net.stelitop.battledudestcg.game.database.entities.collection.ChestOwnership;
import net.stelitop.battledudestcg.game.database.entities.collection.UserCollectionChestKey;
import net.stelitop.battledudestcg.game.database.repositories.ChestOwnershipRepository;
import net.stelitop.battledudestcg.game.database.repositories.ChestRepository;
import net.stelitop.battledudestcg.game.pojo.ChestReward;
import net.stelitop.battledudestcg.game.services.CollectionService;
import net.stelitop.battledudestcg.game.services.UserProfileService;
import net.stelitop.battledudestcg.game.utils.ChestRewardUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Handles the user clicking on either the open chest button or the
 * add to collection button.
 */
@DiscordEventsComponent
public class ChestOpenButtonListener {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

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

    /**
     * Keeps the chest from a chest drop a user received.
     *
     * @param event The event.
     * @return The event reply.
     */
    @ComponentInteraction(event = ButtonInteractionEvent.class, regex = "keepchest\\|[0-9]*\\|[0-9]*")
    public Mono<Void> keepChest(@InteractionEvent ComponentInteractionEvent event) {
        var parseIdActionResult = parseId(event.getInteraction().getUser(), event.getCustomId());
        if (parseIdActionResult.hasFailed()) {
            return event.reply(parseIdActionResult.errorMessage()).withEphemeral(true);
        }
        Chest chest = parseIdActionResult.getResponse().getRight();

        String username = event.getInteraction().getUser().getUsername();
        return event.edit().withEmbeds(EmbedCreateSpec.builder()
                .title(username + " found a " + chest.getName() + "!")
                .description("This chest remains in your collection.")
                .color(colorUtils.getChestEmbedColor())
                .thumbnail(chest.getIconUrl())
                .build())
                .withComponents(new ArrayList<>());
    }

    /**
     * Opens the chest from a chest drop a user received.
     *
     * @param event The event.
     * @return The event reply.
     */
    @ComponentInteraction(event = ButtonInteractionEvent.class, regex = "openchest\\|[0-9]*\\|[0-9]*")
    public Mono<Void> openChest(@InteractionEvent ComponentInteractionEvent event) {
        var parseIdActionResult = parseId(event.getInteraction().getUser(), event.getCustomId());
        if (parseIdActionResult.hasFailed()) {
            return event.reply(parseIdActionResult.errorMessage()).withEphemeral(true);
        }
        long userId = parseIdActionResult.getResponse().getLeft();
        Chest chest = parseIdActionResult.getResponse().getRight();

        var collection = userProfileService.getProfile(userId).getUserCollection();
        var chestOwnershipKey = new UserCollectionChestKey(collection.getCollectionId(), chest.getChestId());
        Optional<ChestOwnership> chestOwnershipOpt = chestOwnershipRepository.findById(chestOwnershipKey);

        if (chestOwnershipOpt.isEmpty() || chestOwnershipOpt.get().getCount() == 0) {
            return event.reply("You've already opened this chest!")
                    .withEphemeral(true);
        }
        var chestOwnership = chestOwnershipOpt.get();
        chestOwnership.setCount(chestOwnership.getCount() - 1);
        chestOwnershipRepository.save(chestOwnership);

        String username = event.getInteraction().getUser().getUsername();
        List<ChestReward> rewards = chest.rollChest(randomUtils);
        rewards = chestRewardUtils.transformExtraRewards(rewards, userId);
        collectionService.awardRewards(userId, rewards);

        String description = "## Chest Contents\n\n" + rewards.stream()
                .map(x -> "> " + x.toString())
                .collect(Collectors.joining("\n> \u200B\n"));

        return event.edit().withEmbeds(EmbedCreateSpec.builder()
                .title(username + " found a " + chest.getName() + "!")
                .description(description)
                .color(colorUtils.getChestEmbedColor())
                .thumbnail(chest.getIconUrl())
                .build())
                .withComponents(new ArrayList<>());
    }

    /**
     * Parses the id to find if the correct user is opening it and if the id is of a real chest.
     *
     * @param user The user.
     * @param componentId The component id.
     * @return The action result.
     */
    private ActionResult<Pair<Long, Chest>> parseId(User user, String componentId) {
        long userId = Long.parseLong(componentId.split("\\|")[1]);
        if (user.getId().asLong() != userId) return ActionResult.fail("This is not your chest!");
        long chestId = Long.parseLong(componentId.split("\\|")[2]);
        Optional<Chest> chestOpt = chestRepository.findById(chestId);
        if (chestOpt.isEmpty()) {
            LOGGER.error("Chest with id=" + chestId + " was not found in the database when user "
                    + user.getUsername() + " tried opening one!");
            return ActionResult.fail("There was an error opening this chest! Missing chest.");
        }
        Chest chest = chestOpt.get();
        return ActionResult.success(Pair.of(userId, chest));
    }
}
