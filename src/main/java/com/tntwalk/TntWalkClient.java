package com.tntwalk;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public class TntWalkClient implements ClientModInitializer {
    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(ResourceLocation.fromNamespaceAndPath(TntWalkMod.MOD_ID, "main"));

    private static KeyMapping openSettingsKey;

    @Override
    public void onInitializeClient() {
        ClientConfig.load();

        openSettingsKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.tntwalk.settings",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_F8,
                CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openSettingsKey.consumeClick()) {
                if (client.player != null && client.screen == null) {
                    client.setScreen(new FuseScreen());
                }
            }
        });

        // Send the saved settings to the server whenever we join a world.
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> sendSettings());
    }

    public static void sendSettings() {
        if (ClientPlayNetworking.canSend(SetFusePayload.TYPE)) {
            ClientPlayNetworking.send(new SetFusePayload(ClientConfig.fuseTicks(), ClientConfig.enabled));
        }
    }
}
