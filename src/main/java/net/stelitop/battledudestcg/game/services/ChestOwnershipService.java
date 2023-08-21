package net.stelitop.battledudestcg.game.services;

import net.stelitop.battledudestcg.game.database.entities.chests.Chest;
import net.stelitop.battledudestcg.game.database.entities.collection.ChestOwnership;
import net.stelitop.battledudestcg.game.database.entities.collection.UserCollection;
import net.stelitop.battledudestcg.game.database.entities.collection.UserCollectionChestKey;
import net.stelitop.battledudestcg.game.database.repositories.ChestOwnershipRepository;
import net.stelitop.battledudestcg.game.database.repositories.ChestRepository;
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
    private CollectionService collectionService;

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
        UserCollection collection = collectionService.getUserCollection(userId);
        return chestOpt.map(chest -> new ChestOwnership(
                new UserCollectionChestKey(collection.getCollectionId(), chest.getChestId()),
                chest,
                collection,
                0
        ));
    }
}
