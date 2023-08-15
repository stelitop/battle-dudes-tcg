package net.stelitop.battledudestcg.discord.ui;

import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.spec.MessageCreateSpec;
import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EditCardUI {

    @Autowired
    private CardInfoUI cardInfoUI;

    public static final String ID_NAME = "name";
    public static final String ID_EFFECT = "effect";
    public static final String ID_ELEMENTAL_TYPES = "types";


    public MessageCreateSpec getEditCardMessage(Card card) {
        var msg = cardInfoUI.getCardInfoMessage(card);

        List<LayoutComponent> msgComponents = List.of(
                ActionRow.of(
                        Button.primary(makeId(card, ID_NAME), "Edit Name"),
                        Button.primary(makeId(card, ID_EFFECT), "Edit Effect"),
                        Button.primary(makeId(card, ID_ELEMENTAL_TYPES), "Edit Elemental Types")
                )
        );

        return msg.withComponents(msgComponents);
    }

    public String makeId(Card card, String uniqueName) {
        return "editcard|" + card.getCardId() + "|" + uniqueName;
    }
}
