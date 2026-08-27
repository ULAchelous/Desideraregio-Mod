package io.ula.drng.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.ula.drng.utils.kei.KeiChatBotUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.Permissions;


public class KeiChatBotCmd {
    private static LiteralArgumentBuilder<CommandSourceStack> keiCmdBuilder = Commands.literal("kei")
            .then(Commands.literal("clear")
                    .executes(commandContext -> {
                        KeiChatBotUtils.clearContext();
                        return 0;
                    })
                    .requires(commandSourceStack -> commandSourceStack.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
            )
            .then(Commands.argument("msg",StringArgumentType.string())
                    .executes(commandContext -> {
                        String msg = commandContext.getArgument("msg",String.class);
                        KeiChatBotUtils.onChat(msg,commandContext.getSource().getPlayer().getUUID());
                        return 0;
                    }))
            ;
    public static LiteralCommandNode<CommandSourceStack> keiCmd = keiCmdBuilder.build();
}
