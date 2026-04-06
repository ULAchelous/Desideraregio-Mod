package io.ula.drng.mixin.server.network;

import io.ula.drng.dialog.DialogHelper;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
public class ServerCommonPacketListenerMixin {
    @Inject(method = "handleCustomClickAction",at = @At("TAIL"))
    private void handleCustomClick(ServerboundCustomClickActionPacket serverboundCustomClickActionPacket, CallbackInfo ci){
        if (!(((ServerCommonPacketListenerImpl) (Object) this instanceof ServerGamePacketListenerImpl playNetworkHandler))) return;
        DialogHelper.onHandleCustomClick(playNetworkHandler.player,serverboundCustomClickActionPacket);
    }
}
