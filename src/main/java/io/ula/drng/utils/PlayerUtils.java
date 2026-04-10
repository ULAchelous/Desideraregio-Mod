package io.ula.drng.utils;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.ula.drng.Main;
import io.ula.drng.attachments.Attachments;
import io.ula.drng.attachments.PlayerCntData;
import io.ula.drng.attachments.PlayerStatusData;
import io.ula.drng.config.ConfigFile;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;




import java.awt.*;
import java.util.Optional;


public class PlayerUtils {
    private static final Logger LOGGER = LogManager.getLogger();
    public static Component getPlayerTitles(String playerName) {
        Component component = Component.empty();
        ConfigFile PLAYER_TITLES = Main.getConfigManager().getConfig("drng:titles");
        if(PLAYER_TITLES.has(playerName)) {
            try {
                for (JsonElement title : PLAYER_TITLES.getKey(playerName).getAsJsonArray()) {
                    try {
                        component = Component.empty()
                                .append(Component.literal("["))
                                .append(ComponentSerialization.CODEC
                                        .decode(JsonOps.INSTANCE,title)
                                        .getOrThrow()
                                        .getFirst())//title component
                                .append(Component.literal("] "))//spacer
                        ;
                    } catch (Exception e) {
                        LOGGER.error("Error while loading Player Titles");
                        LOGGER.error("Error in ./config/player_titles.json");
                        LOGGER.error("Not a valid Component object!");
                        return Component.empty();
                    }
                }
            } catch (ClassCastException e) {
                LOGGER.error("Error while loading Player Titles");
                LOGGER.error("Error in ./config/player_titles.json");
                LOGGER.error(String.format("\"%s\" : ...<(HERE)", playerName));
                LOGGER.error("Wrong JsonElement,need JsonArray!");
                return Component.empty();
            }
        }
        return component;
    }
    public static void initPlayerStatus(ServerPlayer player){
        if(player.getAttached(Attachments.PLAYER_STATUS_DATA) == null)
            player.setAttached(Attachments.PLAYER_STATUS_DATA,new PlayerStatusData(Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty()));
        if(player.getAttached(Attachments.PLAYER_CNT_DATA) == null)
            player.setAttached(Attachments.PLAYER_CNT_DATA,new PlayerCntData(0,0,0));
        //init Metadata

//        ConfigFile CONFIG = ownerPlugin.getConfigManager().getConfig(Key.key("drng:main"));
//
//        if(player.isOp()&&CONFIG.getKey("balancedOp").getAsBoolean()) player.setGameMode(GameMode.SPECTATOR);
//        if (player.getGameMode().equals(GameMode.SPECTATOR) && !player.isOp())
//            player.setGameMode(GameMode.SURVIVAL);
    }
}
