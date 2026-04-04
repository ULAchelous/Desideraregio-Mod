package io.ula.drng.mixin;

import io.ula.drng.Main;
import net.minecraft.server.dedicated.ServerWatchdog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWatchdog.class)
public class ServerWatchDogMixin {
    @Inject(method = "exit()V",at = @At("TAIL"))
    private void autoSave(CallbackInfo ci){
        Main.getConfigManager().onDisabled();
    }
}
