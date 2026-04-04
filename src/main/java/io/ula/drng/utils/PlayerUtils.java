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
//
//    public static String getPlayerChatMsg(String message,Player player){
//        ConfigFile CHAT_REPLACEMENTS = ownerPlugin.getConfigManager().getConfig(Key.key("drng:chat_replacements"));
//        if(!CHAT_REPLACEMENTS.has(player.getName()))
//            CHAT_REPLACEMENTS.addKey(player.getName(),new JsonArray());
//        JsonArray array = CHAT_REPLACEMENTS.getKey(player.getName()).getAsJsonArray();
//        for (int i=0;i<array.size();i++){
//            JsonElement element = array.get(i);
//            if (element.getAsJsonObject().has("removed")){
//                CHAT_REPLACEMENTS.getKey(player.getName()).getAsJsonArray().remove(element);
//                continue;
//            }
//            message+="☐";
//            String key = element.getAsJsonObject().get("key").getAsString();
//            String replace = element.getAsJsonObject().get("replace").getAsString();
//            String[] temp = message.split(key);
//            message="";
//            int len = temp.length;
//            if(temp[len -1].equals("☐")){
//                for(int idx = 0; idx < len-1; idx++) message += temp[idx] + replace;
//            }else {
//                for (int idx = 0; idx < len; idx++) {
//                    if(idx == len-1)
//                        message += temp[idx].substring(0,temp[idx].length()-1);
//                    else
//                        message += temp[idx]+replace;
//                }
//            }
//        }
//        return  message;
//    }
//
//    public static Component getPlayerLoginMsg(Player player){
//        ConfigFile PLAYER_TITLES = ownerPlugin.getConfigManager().getConfig(Key.key("drng:titles"));
//        if(player.isOp())
//            return  Component.empty();
//        Component loginMsg = Component.text("");
//        if(PLAYER_TITLES.has(player.getName()))
//            loginMsg = loginMsg.append(PlayerUtils.getPlayerTitles(player));
//        loginMsg = loginMsg.append(Component.text(player.getName()))
//                .append(Component.text("，欢迎回来～").decorate(TextDecoration.BOLD));
//        return loginMsg;
//    }
//
//    public static void playerUpdateOnlineTime(JavaPlugin plugin){
//        for (Player player : getServer().getOnlinePlayers()) {
//            if (player.hasMetadata("onlineTime")) {
//                int value = player.getMetadata("onlineTime").getFirst().asInt();
//                player.setMetadata("onlineTime", new FixedMetadataValue(plugin, value + 1));
//                ScoreBoardHelper.updateScores(player, 3);
//            }
//        }
//    }
//
//
    public static void initPlayerStatus(ServerPlayer player){
        if(player.getAttached(Attachments.PLAYER_STATUS_DATA) == null)
            player.setAttached(Attachments.PLAYER_STATUS_DATA,new PlayerStatusData(null,null,null));
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
