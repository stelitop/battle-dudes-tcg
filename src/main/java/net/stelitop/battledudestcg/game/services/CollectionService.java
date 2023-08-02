package net.stelitop.battledudestcg.game.services;

import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import net.stelitop.battledudestcg.game.database.entities.chests.Chest;
import net.stelitop.battledudestcg.game.database.entities.profile.collection.CardOwnership;
import net.stelitop.battledudestcg.game.database.entities.profile.collection.ChestOwnership;
import net.stelitop.battledudestcg.game.database.entities.profile.collection.UserCollectionCardKey;
import net.stelitop.battledudestcg.game.database.entities.profile.collection.UserCollectionChestKey;
import net.stelitop.battledudestcg.game.database.repositories.CardOwnershipRepository;
import net.stelitop.battledudestcg.game.database.repositories.ChestOwndershipRepository;
import net.stelitop.battledudestcg.game.database.repositories.UserCollectionRepository;
import net.stelitop.battledudestcg.game.database.repositories.UserProfileRepository;
import net.stelitop.battledudestcg.game.pojo.ChestReward;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Service for performing CRUD operations on user collections.
 */
@Service
public class CollectionService {

    @Autowired
    private CardOwnershipRepository cardOwnershipRepository;
    @Autowired
    private ChestOwndershipRepository chestOwndershipRepository;
    @Autowired
    private UserProfileService userProfileService;
    @Autowired
    private UserCollectionRepository userCollectionRepository;

    /**
     * Grants a user one copy of a specific card.
     *
     * @param userId The id of the user.
     * @param card The card to add.
     * @return A reference to the card ownership object of the card and the user's collection.
     */
    public @NotNull CardOwnership giveUserCard(long userId, @NotNull Card card) {
        return giveUserCards(userId, List.of(card)).get(0);
    }

    public @NotNull List<CardOwnership> giveUserCards(long userId, @NotNull List<Card> cards) {
        if (cards.isEmpty()) {
            return List.of();
        }
        Map<Long, Integer> cardIdToCount = new HashMap<>();
        Map<Long, Card> cardIdToCard = new HashMap<>();
        cards.forEach(c -> {
            cardIdToCount.put(c.getCardId(), cardIdToCount.getOrDefault(c.getCardId(), 0) + 1);
            cardIdToCard.put(c.getCardId(), c);
        });

        var profile = userProfileService.getProfile(userId);
        var collection = profile.getUserCollection();

        List<CardOwnership> batchCardOwnerships = new ArrayList<>();
        for (long cardId : cardIdToCard.keySet()) {
            var key = new UserCollectionCardKey(collection.getCollectionId(), cardId);
            CardOwnership cardOwnership = cardOwnershipRepository.findById(key)
                    .orElse(new CardOwnership(key, cardIdToCard.get(cardId), collection, 0));
            cardOwnership.setOwnedCopies(cardOwnership.getOwnedCopies() + cardIdToCount.get(cardId));
            batchCardOwnerships.add(cardOwnership);
        }
        Iterable<CardOwnership> ret = cardOwnershipRepository.saveAll(batchCardOwnerships);

        return StreamSupport.stream(ret.spliterator(), false)
                .collect(Collectors.toList());
    }

    /**
     * Grants a user one copy of a specific chest.
     *
     * @param userId The id of the user.
     * @param chest The chest to add.
     * @return A reference to the chest ownership object of the chest and the user's collection.
     */
    public @NotNull ChestOwnership giveUserChest(long userId, @NotNull Chest chest) {
        var profile = userProfileService.getProfile(userId);
        var collection = profile.getUserCollection();
        var key = new UserCollectionChestKey(collection.getCollectionId(), chest.getChestId());

        Optional<ChestOwnership> chestOwnershipOpt = chestOwndershipRepository.findById(key);
        if (chestOwnershipOpt.isEmpty()) {
            return chestOwndershipRepository.save(new ChestOwnership(key, chest, collection, 1));
        }
        var chestOwnership = chestOwnershipOpt.get();
        chestOwnership.setCount(chestOwnership.getCount() + 1);
        return chestOwndershipRepository.save(chestOwnership);
    }

    public void awardReward(long userId, ChestReward chestReward) {
        awardRewards(userId, List.of(chestReward));
    }

    public void awardRewards(long userId, List<ChestReward> chestRewards) {
        int newCoins = 0;
        List<Card> newCards = new ArrayList<>();

        for (var chestReward : chestRewards) {
            switch (chestReward.getRewardType()) {
                case Card -> newCards.add(chestReward.getCard());
                case Coins, ExtraCard -> newCoins += Math.max(0, chestReward.getCoins());
            }
        }

        giveUserCards(userId, newCards);
        var profile = userProfileService.getProfile(userId);
        var collection = profile.getUserCollection();
        collection.setCoins(collection.getCoins() + newCoins);
        userCollectionRepository.save(collection);
    }
}
