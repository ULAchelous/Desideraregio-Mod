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
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;


public class HomeCmd {
    private static LiteralArgumentBuilder<CommandSourceStack> homeCmdBuilder = Commands.literal("home")
            .then(Commands.literal("locate")
                    .then(Commands.argument("id", StringArgumentType.string())
                            .executes(context -> {
                                ServerPlayer sender = context.getSource().getPlayer();
                                String senderName = sender.getName().getString();
                                String id = context.getArgument("id",String.class);
                                ConfigFile PLAYER_HOMES = Main.getConfigManager().getConfig("drng:homes");
                                int num = 0;
                                if(!PLAYER_HOMES.has(senderName))
                                    PLAYER_HOMES.addKey(senderName, new JsonArray());
                                if(!PLAYER_HOMES.has("maxCount"))
                                    PLAYER_HOMES.addKey("maxCount", 3);
                                for(JsonElement element : PLAYER_HOMES.getKey(senderName).getAsJsonArray())
                                    if(!element.getAsJsonObject().has("removed")) num++;
                                if(num > PLAYER_HOMES.getKey("maxCount").getAsInt()){
                                    sender.sendSystemMessage(Component.literal("标记点数量到达上限").withStyle(ChatFormatting.RED));
                                    return 0;
                                }
                                JsonObject marker = new JsonObject();
                                marker.addProperty("id",id);
                                marker.addProperty("location",LocationSerializer(sender.position()));
                                marker.addProperty("level",sender.level().dimensionTypeRegistration().getRegisteredName());
                                PLAYER_HOMES.getKey(senderName).getAsJsonArray().add(marker);
                                sender.sendSystemMessage(Component.literal("添加成功").withStyle(ChatFormatting.GREEN));
                                return 0;
                            })
                    )
            )
            .then(Commands.literal("remove")
                    .executes(context -> {
                        ServerPlayer sender = context.getSource().getPlayer();
                        sender.sendSystemMessage(Component.literal("点击标记点以删除它！"));
                        sender.sendSystemMessage(getPlayerHomes(sender,true));
                        return 0;
                    })
            )
            .then(Commands.literal("tp")
                    .executes(context -> {
                        ServerPlayer sender = context.getSource().getPlayer();
                        sender.sendSystemMessage(Component.literal("点击标记点以传送！"));
                        sender.sendSystemMessage(getPlayerHomes(sender,false));
                        return 0;
                    })
            )
            ;
    public static LiteralCommandNode<CommandSourceStack> homeCmd = homeCmdBuilder.build();

    public static LiteralCommandNode<CommandSourceStack> PRHCmd = Commands.literal("player-remove-home")
            .then(Commands.argument("index", IntegerArgumentType.integer())
                    .executes(commandContext -> {
                        ServerPlayer sender = commandContext.getSource().getPlayer();
                        int index = commandContext.getArgument("index", Integer.class);
                        ConfigFile PLAYER_HOMES = Main.getConfigManager().getConfig("drng:homes");
                        JsonElement element = PLAYER_HOMES.getKey(sender.getName().getString()).getAsJsonArray().get(index);
                        element.getAsJsonObject().addProperty("removed",true);
                        sender.sendSystemMessage(Component.literal("删除了 ").append(Component.literal(Integer.toString(index)).withStyle(ChatFormatting.YELLOW)).append(" 号"));
                        return 0;
                    })
            )
            .build();
    public static LiteralCommandNode<CommandSourceStack> PTHCmd = Commands.literal("player-teleport-home")
            .then(Commands.argument("index", IntegerArgumentType.integer())
                    .executes(commandContext -> {
                        MinecraftServer server = commandContext.getSource().getServer();
                        ServerPlayer sender = commandContext.getSource().getPlayer();
                        int index = commandContext.getArgument("index", Integer.class);
                        ConfigFile PLAYER_HOMES = Main.getConfigManager().getConfig("drng:homes");
                        JsonElement element = PLAYER_HOMES.getKey(sender.getName().getString()).getAsJsonArray().get(index);
                        ServerLevel serverLevel;
                        switch (element.getAsJsonObject().get("level").getAsString()){
                            case "minecraft:overworld" -> serverLevel=server.getLevel(Level.OVERWORLD);
                            case "minecraft:the_nether" -> serverLevel=server.getLevel(Level.NETHER);
                            case "minecraft:the_end" -> serverLevel=server.getLevel(Level.END);
                            default -> serverLevel=server.getLevel(Level.OVERWORLD);
                        }
                        Vec3 position = LocationDeserializer(element.getAsJsonObject().get("location").getAsString());
                        sender.teleportTo(
                                serverLevel,
                                position.x,
                                position.y,
                                position.z,
                                Collections.emptySet(),
                                0,
                                0,
                                false
                        );
                        return 0;
                    })
            )
            .build();
//    private static ClickEvent getRemoveClickEvent(JsonElement element,String id){
//        return ClickEvent.callback(audience -> {
//            audience.sendMessage(Component.literal("删除了")
//                    .append(Component.space())
//                    .append(Component.literal(id,TextColor.color(Color.YELLOW.getRGB())))
//            );
//            element.getAsJsonObject().addProperty("removed",true);
//        }, ClickCallback.Options.builder().lifetime(Duration.ofSeconds(15)).build());
//    }
//    private static ClickEvent getTeleportClickEvent(Player player,JsonElement element){
//        return ClickEvent.callback(audience -> {
//            World world = Bukkit.getWorld(element.getAsJsonObject().get("world").getAsString());
//            audience.sendMessage(Component.literal("传送...",TextColor.color(Color.GREEN.getRGB())));
//            player.teleport(LocationDeserializer(element.getAsJsonObject().get("location").getAsString(),world));
//        },ClickCallback.Options.builder().lifetime(Duration.ofSeconds(15)).build());
//    }

    static Component getPlayerHomes(ServerPlayer player,Boolean flag){
        Component component = Component.empty();
        ConfigFile PLAYER_HOMES = Main.getConfigManager().getConfig("drng:homes");
        String playerName = player.getName().getString();
        if(!PLAYER_HOMES.has(playerName))
            PLAYER_HOMES.addKey(playerName, new JsonArray());
        for(int idx = 0;idx<PLAYER_HOMES.getKey(playerName).getAsJsonArray().size();idx++) {
            JsonElement element = PLAYER_HOMES.getKey(playerName).getAsJsonArray().get(idx);
            if(element.getAsJsonObject().has("removed")) {
                PLAYER_HOMES.getKey(playerName).getAsJsonArray().remove(element);
                continue;
            }
            String id = element.getAsJsonObject().get("id").getAsString();
            if(flag) {
                component = component.copy().append(Component.literal("[")
                        .append(Component.literal(id)
                                .setStyle(Style.EMPTY
                                                .applyFormat(ChatFormatting.YELLOW)
                                                .withClickEvent(new ClickEvent.RunCommand(String.format("/player-remove-home %d",idx)))
                                        )
                        )
                        .append(Component.literal("]"))
                );
            }else{
                component = component.copy().append(Component.literal("[")
                        .append(Component.literal(id)
                                .setStyle(Style.EMPTY
                                        .applyFormat(ChatFormatting.YELLOW)
                                        .withClickEvent(new ClickEvent.RunCommand(String.format("/player-teleport-home %d",idx)))
                                )
                        )
                        .append(Component.literal("]"))
                );
            }
            component = component.copy().append(" ");
        }
        return component;
    }
    static String LocationSerializer(Vec3 vec3){
        int x=(int)Math.ceil(vec3.x),y=(int)Math.ceil(vec3.y),z=(int)Math.ceil(vec3.z);
        return String.format("%d %d %d",x,y,z);
    }
    static Vec3 LocationDeserializer(String str){
        String[] location = str.split(" ");
        return new Vec3(Integer.parseInt(location[0]),Integer.parseInt(location[1]),Integer.parseInt(location[2]));
    }
}
