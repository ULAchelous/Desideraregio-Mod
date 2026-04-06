package io.ula.drng.dialog;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.ula.drng.Main;
import io.ula.drng.commands.NoticeCmd;
import io.ula.drng.config.ConfigFile;
import io.ula.drng.config.InlineConfigFile;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dialog.action.Action;
import net.minecraft.server.dialog.action.CustomAll;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;

import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class DialogHelper {
    private static MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();

    private static Map<Identifier, CustomAll> customClicks = new HashMap<>();
    public static void addCustomClick(Identifier identifier,CustomAll customAll){
        customClicks.put(identifier,customAll);
    }
    public static void onHandleCustomClick(ServerPlayer player, ServerboundCustomClickActionPacket packet){
        Identifier id = packet.id();
        if(id.equals(Identifier.tryBuild("drng","eula/reject"))) {
            player.connection.disconnect(Component.literal("未同意许可").withStyle(ChatFormatting.RED));
        }else if(id.equals(Identifier.tryBuild("drng","eula/accept"))){
            ConfigFile PLAYER_EULA = Main.getConfigManager().getConfig("drng:eula");
            PLAYER_EULA.addKey(player.getStringUUID(),true);
        }else if(id.equals(Identifier.tryBuild("drng","notice/boardcast"))){
            if(packet.payload().get() instanceof CompoundTag compoundTag){
                ConfigFile NOTICES = Main.getConfigManager().getConfig("drng:notices");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
                        .withZone(ZoneId.of("Asia/Shanghai"));
                JsonObject notice = new JsonObject();
                String author = compoundTag.get("author").asString().get();
                String title = compoundTag.get("title").asString().get();
                String text = compoundTag.get("text").asString().get();
                String time_limit = compoundTag.get("time_limit").asString().get();
                if(author.isEmpty() || title.isEmpty() || text.isEmpty()) {
                    player.sendSystemMessage(Component.literal("公告未发送：存在空的必填项").withStyle(ChatFormatting.RED));
                }else{
                    notice.addProperty("author",author);
                    notice.addProperty("title",title);
                    notice.addProperty("text",text);
                    notice.addProperty("created_time",formatter.format(LocalDate.now(ZoneId.of("Asia/Shanghai"))));
                    notice.addProperty("deadline",formatter.format(LocalDate.now(ZoneId.of("Asia/Shanghai")).plusDays(Integer.parseInt(time_limit))));

                    if(NOTICES.has("notices")){
                        NOTICES.getKey("notices").getAsJsonArray().add(notice);
                    }else{
                        NOTICES.addKey("notices",new JsonArray());
                       NOTICES.getKey("notices").getAsJsonArray().add(notice);
                    }
                    for(ServerPlayer p : server.getPlayerList().getPlayers()){
                        p.sendSystemMessage(
                                Component.literal("公告栏上有新的信息，点击查看")
                                        .setStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/notice list")).applyFormat(ChatFormatting.GREEN))
                        );
                    }
                }
            }
        }
    }
}
