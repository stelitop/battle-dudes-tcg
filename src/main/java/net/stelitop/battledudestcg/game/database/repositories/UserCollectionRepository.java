package net.stelitop.battledudestcg.game.database.repositories;

import net.stelitop.battledudestcg.game.database.entities.profile.collection.UserCollection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCollectionRepository extends CrudRepository<UserCollection, Long> {

    @Query("SELECT up.userCollection FROM UserProfile up WHERE up.discordId = ?1")
    Optional<UserCollection> findByUserId(long userId);
}
