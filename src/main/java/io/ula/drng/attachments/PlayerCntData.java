package io.ula.drng.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record PlayerCntData(long digCnt, long deathCnt, long onlineTime) {
    public PlayerCntData{
        if(digCnt > Integer.MAX_VALUE) digCnt = 0;
        if(deathCnt > Integer.MAX_VALUE) deathCnt = 0;
        if(onlineTime > Integer.MAX_VALUE) onlineTime = 0;
    }
    public static final Codec<PlayerCntData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("dig_cnt").forGetter(data -> ((int)data.digCnt)),
                    Codec.INT.fieldOf("death_cnt").forGetter(data -> ((int)data.deathCnt)),
                    Codec.INT.fieldOf("online_time").forGetter(data -> ((int)data.deathCnt))
            ).apply(instance, PlayerCntData::new)
    );
}
