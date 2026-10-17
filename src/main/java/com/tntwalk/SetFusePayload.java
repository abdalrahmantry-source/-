package com.tntwalk;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Packet sent from the client to the server with the chosen fuse time (in ticks) and on/off state. */
public record SetFusePayload(int fuseTicks, boolean enabled) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetFusePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(TntWalkMod.MOD_ID, "set_fuse"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetFusePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SetFusePayload::fuseTicks,
            ByteBufCodecs.BOOL, SetFusePayload::enabled,
            SetFusePayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
