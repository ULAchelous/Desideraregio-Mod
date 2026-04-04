package io.ula.drng.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public record PlayerStatusData(@Nullable UUID been_controlled, @Nullable UUID is_controlling, @Nullable Vec3 location_before_control) {
    public static final Codec<PlayerStatusData> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    UUIDUtil.CODEC.fieldOf("been_controlled_by").forGetter(PlayerStatusData::been_controlled),
                    UUIDUtil.CODEC.fieldOf("is_controlling").forGetter(PlayerStatusData::is_controlling),
                    Vec3.CODEC.fieldOf("location_before_control").forGetter(PlayerStatusData::location_before_control)
            ).apply(instance,PlayerStatusData::new));
}
