package io.ula.drng.mixin;


import io.ula.drng.Main;

import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerMixin {

    @Redirect(method = "method_44900",at = @At(value = "INVOKE", target = "net/minecraft/server/MinecraftServer.getChatDecorator ()Lnet/minecraft/network/chat/ChatDecorator;"))
    private ChatDecorator injected(MinecraftServer server){
        return Main.CUSTOM_CHAT_DECORATOR;
    }
}
