package harvester.simpletreeharvester;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.Blocks;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static net.minecraft.server.command.CommandManager.literal;

public class SimpleTreeHarvester implements ModInitializer {
    public static final String MOD_ID = "simple-tree-harvester";

    public static ConfigManager CONFIG_MGR;
    public static Config CFG; // read everywhere

    private static final Set<UUID> FELLING = ConcurrentHashMap.newKeySet();

    private static final int[][] NEIGHBOR_DELTAS_26 = buildNeighborDeltas26();

    private static int[][] buildNeighborDeltas26() {
        List<int[]> deltas = new ArrayList<>(26);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    deltas.add(new int[]{dx, dy, dz});
                }
            }
        }
        return deltas.toArray(new int[0][]);
    }

    @Override
    public void onInitialize() {
        // 1) Load config from config/simple-tree-harvester.json
        Path cfgDir = FabricLoader.getInstance().getConfigDir();
        CONFIG_MGR = new ConfigManager(cfgDir, MOD_ID + ".json");
        CONFIG_MGR.loadOrCreate();
        CFG = CONFIG_MGR.get();

        // 2) Register a simple /sth reload to re-read the file at runtime
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("sth")
                .requires(src -> src.hasPermissionLevel(2))
                .then(literal("reload").executes(ctx -> {
                    CONFIG_MGR.loadOrCreate();
                    CFG = CONFIG_MGR.get();
                    ctx.getSource().sendFeedback(() -> Text.literal("[simpletreeharvester] Config reloaded."), true);
                    return 1;
                }))
        ));

        // 3) Use config values in logic
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient()) return true;
            if (player.isCreative() || player.isSpectator()) return true;
            if (!player.isSneaking()) return true;
            if (FELLING.contains(player.getUuid())) return true;

            ItemStack stack = player.getMainHandStack();
            if (!(stack.getItem() instanceof AxeItem)) return true;
            if (!isTrunk(world.getBlockState(pos))) return true;

            if (CFG.requireFoliageAlongTrunk && !hasFoliageAlongTrunk(world, pos)) return true;

            try {
                FELLING.add(player.getUuid());
                int broken = fellStructure((ServerWorld) world, pos, player, stack);
                if (broken > 0) return false;
            } finally {
                FELLING.remove(player.getUuid());
            }
            return true;
        });
    }

    // ===== Predicates use CFG =====

    private static boolean isTrunk(BlockState s) {
        if (s.isIn(BlockTags.LOGS)) return true;
        return s.isOf(Blocks.CRIMSON_STEM)
                || s.isOf(Blocks.WARPED_STEM)
                || s.isOf(Blocks.MUSHROOM_STEM)
                || s.isOf(Blocks.MANGROVE_ROOTS)
                || s.isOf(Blocks.MUDDY_MANGROVE_ROOTS)
                || s.isOf(Blocks.CHORUS_PLANT);
    }

    private static boolean isFoliage(BlockState s) {
        if (s.isIn(BlockTags.LEAVES) || s.getBlock() instanceof LeavesBlock) return true;
        if (s.isOf(Blocks.NETHER_WART_BLOCK) || s.isOf(Blocks.WARPED_WART_BLOCK)) return true;
        if (s.isOf(Blocks.SHROOMLIGHT)) return true;
        if (s.isOf(Blocks.RED_MUSHROOM_BLOCK) || s.isOf(Blocks.BROWN_MUSHROOM_BLOCK)) return true;
        return s.isOf(Blocks.CHORUS_FLOWER);
    }

    private static boolean isBreakableFoliage(BlockState s) {
        if (CFG.breakOverworldLeaves && (s.isIn(BlockTags.LEAVES) || s.getBlock() instanceof LeavesBlock)) return true;
        if (CFG.breakNetherWartAndShroomlight && (s.isOf(Blocks.NETHER_WART_BLOCK) || s.isOf(Blocks.WARPED_WART_BLOCK) || s.isOf(Blocks.SHROOMLIGHT)))
            return true;
        if (CFG.breakMushroomCaps && (s.isOf(Blocks.RED_MUSHROOM_BLOCK) || s.isOf(Blocks.BROWN_MUSHROOM_BLOCK)))
            return true;
        return (CFG.breakChorusFlowers && s.isOf(Blocks.CHORUS_FLOWER));
    }

    // ===== Use CFG.* instead of constants =====

    private static boolean hasFoliageAlongTrunk(World world, BlockPos start) {
        final int startY = start.getY();
        Deque<BlockPos> stack = new ArrayDeque<>();
        Set<BlockPos> seen = new HashSet<>();
        stack.push(start);
        seen.add(start);

        int logsVisited = 0;
        int foliageSeen = 0;

        while (!stack.isEmpty() && logsVisited < CFG.maxLogs) {
            BlockPos cur = stack.pop();
            if (outOfBounds((ServerWorld) world, start, cur, startY)) continue;

            BlockState s = world.getBlockState(cur);
            if (!isTrunk(s)) continue;

            logsVisited++;

            foliageSeen += countFoliageAround(world, cur, CFG.leafCheckRadius, CFG.minLeaves - foliageSeen);
            if (foliageSeen >= CFG.minLeaves) return true;

            for (int[] d : NEIGHBOR_DELTAS_26) {
                BlockPos pos = cur.add(d[0], d[1], d[2]);
                if (seen.add(pos)) stack.push(pos);
            }
        }
        return false;
    }

    private static int countFoliageAround(World world, BlockPos center, int radius, int need) {
        int found = 0;
        BlockPos.Mutable m = new BlockPos.Mutable();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    m.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    if (isFoliage(world.getBlockState(m))) {
                        found++;
                        if (need > 0 && found >= need) return found;
                    }
                }
            }
        }
        return found;
    }

    private static int fellStructure(ServerWorld world, BlockPos start, net.minecraft.entity.player.PlayerEntity player, ItemStack tool) {
        final int startY = start.getY();

        Queue<BlockPos> q = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        q.add(start);
        visited.add(start);

        List<BlockPos> trunks = new ArrayList<>();

        while (!q.isEmpty() && trunks.size() < CFG.maxLogs) {
            BlockPos cur = q.poll();
            if (outOfBounds(world, start, cur, startY)) continue;

            BlockState state = world.getBlockState(cur);
            if (!isTrunk(state)) continue;

            trunks.add(cur.toImmutable());

            for (int[] d : NEIGHBOR_DELTAS_26) {
                BlockPos n = cur.add(d[0], d[1], d[2]);
                if (!visited.add(n)) continue;
                if (outOfBounds(world, start, n, startY)) continue;
                if (isTrunk(world.getBlockState(n))) q.add(n);
            }
        }

        List<BlockPos> foliage = collectFoliageTouchingTrunk(world, trunks, start, startY);

        foliage.sort(Comparator.comparingInt(BlockPos::getY).reversed());
        trunks.sort(Comparator.comparingInt(BlockPos::getY).reversed());

        int broken = 0;

        for (BlockPos p : foliage) {
            if (!isBreakableFoliage(world.getBlockState(p))) continue;
            world.breakBlock(p, true, player);
            if (damageTool(tool, player)) break; // returns true when BROKEN; stop
            if (++broken >= CFG.maxLogs + CFG.maxFoliage) return broken;
        }

        for (BlockPos p : trunks) {
            if (!isTrunk(world.getBlockState(p))) continue;
            world.breakBlock(p, true, player);
            if (damageTool(tool, player)) break;
            if (++broken >= CFG.maxLogs + CFG.maxFoliage) return broken;
        }

        return broken;
    }

    private static List<BlockPos> collectFoliageTouchingTrunk(World world, List<BlockPos> trunks, BlockPos start, int startY) {
        List<BlockPos> foliage = new ArrayList<>();
        if (trunks.isEmpty() || CFG.maxFoliage <= 0) return foliage;

        Deque<BlockPos> q = new ArrayDeque<>();
        Set<BlockPos> seen = new HashSet<>();
        BlockPos.Mutable m = new BlockPos.Mutable();

        for (BlockPos t : trunks) {
            for (int dx = -CFG.foliageSeedNeighborRadius; dx <= CFG.foliageSeedNeighborRadius; dx++) {
                for (int dy = -CFG.foliageSeedNeighborRadius; dy <= CFG.foliageSeedNeighborRadius; dy++) {
                    for (int dz = -CFG.foliageSeedNeighborRadius; dz <= CFG.foliageSeedNeighborRadius; dz++) {
                        m.set(t.getX() + dx, t.getY() + dy, t.getZ() + dz);
                        if (outOfBounds((ServerWorld) world, start, m, startY)) continue;
                        if (!seen.add(m.toImmutable())) continue;

                        if (isBreakableFoliage(world.getBlockState(m))) {
                            q.add(m.toImmutable());
                        }
                    }
                }
            }
        }

        while (!q.isEmpty() && foliage.size() < CFG.maxFoliage) {
            BlockPos cur = q.poll();
            if (!isBreakableFoliage(world.getBlockState(cur))) continue;
            foliage.add(cur);

            for (int[] d : NEIGHBOR_DELTAS_26) {
                BlockPos n = cur.add(d[0], d[1], d[2]);
                if (!seen.add(n)) continue;
                if (outOfBounds((ServerWorld) world, start, n, startY)) continue;
                if (isBreakableFoliage(world.getBlockState(n))) q.add(n);
            }
        }
        return foliage;
    }

    private static boolean outOfBounds(ServerWorld world, BlockPos start, BlockPos n, int startY) {
        if (n.getY() < world.getBottomY() || n.getY() >= world.getTopY()) return true;
        if (Math.abs(n.getX() - start.getX()) > CFG.maxRadius) return true;
        if (Math.abs(n.getZ() - start.getZ()) > CFG.maxRadius) return true;
        return Math.abs(n.getY() - startY) > CFG.maxVertical;
    }

    // return true when tool BROKE (so caller can stop)
    private static boolean damageTool(ItemStack tool, net.minecraft.entity.player.PlayerEntity player) {
        if (player.isCreative()) return false;
        tool.damage(1, player, net.minecraft.entity.EquipmentSlot.MAINHAND);
        return tool.isEmpty();
    }
}
