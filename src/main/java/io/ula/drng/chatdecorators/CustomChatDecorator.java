package io.ula.drng.chatdecorators;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.ula.drng.Main;
import io.ula.config.*;
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
            String regex = element.getAsJsonObject().get("key").getAsString();
            String replacement = element.getAsJsonObject().get("replace").getAsString();
            message = message.replaceAll(regex,replacement);
        }
        return  Component.literal(message);
    }
}
