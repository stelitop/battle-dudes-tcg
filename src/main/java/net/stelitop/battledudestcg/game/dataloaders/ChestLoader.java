package net.stelitop.battledudestcg.game.dataloaders;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.stelitop.battledudestcg.game.database.entities.chests.ChannelChest;
import net.stelitop.battledudestcg.game.database.entities.chests.Chest;
import net.stelitop.battledudestcg.game.database.repositories.ChestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class ChestLoader implements ApplicationRunner {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ResourceLoader resourceLoader;
    @Autowired
    private ChestRepository chestRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ObjectMapper om = new ObjectMapper();
        List<ChannelChest> chestsFromFile = Arrays.stream(om.readValue(
                resourceLoader.getResource("classpath:chestdata.json").getInputStream(),
                ChannelChest[].class
        )).toList();

        List<ChannelChest> chestsInDb = StreamSupport.stream(chestRepository.findAll().spliterator(), false)
                .filter(x -> x instanceof ChannelChest)
                .map(x -> (ChannelChest) x)
                .toList();

        Map<String, Long> chestNameToId = chestsInDb.stream()
                .collect(Collectors.toMap(Chest::getName, Chest::getChestId));

        Set<String> chestNames = chestNameToId.keySet();

        List<ChannelChest> missingChests = chestsFromFile.stream()
                .filter(x -> !chestNames.contains(x.getName()))
                .toList();

        chestsFromFile.forEach(c -> c.setChestId(chestNameToId.get(c.getName())));

        chestRepository.saveAll(missingChests);
        chestRepository.saveAll(chestsFromFile);

        //chestRepository.saveAll(Arrays.stream(chests).toList());

        if (missingChests.isEmpty()) {
            LOGGER.info("No chests had to be freshly added to the database.");
        } else {
            LOGGER.warn("The following chests were added freshly to the database: " + missingChests.stream()
                    .map(Chest::getName).collect(Collectors.joining(", ")) + ".");
        }
    }
}
