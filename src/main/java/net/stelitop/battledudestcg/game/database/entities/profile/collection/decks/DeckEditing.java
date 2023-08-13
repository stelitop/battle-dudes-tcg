package net.stelitop.battledudestcg.game.database.entities.profile.collection.decks;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.OneToOne;
import lombok.Data;
import net.stelitop.battledudestcg.game.database.entities.chests.Chest;

import java.time.Instant;
import java.util.Date;

@Embeddable
@Data
public class DeckEditing {

    @OneToOne
    private CardDeck editedDeck;

    @Column(name = "last_edit_time")
    private Date lastEditTime = Date.from(Instant.EPOCH);

    @Column(name = "edit_msg_id")
    private Long editMsgId = -1L;

    @Column(name = "edit_msg_channel_id")
    private Long editMsgChannelId = -1L;
}
