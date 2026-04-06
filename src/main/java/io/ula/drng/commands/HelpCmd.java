package io.ula.drng.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class HelpCmd {
    private static LiteralArgumentBuilder<CommandSourceStack> helpBuilder = Commands.literal("help")
            .executes(commandContext -> {
                ServerPlayer sender = commandContext.getSource().getPlayer();
                sender.sendSystemMessage(Component.literal("---------------帮助---------------").withStyle(ChatFormatting.GRAY));
                sender.sendSystemMessage(Component.literal("/home <操作> <参数(可选)>   -> 设置传送点"));
                sender.sendSystemMessage(Component.literal("/tpa <目标>   -> 请求传送"));
                sender.sendSystemMessage(Component.literal("/bind <操作> <参数(可选)> <参数(可选)>   -> 将你聊天中的一个词替换为另一个词"));
                sender.sendSystemMessage(Component.literal("/control <目标>   -> 控制假人"));
                sender.sendSystemMessage(Component.literal("/notice <操作>   -> 发布/查看公告"));
                sender.sendSystemMessage(Component.literal("提示： 当命令要求你输入一个字符串作为参数时，可以加上\" \"来输入中文"));
                return 0;
            })
            .requires(commandSourceStack -> commandSourceStack.isPlayer());
    public static LiteralCommandNode<CommandSourceStack> helpCmd = helpBuilder.build();
}
