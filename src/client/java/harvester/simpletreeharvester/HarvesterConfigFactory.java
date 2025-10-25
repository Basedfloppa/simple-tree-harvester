package harvester.simpletreeharvester;

import me.shedaniel.clothconfig2.api.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class HarvesterConfigFactory {
    public static Screen create(Screen parent, ConfigManager manager) {
        Config working = copy(SimpleTreeHarvester.CFG);

        ConfigBuilder b = ConfigBuilder.create().setParentScreen(parent)
                .setTitle(Text.literal("Simple Tree Harvester"));
        b.setSavingRunnable(() -> {
            SimpleTreeHarvester.CFG = working;
            manager.save();
        });

        ConfigEntryBuilder eb = b.entryBuilder();
        ConfigCategory limits = b.getOrCreateCategory(Text.literal("Limits & Tuning"));

        limits.addEntry(eb.startIntField(Text.literal("Max Logs"), working.maxLogs)
                .setDefaultValue(128).setMin(1).setMax(4096)
                .setSaveConsumer(v -> working.maxLogs = v).build());

        limits.addEntry(eb.startIntField(Text.literal("Max Foliage"), working.maxFoliage)
                .setDefaultValue(512).setMin(1).setMax(4096)
                .setSaveConsumer(v -> working.maxFoliage = v).build());

        limits.addEntry(eb.startIntField(Text.literal("Max Radius"), working.maxRadius)
                .setDefaultValue(8).setMin(1).setMax(4096)
                .setSaveConsumer(v -> working.maxRadius = v).build());

        limits.addEntry(eb.startIntField(Text.literal("Max Vertical"), working.maxVertical)
                .setDefaultValue(48).setMin(1).setMax(4096)
                .setSaveConsumer(v -> working.maxVertical = v).build());

        limits.addEntry(eb.startBooleanToggle(Text.literal("Require Foliage Along Trunk"), working.requireFoliageAlongTrunk)
                .setDefaultValue(true)
                .setSaveConsumer(v -> working.requireFoliageAlongTrunk = v).build());

        limits.addEntry(eb.startIntField(Text.literal("Leaf Check Radius"), working.leafCheckRadius)
                .setDefaultValue(3).setMin(1).setMax(4096)
                .setSaveConsumer(v -> working.leafCheckRadius = v).build());

        limits.addEntry(eb.startIntField(Text.literal("Min Leaves"), working.minLeaves)
                .setDefaultValue(3).setMin(1).setMax(4096)
                .setSaveConsumer(v -> working.minLeaves = v).build());

        limits.addEntry(eb.startBooleanToggle(Text.literal("Break Chorus Flowers"), working.breakChorusFlowers)
                .setDefaultValue(true)
                .setSaveConsumer(v -> working.breakChorusFlowers = v).build());

        limits.addEntry(eb.startBooleanToggle(Text.literal("Break Mushroom Caps"), working.breakMushroomCaps)
                .setDefaultValue(true)
                .setSaveConsumer(v -> working.breakMushroomCaps = v).build());

        limits.addEntry(eb.startBooleanToggle(Text.literal("Break Overworld Leaves"), working.breakOverworldLeaves)
                .setDefaultValue(true)
                .setSaveConsumer(v -> working.breakOverworldLeaves = v).build());

        limits.addEntry(eb.startBooleanToggle(Text.literal("Break Nether Wart And Shroom Lights"), working.breakNetherWartAndShroomlight)
                .setDefaultValue(true)
                .setSaveConsumer(v -> working.breakNetherWartAndShroomlight = v).build());

        limits.addEntry(eb.startIntField(Text.literal("Foliage Seed Neighbor Radius"), working.foliageSeedNeighborRadius)
                .setDefaultValue(1).setMin(1).setMax(4096)
                .setSaveConsumer(v -> working.foliageSeedNeighborRadius = v).build());

        return b.build();
    }

    private static Config copy(Config c) {
        Config x = new Config();
        x.maxLogs = c.maxLogs;
        x.maxFoliage = c.maxFoliage;
        x.maxRadius = c.maxRadius;
        x.maxVertical = c.maxVertical;
        x.requireFoliageAlongTrunk = c.requireFoliageAlongTrunk;
        x.leafCheckRadius = c.leafCheckRadius;
        x.minLeaves = c.minLeaves;
        x.breakOverworldLeaves = c.breakOverworldLeaves;
        x.breakNetherWartAndShroomlight = c.breakNetherWartAndShroomlight;
        x.breakMushroomCaps = c.breakMushroomCaps;
        x.breakChorusFlowers = c.breakChorusFlowers;
        x.foliageSeedNeighborRadius = c.foliageSeedNeighborRadius;
        return x;
    }
}
