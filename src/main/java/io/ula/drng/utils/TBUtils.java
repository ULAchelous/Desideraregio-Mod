package io.ula.drng.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import io.ula.drng.Main;
import io.ula.config.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.objects.PlayerSprite;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;

import java.util.Random;
import java.util.UUID;


public class TBUtils {
    private static MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();
    public static void aliceBehaviour(){
        int owCnt = 0, netherCnt = 0, teCnt = 0;
        ServerLevel overworld = server.overworld();
        ServerLevel nether = server.getLevel(Level.NETHER);
        ServerLevel the_end = server.getLevel(Level.END);
        for (Entity entity : overworld.getAllEntities()) {
                if (entity.getType() == EntityType.ITEM) {
                    entity.remove(Entity.RemovalReason.DISCARDED);
                    owCnt++;
                }
        }
        for (Entity entity : nether.getAllEntities()) {
            if (entity.getType() == EntityType.ITEM) {
                entity.remove(Entity.RemovalReason.DISCARDED);
                netherCnt++;
            }
        }
        for (Entity entity : the_end.getAllEntities()) {
            if (entity.getType() == EntityType.ITEM) {
                entity.remove(Entity.RemovalReason.DISCARDED);
                teCnt++;
            }
        }
        if((owCnt | netherCnt | teCnt) != 0) {
            ResolvableProfile profile = ResolvableProfile.createResolved(new GameProfile(UUID.fromString("793be6b0-de85-412a-8483-636d6f8c74d0"),"AZ9C"));
            new GameProfile(UUID.fromString("793be6b0-de85-412a-8483-636d6f8c74d0"),"AZ9C");
            server.sendSystemMessage(Component.empty()
                    .append(Component.object(new PlayerSprite(profile,true)))
                    .append(Component.literal("["))
                    .append(Component.literal("AL-1S").setStyle(Style.EMPTY.withColor(0x76d7ea).applyFormat(ChatFormatting.BOLD)))
                    .append(Component.literal("]"))
                    .append(" ")
                    .append(Component.literal("吃掉了")
                            .append(" ")
                            .append(Component.literal(Integer.toString(owCnt + netherCnt + teCnt)).withStyle(ChatFormatting.AQUA,ChatFormatting.BOLD))
                            .append(" ")
                            .append(Component.literal("个掉落物！")))
            );
            server.sendSystemMessage(Component.literal("主世界：")
                    .append(Component.literal(Integer.toString(owCnt)).withStyle(ChatFormatting.YELLOW))
                    .append(" ")
                    .append(Component.literal("下界："))
                    .append(Component.literal(Integer.toString(netherCnt)).withStyle(ChatFormatting.YELLOW))
                    .append(" ")
                    .append(Component.literal("末地："))
                    .append(Component.literal(Integer.toString(teCnt)).withStyle(ChatFormatting.YELLOW))
            );
        }
    }

    public static void tipsBehaviour(){
        ConfigFile DRNG_TIPS = Main.getConfigManager().getConfig("drng:tips");
        JsonArray tips = DRNG_TIPS.getKey("tips").getAsJsonArray();
        server.sendSystemMessage(Component.literal("[")
                        .withStyle(ChatFormatting.BOLD)
                .append(Component.literal("提示").withStyle(ChatFormatting.YELLOW))
                .append(Component.literal("]:"))
        );
        server.sendSystemMessage(Component.literal(tips.get(new Random().nextInt(tips.size())).getAsString()));
    }

    public static Component getFlowingNoticeBoard(){
        Component nb = Component.literal("----------流动公告----------").append("\n");
        ConfigFile NOTICE = Main.getConfigManager().getConfig("drng:notices");
        if(NOTICE.has("notices")){
            for(JsonElement element : NOTICE.getKey("notices").getAsJsonArray()){
                JsonObject jsonObject = element.getAsJsonObject();
                nb = nb.copy().append(Component.literal(jsonObject.get("author").getAsString()).withStyle(ChatFormatting.GRAY).append(Component.literal(":")))
                        .append(" ")
                        .append(Component.literal(jsonObject.get("title").getAsString()))
                        .append("\n");
            }
            nb = nb.copy().append(Component.literal("点击查看详情").setStyle(Style.EMPTY.applyFormat(ChatFormatting.AQUA).withClickEvent(new ClickEvent.RunCommand("/notice list"))));
            return nb;
        }else{
            return null;
        }
    }
}
