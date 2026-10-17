package com.tntwalk;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TntWalkMod implements ModInitializer {
    public static final String MOD_ID = "tntwalk";

    private record Settings(int fuseTicks, boolean enabled) {}

    private static final Settings DEFAULT = new Settings(80, true); // 4 seconds
    private static final Map<UUID, Settings> SETTINGS = new HashMap<>();
    private static final Map<UUID, Integer> LAST_TRIGGER = new HashMap<>();
    private static final int COOLDOWN_TICKS = 10;

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(SetFusePayload.TYPE, SetFusePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SetFusePayload.TYPE, (payload, context) -> {
            int ticks = Math.max(0, Math.min(20 * 120, payload.fuseTicks())); // 0..120 seconds
            SETTINGS.put(context.player().getUUID(), new Settings(ticks, payload.enabled()));
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID id = handler.getPlayer().getUUID();
            SETTINGS.remove(id);
            LAST_TRIGGER.remove(id);
        });

        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
    }

    private void onServerTick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Settings settings = SETTINGS.getOrDefault(player.getUUID(), DEFAULT);
            if (!settings.enabled() || player.isSpectator() || !player.onGround()) continue;

            int last = LAST_TRIGGER.getOrDefault(player.getUUID(), -1000);
            if (player.tickCount - last < COOLDOWN_TICKS) continue;

            ServerLevel level = (ServerLevel) player.level();
            BlockPos pos = player.getOnPos();
            BlockState state = level.getBlockState(pos);

            if (state.isAir()
                    || state.is(Blocks.TNT)
                    || !state.getFluidState().isEmpty()
                    || state.getDestroySpeed(level, pos) < 0) {
                continue; // skip air, TNT, liquids and unbreakable blocks (bedrock...)
            }

            LAST_TRIGGER.put(player.getUUID(), player.tickCount);

            level.removeBlock(pos, false);
            PrimedTnt tnt = new PrimedTnt(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player);
            tnt.setFuse(settings.fuseTicks());
            tnt.setDeltaMovement(Vec3.ZERO);
            level.addFreshEntity(tnt);
            level.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }
}
