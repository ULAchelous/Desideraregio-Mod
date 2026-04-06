package io.ula.drng.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.ula.drng.attachments.Attachments;
import io.ula.drng.attachments.PlayerStatusData;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.core.jmx.Server;



import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ControlCmd {
    public static final LiteralArgumentBuilder<CommandSourceStack> cCmd = Commands.literal("control")
            .then(Commands.argument("player", EntityArgument.player())
                    .executes(commandContext -> {
                        ServerPlayer sender = commandContext.getSource().getPlayer();
                        ServerPlayer target = commandContext.getArgument("player", EntitySelector.class).findSinglePlayer(commandContext.getSource());

                        PlayerStatusData data = target.getAttached(Attachments.PLAYER_STATUS_DATA);
                        if(data.is_controlling() != null){
                            sender.sendSystemMessage(Component.literal("无法控制(未退出正在进行的控制)").withStyle(ChatFormatting.RED));
                            return 0;
                        }
                        if (target.equals(sender)) {
                            sender.sendSystemMessage(Component.literal("无法控制(对象为自身)").withStyle(ChatFormatting.RED));
                            return 0;
                        }
                        if (target.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                            sender.sendSystemMessage(Component.literal("无法控制(对象为Operator)").withStyle(ChatFormatting.RED));
                            return 0;
                        }

                        sender.setInvisible(true);

                        target.setAttached(Attachments.PLAYER_STATUS_DATA, new PlayerStatusData(sender.getUUID(),null,null,target.getAttached(Attachments.PLAYER_STATUS_DATA).tpa_target()));//使用元数据标记被控制玩家，存储控制者
                        sender.setAttached(Attachments.PLAYER_STATUS_DATA,new PlayerStatusData(null,target.getUUID(), sender.position(),sender.getAttached(Attachments.PLAYER_STATUS_DATA).tpa_target()));//使用元数据标记控制玩家，存储被控制者
                        // 存储控制者控制前的坐标
                        sender.setPos(target.position());
                        return 0;
                    })
                    .requires(commandSourceStack -> (commandSourceStack.getPlayer() instanceof Player && commandSourceStack.getPlayer().permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)))
            )
            .then(Commands.literal("stop").executes(commandContext -> {
                ServerPlayer player = commandContext.getSource().getPlayer();
                PlayerStatusData data = player.getAttached(Attachments.PLAYER_STATUS_DATA);

                if(data.is_controlling() != null){
                    ServerPlayer target = commandContext.getSource().getServer().getPlayerList().getPlayer(data.is_controlling());
                    player.setPos(data.location_before_control());
                    player.setAttached(Attachments.PLAYER_STATUS_DATA,new PlayerStatusData(null,null,null,player.getAttached(Attachments.PLAYER_STATUS_DATA).tpa_target()));
                    target.setAttached(Attachments.PLAYER_STATUS_DATA,new PlayerStatusData(null,null,null,target.getAttached(Attachments.PLAYER_STATUS_DATA).tpa_target()));

                    player.setInvisible(false);
                }else{
                    player.sendSystemMessage(Component.literal("没有正在进行的控制").withStyle(ChatFormatting.RED));
                }
                return 0;
            }))
            .then(
                    Commands.argument("bot", StringArgumentType.string())
                            .suggests((commandContext, suggestionsBuilder) -> {
                                for(ServerPlayer player : commandContext.getSource().getServer().getPlayerList().getPlayers()){
                                    if(player.getClass().getSimpleName().equals("EntityPlayerMPFake")){
                                        suggestionsBuilder.suggest(player.getName().getString());
                                    }
                                }
                                return CompletableFuture.completedFuture(suggestionsBuilder.build());
                            })
                            .executes(commandContext -> {
                                ServerPlayer sender = commandContext.getSource().getPlayer();
                                String targetName = commandContext.getArgument("bot",String.class);
                                ServerPlayer target = commandContext.getSource().getServer().getPlayerList().getPlayer(targetName);

                                PlayerStatusData data = target.getAttached(Attachments.PLAYER_STATUS_DATA);
                                if(data.is_controlling() != null){
                                    sender.sendSystemMessage(Component.literal("无法控制(未退出正在进行的控制)").withStyle(ChatFormatting.RED));
                                    return 0;
                                }
                                if (target.equals(sender)) {
                                    sender.sendSystemMessage(Component.literal("无法控制(对象为自身)").withStyle(ChatFormatting.RED));
                                    return 0;
                                }
                                if (target.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                                    sender.sendSystemMessage(Component.literal("无法控制(对象为Operator)").withStyle(ChatFormatting.RED));
                                    return 0;
                                }

                                sender.setInvisible(true);

                                target.setAttached(Attachments.PLAYER_STATUS_DATA, new PlayerStatusData(sender.getUUID(),null,null,target.getAttached(Attachments.PLAYER_STATUS_DATA).tpa_target()));//使用元数据标记被控制玩家，存储控制者
                                sender.setAttached(Attachments.PLAYER_STATUS_DATA,new PlayerStatusData(null,target.getUUID(), sender.position(),sender.getAttached(Attachments.PLAYER_STATUS_DATA).tpa_target()));//使用元数据标记控制玩家，存储被控制者
                                // 存储控制者控制前的坐标
                                sender.setPos(target.position());
                                return 0;
                            })
                            .requires(commandSourceStack -> (commandSourceStack.getPlayer() instanceof Player && !commandSourceStack.getPlayer().permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER) && !commandSourceStack.getPlayer().permissions().hasPermission(Permissions.COMMANDS_ADMIN) && !commandSourceStack.getPlayer().permissions().hasPermission(Permissions.COMMANDS_MODERATOR) && !commandSourceStack.getPlayer().permissions().hasPermission(Permissions.COMMANDS_OWNER)))
            );

    public static final LiteralCommandNode<CommandSourceStack> buildCCmd = cCmd.build();
}
