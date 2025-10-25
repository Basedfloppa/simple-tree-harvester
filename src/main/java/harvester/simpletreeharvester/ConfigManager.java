package harvester.simpletreeharvester;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private final Path path;
    private Config config;

    public ConfigManager(Path configDir, String fileName) {
        this.path = configDir.resolve(fileName);
    }

    public Config get() { return config; }

    public void loadOrCreate() {
        try {
            if (Files.notExists(path)) {
                this.config = new Config(); // defaults
                save(); // write defaults
            } else {
                try (Reader r = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                    this.config = GSON.fromJson(r, Config.class);
                }
                if (this.config == null) { // corrupted? fall back to defaults
                    this.config = new Config();
                    save();
                }
            }
        } catch (IOException e) {
            // On error, fall back to defaults but don’t overwrite user file
            this.config = new Config();
        }
    }

    public void save() {
        try {
            Files.createDirectories(path.getParent());
            try (Writer w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(this.config, w);
            }
        } catch (IOException ignored) {}
    }
}
