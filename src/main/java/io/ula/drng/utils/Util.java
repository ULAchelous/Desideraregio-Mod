package io.ula.drng.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import io.ula.drng.Main;
import net.minecraft.world.item.component.ResolvableProfile;

import java.io.InputStream;
import java.util.Base64;
import java.util.UUID;

public class Util {
    public static ResolvableProfile buildProfile(UUID uuid){
        return ResolvableProfile.createUnresolved(uuid);
    }
}
