package net.stelitop.battledudestcg.game.database.entities.cards;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("Warp")
@NoArgsConstructor
@SuperBuilder
public class WarpCard extends Card {

}
