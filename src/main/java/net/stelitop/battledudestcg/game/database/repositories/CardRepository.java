package net.stelitop.battledudestcg.game.database.repositories;

import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends CrudRepository<Card, Long> {

    @Query("SELECT x.name FROM Card x")
    List<String> getAllCardNames();
    Optional<Card> findByNameIgnoreCase(String name);

    @Query("SELECT MAX(d.cardId) FROM DudeCard d")
    @Nullable Integer findMaxDudeId();

    default int findMaxDudeIdSafe() {
        Integer x = findMaxDudeId();
        return x != null ? x : 0;
    }
}
