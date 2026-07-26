package io.ula.drng.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import io.ula.drng.Main;
import net.minecraft.world.item.component.ResolvableProfile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public class Util {
    public static ResolvableProfile buildProfile(UUID uuid){
        return ResolvableProfile.createUnresolved(uuid);
    }

    public static JsonObject requestAPIAsJson(URL api_url){

        try {
            HttpURLConnection connection = (HttpURLConnection) api_url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            if(connection.getResponseCode() == 200){
                if (connection.getResponseCode() == 200) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        return JsonParser.parseString(response.toString()).getAsJsonObject();
                    }
                } else {
                    throw new RuntimeException("Http response code:" + connection.getResponseCode());
                }
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        return new JsonObject();
    }
}
