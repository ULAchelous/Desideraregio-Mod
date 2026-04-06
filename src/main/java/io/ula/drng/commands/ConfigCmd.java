package io.ula.drng.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.ula.drng.Main;
import io.ula.drng.config.ConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.permissions.Permissions;

public class ConfigCmd {
    private static LiteralArgumentBuilder<CommandSourceStack> configCmdBuilder = Commands.literal("configfile")
            .then(Commands.literal("save").executes(
               commandContext -> {
                   ConfigManager configManager = Main.getConfigManager();
                   commandContext.getSource().getPlayer().sendSystemMessage(Component.literal("已保存配置文件中的更改！"));
                   new Thread(configManager::saveAll).start();
                   return 0;
               }
            ))
            .then(Commands.literal("reload").executes(
                    commandContext -> {
                        ConfigManager configManager = Main.getConfigManager();
                        commandContext.getSource().getPlayer().sendSystemMessage(Component.literal("已重新加载配置文件！"));
                        new Thread(configManager::reloadAll).start();
                        return 0;
                    }
            ))
            .then(Commands.literal("autosave")
                    .then(Commands.argument("period", IntegerArgumentType.integer())
                            .executes(commandContext -> {
                                ConfigManager configManager = Main.getConfigManager();
                                int period = commandContext.getArgument("period", Integer.class);
                                commandContext.getSource().getPlayer().sendSystemMessage(Component.literal("已将自动保存间隔设为 ")
                                        .append(Component.literal(Integer.toString(period)).withStyle(ChatFormatting.YELLOW))
                                        .append(" 分钟！")
                                );
                                new Thread(() -> configManager.setAutoSave(period)).start();
                                return 0;
                            })
                    ))
            .requires(commandSourceStack -> commandSourceStack.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER));
    public static LiteralCommandNode<CommandSourceStack> configCmd = configCmdBuilder.build();
}
