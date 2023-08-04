package net.stelitop.battledudestcg.game.database.repositories;

import net.stelitop.battledudestcg.game.database.entities.profile.collection.CardOwnership;
import net.stelitop.battledudestcg.game.database.entities.profile.collection.UserCollectionCardKey;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardOwnershipRepository extends CrudRepository<CardOwnership, UserCollectionCardKey> {

}
