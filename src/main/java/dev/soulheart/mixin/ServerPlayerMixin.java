package dev.soulheart.mixin;

import dev.soulheart.SoulHeartState;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    /** On respawn after a protected death, hand the old inventory to the new player. XP is NOT copied. */
    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void soulheart$restoreInventory(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        if (!alive && oldPlayer.getAttachedOrElse(SoulHeartState.RESTORE, false)) {
            ServerPlayer self = (ServerPlayer) (Object) this;
            self.getInventory().replaceWith(oldPlayer.getInventory());
        }
    }

    /** Blue heart before the name, in the tab list only. */
    @Inject(method = "getTabListDisplayName", at = @At("RETURN"), cancellable = true)
    private void soulheart$tabHeart(CallbackInfoReturnable<Component> cir) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        if (SoulHeartState.isProtected(self)) {
            Component base = cir.getReturnValue();
            if (base == null) {
                base = PlayerTeam.formatNameForTeam(self.getTeam(), self.getName());
            }
            // Empty parent so the heart's blue doesn't bleed into the name.
            cir.setReturnValue(Component.empty()
                    .append(Component.literal("\u2764 ").withStyle(ChatFormatting.BLUE))
                    .append(base));
        }
    }
}
