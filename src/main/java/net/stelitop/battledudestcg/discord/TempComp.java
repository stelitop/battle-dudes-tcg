package net.stelitop.battledudestcg.discord;

import discord4j.core.GatewayDiscordClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import net.stelitop.mad4j.SlashCommandRegistrar;

@Component
public class TempComp implements ApplicationRunner {

    @Autowired
    private GatewayDiscordClient gatewayDiscordClient;
    @Autowired
    private SlashCommandRegistrar slashCommandRegistrar;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println(slashCommandRegistrar);
        System.out.println(gatewayDiscordClient);
    }
}
