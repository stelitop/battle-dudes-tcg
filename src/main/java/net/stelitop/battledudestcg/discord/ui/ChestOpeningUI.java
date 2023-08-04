package net.stelitop.battledudestcg.discord.ui;


import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import net.stelitop.battledudestcg.discord.listeners.buttons.ChestOpenButtonListener;
import net.stelitop.battledudestcg.discord.utils.ColorUtils;
import net.stelitop.battledudestcg.game.database.entities.chests.Chest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChestOpeningUI {

    @Autowired
    private ColorUtils colorUtils;
    @Autowired
    private ChestOpenButtonListener chestOpenButtonListener;

    /**
     * Formats how the id of the button for opening a chest that just dropped
     * should look like.
     *
     * @param userId The id of the user that owns the chest.
     * @param chest The chest that should be opened.
     * @return The formatted component id.
     */
    public String formatOpenChestButtonId(long userId, Chest chest) {
        return "openchest|" + userId + "|" + chest.getChestId();
    }

    /**
     * Formats how the id of the button for keeping a chest instead of opening it
     * that just dropped should look like.
     *
     * @param userId The id of the user that owns the chest.
     * @param chest The chest that should be opened.
     * @return The formatted component id.
     */
    public String formatKeepChestButtonId(long userId, Chest chest) {
        return "keepchest|" + userId + "|" + chest.getChestId();
    }

    public MessageCreateSpec getMessage(Chest chest, User user) {
        return getMessage(chest, user.getUsername(), user.getId().asLong());
    }

    public MessageCreateSpec getMessage(Chest chest, Member member) {
        return getMessage(chest, member.getUsername(), member.getId().asLong());
    }

    private MessageCreateSpec getMessage(Chest chest, String username, long userId) {
        var embed = EmbedCreateSpec.builder()
                .title(username + " found a " + chest.getName() + "!")
                .description("Do you want to open the chest?\n\nNot picking a choice adds it to your collection.")
                .thumbnail(chest.getIconUrl())
                .color(colorUtils.getChestEmbedColor())
                .build();

        String openButtonId = formatOpenChestButtonId(userId, chest);
        String keepButtonId = formatKeepChestButtonId(userId, chest);

        return MessageCreateSpec.builder()
                .addEmbed(embed)
                .addComponent(ActionRow.of(
                        List.of(
                                Button.success(openButtonId, "Open Now!"),
                                Button.primary(keepButtonId, "Add To Collection")
                        )
                )).build();
    }
}
