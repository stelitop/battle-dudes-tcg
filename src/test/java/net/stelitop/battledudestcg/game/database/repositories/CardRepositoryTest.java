package net.stelitop.battledudestcg.game.database.repositories;

import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import net.stelitop.battledudestcg.game.database.entities.cards.DudeCard;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
class CardRepositoryTest {

    @Autowired
    private CardRepository cardRepository;

    @Test
    void getAllCardNamesEmpty() {
        List<String> ret = cardRepository.getAllCardNames();
        assertThat(ret).isEmpty();
    }

    @Test
    void getAllCardNamesFilled() {
        List<String> names = List.of("Name 1", "AnOtHeR nAmE", "last guy123");
        List<DudeCard> dudes = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            dudes.add(DudeCard.builder().name(names.get(i)).dudeId(i + 1).build());
        }
        cardRepository.saveAll(dudes);
        List<String> ret = cardRepository.getAllCardNames();
        assertThat(ret).containsExactlyInAnyOrder(names.toArray(new String[0]));
    }

    @Test
    void findByNameIgnoreCase() {
        long cardId = cardRepository.save(DudeCard.builder().name("Test dude").dudeId(1).build()).getCardId();
        Optional<Card> card = cardRepository.findByNameIgnoreCase("tEst DuDe");
        assertThat(card).isPresent();
        assertThat(card.get().getCardId()).isEqualTo(cardId);
    }
}