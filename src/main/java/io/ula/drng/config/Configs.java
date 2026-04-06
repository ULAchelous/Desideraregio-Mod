package io.ula.drng.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.ula.drng.Main;
import net.fabricmc.loader.api.FabricLoader;



public class Configs {
    public static class ConfigPath{
        public static final String PLAYER = "player";
        public static final String PERMISSION = "permission";
        public static final String TALKBAR = "talkbar";
        public static final String NOTICE = "notice";
        public static final String COMMAND = "command";
        public static final String INLINE_TRANSLATABLE= "translatable";
    }

//    public static ConfigFile PMS_CODES;
//    public static ConfigFile DRNG_PERMISSIONS;
//    public static ConfigFile PLAYER_TITLES ;
//    public static ConfigFile DRNG_TIPS;
//    public static ConfigFile DRNG_NOTICES;
//    public static ConfigFile LOG_CMD;
//    public static ConfigFile PLAYER_HOMES;
//    public static ConfigFile CHAT_REPLACEMENTS;
//    public static ConfigFile PLAYER_EULA;
//    public static ConfigFile CONFIG;
//    public static InlineConfigFile COMMENTARY;
    public static void init(Main ownerMod){
        String version = Main.getVersion();

        JsonObject mainConfigs = new JsonObject();
        JsonObject log_cmd = new JsonObject();
        mainConfigs.addProperty("version",version);
        mainConfigs.addProperty("allowCreativeMode",false);
        mainConfigs.addProperty("balancedOp",true);
        log_cmd.addProperty("version",version);
        log_cmd.add("commands",new JsonArray());

        ConfigManager configManager = ownerMod.getConfigManager();
         configManager.register("drng:main",new ConfigFile("config.json",null,mainConfigs));
        configManager.register("drng:log_cmd",new ConfigFile("log_cmd.json",ConfigPath.COMMAND,log_cmd));
        configManager.register("drng:titles",new ConfigFile("player_titles.json",ConfigPath.PLAYER,null));
        configManager.register("drng:tips",new ConfigFile("tips.json",ConfigPath.TALKBAR,null));
        configManager.register("drng:notices",new ConfigFile("notices.json",ConfigPath.NOTICE,null));
        configManager.register("drng:homes",new ConfigFile("player_homes.json",ConfigPath.PLAYER,null));
        configManager.register("drng:chat_replacements",new ConfigFile("chat_replacements.json",ConfigPath.PLAYER,null));
        configManager.register("drng:eula",new ConfigFile("player_eula.json",ConfigPath.PLAYER,null));
        configManager.register("drng:eula_contents",new InlineConfigFile(ConfigPath.INLINE_TRANSLATABLE,"eula.json"));
    }
}
