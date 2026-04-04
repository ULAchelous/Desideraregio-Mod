package io.ula.drng.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record PlayerCntData(int digCnt, int deathCnt, int onlineTime) {
    public static final Codec<PlayerCntData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("dig_cnt").forGetter(PlayerCntData::digCnt),
                    Codec.INT.fieldOf("death_cnt").forGetter(PlayerCntData::deathCnt),
                    Codec.INT.fieldOf("online_time").forGetter(PlayerCntData::onlineTime)
            ).apply(instance, PlayerCntData::new)
    );
}
