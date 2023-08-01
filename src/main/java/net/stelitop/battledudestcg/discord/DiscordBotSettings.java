package net.stelitop.battledudestcg.discord;

import lombok.Data;

import java.util.Map;

@Data
public class DiscordBotSettings {

    /**
     * Token for the discord bot used in production.
     */
    private String productionToken;

    /**
     * Token for the discord bot used in testing.
     */
    private String testToken;

    /**
     * Discord ids of the admin users.
     */
    private long[] adminUsers;

    /**
     * Mapping of each channel chest to the id of the channel.
     */
    private Map<String, Long> channelChestLocations;
}
