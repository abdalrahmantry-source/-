package com.tntwalk;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import net.fabricmc.loader.api.FabricLoader;

/** Tiny client-side config saved in .minecraft/config/tntwalk.properties */
public class ClientConfig {
    public static double fuseSeconds = 4.0;
    public static boolean enabled = true;

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("tntwalk.properties");
    }

    public static int fuseTicks() {
        return (int) Math.round(fuseSeconds * 20.0);
    }

    public static void load() {
        Path f = file();
        if (!Files.exists(f)) return;
        try (Reader r = Files.newBufferedReader(f)) {
            Properties p = new Properties();
            p.load(r);
            fuseSeconds = Double.parseDouble(p.getProperty("fuseSeconds", "4.0"));
            enabled = Boolean.parseBoolean(p.getProperty("enabled", "true"));
        } catch (IOException | NumberFormatException ignored) {
        }
    }

    public static void save() {
        try (Writer w = Files.newBufferedWriter(file())) {
            Properties p = new Properties();
            p.setProperty("fuseSeconds", Double.toString(fuseSeconds));
            p.setProperty("enabled", Boolean.toString(enabled));
            p.store(w, "TNT Walk settings");
        } catch (IOException ignored) {
        }
    }
}
