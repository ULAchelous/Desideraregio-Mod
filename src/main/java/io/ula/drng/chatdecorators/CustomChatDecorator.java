package io.ula.drng.chatdecorators;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.ula.drng.Main;
import io.ula.drng.config.ConfigFile;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

public class CustomChatDecorator implements ChatDecorator {
    @Override
    public Component decorate(@Nullable ServerPlayer serverPlayer, Component component) {
        String message = component.getString();
        ConfigFile CHAT_REPLACEMENTS = Main.getConfigManager().getConfig("drng:chat_replacements");
        if(!CHAT_REPLACEMENTS.has(serverPlayer.getName().getString()))
            CHAT_REPLACEMENTS.addKey(serverPlayer.getName().getString(),new JsonArray());
        JsonArray array = CHAT_REPLACEMENTS.getKey(serverPlayer.getName().getString()).getAsJsonArray();
        for (int i=0;i<array.size();i++){
            JsonElement element = array.get(i);
            if (element.getAsJsonObject().has("removed")){
                CHAT_REPLACEMENTS.getKey(serverPlayer.getName().getString()).getAsJsonArray().remove(element);
                continue;
            }
            message+="☐";
            String key = element.getAsJsonObject().get("key").getAsString();
            String replace = element.getAsJsonObject().get("replace").getAsString();
            String[] temp = message.split(key);
            message="";
            int len = temp.length;
            if(temp[len -1].equals("☐")){
                for(int idx = 0; idx < len-1; idx++) message += temp[idx] + replace;
            }else {
                for (int idx = 0; idx < len; idx++) {
                    if(idx == len-1)
                        message += temp[idx].substring(0,temp[idx].length()-1);
                    else
                        message += temp[idx]+replace;
                }
            }
        }
        return  Component.literal(message);
    }
}
