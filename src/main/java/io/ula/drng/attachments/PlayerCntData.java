package io.ula.drng.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record IntegerAttachment(int digCnt,int deathCnt,) {
    public static final Codec<IntegerAttachment> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("level").forGetter(MyPlayerData::level),
                    Codec.STRING.fieldOf("rank").forGetter(MyPlayerData::rank)
            ).apply(instance, MyPlayerData::new)
    );
}
