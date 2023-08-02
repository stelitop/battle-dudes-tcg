package net.stelitop.battledudestcg.game;

import net.stelitop.battledudestcg.discord.DiscordBotSettings;
import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import net.stelitop.battledudestcg.game.database.entities.cards.DudeCard;
import net.stelitop.battledudestcg.game.database.entities.chests.Chest;
import net.stelitop.battledudestcg.game.database.repositories.CardRepository;
import net.stelitop.battledudestcg.game.database.repositories.ChestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class TempController implements ApplicationRunner {

    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private ChestRepository chestRepository;
    @Autowired
    private DiscordBotSettings discordBotSettings;

    private Map<Long, List<Chest>> idToChest = null;

    private void initMapping() {
        System.out.println("Initing");
        idToChest = new HashMap<>();
        for (var name : discordBotSettings.getChannelChestLocations().keySet()) {
            System.out.println("name = " + name);
            long channelId = discordBotSettings.getChannelChestLocations().get(name);
            Optional<Chest> chestOpt = chestRepository.findByName(name);
            if (chestOpt.isEmpty()) continue;
            Chest chest = chestOpt.get();

            List<Chest> curChests = idToChest.getOrDefault(channelId, new ArrayList<>());
            curChests.add(chest);
            System.out.println(chest.getName());
            idToChest.put(channelId, curChests);
        }
        System.out.println(idToChest);
    }

    private List<Chest> channelIdToChest(long id) {
        if (idToChest == null) {
            initMapping();
        }
        return idToChest.get(id);
    }

    //@PostMapping("/addcard")
    public ResponseEntity<Void> test(@RequestBody ForeignDude dude) {
        System.out.println(dude);
        Card card = DudeCard.builder()
                .name(dude.getName())
                .artists(Arrays.stream(dude.getArtistName().split(",")).
                        map(String::trim).toList())
                .artUrl(dude.getArtLink())
                .chestSources(dude.getLocations().stream()
                        .map(this::channelIdToChest)
                        .flatMap(Collection::stream)
                        .toList())
                .cost(0)
                .defense(dude.getDefense())
                .dudeId(dude.getId())
                .effectText("")
                .flavorText(dude.getFlavorText())
                .health(dude.getHealth())
                .nextEvolutions(dude.getNextEvolutions())
                .offense(dude.getOffense())
                .previousEvolutions(dude.getPreviousEvolutions())
                .rarity(dude.getRarity())
                .resistances(dude.getResistances())
                .stage(dude.getStage())
                .types(dude.getTypes())
                .weaknesses(dude.getWeaknesses())
                .build();

        System.out.println(card.getChestSources().size());
        card = cardRepository.save(card);
        System.out.println(card);
        return ResponseEntity.ok().build();
        //return ResponseEntity.ok().build();
    }

    @Override
    public void run(ApplicationArguments args) {
//        System.out.println("All cards:");
//        var cards = cardRepository.findAll();
//        for (var card : cards) {
//            System.out.println(card);
//            System.out.println(card.getChestSources().size());
//        }
    }
}
