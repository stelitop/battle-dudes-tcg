package net.stelitop.battledudestcg.game.database.repositories;

import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends CrudRepository<Card, Long> {

    Optional<Card> findByNameIgnoreCase(String name);
}
