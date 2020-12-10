package app;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import reactor.core.publisher.Mono;
import support.BotSupport;
import support.ExtraBotSupport;
import support.VoiceSupport;

public class Main {

    public static void main(String[] args) {

        //GatewayDiscordClient client = DiscordClient.create(System.getenv("token")).login().block();
        GatewayDiscordClient client = DiscordClient.create("Nzg2NjM3ODkwNjA3Nzc1ODA0.X9JToA.WIS3I5nCw8nUyBm4nQKDcORJDck").login().block();
        Mono.when(
                BotSupport.create(client).eventHandlers(),
                ExtraBotSupport.create(client).eventHandlers(),
                VoiceSupport.create(client).eventHandlers())
                .block();
    }
}
