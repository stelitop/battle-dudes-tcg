package net.stelitop.battledudestcg.game.database.repositories;

import net.stelitop.battledudestcg.game.database.entities.profile.collection.UserCollection;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCollectionRepository extends CrudRepository<UserCollection, Long> {
}
