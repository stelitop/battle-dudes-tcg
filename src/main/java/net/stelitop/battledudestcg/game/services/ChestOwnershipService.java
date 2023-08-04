package net.stelitop.battledudestcg.game.services;

import net.stelitop.battledudestcg.game.database.entities.chests.Chest;
import net.stelitop.battledudestcg.game.database.entities.profile.collection.ChestOwnership;
import net.stelitop.battledudestcg.game.database.entities.profile.collection.UserCollection;
import net.stelitop.battledudestcg.game.database.entities.profile.collection.UserCollectionChestKey;
import net.stelitop.battledudestcg.game.database.repositories.ChestOwnershipRepository;
import net.stelitop.battledudestcg.game.database.repositories.ChestRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ChestOwnershipService {

    @Autowired
    private ChestOwnershipRepository chestOwnershipRepository;
    @Autowired
    private ChestRepository chestRepository;
    @Autowired
    private UserProfileService userProfileService;

    /**
     * Returns the chest ownership object for a given user and the name of a chest.
     *
     * @param userId The id of the user.
     * @param name The name of the chest.
     * @return An optional ownership object. If the chest name is not of an existing chest,
     *     the object is empty. Otherwise, it is present.
     */
    public Optional<ChestOwnership> getChestOwnership(long userId, String name) {
        Optional<ChestOwnership> chestOwnershipOpt = chestOwnershipRepository.getChestOwnership(userId, name);
        if (chestOwnershipOpt.isPresent()) {
            return chestOwnershipOpt;
        }
        Optional<Chest> chestOpt = chestRepository.findByName(name);
        UserCollection collection = userProfileService.getProfile(userId).getUserCollection();
        return chestOpt.map(chest -> new ChestOwnership(
                new UserCollectionChestKey(collection.getCollectionId(), chest.getChestId()),
                chest,
                collection,
                0
        ));
    }
}
