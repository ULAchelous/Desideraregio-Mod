package io.ula.drng.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.network.ServerGamePacketListenerImpl$1")
public class ServerGamePacketListenerHolderMixin{

    @Shadow
    @Final
    private Entity val$target;

    @Shadow
    @Final
    private ServerGamePacketListenerImpl field_28963;

    @Inject(method = "performInteraction(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/server/network/ServerGamePacketListenerImpl$EntityInteraction;)V",at = @At("HEAD"),cancellable = true)
    private void playerPassegerBehaviour(InteractionHand interactionHand, ServerGamePacketListenerImpl.EntityInteraction entityInteraction, CallbackInfo ci){
        ServerPlayer excutor = field_28963.player;
        if(
                interactionHand == InteractionHand.MAIN_HAND //判断主手
                && !val$target.getPassengers().contains(field_28963.player)//是否已经骑乘
        ){
            Boolean b = excutor.startRiding(val$target,true,false);

            if(b)
                excutor.sendSystemMessage(Component.literal("Injected!"));
            else
                excutor.sendSystemMessage(Component.literal(":("));
            ci.cancel();
        }
    }
}