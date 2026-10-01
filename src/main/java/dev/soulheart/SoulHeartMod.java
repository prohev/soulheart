package dev.soulheart;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.Set;

public class SoulHeartMod implements ModInitializer {
    public static final String MOD_ID = "soulheart";

    /** Chance (per chest) that a Soul Shard is added to ancient city loot. Tune to taste. */
    private static final float SHARD_CHANCE = 0.10f;

    private static final Set<Identifier> SHARD_LOOT = Set.of(
            Identifier.withDefaultNamespace("chests/ancient_city"),
            Identifier.withDefaultNamespace("chests/ancient_city_ice_box"));

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        // Bundle assets/soulheart/** into Polymer's generated resource pack (optional for players).
        PolymerResourcePackUtils.addModAssets(MOD_ID);
        SoulHeartItems.register();

        // Make the Soul Shard rare: a small chance in ancient city chests.
        LootTableEvents.MODIFY.register((key, builder, source, registries) -> {
            if (source.isBuiltin() && SHARD_LOOT.contains(key.identifier())) {
                builder.withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0f))
                        .when(LootItemRandomChanceCondition.randomChance(SHARD_CHANCE))
                        .add(LootItem.lootTableItem(SoulHeartItems.SOUL_SHARD)));
            }
        });

        // After a protected player dies: consume the heart, tell everyone, update the tab list.
        // (Inventory drops were already skipped by PlayerMixin, which runs earlier inside die().)
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof ServerPlayer player && SoulHeartState.isProtected(player)) {
                player.setAttached(SoulHeartState.RESTORE, true); // read by ServerPlayerMixin on respawn
                SoulHeartState.setProtected(player, false);       // also refreshes the tab list

                var level = player.level();
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f);
                level.sendParticles(ParticleTypes.SCULK_SOUL,
                        player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.4, 0.6, 0.4, 0.02);

                Component msg = Component.literal(player.getName().getString() + "'s soul was protected")
                        .withStyle(ChatFormatting.AQUA);
                level.getServer().getPlayerList().broadcastSystemMessage(msg, false);
            }
        });
    }
}
