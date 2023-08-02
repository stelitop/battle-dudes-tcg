package net.stelitop.battledudestcg.game.utils;

import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import net.stelitop.battledudestcg.game.database.entities.profile.collection.CardOwnership;
import net.stelitop.battledudestcg.game.pojo.ChestReward;
import net.stelitop.battledudestcg.game.services.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChestRewardUtils {

    @Autowired
    private UserProfileService userProfileService;

    public List<ChestReward> transformExtraRewards(List<ChestReward> originalRewards, long userId) {
        List<ChestReward> newRewards = new ArrayList<>();
        var profile = userProfileService.getProfile(userId);
        List<CardOwnership> cards = profile.getUserCollection().getOwnedCards();
        Map<Long, Integer> cardCounts = cards.stream()
                .collect(Collectors.toMap(x -> x.getCard().getCardId(), CardOwnership::getOwnedCopies));

        for (var reward : originalRewards) {
            if (reward.getRewardType() != ChestReward.RewardType.Card) {
                newRewards.add(reward);
                continue;
            }
            Card card = reward.getCard();
            if (cardCounts.getOrDefault(card.getCardId(), 0) >= card.getRarity().getCardLimit()) {
                newRewards.add(ChestReward.extraCard(card, card.getRarity().getCoinValue()));
            } else {
                cardCounts.put(card.getCardId(), cardCounts.getOrDefault(card.getCardId(), 0) + 1);
                newRewards.add(reward);
            }
        }

        return newRewards;
    }
}
