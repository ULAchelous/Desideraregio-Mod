package io.ula.drng;

import io.ula.drng.chatdecorators.CustomChatDecorator;
import io.ula.drng.commands.BindCmd;
import io.ula.drng.commands.ConfigCmd;
import io.ula.drng.commands.ControlCmd;
import io.ula.drng.config.ConfigManager;
import io.ula.drng.config.Configs;
import io.ula.drng.utils.PlayerUtils;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;


public class Main implements ModInitializer {

    public static final CustomChatDecorator CUSTOM_CHAT_DECORATOR = new CustomChatDecorator();

    private static ConfigManager configManager;
    public static ConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    public void onInitialize() {
        configManager = new ConfigManager();
        Configs.init(this);
        commandsRegister();
        callbackRegister();
    }

    private void commandsRegister() {
        CommandRegistrationCallback.EVENT.register((commandDispatcher, commandBuildContext, commandSelection) -> {
            commandDispatcher.getRoot().addChild(BindCmd.bindCmd);
            commandDispatcher.getRoot().addChild(BindCmd.rmPlCB);
            commandDispatcher.getRoot().addChild(ConfigCmd.configCmd);
            commandDispatcher.getRoot().addChild(ControlCmd.buildCCmd);
        });
    }

    private void callbackRegister(){
        ServerLifecycleEvents.SERVER_STOPPING.register(minecraftServer -> {
            this.configManager.onDisabled();
        });
        ServerPlayerEvents.JOIN.register(serverPlayer -> {
            PlayerUtils.initPlayerStatus(serverPlayer);
        });
    }


    /*
    TODO:完成监听器的迁移、同时为configFile命令添加管理自动保存的功能
     */
}
