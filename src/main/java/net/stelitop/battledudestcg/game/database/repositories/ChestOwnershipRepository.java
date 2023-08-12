package net.stelitop.battledudestcg.game.database.repositories;

import net.stelitop.battledudestcg.game.database.entities.profile.collection.chests.ChestOwnership;
import net.stelitop.battledudestcg.game.database.entities.profile.collection.chests.UserCollectionChestKey;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChestOwnershipRepository extends CrudRepository<ChestOwnership, UserCollectionChestKey> {

    @Query("SELECT co FROM UserProfile up " +
            "JOIN UserCollection uc ON up.userCollection = uc " +
            "JOIN ChestOwnership co ON uc.collectionId  = co.id.collectionId " +
            "JOIN Chest c ON co.id.chestId = c.chestId " +
            "WHERE up.discordId = ?1 AND lower(c.name) = lower(?2)")
    Optional<ChestOwnership> getChestOwnership(long userId, String chestName);
}
