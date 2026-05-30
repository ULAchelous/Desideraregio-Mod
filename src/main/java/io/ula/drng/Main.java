package io.ula.drng;

import com.mojang.brigadier.tree.RootCommandNode;
import io.ula.config.*;
import io.ula.drng.attachments.Attachments;
import io.ula.drng.attachments.PlayerCntData;
import io.ula.drng.chatdecorators.CustomChatDecorator;
import io.ula.drng.commands.*;
import io.ula.drng.config.Configs;
import io.ula.drng.dialog.CustomDialogs;
import io.ula.drng.scheduler.ScheduleTask;
import io.ula.drng.scheduler.ServerScheduler;
import io.ula.drng.scheduler.ServerSchedulerHolder;
import io.ula.drng.utils.PlayerUtils;
import io.ula.drng.utils.TBUtils;
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
import java.nio.file.Path;


public class Main implements ModInitializer {

    public static final CustomChatDecorator CUSTOM_CHAT_DECORATOR = new CustomChatDecorator();
    public static final String VERSION = FabricLoader.getInstance().getModContainer("dr-ng").get().getMetadata().getVersion().toString();
    public static final Path SERVER_ROOT = FabricLoader.getInstance().getGameDir();
    public static final Path CONFIG_PATH = Path.of(new File(SERVER_ROOT.toString() + "/config/dr-ng").toURI());
    private static ConfigManager configManager;
    public static ConfigManager getConfigManager() {
        return configManager;
    }
    public static String getVersion(){return VERSION;}

    @Override
    public void onInitialize() {
        configManager = new ConfigManager("dr-ng",VERSION,CONFIG_PATH);
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
        });
    }

    private void callbackRegister(){
        ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> {
            ScoreBoardHelper.initOverrideObjectives();
            ServerScheduler scheduler = ((ServerSchedulerHolder) minecraftServer).drng$getServerSchedule();
            scheduler.runTask(new ScheduleTask("UpdateOnlineTime",() -> {
                for(ServerPlayer player : minecraftServer.getPlayerList().getPlayers()){
                    if(!PlayerUtils.isFakePlayer(player)) {
                        PlayerCntData data = player.getAttached(Attachments.PLAYER_CNT_DATA);
                        player.setAttached(Attachments.PLAYER_CNT_DATA, new PlayerCntData(data.digCnt(), data.deathCnt(), data.onlineTime() + 1));
                        ScoreBoardHelper.updateScores(player, ScoreBoardHelper.UpdateType.ONLINE_TIME);
                    }
                }
            },0,1200));
            scheduler.runTask(new ScheduleTask("DisplayTips",TBUtils::tipsBehaviour,0,30*60*20));
            scheduler.runTask(new ScheduleTask("Alice",TBUtils::aliceBehaviour,0,60*60*20));
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
            serverPlayer.sendSystemMessage(TBUtils.getFlowingNoticeBoard());
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
