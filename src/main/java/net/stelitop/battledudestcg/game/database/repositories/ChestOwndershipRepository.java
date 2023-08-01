package net.stelitop.battledudestcg.game.database.repositories;

import net.stelitop.battledudestcg.game.database.entities.profile.collection.ChestOwnership;
import net.stelitop.battledudestcg.game.database.entities.profile.collection.UserCollectionChestKey;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChestOwndershipRepository extends CrudRepository<ChestOwnership, UserCollectionChestKey> {
}
