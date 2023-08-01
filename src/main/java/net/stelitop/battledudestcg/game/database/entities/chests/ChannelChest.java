package net.stelitop.battledudestcg.game.database.entities.chests;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.ToString;

@Entity
@DiscriminatorValue("ChannelChest")
@Data
@ToString
public class ChannelChest extends Chest {

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }
}
