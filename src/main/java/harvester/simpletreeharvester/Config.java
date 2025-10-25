package harvester.simpletreeharvester;

public class Config {
    // Limits / tuning
    public int maxLogs = 128;
    public int maxFoliage = 512;
    public int maxRadius = 8;
    public int maxVertical = 48;

    // Validation
    public boolean requireFoliageAlongTrunk = true;
    public int leafCheckRadius = 3;
    public int minLeaves = 3;

    // Behavior toggles
    public boolean breakOverworldLeaves = true;
    public boolean breakNetherWartAndShroomlight = true;
    public boolean breakMushroomCaps = true;
    public boolean breakChorusFlowers = true;

    // Foliage seeding
    public int foliageSeedNeighborRadius = 1;
}
