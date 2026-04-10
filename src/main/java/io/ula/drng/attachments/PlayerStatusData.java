package io.ula.drng.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public record PlayerStatusData(Optional<UUID> been_controlled, Optional<UUID> is_controlling,Optional<Vec3> location_before_control,Optional<UUID> tpa_target) {


    public static final Codec<PlayerStatusData> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    UUIDUtil.CODEC.optionalFieldOf("been_controlled_by").forGetter(PlayerStatusData::been_controlled),
                    UUIDUtil.CODEC.optionalFieldOf("is_controlling").forGetter(PlayerStatusData::is_controlling),
                    Vec3.CODEC.optionalFieldOf("location_before_control").forGetter(PlayerStatusData::location_before_control),
                    UUIDUtil.CODEC.optionalFieldOf("tpa_target").forGetter(PlayerStatusData::tpa_target)
            ).apply(instance,PlayerStatusData::new));
}
