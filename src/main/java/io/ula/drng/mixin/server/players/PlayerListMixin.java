package io.ula.drng.mixin.server.players;

import io.ula.drng.dialog.CustomDialogs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.common.ClientboundShowDialogPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @Shadow
    public abstract void broadcastSystemMessage(Component component, boolean bl);

    @Redirect(method = "placeNewPlayer",at = @At(value = "INVOKE", target = "net/minecraft/server/players/PlayerList.broadcastSystemMessage (Lnet/minecraft/network/chat/Component;Z)V"))
    private void modifyLoginMsg(PlayerList playerList,Component component,boolean b1){
        Component playerName = Component.literal("Unknown");
        if(component.copy().getContents()  instanceof TranslatableContents){
            playerName = (Component) (((TranslatableContents)component.copy().getContents()).getArgument(0));
        }
        Component loginMsg = playerName.copy().append(Component.literal("，欢迎回来～")
                .withStyle(ChatFormatting.WHITE,ChatFormatting.BOLD,ChatFormatting.ITALIC));
        this.broadcastSystemMessage(loginMsg,b1);
    }

//    @Inject(method = "placeNewPlayer",at = @At(value = "INVOKE", target = "net/minecraft/server/network/ServerGamePacketListenerImpl.send (Lnet/minecraft/network/protocol/Packet;)V",ordinal = 0,shift = At.Shift.AFTER),cancellable = true)
//    private void showEula(Connection connection, ServerPlayer serverPlayer, CommonListenerCookie commonListenerCookie,CallbackInfo ci){
//        connection.send(new ClientboundShowDialogPacket(Holder.direct(CustomDialogs.EULA_DIALOG)));
//        ci.cancel();
//    }
}
