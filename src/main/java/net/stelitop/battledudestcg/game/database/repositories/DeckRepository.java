package net.stelitop.battledudestcg.game.database.repositories;

import net.stelitop.battledudestcg.game.database.entities.profile.collection.decks.CardDeck;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeckRepository extends CrudRepository<CardDeck, Long> {

    @Query("SELECT uc.decks FROM UserProfile up " +
            "JOIN UserCollection uc ON up.userCollection = uc " +
            "WHERE up.discordId = ?1")
    List<CardDeck> findCardDecksByUserId(long userId);

    @Query("SELECT cd.name FROM UserProfile up " +
            "JOIN UserCollection uc ON up.userCollection = uc " +
            "JOIN CardDeck cd ON cd.userCollection = uc " +
            "WHERE up.discordId = ?1")
    List<String> findNamesOfDecksOfUser(long userId);
}
