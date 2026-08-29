package io.ula.drng;

import com.google.gson.JsonObject;
import com.mojang.brigadier.tree.RootCommandNode;
import io.ula.api.config.*;
import io.ula.drng.attachments.Attachments;
import io.ula.drng.attachments.PlayerCntData;
import io.ula.drng.chatdecorators.CustomChatDecorator;
import io.ula.drng.commands.*;
import io.ula.drng.config.Configs;
import io.ula.drng.dialog.CustomDialogs;
import io.ula.api.scheduler.*;
import io.ula.drng.utils.PlayerUtils;
import io.ula.drng.utils.TBUtils;
import io.ula.drng.utils.kei.PucaUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.common.ClientboundShowDialogPacket;
import net.minecraft.server.level.ServerPlayer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;


public class Main implements ModInitializer {

    public static final CustomChatDecorator CUSTOM_CHAT_DECORATOR = new CustomChatDecorator();
    public static final String MOD_ID = "dr-ng";
    public static final String VERSION = FabricLoader.getInstance().getModContainer(MOD_ID).get().getMetadata().getVersion().toString();
    public static final Path SERVER_ROOT = FabricLoader.getInstance().getGameDir();
    public static final Path CONFIG_PATH = Path.of(new File(SERVER_ROOT.toString() + "/config/dr-ng").toURI());
    private static ConfigManager configManager = new ConfigManager(MOD_ID,VERSION,SERVER_ROOT);
    public static ConfigManager getConfigManager() {
        return configManager;
    }
    public static String getVersion(){return VERSION;}

    @Override
    public void onInitialize() {
        Configs.init();
        commandsRegister();
        callbackRegister();
    }

    private void commandsRegister() {
        CommandRegistrationCallback.EVENT.register((commandDispatcher, commandBuildContext, commandSelection) -> {
            RootCommandNode<CommandSourceStack> rootNode = commandDispatcher.getRoot();
            rootNode.addChild(BindCmd.bindCmd);
            rootNode.addChild(BindCmd.rmPlCB);
            rootNode.addChild(ConfigCmd.configCmd);
            rootNode.addChild(ControlCmd.buildCCmd);
            rootNode.addChild(TpaCmd.tpaCmd);
            rootNode.addChild(TpaCmd.PTACmd);
            rootNode.addChild(TpaCmd.RTTCmd);
            rootNode.addChild(HomeCmd.homeCmd);
            rootNode.addChild(HomeCmd.PTHCmd);
            rootNode.addChild(HomeCmd.PRHCmd);
            rootNode.addChild(HelpCmd.helpCmd);
            rootNode.addChild(NoticeCmd.noticeCmd);
            rootNode.addChild(AliceCmd.aliceCmd);
            rootNode.addChild(KeiChatBotCmd.keiCmd);
        });
    }

    private void callbackRegister(){
        ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> {
            ScoreBoardHelper.initOverrideObjectives();
            ServerScheduler scheduler = ((ServerSchedulerHolder) minecraftServer).getServerSchedule();
            scheduler.runTask(new ScheduleTask("UpdateOnlineTime",(server,task) -> {
                for(ServerPlayer player : minecraftServer.getPlayerList().getPlayers()){
                    if(!PlayerUtils.isFakePlayer(player)) {
                        PlayerCntData data = player.getAttached(Attachments.PLAYER_CNT_DATA);
                        player.setAttached(Attachments.PLAYER_CNT_DATA, new PlayerCntData(data.digCnt(), data.deathCnt(), data.onlineTime() + 1));
                        ScoreBoardHelper.updateScores(player, ScoreBoardHelper.UpdateType.ONLINE_TIME);
                    }
                }
            },0,1200));
            scheduler.runTask(new ScheduleTask("DisplayTips",(server,task) -> TBUtils.tipsBehaviour(),0,25*60*20));
            scheduler.runTask(new ScheduleTask("Alice",(server,task) -> TBUtils.alice(),27*60*20,27*60*20));   
            scheduler.runTask(new ScheduleTask("PucaPoll",(server,task) -> PucaUtil.pollMessages(),0,20));
            //30*60*20
            //注册任务
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(minecraftServer -> {
            this.configManager.onDisabled();
        });
        ServerPlayerEvents.JOIN.register(serverPlayer -> {
            PlayerUtils.initPlayerStatus(serverPlayer);
            ScoreBoardHelper.initObjective(serverPlayer);

            ConfigFile PLAYER_EULA = configManager.getConfig("drng:eula");
            String playerId = serverPlayer.getStringUUID();
            if(!PLAYER_EULA.has(playerId))
                PLAYER_EULA.addKey(playerId,false);
            Boolean b = PLAYER_EULA.getKey(playerId).getAsBoolean();
            if(!b){
                ClientboundShowDialogPacket packet = new ClientboundShowDialogPacket(Holder.direct(CustomDialogs.EULA_DIALOG));
                serverPlayer.connection.send(packet);
            }

            serverPlayer.sendSystemMessage(TBUtils.getFlowingNoticeBoard(serverPlayer.clientInformation().language()));
        });
        PlayerBlockBreakEvents.AFTER.register((level, player, blockPos, blockState, blockEntity) -> {
            if(!PlayerUtils.isFakePlayer((ServerPlayer) player)) {
                PlayerCntData data = player.getAttached(Attachments.PLAYER_CNT_DATA);
                player.setAttached(Attachments.PLAYER_CNT_DATA, new PlayerCntData(data.digCnt() + 1, data.deathCnt(), data.onlineTime()));
                ScoreBoardHelper.updateScores((ServerPlayer) player, ScoreBoardHelper.UpdateType.DIG_COUNT);
            }
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((serverPlayer, serverPlayer1, b) -> {
            if(!PlayerUtils.isFakePlayer(serverPlayer)) {
                PlayerCntData data = serverPlayer.getAttached(Attachments.PLAYER_CNT_DATA);
                serverPlayer.setAttached(Attachments.PLAYER_CNT_DATA, new PlayerCntData(data.digCnt(), data.deathCnt() + 1, data.onlineTime()));
                ScoreBoardHelper.updateScores(serverPlayer, ScoreBoardHelper.UpdateType.DEATH_COUNT);
            }
        });
    }

}
