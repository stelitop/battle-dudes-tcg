package net.stelitop.battledudestcg.game.database.entities.chests;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("ChannelChest")
@Data
@ToString
@SuperBuilder
@NoArgsConstructor
public class ChannelChest extends Chest {

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
