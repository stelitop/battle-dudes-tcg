package net.stelitop.battledudestcg.game.database.entities.profile;

import jakarta.persistence.*;
import lombok.Data;
import net.stelitop.battledudestcg.game.database.entities.profile.collection.UserCollection;

import java.util.Date;

@Entity
@Table(name = "UserProfile")
@Data
public class UserProfile {

    /**
     * The discord id of the user, also used as a key to for the user table.
     */
    @Id
    @Column(name = "discord_id", nullable = false, unique = true)
    private long discordId;

    /**
     * The time of the last message sent by the user. Used to prevent spamming
     * as a means of acquiring dudes.
     */
    @Column(name = "last_message")
    private Date lastMessage;

    /**
     * The card collection of the user.
     */
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private UserCollection userCollection;

    /**
     * The settings of the user.
     */
    @Embedded
    private UserSettings userSettings = new UserSettings();
}
