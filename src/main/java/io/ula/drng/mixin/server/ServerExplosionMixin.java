package io.ula.drng.mixin.server;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ServerExplosion.class)
public class ServerExplosionMixin {
    @Shadow @Final
    private Entity source;


    @Shadow
    @Final
    private ServerLevel level;

    @Inject(method = "calculateExplodedPositions",at = @At("HEAD"),cancellable = true)
    private void removeCreeperExplosion(CallbackInfoReturnable<List<BlockPos>> cir){
        if(this.source.getType().equals(EntityTypes.CREEPER)){
            cir.setReturnValue(new ObjectArrayList<>());
        }
    }

    @Inject(method = "explode",at = @At(value = "RETURN",shift = At.Shift.BEFORE))
    private void spawnFireworkRocketEntity(CallbackInfoReturnable<Integer> cir){
        if(source!=null && source.getType().equals(EntityTypes.CREEPER)){
            Vec3 pos = source.position().add(new Vec3(0,2,0));
            Level level = source.level();
            FireworkExplosion fireworkExplosion = new FireworkExplosion(
                    FireworkExplosion.Shape.CREEPER,
                    IntList.of(0x00FF00),
                    IntList.of(0xCCFF33),
                    true,
                    true
            );
            Fireworks fireworkComponent = new Fireworks(0,List.of(fireworkExplosion));
            ItemStack fireworkStack = new ItemStack(Items.FIREWORK_ROCKET);
            fireworkStack.set(DataComponents.FIREWORKS,fireworkComponent);

            Projectile fireworkRocketProjectile = new FireworkRocketEntity(
                    level,
                    null,
                    pos.x + (double) Direction.UP.getStepX() * 0.15,
                    pos.y + (double) Direction.UP.getStepY() * 0.15,
                    pos.z + (double) Direction.UP.getStepZ() * 0.15,
                    fireworkStack
            );

            fireworkRocketProjectile.applyComponentsFromItemStack(fireworkStack);
            fireworkRocketProjectile.setDeltaMovement(new Vec3(0,0.5,0));
            fireworkRocketProjectile.setInvulnerable(true);

            if(level instanceof ServerLevel) {
                Projectile.spawnProjectile(fireworkRocketProjectile,(ServerLevel) level,fireworkStack);
            }
        }
    }
}
