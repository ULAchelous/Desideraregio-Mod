//package io.ula.drng;
//
//
//
//import net.fabricmc.loader.api.FabricLoader;
//import net.minecraft.ChatFormatting;
//import net.minecraft.network.chat.Component;
//import net.minecraft.network.chat.numbers.BlankFormat;
//import net.minecraft.network.chat.numbers.NumberFormat;
//import net.minecraft.server.MinecraftServer;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.world.scores.DisplaySlot;
//import net.minecraft.world.scores.Objective;
//import net.minecraft.world.scores.ScoreHolder;
//import net.minecraft.world.scores.Scoreboard;
//import net.minecraft.world.scores.criteria.ObjectiveCriteria;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//
//public class ScoreBoardHelper {
//    private static ScoreboardManager scoreboardManager;
//    private static Map<UUID, Objective> objectives = new HashMap<>();
//    private static MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();
////    private static JavaPlugin plugin;
////    public static void init(JavaPlugin plg){
////        plugin=plg;
////        scoreboardManager = plugin.getServer().getScoreboardManager();
////    }
//    public static void createObjective(ServerPlayer player){
//        Scoreboard scoreboard = server.getScoreboard();
//        Component server_name = Component.literal("希").withColor(0x00CCFF)
//                .append(Component.literal("望").withColor(0x0099CC))
//                .append(Component.literal("之").withColor(0x006699))
//                .append(Component.literal("地").withColor(0x003366))
//                .append(Component.literal(" - NextGen").withColor(0x003366))
//                .withStyle(ChatFormatting.BOLD,ChatFormatting.ITALIC);
//
//
//        Objective sidebar= scoreboard.addObjective(player.getName().getString(),
//                ObjectiveCriteria.DUMMY,
//                server_name,
//                ObjectiveCriteria.RenderType.INTEGER,
//                true,
//                BlankFormat.INSTANCE
//        );
//        Objective health_display = scoreboard.addObjective("health",
//                ObjectiveCriteria.HEALTH,
//                Component.literal("health"),
//                ObjectiveCriteria.RenderType.HEARTS,
//                true,
//                null
//                );
//
//        health_display.setDisplayAutoUpdate(true);
//        scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly("在线时长: " + getOnlineTime(player.getMetadata("onlineTime").getFirst().asInt())),sidebar).set(9);
//        scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly(String.format("死亡计数: §b§l%d",player.getMetadata("deathCount").getFirst().asInt())),sidebar).set(8);
//        scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly(String.format("挖掘计数: §b§l%d",player.getMetadata("digCount").getFirst().asInt())),sidebar).set(7);
//        scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly( "使用 §e§l/notice"),sidebar).set(6);
//        scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly("来发布和查看公告"),sidebar).set(5);
//        scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly("使用 §e§l/home"),sidebar).set(4);
//        scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly(" 来设置传送点"),sidebar).set(3);
//        scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly("使用 §e§l/tpa"),sidebar).set(2);
//        scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly(" 来传送到玩家"),sidebar).set(1);
//
//        scoreboard.setDisplayObjective(DisplaySlot.SIDEBAR,sidebar);
//        scoreboard.setDisplayObjective(DisplaySlot.LIST,health_display);
//        player;
//        objectives.put(player.getUUID(),sidebar);
//    }
//
//    public static void removeObjective(Player player) {
//        objectives.remove(player.getUniqueId());
//        player.setScoreboard(scoreboardManager.getMainScoreboard());
//    }
//    private static String getOnlineTime(int time){
//        int hour = time / 60;
//        int minute = time % 60;
//        return String.format("§e%d§r小时§e%d§r分钟",hour,minute);
//    }
//
//    public static void updateScores(ServerPlayer player,int type) {
//        Objective objective = objectives.get(player.getUUID());
//        //更新数值
//        switch(type) {
//            case 1:
//                player.getScoreboard().resetScores(String.format("死亡计数: §b§l%d", player.getMetadata("deathCount").getFirst().asInt() -1 ));
//                objective.getScore(String.format("死亡计数: §b§l%d", player.getMetadata("deathCount").getFirst().asInt())).setScore(8);
//                break;
//            case 2:
//                player.getScoreboard().resetScores(String.format("挖掘计数: §b§l%d", player.getMetadata("digCount").getFirst().asInt() -1));
//                objective.getScore(String.format("挖掘计数: §b§l%d", player.getMetadata("digCount").getFirst().asInt())).setScore(7);
//                break;
//            case 3:
//                player.getScoreboard().resetScores("在线时长: " + getOnlineTime(player.getMetadata("onlineTime").getFirst().asInt() -1));
//                objective.getScore("在线时长: " + getOnlineTime(player.getMetadata("onlineTime").getFirst().asInt())).setScore(9);
//                break;
//        }
//    }
//    public static Objective getObjective(Player player){
//        return objectives.get(player.getUniqueId());
//    }
//}
