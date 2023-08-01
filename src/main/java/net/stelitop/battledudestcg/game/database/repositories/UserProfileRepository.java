package net.stelitop.battledudestcg.game.database.repositories;

import net.stelitop.battledudestcg.game.database.entities.profile.UserProfile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends CrudRepository<UserProfile, Long> {
}
