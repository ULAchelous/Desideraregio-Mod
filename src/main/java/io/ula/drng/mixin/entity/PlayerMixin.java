package io.ula.drng.mixin;

import com.mojang.authlib.GameProfile;
import io.ula.drng.utils.PlayerUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.ObjectContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.objects.AtlasSprite;
import net.minecraft.network.chat.contents.objects.PlayerSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ResolvableProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.logging.LogManager;
import java.util.logging.Logger;

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
}
