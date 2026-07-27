package io.ula.drng.mixin.carpet.patches;

import carpet.patches.EntityPlayerMPFake;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityPlayerMPFake.class)
public class EntityPlayerMPFakeMixin {
    @ModifyVariable(method = "createFake",at = @At("HEAD"),argsOnly = true)
    private static String FPNameInject(String name){
        return name + "(bot)";
    }
}
