package net.stelitop.battledudestcg.game.database.repositories;

import net.stelitop.battledudestcg.game.database.entities.chests.Chest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChestRepository extends CrudRepository<Chest, Long> {

    @Query("SELECT x.name FROM Chest x")
    List<String> getAllChestNames();

    Optional<Chest> findByName(String name);
}
