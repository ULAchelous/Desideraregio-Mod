package io.ula.drng.attachments;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public class Attachments {
    public static final AttachmentType<PlayerCntData> PLAYER_CNT_DATA = AttachmentRegistry.create(Identifier.fromNamespaceAndPath("dr-ng","player_cnt")
    ,playerCntDataBuilder -> playerCntDataBuilder
                    .initializer(() -> new PlayerCntData(0,0,0))
                    .copyOnDeath()
    );
    public static final AttachmentType<PlayerStatusData> PLAYER_STATUS_DATA = AttachmentRegistry.create(Identifier.fromNamespaceAndPath("dr-ng","player_status"),
            playerStatusDataBuilder -> playerStatusDataBuilder
                    .initializer(() -> new PlayerStatusData(null,null,null,null))
                    .persistent(PlayerStatusData.CODEC)
                    .copyOnDeath()
    );
}
