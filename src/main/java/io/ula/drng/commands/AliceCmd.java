package io.ula.drng.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.ula.drng.utils.TBUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.Permissions;

public class AliceCmd {
    static final LiteralArgumentBuilder<CommandSourceStack> aliceCmdBuilder = Commands.literal("al-1s").executes(context -> {
                TBUtils.aliceBehaviour();
                return 0;
            })
            .requires(commandSourceStack -> commandSourceStack.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER));
    public static final LiteralCommandNode<CommandSourceStack> aliceCmd = aliceCmdBuilder.build();
}
