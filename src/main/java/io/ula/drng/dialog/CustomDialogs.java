package io.ula.drng.dialog;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.ula.drng.Main;
import io.ula.api.config.*;
import io.ula.api.dialog.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dialog.*;
import net.minecraft.server.dialog.action.*;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.server.dialog.input.SingleOptionInput;
import net.minecraft.server.dialog.input.TextInput;
import net.minecraft.server.level.ServerPlayer;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class CustomDialogs {
    private static MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();
    private static InlineConfigFile EULA_CONTENTS = (InlineConfigFile) Main.getConfigManager().getConfig("drng:eula_contents");
    public static final Dialog EULA_DIALOG = new DialogBuilder(Component.literal("EULA"))
            .bodies(List.of(
                    new PlainMessage(Component.literal(String.format("%s 更新内容：",Main.getVersion())),600),
                    new PlainMessage(Component.literal(EULA_CONTENTS.getKey("drng.eula.update").getAsString()),600),
                    new PlainMessage(Component.literal("\n服务器规则："),600),
                    new PlainMessage(Component.literal(EULA_CONTENTS.getKey("drng.eula.content").getAsString()),600)
            ))
            .actions(List.of(

                    new Button(new CommonButtonData(Component.literal("同意"),180),new CustomAll(Identifier.tryBuild("drng","eula/accept"),Optional.empty()),(serverPlayer,packet) -> {
                        ConfigFile PLAYER_EULA = Main.getConfigManager().getConfig("drng:eula");
                        PLAYER_EULA.addKey(serverPlayer.getStringUUID(),true);
                    }),
                    new Button(new CommonButtonData(Component.literal("拒绝"),180),new CustomAll(Identifier.tryBuild("drng","eula/reject"),Optional.empty()),(serverPlayer,packet) -> serverPlayer.connection.disconnect(Component.literal("未同意许可").withStyle(ChatFormatting.RED)))
                    )
            )
            .build();


    private static ParsedTemplate template = ParsedTemplate.parse("/player-write- $(author)")
            .result()
            .orElseThrow();
    public static final Dialog NEW_NOTICE_DIALOG = new DialogBuilder(Component.literal("发布公告"))
            .inputs(
                    List.of(
                            new Input("author",new TextInput(500,Component.literal("发布者"),true,"",10,Optional.empty())),
                            new Input("title",new TextInput(500,Component.literal("简介"),true,"",20,Optional.empty())),
                            new Input("text",new TextInput(500,Component.literal("正文"),true,"",131,Optional.of(new TextInput.MultilineOptions(Optional.of(512),Optional.of(512))))),
                            new Input("time_limit",new SingleOptionInput(130,List.of(
                                    new SingleOptionInput.Entry("1",Optional.of(Component.literal("1天")),true),
                                    new SingleOptionInput.Entry("5",Optional.of(Component.literal("5天")),false),
                                    new SingleOptionInput.Entry("30",Optional.of(Component.literal("30天")),false)
                            ),Component.literal("时间限制"),true))
                    )
            )
            .actions(
                    List.of(
                            new Button(new CommonButtonData(Component.literal("发布"),180),new CustomAll(Identifier.tryBuild("drng","notice/boardcast"),Optional.empty()),(player,packet) -> {
                                if(packet.payload().get() instanceof CompoundTag compoundTag) {
                                    ConfigFile NOTICES = Main.getConfigManager().getConfig("drng:notices");
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
                                            .withZone(ZoneId.of("Asia/Shanghai"));
                                    JsonObject notice = new JsonObject();
                                    String author = compoundTag.get("author").asString().get();
                                    String title = compoundTag.get("title").asString().get();
                                    String text = compoundTag.get("text").asString().get();
                                    String time_limit = compoundTag.get("time_limit").asString().get();
                                    if (author.isEmpty() || title.isEmpty() || text.isEmpty()) {
                                        player.sendSystemMessage(Component.literal("公告未发送：存在空的必填项").withStyle(ChatFormatting.RED));
                                    } else {
                                        notice.addProperty("author", author);
                                        notice.addProperty("title", title);
                                        notice.addProperty("text", text);
                                        notice.addProperty("created_time", formatter.format(LocalDate.now(ZoneId.of("Asia/Shanghai"))));
                                        notice.addProperty("deadline", formatter.format(LocalDate.now(ZoneId.of("Asia/Shanghai")).plusDays(Integer.parseInt(time_limit))));

                                        if (NOTICES.has("notices")) {
                                            NOTICES.getKey("notices").getAsJsonArray().add(notice);
                                        } else {
                                            NOTICES.addKey("notices", new JsonArray());
                                            NOTICES.getKey("notices").getAsJsonArray().add(notice);
                                        }
                                        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                                            p.sendSystemMessage(
                                                    Component.literal("公告栏上有新的信息，点击查看")
                                                            .setStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/notice list")).applyFormat(ChatFormatting.GREEN))
                                            );
                                        }
                                    }
                                }
                            })
                    )
            )
            .cancellable(true)
            .build();
}