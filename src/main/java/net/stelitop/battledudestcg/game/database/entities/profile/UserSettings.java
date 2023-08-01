package net.stelitop.battledudestcg.game.database.entities.profile;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class UserSettings {

    @Column(name = "participating")
    private boolean participating = false;
}
