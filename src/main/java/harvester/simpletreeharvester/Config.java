package harvester.simpletreeharvester;

public class Config {
    // Limits / tuning
    public int maxLogs = 128;
    public int maxFoliage = 512;
    public boolean ignoreLimits = false;
    public int maxRadius = 8;
    public int maxVertical = 48;
    public boolean clumpDrops = false;
    public boolean placeItemsInInventory = false;

    // Validation
    public boolean requireFoliageAlongTrunk = true;
    public int leafCheckRadius = 3;
    public int minLeaves = 3;

    // Behavior toggles
    public boolean breakOverworldLeaves = false;
    public boolean breakNetherWartAndShroomlight = false;
    public boolean breakMushroomCaps = false;
    public boolean breakChorusFlowers = false;

    // Foliage seeding
    public int foliageSeedNeighborRadius = 1;
}
