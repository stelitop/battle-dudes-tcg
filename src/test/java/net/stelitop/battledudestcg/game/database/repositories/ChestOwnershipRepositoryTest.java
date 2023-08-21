package net.stelitop.battledudestcg.game.database.repositories;

import net.stelitop.battledudestcg.game.database.entities.chests.ChannelChest;
import net.stelitop.battledudestcg.game.database.entities.chests.Chest;
import net.stelitop.battledudestcg.game.database.entities.collection.ChestOwnership;
import net.stelitop.battledudestcg.game.database.entities.collection.UserCollection;
import net.stelitop.battledudestcg.game.database.entities.collection.UserCollectionChestKey;
import net.stelitop.battledudestcg.game.database.entities.profile.UserProfile;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
class ChestOwnershipRepositoryTest {

    @Autowired
    private ChestOwnershipRepository chestOwnershipRepository;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private UserCollectionRepository userCollectionRepository;
    @Autowired
    private ChestRepository chestRepository;

    @Test
    void getChestOwnershipPresent() {
        long userId = 1234;
        int chestCount = 5;
        Chest chestA = ChannelChest.builder().name("ChestA").description("DescriptionA").build();
        Chest chestB = ChannelChest.builder().name("ChestB").description("DescriptionB").build();
        Chest chestC = ChannelChest.builder().name("ChestC").description("DescriptionC").build();
        chestRepository.saveAll(List.of(chestA, chestB, chestC));
        chestA = chestRepository.findByName(chestA.getName()).get();
        UserCollection collection = new UserCollection();
        collection = userCollectionRepository.save(collection);
        UserProfile profile = new UserProfile();
        profile.setDiscordId(userId);
        profile.setUserCollection(collection);
        profile = userProfileRepository.save(profile);
        chestOwnershipRepository.save(new ChestOwnership(new UserCollectionChestKey(collection.getCollectionId(), chestA.getChestId())
                , chestA, collection, chestCount));

        var chestOwnershipOpt = chestOwnershipRepository.getChestOwnership(userId, chestA.getName());
        assertThat(chestOwnershipOpt).isPresent();
        assertThat(chestOwnershipOpt.get().getChest().getChestId()).isEqualTo(chestA.getChestId());
        assertThat(chestOwnershipOpt.get().getUserCollection().getCollectionId()).isEqualTo(collection.getCollectionId());
        assertThat(chestOwnershipOpt.get().getCount()).isEqualTo(chestCount);
        assertThat(chestOwnershipOpt.get().getId().getChestId()).isEqualTo(chestA.getChestId());
        assertThat(chestOwnershipOpt.get().getId().getCollectionId()).isEqualTo(collection.getCollectionId());
    }

    @Test
    void getChestOwnershipMissing() {
        long userId = 1234;
        int chestCount = 5;
        Chest chestA = ChannelChest.builder().name("ChestA").description("DescriptionA").build();
        Chest chestB = ChannelChest.builder().name("ChestB").description("DescriptionB").build();
        Chest chestC = ChannelChest.builder().name("ChestC").description("DescriptionC").build();
        chestRepository.saveAll(List.of(chestA, chestB, chestC));
        chestA = chestRepository.findByName(chestA.getName()).get();
        UserCollection collection = new UserCollection();
        collection = userCollectionRepository.save(collection);
        UserProfile profile = new UserProfile();
        profile.setDiscordId(userId);
        profile.setUserCollection(collection);
        profile = userProfileRepository.save(profile);
        chestOwnershipRepository.save(new ChestOwnership(new UserCollectionChestKey(collection.getCollectionId(), chestA.getChestId())
                , chestA, collection, chestCount));

        var chestOwnershipOpt = chestOwnershipRepository.getChestOwnership(userId, chestB.getName());
        assertThat(chestOwnershipOpt).isEmpty();
    }
}