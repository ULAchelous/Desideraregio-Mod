package io.ula.drng.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.ula.drng.utils.kei.KeiChatBotUtils;
import io.ula.drng.utils.kei.tool.FuncTool;
import io.ula.drng.utils.kei.tool.Tool;
import io.ula.drng.utils.kei.tool.Tools;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;


public class KeiChatBotCmd {
    private static LiteralArgumentBuilder<CommandSourceStack> keiCmdBuilder = Commands.literal("kei")
            .then(Commands.literal("clear")
                    .executes(commandContext -> {
                        KeiChatBotUtils.clearContext();
                        commandContext.getSource().getPlayer().sendSystemMessage(Component.literal("上下文已清除"));
                        return 0;
                    })
                    .requires(commandSourceStack -> commandSourceStack.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
            )
            .then(Commands.literal("tools")
                    .executes(commandContext -> {
                        commandContext.getSource().getPlayer().sendSystemMessage(Component.literal("已加载的工具:"));
                        for(Tool tool : Tools.TOOLS){
                            Component perfix = Component.empty();
                            if(tool instanceof FuncTool funcTool) {
                                perfix = Component.literal("function:").withStyle(ChatFormatting.YELLOW);
                                commandContext.getSource().getPlayer().sendSystemMessage(perfix.copy().append(Component.literal(funcTool.getName()).withStyle(ChatFormatting.AQUA)));
                            }
                        }
                        return 0;
                    })
                    .requires(commandSourceStack -> commandSourceStack.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
            )
            .then(Commands.argument("msg",StringArgumentType.string())
                    .executes(commandContext -> {
                        String msg = commandContext.getArgument("msg",String.class);
                        KeiChatBotUtils.map.put(commandContext.getSource().getPlayer().getUUID(),true);
                        KeiChatBotUtils.onChat(msg,commandContext.getSource().getPlayer().getUUID());
                        return 0;
                    }))
            ;
    public static LiteralCommandNode<CommandSourceStack> keiCmd = keiCmdBuilder.build();
}
