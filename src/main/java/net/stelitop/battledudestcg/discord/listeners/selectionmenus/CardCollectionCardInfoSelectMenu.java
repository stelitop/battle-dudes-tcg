package net.stelitop.battledudestcg.discord.listeners.selectionmenus;


import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import net.stelitop.battledudestcg.discord.ui.CardCollectionUI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CardCollectionCardInfoSelectMenu implements ApplicationRunner {

    @Autowired
    private GatewayDiscordClient client;

    @Override
    public void run(ApplicationArguments args) {
        client.on(SelectMenuInteractionEvent.class, this::handle).subscribe();
    }

    private Mono<Void> handle(SelectMenuInteractionEvent event) {
        String eventId = event.getCustomId();
        String idPrefix = "opencardinfopage|";
        if (!eventId.startsWith(idPrefix)) return Mono.empty();
        System.out.println(eventId);
        CardCollectionUI.Model model = CardCollectionUI.Model.deserialize(eventId.substring(idPrefix.length()));

        return Mono.empty();
    }
}
