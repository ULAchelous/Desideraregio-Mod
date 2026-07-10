package io.ula.drng.commands;

import com.google.gson.JsonElement;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.ula.drng.Main;
import io.ula.api.config.*;
import io.ula.drng.dialog.CustomDialogs;
import io.ula.api.scheduler.ScheduleTask;
import io.ula.api.scheduler.ServerSchedulerHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundShowDialogPacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class NoticeCmd {
    static LiteralArgumentBuilder<CommandSourceStack> noticeCmdBuilder = Commands.literal("notice")
            .then(Commands.literal("list")
                    .executes(commandContext -> {
                        ServerPlayer sender = commandContext.getSource().getPlayer();
                        String local = sender.clientInformation().language();
                        ItemStack is = sender.getItemInHand(InteractionHand.MAIN_HAND);
                        sender.setItemInHand(InteractionHand.MAIN_HAND,getNoticeBook(local));
                        ((ServerSchedulerHolder)commandContext.getSource().getServer()).drng$getServerSchedule().runTask(new ScheduleTask(sender.getName().getString()+"replaceItem",() -> {
                            sender.connection.send(new ClientboundOpenBookPacket(InteractionHand.MAIN_HAND));
                            sender.setItemInHand(InteractionHand.MAIN_HAND,is);
                        },5));
                        return 0;
                    }
            ))
            .then(Commands.literal("write")
                    .executes(context -> {
                        ServerPlayer sender = context.getSource().getPlayer();
                        String local = sender.clientInformation().language();
                        ClientboundShowDialogPacket packet = local.equals("zh_cn")? new ClientboundShowDialogPacket(Holder.direct(CustomDialogs.NEW_NOTICE_DIALOG)) : new ClientboundShowDialogPacket(Holder.direct(CustomDialogs.NEW_NOTICE_DIALOG_EN));
                        sender.connection.send(packet);
                        return 0;
                    })
            )
            .requires(commandSourceStack -> (commandSourceStack.isPlayer()));
    public static LiteralCommandNode<CommandSourceStack> noticeCmd = noticeCmdBuilder.build();

    public static LiteralCommandNode<CommandSourceStack> PRNCmd = Commands.literal("player-write-notice")
            .build();
    public static ItemStack getNoticeBook(String local){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
                .withZone(ZoneId.of("Asia/Shanghai"));

        List<Filterable<Component>> pages = new ArrayList<>();

        ConfigFile DRNG_NOTICES = Main.getConfigManager().getConfig("drng:notices");

        if(DRNG_NOTICES.has("notices")) {
            for (int i=0;i<DRNG_NOTICES.getKey("notices").getAsJsonArray().size();i++) {
                JsonElement notice = DRNG_NOTICES.getKey("notices").getAsJsonArray().get(i);
                String author = notice.getAsJsonObject().get("author").getAsString();
                String title = notice.getAsJsonObject().get("title").getAsString();
                String content = notice.getAsJsonObject().get("text").getAsString();
                String deadline = notice.getAsJsonObject().get("deadline").getAsString();
                if (LocalDate.now(ZoneId.of("Asia/Shanghai")).isAfter(LocalDate.parse(deadline,formatter))){
                    notice.getAsJsonObject().addProperty("removed",true);
                    DRNG_NOTICES.getKey("notices").getAsJsonArray().set(i,notice);
                    continue;
                }
                Component page;
                if(local.equals("zh_cn")) {
                    page = Component.empty()
                            .append(Component.literal("发布者: ").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
                            .append(Component.literal(author))
                            .append("\n")
                            .append(title)
                            .append("。")
                            .append(Component.literal(content))
                            .append("\n")
                            .append(Component.literal("发布时间：").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
                            .append(Component.literal(notice.getAsJsonObject().get("created_time").getAsString()))
                            .append("\n")
                            .append(Component.literal("截止时间：").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
                            .append(Component.literal(notice.getAsJsonObject().get("deadline").getAsString()));
                }else{
                    page = Component.empty()
                            .append(Component.literal("Author: ").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
                            .append(Component.literal(author))
                            .append("\n")
                            .append(title)
                            .append("。")
                            .append(Component.literal(content))
                            .append("\n")
                            .append(Component.literal("PublishTime(CST)：").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
                            .append(Component.literal(notice.getAsJsonObject().get("created_time").getAsString()))
                            .append("\n")
                            .append(Component.literal("DeadLine：").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
                            .append(Component.literal(notice.getAsJsonObject().get("deadline").getAsString()));
                }
               pages.add(Filterable.passThrough(page));
            }
            for(int i=0;i<DRNG_NOTICES.getKey("notices").getAsJsonArray().size();i++){
                JsonElement notice = DRNG_NOTICES.getKey("notices").getAsJsonArray().get(i);
                if(notice.getAsJsonObject().has("removed")) {
                    DRNG_NOTICES.getKey("notices").getAsJsonArray().remove(i);
                    i=0;
                }
            }
        }
        WrittenBookContent content;
        if(local.equals("zh_cn")) {
            content = new WrittenBookContent(
                    Filterable.passThrough("公告栏"),
                    "希望之地",
                    0,
                    pages,
                    true
            );
        }else{
            content = new WrittenBookContent(
                    Filterable.passThrough("NoticeBar"),
                    "Desideraregio",
                    0,
                    pages,
                    true
            );
        }
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(DataComponents.WRITTEN_BOOK_CONTENT,content);
        return book;
    }
}
