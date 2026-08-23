package io.ula.drng.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.ula.drng.attachments.Attachments;
import io.ula.drng.attachments.PlayerStatusData;
import io.ula.api.scheduler.*;
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
import java.util.Collections;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class TpaCmd {
    private static LiteralArgumentBuilder<CommandSourceStack> tpaCmdBuilder = Commands.literal("tpa")
            .then(Commands.argument("target", EntityArgument.player()).executes(context -> {
                ServerPlayer sendr = (ServerPlayer) context.getSource().getPlayer();
                String local = sendr.clientInformation().language();
                ServerPlayer target = context.getArgument("target", EntitySelector.class).findSinglePlayer(context.getSource());
                ServerScheduler serverScheduler = ((ServerSchedulerHolder)context.getSource().getServer()).getServerSchedule();
                serverScheduler.runTask(new ScheduleTask(sendr.getName().getString() + "TpaTimeOut",(server,task) -> {
                    sendr.sendSystemMessage(Component.literal("传送请求超时").withStyle(ChatFormatting.RED));
                    PlayerStatusData targetData = sendr.getAttached(Attachments.PLAYER_STATUS_DATA);
                    sendr.setAttached(Attachments.PLAYER_STATUS_DATA,new PlayerStatusData(targetData.been_controlled(),targetData.is_controlling(),targetData.location_before_control(),Optional.empty()));
                },20*15));

                PlayerStatusData senderData = sendr.getAttached(Attachments.PLAYER_STATUS_DATA);
                sendr.setAttached(Attachments.PLAYER_STATUS_DATA,new PlayerStatusData(senderData.been_controlled(),senderData.is_controlling(),senderData.location_before_control(), Optional.of(target.getUUID())));

                if(local.equals("zh_cn")) {
                    sendr.sendSystemMessage(Component.literal("请耐心等待对方接受传送哦~"));
                    target.sendSystemMessage(Component.literal("玩家 ")
                            .append(sendr.getName().copy().withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW))
                            .append(Component.literal(" 想要传送到你这里！"))
                    );
                    target.sendSystemMessage(Component.empty()
                            .append(Component.literal("[同意]")
                                    .append(Component.object(new AtlasSprite(Identifier.tryBuild("minecraft", "gui"), Identifier.tryBuild("minecraft", "pending_invite/accept"))))
                                    .setStyle(Style.EMPTY
                                            .withHoverEvent(new HoverEvent.ShowText(Component.literal("点击确认传送").withStyle(ChatFormatting.GREEN)))
                                            .withClickEvent(new ClickEvent.RunCommand(String.format("/player-teleport-accept %s", sendr.getName().getString())))
                                    )
                            )
                            .append(" ")
                            .append(Component.literal("[拒绝]")
                                    .append(Component.object(new AtlasSprite(Identifier.tryBuild("minecraft", "gui"), Identifier.tryBuild("minecraft", "pending_invite/reject"))))
                                    .setStyle(Style.EMPTY
                                            .withHoverEvent(new HoverEvent.ShowText(Component.literal("点击拒绝传送").withStyle(ChatFormatting.GREEN)))
                                            .withClickEvent(new ClickEvent.RunCommand(String.format("/reset-tpa-target %s", sendr.getName().getString())))
                                    ))
                    );
                }else{
                    sendr.sendSystemMessage(Component.literal("Please wait"));
                    target.sendSystemMessage(Component.literal("Player ")
                            .append(sendr.getName().copy().withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW))
                            .append(Component.literal(" Ask to teleport to your location"))
                    );
                    target.sendSystemMessage(Component.empty()
                            .append(Component.literal("[Accept]")
                                    .append(Component.object(new AtlasSprite(Identifier.tryBuild("minecraft", "gui"), Identifier.tryBuild("minecraft", "pending_invite/accept"))))
                                    .setStyle(Style.EMPTY
                                            .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to accept").withStyle(ChatFormatting.GREEN)))
                                            .withClickEvent(new ClickEvent.RunCommand(String.format("/player-teleport-accept %s", sendr.getName().getString())))
                                    )
                            )
                            .append(" ")
                            .append(Component.literal("[Reject]")
                                    .append(Component.object(new AtlasSprite(Identifier.tryBuild("minecraft", "gui"), Identifier.tryBuild("minecraft", "pending_invite/reject"))))
                                    .setStyle(Style.EMPTY
                                            .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to reject").withStyle(ChatFormatting.GREEN)))
                                            .withClickEvent(new ClickEvent.RunCommand(String.format("/reset-tpa-target %s", sendr.getName().getString())))
                                    ))
                    );
                }
                return 0;
            }))
            .requires(commandSourceStack -> commandSourceStack.isPlayer());

    public static LiteralCommandNode<CommandSourceStack> PTACmd = Commands.literal("player-teleport-accept")
            .then(Commands.argument("target",EntityArgument.player())
                    .executes(commandContext -> {
                        ServerScheduler serverScheduler = ((ServerSchedulerHolder)commandContext.getSource().getServer()).getServerSchedule();
                        ServerPlayer sender = commandContext.getSource().getPlayer();
                        ServerPlayer target = commandContext.getArgument("target", EntitySelector.class).findSinglePlayer(commandContext.getSource());
                        PlayerStatusData targetData = target.getAttached(Attachments.PLAYER_STATUS_DATA);
                        if(targetData.tpa_target().isPresent() && targetData.tpa_target().get().equals(sender.getUUID())) {
                            serverScheduler.getTask(target.getName().getString()+"TpaTimeOut").cancel();
                            target.teleportTo(
                                    sender.level(),
                                    sender.getX(), sender.getY(), sender.getZ(),
                                    Collections.emptySet(),
                                    sender.getYRot(),
                                    sender.getXRot(),
                                    false
                                    );
                            target.setAttached(Attachments.PLAYER_STATUS_DATA,new PlayerStatusData(targetData.been_controlled(),targetData.is_controlling(),targetData.location_before_control(),Optional.empty()));
                        }
                        return 0;
                    }))
            .build();
    public static LiteralCommandNode<CommandSourceStack> RTTCmd = Commands.literal("reset-tpa-target")
            .then(Commands.argument("target",EntityArgument.player())
                    .executes(commandContext -> {
                        ServerPlayer sender = commandContext.getSource().getPlayer();
                        String local = sender.clientInformation().language();
                        ServerPlayer target = commandContext.getArgument("target", EntitySelector.class).findSinglePlayer(commandContext.getSource());
                        PlayerStatusData targetData = target.getAttached(Attachments.PLAYER_STATUS_DATA);
                        if(targetData.tpa_target().isPresent() && targetData.tpa_target().get().equals(sender.getUUID())) {
                            target.setAttached(Attachments.PLAYER_STATUS_DATA, new PlayerStatusData(targetData.been_controlled(), targetData.is_controlling(), targetData.location_before_control(), Optional.empty()));
                            if(local.equals("zh_cn"))
                                target.sendSystemMessage(Component.literal("你被拒绝了！").withStyle(ChatFormatting.RED));
                            else
                                target.sendSystemMessage(Component.literal("Your request have been rejected").withStyle(ChatFormatting.RED));
                        }
                            return  0;
                    }))
            .build();
    public static LiteralCommandNode<CommandSourceStack> tpaCmd = tpaCmdBuilder.build();
}
