package io.ula.drng.mixin.server.network;


import com.llamalad7.mixinextras.sugar.Local;
import io.ula.drng.Main;

import io.ula.drng.attachments.Attachments;
import io.ula.drng.attachments.PlayerStatusData;
import io.ula.drng.utils.kei.KeiChatBotUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerMixin {
    private MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();
    @Shadow
    private ServerPlayer player;

    @Inject(method = "lambda$handleChat$0(Lnet/minecraft/network/protocol/game/ServerboundChatPacket;Ljava/util/Optional;)V",at = @At("TAIL"))
    private void KeyChatMsgListener(CallbackInfo callbackInfo, @Local PlayerChatMessage signedMessage){
        String literalMsg = signedMessage.decoratedContent().getString();
        KeiChatBotUtils.appendMsg(literalMsg,signedMessage.sender());
        if(!KeiChatBotUtils.map.containsKey(signedMessage.sender()))
            KeiChatBotUtils.map.put(signedMessage.sender(),false);
        if(KeiChatBotUtils.map.get(signedMessage.sender())){
            KeiChatBotUtils.onChat(literalMsg,signedMessage.sender());
            KeiChatBotUtils.map.put(signedMessage.sender(),false);
        }
        if(literalMsg.contains("kei") || literalMsg.contains("凯伊") || literalMsg.contains("柯伊") || literalMsg.contains("ケイ") || literalMsg.contains("爱丽丝") || literalMsg.contains("王女") || literalMsg.contains("AL-1S")){
            KeiChatBotUtils.map.put(signedMessage.sender(),true);
            KeiChatBotUtils.onChat(literalMsg,signedMessage.sender());
        }

    }

    @Redirect(method = "lambda$handleChat$0(Lnet/minecraft/network/protocol/game/ServerboundChatPacket;Ljava/util/Optional;)V",at = @At(value = "INVOKE", target = "net/minecraft/server/MinecraftServer.getChatDecorator ()Lnet/minecraft/network/chat/ChatDecorator;"))
    private ChatDecorator injected(MinecraftServer server){
        return Main.CUSTOM_CHAT_DECORATOR;
    }

    @Inject(method = "handleInteract",at = @At(value = "INVOKE", target = "net/minecraft/server/level/ServerPlayer.isWithinEntityInteractionRange (Lnet/minecraft/world/phys/AABB;D)Z",shift = At.Shift.AFTER))
    private void playerPassengerBehaviour(CallbackInfo callbackInfo, @Local ServerboundInteractPacket interactPacket, @Local Entity target){
        ServerPlayer excutor = player;
        MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();
        InteractionHand interactionHand = interactPacket.hand();
        if(
                interactionHand == InteractionHand.MAIN_HAND
                        && target instanceof Player
                        && !target.getPassengers().contains(player)
                        && player.getItemInHand(interactionHand).is(Items.AIR)
        ){
            Boolean b = excutor.startRiding(target,true,true);
            if(b){
                ClientboundSetPassengersPacket packet = new ClientboundSetPassengersPacket(target);
                for(ServerPlayer player : server.getPlayerList().getPlayers()){
                    player.connection.send(packet);
                }
            }
//            ci.cancel();
        }
    }

    @Inject(method = "handleAttack",at = @At(value = "INVOKE", target = "net/minecraft/world/item/ItemStack.has (Lnet/minecraft/core/component/DataComponentType;)Z",shift = At.Shift.AFTER))
    private void playerPassengerUnrideBehaviour(CallbackInfo callbackInfo,@Local Entity target){
        ServerPlayer excutor = player;
        MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();
        if(excutor.getPassengers().contains(target)) {
            target.stopRiding();
            ClientboundSetPassengersPacket packet = new ClientboundSetPassengersPacket(excutor);
            for(ServerPlayer player : server.getPlayerList().getPlayers()){
                player.connection.send(packet);
            }
        }
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
