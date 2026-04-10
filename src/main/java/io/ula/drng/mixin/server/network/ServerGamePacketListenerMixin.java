package io.ula.drng.mixin.server.network;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ula.drng.Main;

import io.ula.drng.attachments.Attachments;
import io.ula.drng.attachments.PlayerStatusData;
import io.ula.drng.config.ConfigFile;
import io.ula.drng.utils.PlayerUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerMixin {
    private MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();
    @Shadow
    private ServerPlayer player;

    @Redirect(method = "method_44900",at = @At(value = "INVOKE", target = "net/minecraft/server/MinecraftServer.getChatDecorator ()Lnet/minecraft/network/chat/ChatDecorator;"))
    private ChatDecorator injected(MinecraftServer server){
        return Main.CUSTOM_CHAT_DECORATOR;
    }

    @Redirect(method = "removePlayerFromWorld",at = @At(value = "INVOKE",target = "net/minecraft/server/players/PlayerList.broadcastSystemMessage (Lnet/minecraft/network/chat/Component;Z)V"))
    private void modifyQuitMsg(PlayerList playerList, Component component,boolean b1){
        Component playerName = Component.literal("Unknown");
        if(component.copy().getContents()  instanceof TranslatableContents){
            playerName = (Component) (((TranslatableContents)component.copy().getContents()).getArgument(0));
        }
        Component loginMsg = playerName.copy().append(Component.literal("，再见～")
                .withStyle(ChatFormatting.WHITE,ChatFormatting.BOLD,ChatFormatting.ITALIC));
        playerList.broadcastSystemMessage(loginMsg,b1);
    }

    @Inject(method="handleMovePlayer",at = @At("TAIL"))
    private void movementInject(CallbackInfo ci){
        ServerPlayer sender = this.player;
        PlayerStatusData data = sender.getAttached(Attachments.PLAYER_STATUS_DATA);
        if(data!=null&& data.is_controlling().isPresent()){
            ServerPlayer target = this.server.getPlayerList().getPlayer(data.is_controlling().get());

            ClientboundSetActionBarTextPacket controllerPacket = new ClientboundSetActionBarTextPacket(Component.literal(String.format("你正在控制%s!", target.getName().getString())).withStyle(ChatFormatting.RED));
            ClientboundSetActionBarTextPacket targetPacket = new ClientboundSetActionBarTextPacket(Component.literal(String.format("你正在被 %s 控制!", player.getName().getString())).withStyle(ChatFormatting.RED));
            player.connection.send(controllerPacket);
            target.connection.send(targetPacket);
            double nx= player.getX(), nz=player.getZ();
            switch(player.getDirection()){
                case NORTH -> nz+=0.6;
                case SOUTH -> nz=nz-0.6;
                case WEST -> nx +=0.6;
                case EAST -> nx = nx-0.6;
            }
            target.teleportTo(sender.level(),
                    nx,
                    player.getY(),
                    nz,
                    Collections.emptySet(),
                    player.getYRot(),
                    player.getXRot(),
                    false
            );
        }
    }

}
