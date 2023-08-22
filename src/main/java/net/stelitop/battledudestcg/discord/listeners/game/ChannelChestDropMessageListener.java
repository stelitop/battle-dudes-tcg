package net.stelitop.battledudestcg.discord.listeners.game;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.spec.MessageCreateSpec;
import net.stelitop.battledudestcg.commons.configs.EnvironmentVariables;
import net.stelitop.battledudestcg.commons.utils.RandomUtils;
import net.stelitop.battledudestcg.discord.ui.ChestOpeningUI;
import net.stelitop.battledudestcg.game.database.entities.chests.Chest;
import net.stelitop.battledudestcg.game.database.entities.profile.UserProfile;
import net.stelitop.battledudestcg.game.services.ChestService;
import net.stelitop.battledudestcg.game.services.CollectionService;
import net.stelitop.battledudestcg.game.services.UserProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Date;

@Component
public class ChannelChestDropMessageListener implements ApplicationRunner {

    /**
     * The time the user has to wait after sending a message to be eligible for
     * a chest drop, in milliseconds.
     */
    public static final long MESSAGE_COOLDOWN = 20 * 1000;

    /**
     * The probability for a chest to drop, measured as a number between 0 and 1.
     */
    //public static final double CHEST_DROP_PROBABILITY = 0.01;
    public static final double CHEST_DROP_PROBABILITY = 1;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private GatewayDiscordClient client;
    @Autowired
    private UserProfileService userProfileService;
    @Autowired
    private RandomUtils randomUtils;
    @Autowired
    private ChestService chestService;
    @Autowired
    private CollectionService collectionService;
    @Autowired
    private EnvironmentVariables evs;
    @Autowired
    private ChestOpeningUI chestOpeningUI;

    @Override
    public void run(ApplicationArguments args) {
        client.on(MessageCreateEvent.class, this::handle).subscribe();
    }

    /**
     * Handles the message create event.
     *
     * <p>Checks if the user is toggled in the game and if enough time has passed
     * since their last message to be eligible for a chest drop. If they are, an
     * attempt is made to award them a chest.</p>*
     *
     * @param event The MessageCreateEvent.
     * @return Mandatory mono void.
     */
    private Mono<Void> handle(MessageCreateEvent event) {
        if (event.getMember().isEmpty()) {
            return Mono.empty();
        }
        Member member = event.getMember().get();
        long userId = member.getMemberData().user().id().asLong();
        UserProfile profile = userProfileService.getProfile(userId);
        if (!profile.getUserSettings().isParticipating()) {
            return Mono.empty();
        }
        Date lastMsgTime = profile.getLastMessage();
        if (Instant.now().toEpochMilli() - lastMsgTime.toInstant().toEpochMilli() <= MESSAGE_COOLDOWN) {
            return Mono.empty();
        }
        profile = userProfileService.updateLastMessageTime(userId);
        return rollForChest(event, profile, member);
    }

    /**
     * Rolls a random event to try to award the player a chest for the channel
     * the event happened in.
     *
     * @param event The MessageCreateEvent.
     * @param profile The profile of the user.
     * @param member The discord member object of the user.
     * @return Mandatory mono void.
     */
    private Mono<Void> rollForChest(MessageCreateEvent event, UserProfile profile, Member member) {
        if (!randomUtils.invokeProbability(CHEST_DROP_PROBABILITY)) {
            return Mono.empty();
        }
        // give chest
        long channelId = event.getMessage().getChannelId().asLong();
        Chest chest = evs.inDevmode() ? chestService.getRandomChest() : chestService.getChestOfChannel(channelId);
        if (chest == null) {
            LOGGER.warn("Could not award chest in channel with id = " + channelId + ".");
            return Mono.empty();
        }

        collectionService.giveUserChest(profile.getDiscordId(), chest);
        return sendChestDropMessage(event, chest, member);
    }

    /**
     * Creates a message and sends it to the channel of the event to inform the
     * user they received a chest.
     *
     * @param event The MessageCreateEvent.
     * @param chest The chest the user is awarded.
     * @param member The discord member object of the user
     * @return Mandatory Mono Void.
     */
    private Mono<Void> sendChestDropMessage(MessageCreateEvent event, Chest chest, Member member) {
        MessageCreateSpec message = chestOpeningUI.getMessage(chest, member);
        return event.getMessage().getChannel().flatMap(channel -> channel.createMessage(message)).then();
    }
}
