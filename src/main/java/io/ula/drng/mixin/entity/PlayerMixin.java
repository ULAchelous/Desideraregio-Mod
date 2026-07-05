package io.ula.drng.mixin.entity;

import com.mojang.authlib.GameProfile;
import io.ula.drng.utils.PlayerUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.ObjectContents;
import net.minecraft.network.chat.contents.objects.PlayerSprite;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ResolvableProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {


    @Shadow
    public abstract Component getName();
    @Shadow
    public abstract GameProfile getGameProfile();

    @Inject(method = "decorateDisplayNameComponent",at = @At("RETURN"),cancellable = true)
    private void injected(MutableComponent mutableComponent,CallbackInfoReturnable<MutableComponent> cir){
        Component titles = PlayerUtils.getPlayerTitles(getName().getString());
        ResolvableProfile resolvableProfile = ResolvableProfile.createResolved(getGameProfile());
        MutableComponent returnValue =
                MutableComponent.create(new ObjectContents(new PlayerSprite(resolvableProfile,true)))
                        .append(Component.literal(" "))
                        .append(titles.copy())
                        .append(cir.getReturnValue());
        cir.setReturnValue(returnValue);
    }

    @Redirect(method = "rideTick",at = @At(value = "INVOKE", target = "net/minecraft/world/entity/player/Player.stopRiding ()V"))
    private void stopRidingInject(Player player){
        ServerPlayer serverPlayer;
        if(player instanceof ServerPlayer) {
            serverPlayer = (ServerPlayer) player;
        }else {
            player.stopRiding();
            return;
        }
        Entity vehicle = serverPlayer.getVehicle();
        if(vehicle.getType().equals(EntityType.PLAYER)){
            player.stopRiding();
            ClientboundSetPassengersPacket packet = new ClientboundSetPassengersPacket(vehicle);
            for(ServerPlayer p :  serverPlayer.level().getServer().getPlayerList().getPlayers()){
                p.connection.send(packet);
            }
        }
    }
}
