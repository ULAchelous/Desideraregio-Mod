package io.ula.drng.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModMixinPlugin implements IMixinConfigPlugin {

    public static Map<String,String> MIXIN_RULES = new HashMap<>();

    static{
        MIXIN_RULES.put("EntityPlayerMPFakeMixin","carpet");
    }

    public void onLoad(String mixinPackage){}


    public String getRefMapperConfig(){return null;}


    public boolean shouldApplyMixin(String targetClassName, String mixinClassName){
        for(Map.Entry<String,String> entry : MIXIN_RULES.entrySet()){
            String className = entry.getKey();
            String modId = entry.getValue();
            if(mixinClassName.contains(className)){
                return FabricLoader.getInstance().isModLoaded(modId);
            }
        }
        return true;
    }


    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets){}

    public List<String> getMixins(){return null;}

    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo){}


    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo){}
}
