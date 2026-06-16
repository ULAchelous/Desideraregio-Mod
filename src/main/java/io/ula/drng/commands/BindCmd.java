package io.ula.drng.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.ula.drng.Main;
import io.ula.api.config.*;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;

import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.objects.AtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.player.Player;


import java.util.List;


public class BindCmd {
    static LiteralArgumentBuilder<CommandSourceStack> bindCmdBuilder = Commands.literal("bind")
            .then(Commands.argument("targets", EntityArgument.players())
                    .then(Commands.literal("add")
                            .then(Commands.argument("key",StringArgumentType.string())
                                .then(Commands.argument("_replace",StringArgumentType.string())
                                    .executes(context -> {
                                        List<ServerPlayer> targets = context.getArgument("targets", EntitySelector.class).findPlayers(context.getSource());
                                        String key = context.getArgument("key",String.class);
                                        String _replace = context.getArgument("_replace",String.class);
                                        for(Player target : targets){
                                            if(target instanceof Player) {
                                                String targetName = target.getName().getString();
                                                JsonObject bind = new JsonObject();
                                                bind.addProperty("key", key);
                                                bind.addProperty("replace", _replace);
                                                ConfigFile CHAT_REPLACEMENTS = Main.getConfigManager().getConfig("drng:chat_replacements");

                                                if (!CHAT_REPLACEMENTS.has(targetName))
                                                    CHAT_REPLACEMENTS.addKey(targetName, new JsonArray());
                                                CHAT_REPLACEMENTS.getKey(targetName).getAsJsonArray().add(bind);
                                            }
                                        }
                                        context.getSource().getPlayer().sendSystemMessage(Component.literal("添加成功").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.YELLOW));
                                        return 0;
                                    }))))
                    .then(Commands.literal("remove")
                            .executes(context -> {
                                List<ServerPlayer> targets = context.getArgument("targets", EntitySelector.class).findPlayers(context.getSource());
                                ServerPlayer sender = context.getSource().getPlayer();
                                for(ServerPlayer target : targets) {
                                    sender.sendSystemMessage(Component.literal("点击")
                                            .append(Component.object(new AtlasSprite(Identifier.tryBuild("minecraft","gui"),Identifier.tryBuild("minecraft","pending_invite/reject"))))
                                            .append(Component.literal("以删除"))
                                            .append(Component.literal(String.format("(%s)",target.getName().getString())))
                                    );
                                    sender.sendSystemMessage(getPlayerBinds(target));
                                }
                                return  0;
                            }))
                    .requires(commandSourceStack -> commandSourceStack.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
            )
            .then(Commands.literal("add")
                    .then(Commands.argument("key",StringArgumentType.string())
                            .then(Commands.argument("_replace",StringArgumentType.string())
                                    .executes(context -> {
                                        ServerPlayer target = context.getSource().getPlayer();
                                        String key = context.getArgument("key",String.class);
                                        String _replace = context.getArgument("_replace",String.class);
                                        String targetName = target.getName().getString();
                                        JsonObject bind = new JsonObject();
                                        bind.addProperty("key",key);
                                        bind.addProperty("replace",_replace);
                                        ConfigFile CHAT_REPLACEMENTS = Main.getConfigManager().getConfig("drng:chat_replacements");
                                        if(!CHAT_REPLACEMENTS.has(targetName))
                                            CHAT_REPLACEMENTS.addKey(targetName,new JsonArray());
                                        CHAT_REPLACEMENTS.getKey(targetName).getAsJsonArray().add(bind);
                                        target.sendSystemMessage(Component.literal("添加成功"));
                                        return 0;
                                    })
                            )
                    ).requires(commandSourceStack -> !commandSourceStack.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)))
            .then(Commands.literal("remove")
                    .executes(context -> {
                        ServerPlayer sender = context.getSource().getPlayer();
                        sender.sendSystemMessage(Component.literal("点击")
                                .append(Component.object(new AtlasSprite(Identifier.tryBuild("minecraft","gui"),Identifier.tryBuild("minecraft","pending_invite/reject"))))
                                .append(Component.literal("以删除"))
                        );
                        sender.sendSystemMessage(getPlayerBinds(sender));
                        return 0;
                    }).requires(commandSourceStack -> !commandSourceStack.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)))
            .requires(commandSourceStack -> commandSourceStack.isPlayer());
    public static LiteralCommandNode<CommandSourceStack> bindCmd = bindCmdBuilder.build();
    private static Component getPlayerBinds(ServerPlayer player){
        Component component = Component.empty();
        int idx = 1;
        ConfigFile CHAT_REPLACEMENTS = Main.getConfigManager().getConfig("drng:chat_replacements");

        for(JsonElement element : CHAT_REPLACEMENTS.getKey(player.getName().getString()).getAsJsonArray()){
            if(element.getAsJsonObject().has("removed")) continue;
            String key = element.getAsJsonObject().get("key").getAsString();
            String replace = element.getAsJsonObject().get("replace").getAsString();
            component = Component.empty()
                    .append(Component.literal(Integer.toString(idx)).append(Component.literal(" ")))
                    .append(Component.literal(String.format("\"%s\" ",key)).withStyle(ChatFormatting.AQUA))
                    .append(Component.literal("-> ").withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(String.format("\"%s\" ",replace)).withStyle(ChatFormatting.AQUA))
                    .append(Component.object(new AtlasSprite(Identifier.tryBuild("minecraft","gui"),Identifier.tryBuild("minecraft","pending_invite/reject")))
                            .setStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand(String.format("/remove-player-chat-binding %s %d",player.getName().getString(),idx-1)))))
                    .append(Component.literal("\n"));
            idx++;
        }
        return component;
    }

    public static LiteralCommandNode<CommandSourceStack> rmPlCB = Commands.literal("remove-player-chat-binding")
            .then(Commands.argument("target",EntityArgument.player())
                    .then(Commands.argument("index", IntegerArgumentType.integer())
                            .executes(commandContext -> {
                                ServerPlayer sender = commandContext.getSource().getPlayer();
                                ConfigFile CHAT_REPLACEMENTS = Main.getConfigManager().getConfig("drng:chat_replacements");
                                int idx = commandContext.getArgument("index",Integer.class);
                                ServerPlayer target = commandContext.getArgument("target", EntitySelector.class).findSinglePlayer(commandContext.getSource());
                                CHAT_REPLACEMENTS.getKey(target.getName().getString()).getAsJsonArray().get(idx).getAsJsonObject().addProperty("removed",true);
                                sender.sendSystemMessage(Component.literal("删除了 ")
                                        .append(Component.literal(Integer.toString(idx+1)).withStyle(ChatFormatting.YELLOW))
                                        .append(Component.literal(" 号")));
                                return 0;
                            })))
            .build();
}
