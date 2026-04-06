package io.ula.drng.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.ula.drng.attachments.Attachments;
import io.ula.drng.attachments.PlayerStatusData;
import io.ula.drng.scheduler.ScheduleTask;
import io.ula.drng.scheduler.ServerScheduler;
import io.ula.drng.scheduler.ServerSchedulerHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.objects.AtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;


import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

public class TpaCmd {
    private static LiteralArgumentBuilder<CommandSourceStack> tpaCmdBuilder = Commands.literal("tpa")
            .then(Commands.argument("target", EntityArgument.player()).executes(context -> {
                ServerPlayer sendr = (ServerPlayer) context.getSource().getPlayer();
                ServerPlayer target = context.getArgument("target", EntitySelector.class).findSinglePlayer(context.getSource());

                ServerScheduler serverScheduler = ((ServerSchedulerHolder)context.getSource().getServer()).drng$getServerSchedule();
                serverScheduler.runTask(new ScheduleTask(sendr.getName().getString() + "TpaTimeOut",() -> {
                    sendr.sendSystemMessage(Component.literal("传送请求超时").withStyle(ChatFormatting.RED));
                    PlayerStatusData targetData = sendr.getAttached(Attachments.PLAYER_STATUS_DATA);
                    sendr.setAttached(Attachments.PLAYER_STATUS_DATA,new PlayerStatusData(targetData.been_controlled(),targetData.is_controlling(),targetData.location_before_control(),null));
                },20*15));

                PlayerStatusData senderData = sendr.getAttached(Attachments.PLAYER_STATUS_DATA);
                sendr.setAttached(Attachments.PLAYER_STATUS_DATA,new PlayerStatusData(senderData.been_controlled(),senderData.is_controlling(),senderData.location_before_control(),target.getUUID()));

                sendr.sendSystemMessage(Component.literal("请耐心等待对方接受传送哦~"));
                target.sendSystemMessage(Component.literal("玩家 ")
                        .append(sendr.getName().copy().withStyle(ChatFormatting.BOLD,ChatFormatting.YELLOW))
                        .append(Component.literal(" 想要传送到你这里！"))
                );
                target.sendSystemMessage(Component.empty()
                        .append(Component.literal("[同意]")
                                .append(Component.object(new AtlasSprite(Identifier.tryBuild("minecraft","gui"),Identifier.tryBuild("minecraft","pending_invite/accept"))))
                                .setStyle(Style.EMPTY
                                                .withHoverEvent(new HoverEvent.ShowText(Component.literal("点击确认传送").withStyle(ChatFormatting.GREEN)))
                                                .withClickEvent(new ClickEvent.RunCommand(String.format("/player-teleport-accept %s",sendr.getName().getString())))
                                )
                        )
                        .append(" ")
                        .append(Component.literal("[拒绝]")
                                .append(Component.object(new AtlasSprite(Identifier.tryBuild("minecraft","gui"),Identifier.tryBuild("minecraft","pending_invite/reject"))))
                                .setStyle(Style.EMPTY
                                        .withHoverEvent(new HoverEvent.ShowText(Component.literal("点击拒绝传送").withStyle(ChatFormatting.GREEN)))
                                        .withClickEvent(new ClickEvent.RunCommand(String.format("/reset-tpa-target %s",sendr.getName().getString())))
                                ))
                );
                return 0;
            }))
            .requires(commandSourceStack -> commandSourceStack.isPlayer());

    public static LiteralCommandNode<CommandSourceStack> PTACmd = Commands.literal("player-teleport-accept")
            .then(Commands.argument("target",EntityArgument.player())
                    .executes(commandContext -> {
                        ServerScheduler serverScheduler = ((ServerSchedulerHolder)commandContext.getSource().getServer()).drng$getServerSchedule();
                        ServerPlayer sender = commandContext.getSource().getPlayer();
                        ServerPlayer target = commandContext.getArgument("target", EntitySelector.class).findSinglePlayer(commandContext.getSource());
                        PlayerStatusData targetData = target.getAttached(Attachments.PLAYER_STATUS_DATA);
                        if(targetData.tpa_target() != null && targetData.tpa_target().equals(sender.getUUID())) {
                            serverScheduler.getTask(target.getName().getString()+"TpaTimeOut").cancel();
                            target.teleportTo(sender.getX(), sender.getY(), sender.getZ());
                            target.setAttached(Attachments.PLAYER_STATUS_DATA,new PlayerStatusData(targetData.been_controlled(),targetData.is_controlling(),targetData.location_before_control(),null));
                        }
                        return 0;
                    }))
            .build();
    public static LiteralCommandNode<CommandSourceStack> RTTCmd = Commands.literal("reset-tpa-target")
            .then(Commands.argument("target",EntityArgument.player())
                    .executes(commandContext -> {
                        ServerPlayer sender = commandContext.getSource().getPlayer();
                        ServerPlayer target = commandContext.getArgument("target", EntitySelector.class).findSinglePlayer(commandContext.getSource());
                        PlayerStatusData targetData = target.getAttached(Attachments.PLAYER_STATUS_DATA);
                        if(targetData.tpa_target() != null && targetData.tpa_target().equals(sender.getUUID()))
                            target.setAttached(Attachments.PLAYER_STATUS_DATA,new PlayerStatusData(targetData.been_controlled(),targetData.is_controlling(),targetData.location_before_control(),null));
                        return  0;
                    }))
            .build();
    public static LiteralCommandNode<CommandSourceStack> tpaCmd = tpaCmdBuilder.build();
}
