package io.ula.drng.attachments;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;

import java.util.Optional;
import java.util.UUID;

public class Attachments {
    public static final AttachmentType<PlayerCntData> PLAYER_CNT_DATA = AttachmentRegistry.create(Identifier.fromNamespaceAndPath("dr-ng","player_cnt")
    ,playerCntDataBuilder -> playerCntDataBuilder
                    .initializer(() -> new PlayerCntData(0,0,0))
                    .copyOnDeath()
    );
    public static final AttachmentType<PlayerStatusData> PLAYER_STATUS_DATA = AttachmentRegistry.create(Identifier.fromNamespaceAndPath("dr-ng","player_status"),
            playerStatusDataBuilder -> playerStatusDataBuilder
                    .initializer(() -> new PlayerStatusData(Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty()))
                    .persistent(PlayerStatusData.CODEC)
                    .copyOnDeath()
    );
}
