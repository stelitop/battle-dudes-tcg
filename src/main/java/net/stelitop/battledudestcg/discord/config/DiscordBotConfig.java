package net.stelitop.battledudestcg.discord.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.rest.RestClient;
import net.stelitop.battledudestcg.discord.DiscordBotSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

@Configuration
public class DiscordBotConfig {

    @Autowired
    private Environment environment;
    @Autowired
    private ResourceLoader resourceLoader;

    /**
     * Bean for the discord bot token.
     *
     * @return Token bean.
     */
    @Bean
    public DiscordBotSettings discordBotSettings() throws IOException {
        ObjectMapper om = new ObjectMapper();
        return om.readValue(
                resourceLoader.getResource("classpath:botconfig.json").getInputStream(),
                DiscordBotSettings.class
        );
    }

    /**
     * Bean for the discord gateway client.
     *
     * @return Gateway discord client bean.
     */
    @Bean
    public GatewayDiscordClient gatewayDiscordClient(DiscordBotSettings botSettings) {
        boolean isDevmode = Boolean.parseBoolean(environment.getProperty("devmode"));
        String tokenStr = isDevmode ? botSettings.getTestToken() : botSettings.getProductionToken();

        return DiscordClientBuilder.create(tokenStr).build()
                .gateway()
                .setInitialPresence(ignore -> ClientPresence.online(ClientActivity.playing("Battle Dudes")))
                .login()
                .block();
    }

    @Bean
    public RestClient discordRestClient(GatewayDiscordClient gatewayDiscordClient) {
        return gatewayDiscordClient.getRestClient();
    }
}
