package net.stelitop.battledudestcg.game.database.repositories;

import net.stelitop.battledudestcg.game.database.entities.profile.collection.cards.CardOwnership;
import net.stelitop.battledudestcg.game.database.entities.profile.collection.cards.UserCollectionCardKey;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardOwnershipRepository extends CrudRepository<CardOwnership, UserCollectionCardKey> {

}
