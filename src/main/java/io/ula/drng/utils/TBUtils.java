package io.ula.drng.utils;

import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import io.ula.api.scheduler.ScheduleTask;
import io.ula.api.scheduler.ServerScheduler;
import io.ula.api.scheduler.ServerSchedulerHolder;
import io.ula.drng.Main;
import io.ula.api.config.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.objects.PlayerSprite;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.UUID;


public class TBUtils {
    private static MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();
    private static volatile int al1sTimerCnt = 3;
    public static void alice(){

        ResolvableProfile profile = Util.buildProfile(UUID.fromString("793be6b0-de85-412a-8483-636d6f8c74d0"));
        Component component = Component.empty()
                .append(Component.object(new PlayerSprite(profile,true)))
                .append(Component.literal("["))
                .append(Component.literal("AL-1S").setStyle(Style.EMPTY.withColor(0x76d7ea).applyFormat(ChatFormatting.BOLD)))
                .append(Component.literal("] 距离下次清理还有"));

        ServerScheduler scheduler = ((ServerSchedulerHolder)server).getServerSchedule();
        scheduler.runTask(new ScheduleTask("cleanTimer",(server,task) -> {
            if(al1sTimerCnt <= 0) {
                aliceBehaviour();
                al1sTimerCnt = 3;
                task.cancel();
            }else {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    player.sendSystemMessage(component.copy().append(Component.literal(Integer.toString(al1sTimerCnt)).withStyle(ChatFormatting.AQUA)).append("分钟"));
                }
                al1sTimerCnt--;
            }
        },0,60*20));
    }
    public static void aliceBehaviour(){
        int owCnt = 0, netherCnt = 0, teCnt = 0;
        ServerLevel overworld = server.overworld();
        ServerLevel nether = server.getLevel(Level.NETHER);
        ServerLevel the_end = server.getLevel(Level.END);

        List<? extends ItemEntity> owEntities = overworld.getEntities(EntityTypeTest.forClass(ItemEntity.class),itemEntity -> itemEntity.isAlive());
        List<? extends ItemEntity> tnEntities = nether.getEntities(EntityTypeTest.forClass(ItemEntity.class),itemEntity -> itemEntity.isAlive());
        List<? extends ItemEntity> teEntities = the_end.getEntities(EntityTypeTest.forClass(ItemEntity.class),itemEntity -> itemEntity.isAlive());
        for (Entity entity : owEntities) {
                if (entity != null) {
                    entity.remove(Entity.RemovalReason.DISCARDED);
                    owCnt++;
                }
        }
        for (Entity entity : tnEntities) {
            if (entity != null) {
                entity.remove(Entity.RemovalReason.DISCARDED);
                netherCnt++;
            }
        }
        for (Entity entity : teEntities) {
            if (entity != null) {
                entity.remove(Entity.RemovalReason.DISCARDED);
                teCnt++;
            }
        }
        if((owCnt | netherCnt | teCnt) != 0) {
            ResolvableProfile profile = Util.buildProfile(UUID.fromString("793be6b0-de85-412a-8483-636d6f8c74d0"));
            Component component = Component.empty()
                    .append(Component.object(new PlayerSprite(profile,true)))
                    .append(Component.literal("["))
                    .append(Component.literal("AL-1S").setStyle(Style.EMPTY.withColor(0x76d7ea).applyFormat(ChatFormatting.BOLD)))
                    .append(Component.literal("]"))
                    .append(" ")
                    .append(Component.literal("吃掉了")
                            .append(" ")
                            .append(Component.literal(Integer.toString(owCnt + netherCnt + teCnt)).withStyle(ChatFormatting.AQUA,ChatFormatting.BOLD))
                            .append(" ")
                            .append(Component.literal("个掉落物！")));
            Component component1 = Component.literal("主世界：")
                    .append(Component.literal(Integer.toString(owCnt)).withStyle(ChatFormatting.YELLOW))
                    .append(" ")
                    .append(Component.literal("下界："))
                    .append(Component.literal(Integer.toString(netherCnt)).withStyle(ChatFormatting.YELLOW))
                    .append(" ")
                    .append(Component.literal("末地："))
                    .append(Component.literal(Integer.toString(teCnt)).withStyle(ChatFormatting.YELLOW));

            for(ServerPlayer player : server.getPlayerList().getPlayers()){
                player.sendSystemMessage(component);
                player.sendSystemMessage(component1);
            }
        }
    }

    public static void tipsBehaviour(){
        ConfigFile DRNG_TIPS = Main.getConfigManager().getConfig("drng:tips");
        JsonArray tips = DRNG_TIPS.getKey("tips").getAsJsonArray();
        Component tip = Component.literal("[")
                .withStyle(ChatFormatting.BOLD)
                .append(Component.literal("提示").withStyle(ChatFormatting.YELLOW))
                .append(Component.literal("]:"));
        Component tipContent = Component.literal(tips.get(new Random().nextInt(tips.size())).getAsString());
        for(ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.sendSystemMessage(tip);
            player.sendSystemMessage(tipContent);
        }
    }

    public static Component getFlowingNoticeBoard(String local){
        Component nb;
        if(local.equals("zh_cn"))
            nb = Component.literal("----------流动公告----------").append("\n");
        else
            nb = Component.literal("----------Flowing Notice----------").append("\n");
        ConfigFile NOTICE = Main.getConfigManager().getConfig("drng:notices");
        if(NOTICE.has("notices")) {
            for (JsonElement element : NOTICE.getKey("notices").getAsJsonArray()) {
                JsonObject jsonObject = element.getAsJsonObject();
                nb = nb.copy().append(Component.literal(jsonObject.get("author").getAsString()).withStyle(ChatFormatting.GRAY).append(Component.literal(":")))
                        .append(" ")
                        .append(Component.literal(jsonObject.get("title").getAsString()))
                        .append("\n");
            }
            if (local.equals("zh_cn"))
                nb = nb.copy().append(Component.literal("点击查看详情").setStyle(Style.EMPTY.applyFormat(ChatFormatting.AQUA).withClickEvent(new ClickEvent.RunCommand("/notice list"))));
            else
                nb = nb.copy().append(Component.literal("Click to view.").setStyle(Style.EMPTY.applyFormat(ChatFormatting.AQUA).withClickEvent(new ClickEvent.RunCommand("/notice list"))));
        }
        return nb;
    }

}
