package io.ula.drng.utils.kei;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.ula.api.config.ConfigFile;
import io.ula.drng.Main;
import io.ula.drng.utils.PlayerUtils;
import io.ula.drng.utils.Util;
import io.ula.drng.utils.kei.tool.FuncTool;
import io.ula.drng.utils.kei.tool.Tools;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.objects.PlayerSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class KeiChatBotUtils {
    private static record Message(String msg,UUID user){};
    private static final int MAX_TOOL_ROUNDS = 4;
    private static String prompt = "";
    public static Map<UUID,Boolean> map = new HashMap<>();
    private static Queue<String> msgQueue = new LinkedList<>();
    private static Queue<Message> queuedMsg = new LinkedList<>();
    private static MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();
    private static ConfigFile keiConfig;
    private static Logger LOGGER = LogManager.getLogger("dr-ng/kei");
    private static volatile Boolean isGenerating = false;
    public static void onChat(String msg, UUID user){
        if(!isGenerating)
            CompletableFuture.runAsync(() -> callModel(user,msg));
        else
            queuedMsg.offer(new Message(msg,user));
    }
    public static void callModel(UUID user,String msg){
        isGenerating = true;
        try {
            URL baseURL;
            try {
                baseURL = new URL(keiConfig.getKey("api").getAsString());
            }catch (MalformedURLException e){
                LOGGER.error(e.getMessage());
                LogErr("apiURL格式不正确");
                return;
            }
            String apiKey = keiConfig.getKey("api_key").getAsString();
            String model = keiConfig.getKey("model").getAsString();
            String systemPrompt = keiConfig.getKey("system_prompt").getAsString() + prompt;
            if(apiKey.isBlank()){
                LogErr("apiKey为空");
                return;
            }
            if(model.isBlank()){
                LogErr("模型名称为空");
                return;
            }

            JsonArray messages = new JsonArray();
            JsonObject systemMsg = new JsonObject();
            systemMsg.addProperty("role","system");
            systemMsg.addProperty("content", systemPrompt);
            JsonObject userMsg = new JsonObject();
            userMsg.addProperty("role","user");
            userMsg.addProperty("content",user.toString() + ":" + msg);
            messages.add(systemMsg);
            messages.add(userMsg);

            JsonObject payload = new JsonObject();
            payload.addProperty("model", model);
            payload.add("messages", messages);
            payload.add("tools", buildTools());

            for (int round = 0; round < MAX_TOOL_ROUNDS; round++) {
                JsonObject response;
                try {
                    response = Util.requestOpenAIAPIAsJson(baseURL, payload, 10000, 60000, apiKey);
                } catch (Exception e){
                    LOGGER.error("请求模型 API 失败", e);
                    LogErr("请求模型 API 失败，详见服务端日志");
                    return;
                }
                if (response.has("http_code")) {
                    String errorInfo = response.has("error") ? response.get("error").toString() : "未知错误";
                    LOGGER.error("模型 API 返回错误码 {}: {}", response.get("http_code").getAsInt(), errorInfo);
                    LogErr("模型 API 返回错误，详见服务端日志");
                    return;
                }
                JsonArray choices = response.getAsJsonArray("choices");
                if (choices == null || choices.size() == 0) {
                    LogErr("API 响应异常：未包含 choices");
                    return;
                }
                JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
                if (!message.has("tool_calls") || message.getAsJsonArray("tool_calls").size() == 0) {
                    String content = message.has("content") && !message.get("content").isJsonNull()
                            ? message.get("content").getAsString() : "";
                    sendReply(content,server.getPlayerList().getPlayer(user));
                    return;
                }
                messages.add(message);
                for (JsonElement toolCallElement : message.getAsJsonArray("tool_calls")) {
                    JsonObject toolCall = toolCallElement.getAsJsonObject();
                    String toolName = toolCall.getAsJsonObject("function").get("name").getAsString();

                    JsonObject args = new JsonObject();
                    JsonObject fn = toolCall.getAsJsonObject("function");
                    if(fn.has("arguments") && !fn.get("arguments").isJsonNull()){
                        String argsStr = fn.get("arguments").getAsString();
                        if(!argsStr.isBlank()){
                            try {
                                args = JsonParser.parseString(argsStr).getAsJsonObject();
                            }catch (Exception e){
                                LOGGER.error("工具参数解析失败: {}", argsStr, e);
                            }
                        }
                    }
                    JsonObject toolResult = executeTool(toolName, args);
                    JsonObject toolMsg = new JsonObject();
                    toolMsg.addProperty("role","tool");
                    toolMsg.addProperty("tool_call_id", toolCall.get("id").getAsString());
                    toolMsg.addProperty("content", toolResult.toString());
                    messages.add(toolMsg);
                }
            }
            LogErr("工具调用轮数超限，已停止");
        } finally {
            isGenerating = false;
        }
        Message next = queuedMsg.poll();
        if(next != null){
            CompletableFuture.runAsync(() -> callModel(next.user(),next.msg()));
        }
    }


    private static JsonObject executeTool(String name, JsonObject args){
        for(FuncTool tool : Tools.TOOLS){
            if(!tool.getName().equals(name))
                continue;

            MinecraftServer srv = server;
            if(srv == null) return errorJson("server 不可用");
            if(srv.isSameThread()) return tool.func(args);
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<JsonObject> result = new AtomicReference<>();
            srv.execute(() -> {
                try {
                    result.set(tool.func(args));
                } catch (Exception e){
                    LOGGER.error("工具执行异常", e);
                    result.set(errorJson("工具执行异常"));
                } finally {
                    latch.countDown();
                }
            });
            try {
                latch.await();
            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }
            return result.get();
        }
        return errorJson("未知工具: " + name);
    }

    /**
     * 按 UUID 查玩家（工具执行时已回主线程，可直接调用）。非法 UUID 或玩家不在线返回 null。
     */
    public static ServerPlayer getPlayerByUuid(String uuid){
        if(server == null || uuid == null || uuid.isBlank())
            return null;
        try {
            return server.getPlayerList().getPlayer(UUID.fromString(uuid));
        } catch (Exception e){
            return null;
        }
    }

    public static void clearContext(){
        msgQueue = new LinkedList<>();
        prompt = "";
    }
    public static JsonObject errorJson(String info){
        JsonObject err = new JsonObject();
        err.addProperty("error", info);
        return err;
    }

    private static JsonArray buildTools(){
        JsonArray tools = new JsonArray();
        for(FuncTool tool : Tools.TOOLS)
            tools.add(tool.getSchema());
        return tools;
    }

    private static void sendReply(String content,ServerPlayer sender){
        if(content == null || content.isBlank()) return;
        MinecraftServer srv = server;
        if(srv == null) return;
        if(content.contains(sender.getName().getString()))
            map.put(sender.getUUID(),true);
        srv.execute(() -> {
            ResolvableProfile profile = Util.buildProfile(UUID.fromString("2b856f35-91bb-4a09-80b6-6c81d7d28787"));
            MutableComponent mu = Component.literal("<").append(Component.object(new PlayerSprite(profile,true))).append(" ")
                    .append(PlayerUtils.getPlayerTitles("kei"))
                    .append("kei>")
                    .append(" ");
            for(ServerPlayer player : srv.getPlayerList().getPlayers())
                player.sendSystemMessage(mu.copy().append(content));
        });
    }

    public static void appendMsg(String msg,UUID user){
        keiConfig = Main.getConfigManager().getConfig("drng:kei");
        String msgh = String.format("%s:%s",server.getPlayerList().getPlayer(user).getName().getString(),msg);
        if(msgQueue.size() >= keiConfig.getKey("max_history_size").getAsInt()) {
            msgQueue.remove();
            if(prompt.indexOf('\n') != -1)
                prompt = prompt.substring(prompt.indexOf('\n'));
        }
        msgQueue.offer(msgh);
        if(!prompt.isBlank()) prompt += '\n';
        prompt += msgh;
    }
    private static void LogErr(String info){
        MinecraftServer srv = server;
        if(srv == null){
            LOGGER.error("kei错误: {}", info);
            return;
        }
        srv.execute(() -> {
            Component component = Component.literal("kei出现错误：").append(info).withStyle(ChatFormatting.RED);
            for(ServerPlayer p : srv.getPlayerList().getPlayers())
                p.sendSystemMessage(component);
        });
    }
    public static JsonObject getPlayerListAsJson(){
        JsonObject result = new JsonObject();
        result.add("players",new JsonArray());
        JsonArray playerList = result.get("players").getAsJsonArray();
        for(ServerPlayer player : server.getPlayerList().getPlayers()){
            if(!PlayerUtils.isFakePlayer(player)) {
                JsonObject key = new JsonObject();
                key.addProperty("uuid", player.getUUID().toString());
                key.addProperty("name", player.getName().getString());
                key.addProperty("x",player.getBlockX());
                key.addProperty("y",player.getBlockY());
                key.addProperty("z",player.getBlockZ());
                playerList.add(key);
            }
        }
        return result;
    }

    public static JsonObject getBlockListAsJson(int radius,ServerPlayer player){
        ServerLevel level = player.level();
        BlockPos source = player.getOnPos();
        JsonObject result = new JsonObject();
        result.add("blocks",new JsonArray());
        if(radius > 16)  radius = 16;
        int minX = source.getX()-(radius/2),maxX = source.getX()+(radius - (radius/2));
        int minZ = source.getZ()-(radius/2),maxZ = source.getZ()+(radius - (radius/2));
        int minY = source.getY(),maxY = source.getY() + radius;
        for(int i = minY;i<=maxY;i++){
            for(int j = minX;j<=maxX;j++){
                for(int k = minZ;k<=maxZ;k++){
                    BlockState blockState = level.getBlockState(new BlockPos(j,i,k));
                    String id = BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).toString();
                    JsonObject block = new JsonObject();
                    block.addProperty("type",id);
                    block.add("pos",buildPosAsJson(j,i,k));
                    result.get("blocks").getAsJsonArray().add(block);
                }
            }
        }
        JsonObject playerInfo = new JsonObject();
        playerInfo.addProperty("uuid",player.getUUID().toString());
        playerInfo.add("pos",buildPosAsJson(player.getBlockX(),player.getBlockY(),player.getBlockZ()));
        result.add("player",playerInfo);
        return result;
    }
    private static JsonObject buildPosAsJson(Number x,Number y,Number z){
        JsonObject result = new JsonObject();
        result.addProperty("x",x);
        result.addProperty("y",y);
        result.addProperty("z",z);
        return result;
    }
}
