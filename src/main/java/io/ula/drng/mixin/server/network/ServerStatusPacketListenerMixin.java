package io.ula.drng.mixin.server.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import io.ula.api.motd.CustomMotdHolder;
import io.ula.drng.utils.TBUtils;
import io.ula.drng.utils.Util;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerStatusPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

@Mixin(ServerStatusPacketListenerImpl.class)
public class ServerStatusPacketListenerMixin {
    private static volatile String cachedHitokoto = "";
    private static volatile long ttl = 60*1000;
    private static volatile long lastFetched = 0;
    private static volatile Boolean isFetching = false;

    @Inject(method = "handleStatusRequest",at = @At("HEAD"))
    private void hitokotoInject(CallbackInfo ci){
        CustomMotdHolder holder =  (CustomMotdHolder) FabricLoader.getInstance().getGameInstance();
        long now = System.currentTimeMillis();
        if(now - lastFetched > ttl && ! isFetching){
            isFetching = true;
            CompletableFuture.runAsync(() -> {
                try {
                    JsonObject response = Util.requestAPIAsJson(new URL("https://v1.hitokoto.cn"));

                    String hitokoto = response.get("hitokoto").getAsString();
                    JsonElement e1 = response.get("from");
                    String from = !e1.isJsonNull() ? e1.getAsString() : "未知";
                    JsonElement e2 = response.get("from_who");
                    String from_who = !e2.isJsonNull() ? e2.getAsString() : "佚名";
                    String motd = String.format("§f§o“%s”\n   —— §e%s§b「%s」",hitokoto,from_who,from);

                    cachedHitokoto = motd;
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    isFetching = false;
                    lastFetched = now;
                }
            });
        }
        if(!cachedHitokoto.isBlank())
            holder.setMotd(cachedHitokoto, CustomMotdHolder.Type.DIRECT_REPLACE);
    }
}
