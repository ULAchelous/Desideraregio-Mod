package io.ula.drng.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.ula.drng.attachments.Attachments;
import io.ula.drng.attachments.PlayerStatusData;
import io.ula.drng.utils.PlayerUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;


import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ControlCmd {
    public static final LiteralArgumentBuilder<CommandSourceStack> cCmd = Commands.literal("control")
            .then(Commands.literal("player")
                    .then(Commands.argument("player", EntityArgument.player())
                            .executes(commandContext -> {
                                ServerPlayer sender = commandContext.getSource().getPlayer();
                                String local = sender.clientInformation().language();
                                ServerPlayer target = commandContext.getArgument("player", EntitySelector.class).findSinglePlayer(commandContext.getSource());

                                PlayerStatusData data = target.getAttached(Attachments.PLAYER_STATUS_DATA);
                                if(local.equals("zh_cn")) {
                                    if (data.is_controlling().isPresent()) {
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
                                }else{
                                    if (data.is_controlling().isPresent()) {
                                        sender.sendSystemMessage(Component.literal("Cannot control: You must exit your current control session first.").withStyle(ChatFormatting.RED));
                                        return 0;
                                    }
                                    if (target.equals(sender)) {
                                        sender.sendSystemMessage(Component.literal("Cannot control: You cannot control yourself.").withStyle(ChatFormatting.RED));
                                        return 0;
                                    }
                                    if (target.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                                        sender.sendSystemMessage(Component.literal("Cannot control: Target is an Operator.").withStyle(ChatFormatting.RED));
                                        return 0;
                                    }
                                }

                                sender.setInvisible(true);
                                sender.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, -1, 0, false, false));

                                sender.teleportTo(
                                        target.level(),
                                        target.getX(), target.getY(), target.getZ(),
                                        Collections.emptySet(),
                                        sender.getYRot(),
                                        sender.getXRot(),
                                        false
                                );

                                target.setAttached(Attachments.PLAYER_STATUS_DATA, new PlayerStatusData(Optional.of(sender.getUUID()),Optional.empty(),Optional.empty(),target.getAttached(Attachments.PLAYER_STATUS_DATA).tpa_target()));//使用元数据标记被控制玩家，存储控制者
                                sender.setAttached(Attachments.PLAYER_STATUS_DATA,new PlayerStatusData(Optional.empty(),Optional.of(target.getUUID()), Optional.of(sender.position()),sender.getAttached(Attachments.PLAYER_STATUS_DATA).tpa_target()));//使用元数据标记控制玩家，存储被控制者
                                // 存储控制者控制前的坐标
                                return 0;
                            })
                    ).requires(commandSourceStack -> (commandSourceStack.isPlayer() && commandSourceStack.getPlayer().permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))))
            .then(Commands.literal("stop").executes(commandContext -> {
                ServerPlayer player = commandContext.getSource().getPlayer();
                String local = player.clientInformation().language();
                PlayerStatusData data = player.getAttached(Attachments.PLAYER_STATUS_DATA);

                if(data.is_controlling().isPresent()){
                    ServerPlayer target = commandContext.getSource().getServer().getPlayerList().getPlayer(data.is_controlling().get());
                    player.setAttached(Attachments.PLAYER_STATUS_DATA,new PlayerStatusData(Optional.empty(),Optional.empty(),Optional.empty(),player.getAttached(Attachments.PLAYER_STATUS_DATA).tpa_target()));
                    target.setAttached(Attachments.PLAYER_STATUS_DATA,new PlayerStatusData(Optional.empty(),Optional.empty(),Optional.empty(),target.getAttached(Attachments.PLAYER_STATUS_DATA).tpa_target()));

                    player.setInvisible(false);
                    player.removeEffect(MobEffects.INVISIBILITY);
                }else{
                    if(local.equals("zh_cn"))
                        player.sendSystemMessage(Component.literal("没有正在进行的控制").withStyle(ChatFormatting.RED));
                    else
                        player.sendSystemMessage(Component.literal("No recent control").withStyle(ChatFormatting.RED));
                }
                return 0;
            }))
            .then(Commands.literal("bot")
                    .then(
                            Commands.argument("bot", StringArgumentType.string())
                                    .suggests((commandContext, suggestionsBuilder) -> {
                                        for(ServerPlayer player : commandContext.getSource().getServer().getPlayerList().getPlayers()){
                                    //commandContext.getSource().getServer().sendSystemMessage(Component.literal(player.getClass().getSimpleName()));
                                            if(PlayerUtils.isFakePlayer(player)){
                                                suggestionsBuilder.suggest(player.getName().getString());
                                            }
                                        }
                                        return CompletableFuture.completedFuture(suggestionsBuilder.build());
                                    })
                                    .executes(commandContext -> {
                                        ServerPlayer sender = commandContext.getSource().getPlayer();
                                        String local = sender.clientInformation().language();
                                        String targetName = commandContext.getArgument("bot",String.class);
                                        ServerPlayer target = commandContext.getSource().getServer().getPlayerList().getPlayer(targetName);

                                        if(target != null && PlayerUtils.isFakePlayer(target)) {
                                            PlayerStatusData data = target.getAttached(Attachments.PLAYER_STATUS_DATA);
                                            if(local.equals("zh_cn")) {
                                                if (data.is_controlling().isPresent()) {
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
                                            }else{
                                                if (data.is_controlling().isPresent()) {
                                                    sender.sendSystemMessage(Component.literal("Cannot control: You must exit your current control session first.").withStyle(ChatFormatting.RED));
                                                    return 0;
                                                }
                                                if (target.equals(sender)) {
                                                    sender.sendSystemMessage(Component.literal("Cannot control: You cannot control yourself.").withStyle(ChatFormatting.RED));
                                                    return 0;
                                                }
                                                if (target.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                                                    sender.sendSystemMessage(Component.literal("Cannot control: Target is an Operator.").withStyle(ChatFormatting.RED));
                                                    return 0;
                                                }
                                            }

                                            sender.setInvisible(true);
                                            sender.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, -1, 0, false, false));

                                            sender.teleportTo(
                                                    target.level(),
                                                    target.getX(), target.getY(), target.getZ(),
                                                    Collections.emptySet(),
                                                    sender.getYRot(),
                                                    sender.getXRot(),
                                                    false
                                            );

                                            target.setAttached(Attachments.PLAYER_STATUS_DATA, new PlayerStatusData(Optional.of(sender.getUUID()), Optional.empty(), Optional.empty(), target.getAttached(Attachments.PLAYER_STATUS_DATA).tpa_target()));//使用元数据标记被控制玩家，存储控制者
                                            sender.setAttached(Attachments.PLAYER_STATUS_DATA, new PlayerStatusData(Optional.empty(), Optional.of(target.getUUID()), Optional.of(sender.position()), sender.getAttached(Attachments.PLAYER_STATUS_DATA).tpa_target()));//使用元数据标记控制玩家，存储被控制者
                                            // 存储控制者控制前的坐标
                                        }else{
                                            if(local.equals("zh_cn"))
                                                sender.sendSystemMessage(Component.literal("无法控制(玩家不存在或无权控制)").withStyle(ChatFormatting.RED));
                                            else
                                                sender.sendSystemMessage(Component.literal("Unable to control(Lack permission or player dose not exist)").withStyle(ChatFormatting.RED));
                                        }
                                        return 0;
                                    })
                    ))
;

    public static final LiteralCommandNode<CommandSourceStack> buildCCmd = cCmd.build();
}
