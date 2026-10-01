package dev.soulheart;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Per-player state, saved with the player so it survives relogs and restarts. */
public final class SoulHeartState {
    /** True while the player has an active Soul Heart. */
    public static final AttachmentType<Boolean> PROTECTED = AttachmentRegistry.create(
            SoulHeartMod.id("protected"), b -> b.persistent(Codec.BOOL));

    /** Set at death so the respawned player gets the old inventory back. Not copied to the new player. */
    public static final AttachmentType<Boolean> RESTORE = AttachmentRegistry.create(
            SoulHeartMod.id("restore_inventory"), b -> b.persistent(Codec.BOOL));

    private SoulHeartState() {}

    public static boolean isProtected(Player player) {
        return player.getAttachedOrElse(PROTECTED, false);
    }

    public static void setProtected(ServerPlayer player, boolean value) {
        if (value) {
            player.setAttached(PROTECTED, true);
        } else {
            player.removeAttached(PROTECTED);
        }
        refreshTab(player);
    }

    /** Re-sends this player's tab list entry to everyone so the blue heart appears/disappears now. */
    public static void refreshTab(ServerPlayer player) {
        player.level().getServer().getPlayerList().broadcastAll(
                new ClientboundPlayerInfoUpdatePacket(
                        ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, player));
    }
}
