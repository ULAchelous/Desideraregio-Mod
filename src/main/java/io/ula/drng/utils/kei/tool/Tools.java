package io.ula.drng.utils.kei.tool;

import io.ula.drng.utils.kei.KeiChatBotUtils;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class Tools {
    public static final List<FuncTool> TOOLS = List.of(
            new FuncTool("get_player_list",
                    "获取当前服务器在线玩家列表（不含假人）。当玩家询问\"谁在线\"\"现在有哪些人在线\"\"服务器里有人吗\"或者你遇到不认识的uuid发送消息等需要实时在线信息的问题时调用。返回 {\"players\":[{\"uuid\":\"...\",\"name\":\"...\",\"x\":..,\"y\":..,\"z\":..}]}。",
                    Property.object("", ""),
                    args -> KeiChatBotUtils.getPlayerListAsJson()),
            new FuncTool("query_blocks",
                    "获取指定玩家周围方块列表（以该玩家脚下为中心，横向扫描半径、纵向向上同样高度的方块）。uuid 取当前对话玩家的 UUID。当玩家询问\"我周围有什么方块\"\"附近是什么\"\"脚下是什么\",或者你希望了解玩家周边信息时调用。返回 {\"player\":{\"uuid\":\"...\",\"pos\":{\"x\":..,\"y\":..,\"z\":..}},\"blocks\":[{\"type\":\"minecraft:stone\",\"pos\":{\"x\":..,\"y\":..,\"z\":..}}]}。",
                    Property.object("", "查询参数")
                            .field(
                                    Property.string("uuid", "目标玩家的 UUID"),
                                    Property.integer("radius", "扫描半径，1-16")
                            )
                            .required("uuid", "radius"),
                    args -> {
                        ServerPlayer player = KeiChatBotUtils.getPlayerByUuid(args.get("uuid").getAsString());
                        if(player == null)
                            return KeiChatBotUtils.errorJson("目标玩家不在线或 UUID 无效");
                        int radius = args.has("radius") ? args.get("radius").getAsInt() : 4;
                        return KeiChatBotUtils.getBlockListAsJson(radius, player);
                    })
    );
}
