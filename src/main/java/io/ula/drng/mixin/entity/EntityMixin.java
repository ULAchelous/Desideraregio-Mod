package io.ula.drng.mixin.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class EntityMixin {
    @Redirect(method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z",at = @At(value = "INVOKE", target = "net/minecraft/world/entity/EntityType.canSerialize ()Z"))
    private boolean injected(EntityType entityType){
        if(entityType == EntityType.PLAYER)
            return true;
        else
            return entityType.canSerialize();
    }
}
