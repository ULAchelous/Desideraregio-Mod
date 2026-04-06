package io.ula.drng;



import io.ula.drng.attachments.Attachments;
import io.ula.drng.attachments.PlayerCntData;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ScoreBoardHelper {
    private static Map<UUID, Objective> objectives = new HashMap<>();
    private static MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();
//    private static JavaPlugin plugin;
//    public static void init(JavaPlugin plg){
//        plugin=plg;
//        scoreboardManager = plugin.getServer().getScoreboardManager();
//    }
    public static void initObjective(ServerPlayer player){
        Objective sidebar;
        if(objectives.containsKey(player.getUUID())){
            sidebar = getObjective(player);
        }else {

            Scoreboard scoreboard = server.getScoreboard();
            Component server_name = Component.literal("希").withColor(0x00CCFF)
                    .append(Component.literal("望").withColor(0x0099CC))
                    .append(Component.literal("之").withColor(0x006699))
                    .append(Component.literal("地").withColor(0x003366))
                    .append(Component.literal(" - NextGen").withColor(0x003366))
                    .withStyle(ChatFormatting.BOLD, ChatFormatting.ITALIC);


            sidebar = new Objective(
                    scoreboard,
                    player.getName().getString(),
                    ObjectiveCriteria.DUMMY,
                    server_name,
                    ObjectiveCriteria.RenderType.INTEGER,
                    true,
                    BlankFormat.INSTANCE
            );

            objectives.put(player.getUUID(), sidebar);
        }
        setPlayerObjective(player, sidebar, DisplaySlot.SIDEBAR);
        updateScores(player,UpdateType.ONLINE_TIME);
        updateScores(player,UpdateType.DEATH_COUNT);
        updateScores(player,UpdateType.DIG_COUNT);
        sendScore2Player(player,"helpCmd",sidebar,Component.literal("使用 ")
                        .append(Component.literal("/help").withStyle(ChatFormatting.BOLD,ChatFormatting.YELLOW))
                ,BlankFormat.INSTANCE,2);
        sendScore2Player(player,"helpCmd1",sidebar,Component.literal("来获取帮助"),BlankFormat.INSTANCE,2);

        //scoreboard.setDisplayObjective(DisplaySlot.SIDEBAR,sidebar);
    }

    public static void initOverrideObjectives(){
        Scoreboard scoreboard = server.getScoreboard();

        Objective health_display = new Objective(scoreboard
                ,"health",
                ObjectiveCriteria.HEALTH,
                Component.literal("health"),
                ObjectiveCriteria.RenderType.HEARTS,
                true,
                null
        );

        health_display.setDisplayAutoUpdate(true);
        scoreboard.setDisplayObjective(DisplaySlot.LIST,health_display);
    }

    public static void removeObjective(ServerPlayer player) {
        objectives.remove(player.getUUID());
    }
    private static String getOnlineTime(int time){
        int hour = time / 60;
        int minute = time % 60;
        return String.format("§e%d§r小时§e%d§r分钟",hour,minute);
    }

    public static void updateScores(ServerPlayer player,UpdateType type) {
        Objective objective = objectives.get(player.getUUID());
        PlayerCntData playerCntData = player.getAttached(Attachments.PLAYER_CNT_DATA);
        Objective playerObjective = objectives.get(player.getUUID());
        //更新数值
        switch(type) {
            case DEATH_COUNT:
                Component c1 = Component.literal("死亡计数: ").append(Component.literal(Integer.toString(playerCntData.deathCnt())).withStyle(ChatFormatting.AQUA,ChatFormatting.BOLD));
                sendScore2Player(player,"deathCnt",objective,c1,BlankFormat.INSTANCE,3);
                break;
            case DIG_COUNT:
                Component c2 = Component.literal("挖掘计数: ").append(Component.literal(Integer.toString(playerCntData.digCnt())).withStyle(ChatFormatting.AQUA,ChatFormatting.BOLD));
                sendScore2Player(player,"digCnt",objective,c2,BlankFormat.INSTANCE,3);
                break;
            case ONLINE_TIME:
                Component c3 = Component.literal("在线时长: ").append(Component.literal(getOnlineTime(playerCntData.onlineTime())).withStyle(ChatFormatting.AQUA,ChatFormatting.BOLD));
                sendScore2Player(player,"onlineTime",objective,c3,BlankFormat.INSTANCE,3);
                break;
        }
    }
    public static Objective getObjective(ServerPlayer player){
        return objectives.get(player.getUUID());
    }

    public static void setPlayerObjective(ServerPlayer player,Objective objective,DisplaySlot solt){
        ClientboundSetObjectivePacket removeObjectivePacket = new ClientboundSetObjectivePacket(objective,1);
        ClientboundSetObjectivePacket setObjectivePacket = new ClientboundSetObjectivePacket(objective,0);
        ClientboundSetDisplayObjectivePacket displayObjectivePacket = new ClientboundSetDisplayObjectivePacket(solt,objective);
        player.connection.send(removeObjectivePacket);
        player.connection.send(setObjectivePacket);
        player.connection.send(displayObjectivePacket);
    }

    public static void sendScore2Player(ServerPlayer player,String id,Objective objective,Component content,NumberFormat format, int score){
        ClientboundSetScorePacket packet = new ClientboundSetScorePacket(
                id,
                objective.getName(),
                score,
                Optional.of(content),
                Optional.of(format)
        );
        player.connection.send(packet);
    }

    public static enum UpdateType{
        DEATH_COUNT,
        DIG_COUNT,
        ONLINE_TIME
    }
}
