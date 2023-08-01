package net.stelitop.battledudestcg.game.database.entities.cards;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("Portal")
@NoArgsConstructor
@SuperBuilder
public class PortalCard extends Card {

}
