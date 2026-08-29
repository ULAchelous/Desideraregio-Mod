package io.ula.drng.utils.kei;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ula.api.config.ConfigFile;
import io.ula.drng.Main;
import io.ula.drng.utils.Util;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class PucaUtil {
    private static MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();
    private static ConfigFile keiConfig;

    private PucaUtil(){}

    public static void pollMessages(){
        if(keiConfig == null)
            keiConfig = Main.getConfigManager().getConfig("drng:kei");
        try {
            if(!keiConfig.getKey("use_puca_bot").getAsBoolean())
                return;
            JsonObject puca = keiConfig.getKey("puca").getAsJsonObject();
            if(!puca.get("message_poll").getAsBoolean())
                return;
            String api = puca.get("api").getAsString();
            if(api.isBlank()) return;
            String pollBase = api.endsWith("/chat/completions")
                    ? api.substring(0, api.length() - "/chat/completions".length()) : api;
            String pollUrl = (pollBase.endsWith("/") ? pollBase : pollBase + "/")
                    + "poll_msg?cid=mc-desideraregio";
            CompletableFuture.runAsync(() -> {
                try {
                    JsonObject resp = Util.requestAPIAsJson(new URL(pollUrl), 5000, 10000);
                    if(resp == null || !resp.has("messages")) return;
                    JsonArray messages = resp.getAsJsonArray("messages");
                    for(JsonElement e : messages){
                        JsonObject m = e.getAsJsonObject();
                        String type = m.get("type").getAsString();
                        String content = m.get("content").getAsString();
                        if("chat".equals(type)){
                            KeiChatBotUtils.sendReply(content, null);
                        } else if("action".equals(type) && "broadcast".equals(m.get("action").getAsString())){
                            MinecraftServer srv = server;
                            if(srv == null) continue;
                            srv.execute(() -> {
                                for(ServerPlayer p : srv.getPlayerList().getPlayers())
                                    p.sendSystemMessage(Component.literal(content));
                            });
                        }
                    }
                } catch (Exception ex){
                }
            });
        } catch (Exception e){

        }
    }
}
