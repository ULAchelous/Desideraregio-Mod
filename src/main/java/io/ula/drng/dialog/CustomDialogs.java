package io.ula.drng.dialog;

import io.ula.drng.Main;
import io.ula.config.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dialog.*;
import net.minecraft.server.dialog.action.*;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.server.dialog.input.SingleOptionInput;
import net.minecraft.server.dialog.input.TextInput;

import java.util.List;
import java.util.Optional;

public class CustomDialogs {
    private static InlineConfigFile EULA_CONTENTS = (InlineConfigFile) Main.getConfigManager().getConfig("drng:eula_contents");
    public static final Dialog EULA_DIALOG = new DialogBuilder(Component.literal("EULA"))
            .bodies(List.of(
                    new PlainMessage(Component.literal(String.format("%s 更新内容：",Main.getVersion())),600),
                    new PlainMessage(Component.literal(EULA_CONTENTS.getKey("drng.eula.update").getAsString()),600),
                    new PlainMessage(Component.literal("\n服务器规则："),600),
                    new PlainMessage(Component.literal(EULA_CONTENTS.getKey("drng.eula.content").getAsString()),600)
            ))
            .actions(List.of(
                    new ActionButton(new CommonButtonData(Component.literal("同意"),180),Optional.of(new CustomAll(Identifier.tryBuild("drng","eula/accept"),Optional.empty()))),
                    new ActionButton(new CommonButtonData(Component.literal("拒绝"),180),Optional.of(new CustomAll(Identifier.tryBuild("drng","eula/reject"),Optional.empty())))
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
                            new ActionButton(new CommonButtonData(Component.literal("发布"),180),Optional.of(new CustomAll(Identifier.tryBuild("drng","notice/boardcast"),Optional.empty())))
                    )
            )
            .cancellable(true)
            .build();
}
