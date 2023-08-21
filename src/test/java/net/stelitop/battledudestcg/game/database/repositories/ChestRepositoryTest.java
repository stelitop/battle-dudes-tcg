package net.stelitop.battledudestcg.game.database.repositories;

import net.stelitop.battledudestcg.game.database.entities.chests.ChannelChest;
import net.stelitop.battledudestcg.game.database.entities.chests.Chest;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
class ChestRepositoryTest {

    @Autowired
    private ChestRepository chestRepository;

    @Test
    void getAllChestNames() {
        List<String> names = List.of("ChestA", "ChestB", "ChestC", "ChestD");
        var chests = names.stream()
                .map(x -> ChannelChest.builder().name(x).description("Description " + x).build())
                .toList();
        chestRepository.saveAll(chests);
        List<String> ret = chestRepository.getAllChestNames();
        assertThat(ret).containsExactlyInAnyOrder(names.toArray(new String[0]));
    }

    @Test
    void findByNameFound() {
        Chest chest = chestRepository.save(ChannelChest.builder().name("Chest Name").build());
        Optional<Chest> ret = chestRepository.findByName("Chest Name");
        assertThat(ret).isPresent();
        assertThat(ret.get().getChestId()).isEqualTo(chest.getChestId());
    }

    @Test
    void findByNameMissingDifferentCase() {
        Chest chest = chestRepository.save(ChannelChest.builder().name("Chest Name").build());
        Optional<Chest> ret = chestRepository.findByName("chesT namE");
        assertThat(ret).isEmpty();
    }


    @Test
    void findByNameIgnoreCase() {
        Chest chest = chestRepository.save(ChannelChest.builder().name("Chest Name").build());
        Optional<Chest> ret = chestRepository.findByNameIgnoreCase("chesT namE");
        assertThat(ret).isPresent();
        assertThat(ret.get().getChestId()).isEqualTo(chest.getChestId());
    }

    @Test
    void existsByNameFound() {
        Chest chest = chestRepository.save(ChannelChest.builder().name("Chest Name").build());
        boolean ret = chestRepository.existsByName("Chest Name");
        assertThat(ret).isTrue();
    }

    @Test
    void existsByNameMissingDifferentCase() {
        Chest chest = chestRepository.save(ChannelChest.builder().name("Chest Name").build());
        boolean ret = chestRepository.existsByName("chesT namE");
        assertThat(ret).isFalse();
    }
}