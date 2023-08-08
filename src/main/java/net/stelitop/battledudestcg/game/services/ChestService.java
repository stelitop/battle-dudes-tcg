package net.stelitop.battledudestcg.game.services;

import net.stelitop.battledudestcg.discord.DiscordBotSettings;
import net.stelitop.battledudestcg.game.database.entities.chests.ChannelChest;
import net.stelitop.battledudestcg.game.database.entities.chests.Chest;
import net.stelitop.battledudestcg.game.database.repositories.ChestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class ChestService {

    @Autowired
    private ChestRepository chestRepository;
    @Autowired
    private DiscordBotSettings discordBotSettings;
    @Autowired
    private Random random;

    public @Nullable Chest getChestOfChannel(long channelId) {
        Map<String, Long> chestLocations = discordBotSettings.getChannelChestLocations();

        Optional<String> chestNameOpt = chestLocations.keySet().stream()
                .filter(k -> chestLocations.get(k) == channelId)
                .findFirst();

        if (chestNameOpt.isEmpty()) {
            return null;
        }
        String chestName = chestNameOpt.get();
        Optional<Chest> chestOpt = chestRepository.findByName(chestName);
        return chestOpt.orElse(null);
    }

    public Optional<Long> getChannelIdOfChest(ChannelChest chest) {
        Long id = discordBotSettings.getChannelChestLocations()
                .getOrDefault(chest.getName(), null);
        return id == null ? Optional.empty() : Optional.of(id);
    }

    public @Nullable Chest getChest(String name) {
        Optional<Chest> chest = chestRepository.findByNameIgnoreCase(name);
        return chest.orElse(null);
    }

    public @Nullable Chest getRandomChest() {
        if (chestRepository.count() == 0) {
            return null;
        }

        List<String> chestNames = chestRepository.getAllChestNames();
        String picked = chestNames.get(random.nextInt(chestNames.size()));
        return chestRepository.findByName(picked).orElse(null);
    }
}
