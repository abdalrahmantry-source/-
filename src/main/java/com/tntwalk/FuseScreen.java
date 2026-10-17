package com.tntwalk;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class FuseScreen extends Screen {
    private EditBox secondsBox;
    private Button toggleButton;
    private boolean enabled;

    public FuseScreen() {
        super(Component.literal("TNT Walk"));
        this.enabled = ClientConfig.enabled;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;

        secondsBox = new EditBox(this.font, cx - 50, cy - 20, 100, 20, Component.literal("Seconds"));
        secondsBox.setFilter(s -> s.matches("\\d{0,3}(\\.\\d{0,2})?"));
        secondsBox.setValue(trim(ClientConfig.fuseSeconds));
        this.addRenderableWidget(secondsBox);
        this.setInitialFocus(secondsBox);

        toggleButton = Button.builder(toggleLabel(), b -> {
            enabled = !enabled;
            b.setMessage(toggleLabel());
        }).bounds(cx - 50, cy + 6, 100, 20).build();
        this.addRenderableWidget(toggleButton);

        this.addRenderableWidget(Button.builder(Component.literal("Save"), b -> saveAndClose())
                .bounds(cx - 50, cy + 32, 100, 20).build());
    }

    private Component toggleLabel() {
        return Component.literal("TNT: " + (enabled ? "ON" : "OFF"));
    }

    private static String trim(double v) {
        return v == Math.floor(v) ? Integer.toString((int) v) : Double.toString(v);
    }

    private void saveAndClose() {
        try {
            double v = Double.parseDouble(secondsBox.getValue());
            ClientConfig.fuseSeconds = Math.max(0.0, Math.min(120.0, v));
        } catch (NumberFormatException ignored) {
            // keep the old value if the text is empty / invalid
        }
        ClientConfig.enabled = enabled;
        ClientConfig.save();
        TntWalkClient.sendSettings();
        this.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(this.font, Component.literal("TNT fuse time (seconds)"),
                this.width / 2, this.height / 2 - 36, 0xFFFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
