package net.stelitop.battledudestcg.discord.slashcommands.implementations.devcommands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateFields;
import net.stelitop.battledudestcg.TabletopSimulatorUtils;
import net.stelitop.battledudestcg.discord.framework.DiscordEventsComponent;
import net.stelitop.battledudestcg.discord.framework.InteractionEvent;
import net.stelitop.battledudestcg.discord.framework.commands.CommandParam;
import net.stelitop.battledudestcg.discord.framework.commands.SlashCommand;
import net.stelitop.battledudestcg.discord.slashcommands.implementations.autocomplete.DeckNameAutocomplete;
import net.stelitop.battledudestcg.discord.slashcommands.implementations.requirements.RequireAdmin;
import net.stelitop.battledudestcg.game.database.repositories.DeckRepository;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@DiscordEventsComponent
public class ExportDeckCommand {

    @Autowired
    private DeckRepository deckRepository;
    @Autowired
    private TabletopSimulatorUtils tabletopSimulatorUtils;

    @RequireAdmin
    @SlashCommand(
            name = "dev exportdeck",
            description = "Description."
    )
    public Mono<Void> testImageCommand(
            @InteractionEvent ChatInputInteractionEvent event,
            @CommandParam(
                    name = "deckname",
                    description = "The name of the deck",
                    autocomplete = DeckNameAutocomplete.class
            ) String deckName
    ) throws IOException {

        var decks = deckRepository.findCardDecksByUserId(event.getInteraction().getUser().getId().asLong());
        var deckOpt = decks.stream().filter(x -> x.getName().equalsIgnoreCase(deckName)).findFirst();
        if (deckOpt.isEmpty()) {
            return event.reply("No deck with this name was found!")
                    .withEphemeral(true);
        }

        event.deferReply().block();

        new File("./temp").mkdir();
        Mat mat = tabletopSimulatorUtils.createDeckCardSheet(deckOpt.get());
        String filename = "./temp/" + UUID.randomUUID() + ".png";
        Imgcodecs.imwrite(filename, mat);

        File initialFile = new File(filename);
        InputStream targetStream = new FileInputStream(initialFile);

        event.editReply()
                .withFiles(MessageCreateFields.File.of("img.png", targetStream))
                .withEmbeds(EmbedCreateSpec.builder()
                        .title("CardSheet of deck \"" + deckOpt.get().getName() + "\"")
                        .image("attachment://img.png")
                        .build()).block();

        targetStream.close();
        new File(filename).delete();

        return Mono.empty();
    }
}
