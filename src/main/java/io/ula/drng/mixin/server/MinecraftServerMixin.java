package io.ula.drng.mixin.server;

import com.llamalad7.mixinextras.sugar.Local;
import io.ula.drng.scheduler.ServerScheduler;
import io.ula.drng.scheduler.ServerSchedulerHolder;
import io.ula.drng.utils.PlayerUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements ServerSchedulerHolder {
    private final ServerScheduler serverScheduler = new ServerScheduler();



    @Override
    public ServerScheduler drng$getServerSchedule(){
        return  this.serverScheduler;
    }

    @Inject(method = "tickChildren",at = @At("TAIL"))
    private void tickableInject(BooleanSupplier booleanSupplier, CallbackInfo ci){
        serverScheduler.tickable();
    }

    @Inject(method = "buildPlayerStatus",at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "net/minecraft/util/Util.shuffle (Ljava/util/List;Lnet/minecraft/util/RandomSource;)V"),order = 2,cancellable = true)
    private void playerStatusInject(CallbackInfoReturnable cir,@Local List<ServerPlayer> list,@Local ObjectArrayList<NameAndId> objectArrayList){
        MinecraftServer target = (MinecraftServer) (Object) this;
        int i = target.getMaxPlayers();
        int fixedPlayerCount = 0;
        for(ServerPlayer serverPlayer : list)
            if(!PlayerUtils.isFakePlayer(serverPlayer)) fixedPlayerCount ++;
        cir.setReturnValue(new ServerStatus.Players(i,fixedPlayerCount,objectArrayList));
    }
}
