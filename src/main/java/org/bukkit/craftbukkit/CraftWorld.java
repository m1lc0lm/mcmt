package org.bukkit.craftbukkit;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.server.AxisAlignedBB;
import net.minecraft.server.BiomeBase;
import net.minecraft.server.BlockChorusFlower;
import net.minecraft.server.BlockDiodeAbstract;
import net.minecraft.server.BlockPosition;
import net.minecraft.server.Blocks;
import net.minecraft.server.ChunkCoordIntPair;
import net.minecraft.server.ChunkStatus;
import net.minecraft.server.EntityAreaEffectCloud;
import net.minecraft.server.EntityArmorStand;
import net.minecraft.server.EntityArrow;
import net.minecraft.server.EntityBoat;
import net.minecraft.server.EntityEgg;
import net.minecraft.server.EntityEnderSignal;
import net.minecraft.server.EntityEvokerFangs;
import net.minecraft.server.EntityExperienceOrb;
import net.minecraft.server.EntityFallingBlock;
import net.minecraft.server.EntityFireball;
import net.minecraft.server.EntityFireworks;
import net.minecraft.server.EntityHanging;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityInsentient;
import net.minecraft.server.EntityItem;
import net.minecraft.server.EntityItemFrame;
import net.minecraft.server.EntityLeash;
import net.minecraft.server.EntityLightning;
import net.minecraft.server.EntityMinecartChest;
import net.minecraft.server.EntityMinecartCommandBlock;
import net.minecraft.server.EntityMinecartFurnace;
import net.minecraft.server.EntityMinecartHopper;
import net.minecraft.server.EntityMinecartMobSpawner;
import net.minecraft.server.EntityMinecartRideable;
import net.minecraft.server.EntityMinecartTNT;
import net.minecraft.server.EntityPainting;
import net.minecraft.server.EntityPotion;
import net.minecraft.server.EntitySnowball;
import net.minecraft.server.EntityTNTPrimed;
import net.minecraft.server.EntityTippedArrow;
import net.minecraft.server.EntityTypes;
import net.minecraft.server.EntityZombie;
import net.minecraft.server.EnumDifficulty;
import net.minecraft.server.EnumDirection;
import net.minecraft.server.EnumMobSpawn;
import net.minecraft.server.ExceptionWorldConflict;
import net.minecraft.server.Explosion;
import net.minecraft.server.GameRules;
import net.minecraft.server.GroupDataEntity;
import net.minecraft.server.HeightMap;
import net.minecraft.server.IBlockData;
import net.minecraft.server.IChunkAccess;
import net.minecraft.server.MinecraftKey;
import net.minecraft.server.MovingObjectPosition;
import net.minecraft.server.PacketPlayOutCustomSoundEffect;
import net.minecraft.server.PacketPlayOutUpdateTime;
import net.minecraft.server.PacketPlayOutWorldEvent;
import net.minecraft.server.PlayerChunk;
import net.minecraft.server.ProtoChunkExtension;
import net.minecraft.server.RayTrace;
import net.minecraft.server.SoundCategory;
import net.minecraft.server.TicketType;
import net.minecraft.server.Unit;
import net.minecraft.server.Vec3D;
import net.minecraft.server.WorldGenFeatureEmptyConfiguration;
import net.minecraft.server.WorldGenerator;
import net.minecraft.server.WorldNBTStorage;
import net.minecraft.server.WorldProviderHell;
import net.minecraft.server.WorldProviderNormal;
import net.minecraft.server.WorldProviderTheEnd;
import net.minecraft.server.WorldServer;
import org.apache.commons.lang.Validate;
import org.bukkit.BlockChangeDelegate;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Difficulty;
import org.bukkit.Effect;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.StructureType;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftItem;
import org.bukkit.craftbukkit.entity.CraftLightningStrike;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.metadata.BlockMetadataStore;
import org.bukkit.craftbukkit.potion.CraftPotionUtil;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.CraftRayTraceResult;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Cat;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cod;
import org.bukkit.entity.ComplexLivingEntity;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Dolphin;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.Egg;
import org.bukkit.entity.ElderGuardian;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.EnderSignal;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Endermite;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Evoker;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Golem;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Husk;
import org.bukkit.entity.Illager;
import org.bukkit.entity.Illusioner;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LingeringPotion;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Mule;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Pillager;
import org.bukkit.entity.Player;
import org.bukkit.entity.PolarBear;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.PufferFish;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Ravager;
import org.bukkit.entity.Salmon;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.entity.Slime;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.Spellcaster;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Stray;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.TippedArrow;
import org.bukkit.entity.TraderLlama;
import org.bukkit.entity.Trident;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.Turtle;
import org.bukkit.entity.Vex;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Vindicator;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.entity.WitherSkull;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieHorse;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.minecart.SpawnerMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.world.SpawnChangeEvent;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Consumer;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class CraftWorld implements World {
    public static final int CUSTOM_DIMENSION_OFFSET = 10;

    private final WorldServer world;
    private WorldBorder worldBorder;
    private Environment environment;
    private final CraftServer server = (CraftServer) Bukkit.getServer();
    private final ChunkGenerator generator;
    private final List<BlockPopulator> populators = new ArrayList<BlockPopulator>();
    private final BlockMetadataStore blockMetadata = new BlockMetadataStore(this);
    private int monsterSpawn = -1;
    private int animalSpawn = -1;
    private int waterAnimalSpawn = -1;
    private int ambientSpawn = -1;

    // Paper start - Provide fast information methods
    // TODO review these changes
    public int getEntityCount() {
        return world.entitiesById.size();
    }
    public int getTileEntityCount() {
        // We don't use the full world tile entity list, so we must iterate chunks
        Long2ObjectLinkedOpenHashMap<PlayerChunk> chunks = world.getChunkProvider().playerChunkMap.visibleChunks;
        int size = 0;
        for (net.minecraft.server.PlayerChunk playerchunk : chunks.values()) {
            net.minecraft.server.Chunk chunk = playerchunk.getChunk();
            if (chunk == null) {
                continue;
            }
            size += chunk.tileEntities.size();
        }
        return size;
    }
    public int getTickableTileEntityCount() {
        return world.tileEntityListTick.size();
    }
    public int getChunkCount() {
        return world.getChunkProvider().playerChunkMap.visibleChunks.size();
    }
    public int getPlayerCount() {
        return world.players.size();
    }
    // Paper end

    private static final Random rand = new Random();

    public CraftWorld(WorldServer world, ChunkGenerator gen, Environment env) {
        this.world = world;
        this.generator = gen;

        environment = env;
    }

    @Override
    public Block getBlockAt(int x, int y, int z) {
        return CraftBlock.at(world, new BlockPosition(x, y, z));
    }

    @Override
    public int getHighestBlockYAt(int x, int z) {
        if (!isChunkLoaded(x >> 4, z >> 4)) {
            getChunkAt(x >> 4, z >> 4); // Transient load for this tick
        }

        return world.getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING, new BlockPosition(x, 0, z)).getY();
    }

    @Override
    public Location getSpawnLocation() {
        BlockPosition spawn = world.getSpawn();
        return new Location(this, spawn.getX(), spawn.getY(), spawn.getZ());
    }

    @Override
    public boolean setSpawnLocation(Location location) {
        Preconditions.checkArgument(location != null, "location");

        return equals(location.getWorld()) ? setSpawnLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ()) : false;
    }

    @Override
    public boolean setSpawnLocation(int x, int y, int z) {
        try {
            Location previousLocation = getSpawnLocation();
            world.worldData.setSpawn(new BlockPosition(x, y, z));

            // Notify anyone who's listening.
            SpawnChangeEvent event = new SpawnChangeEvent(this, previousLocation);
            server.getPluginManager().callEvent(event);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Chunk getChunkAt(int x, int z) {
        return this.world.getChunkProvider().getChunkAt(x, z, true).bukkitChunk;
    }

    @Override
    public Chunk getChunkAt(Block block) {
        return getChunkAt(block.getX() >> 4, block.getZ() >> 4);
    }

    @Override
    public boolean isChunkLoaded(int x, int z) {
        net.minecraft.server.Chunk chunk = world.getChunkProvider().getChunkAt(x, z, false);
        return chunk != null;
    }

    @Override
    public boolean isChunkGenerated(int x, int z) {
        try {
            return isChunkLoaded(x, z) || world.getChunkProvider().playerChunkMap.chunkExists(new ChunkCoordIntPair(x, z));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Chunk[] getLoadedChunks() {
        Long2ObjectLinkedOpenHashMap<PlayerChunk> chunks = world.getChunkProvider().playerChunkMap.visibleChunks;
        return chunks.values().stream().map(PlayerChunk::getFullChunk).filter(Objects::nonNull).map(net.minecraft.server.Chunk::getBukkitChunk).toArray(Chunk[]::new);
    }

    @Override
    public void loadChunk(int x, int z) {
        loadChunk(x, z, true);
    }

    @Override
    public boolean unloadChunk(Chunk chunk) {
        return unloadChunk(chunk.getX(), chunk.getZ());
    }

    @Override
    public boolean unloadChunk(int x, int z) {
        return unloadChunk(x, z, true);
    }

    @Override
    public boolean unloadChunk(int x, int z, boolean save) {
        return unloadChunk0(x, z, save);
    }

    @Override
    public boolean unloadChunkRequest(int x, int z) {
        org.spigotmc.AsyncCatcher.catchOp( "chunk unload"); // Spigot
        net.minecraft.server.IChunkAccess chunk = world.getChunkProvider().getChunkAt(x, z, ChunkStatus.FULL, false);
        if (chunk != null) {
            world.getChunkProvider().removeTicket(TicketType.PLUGIN, chunk.getPos(), 1, Unit.INSTANCE);
        }

        return true;
    }

    private boolean unloadChunk0(int x, int z, boolean save) {
        org.spigotmc.AsyncCatcher.catchOp( "chunk unload" ); // Spigot
        net.minecraft.server.Chunk chunk = (net.minecraft.server.Chunk) world.getChunkProvider().getChunkAt(x, z, ChunkStatus.FULL, false);
        if (chunk == null) {
            return true;
        }

        chunk.mustNotSave = !save;
        unloadChunkRequest(x, z);

        world.getChunkProvider().purgeUnload();
        return !isChunkLoaded(x, z);
    }

    @Override
    public boolean regenerateChunk(int x, int z) {
        org.spigotmc.AsyncCatcher.catchOp( "chunk regenerate" ); // Spigot
        throw new UnsupportedOperationException("Not supported in this Minecraft version! Unless you can fix it, this is not a bug :)");
        /*
        if (!unloadChunk0(x, z, false)) {
            return false;
        }

        final long chunkKey = ChunkCoordIntPair.pair(x, z);
        world.getChunkProvider().unloadQueue.remove(chunkKey);

        net.minecraft.server.Chunk chunk = world.getChunkProvider().generateChunk(x, z);
        PlayerChunk playerChunk = world.getPlayerChunkMap().getChunk(x, z);
        if (playerChunk != null) {
            playerChunk.chunk = chunk;
        }

        if (chunk != null) {
            refreshChunk(x, z);
        }

        return chunk != null;
        */
    }

    @Override
    public boolean refreshChunk(int x, int z) {
        if (!isChunkLoaded(x, z)) {
            return false;
        }

        int px = x << 4;
        int pz = z << 4;

        // If there are more than 64 updates to a chunk at once, it will update all 'touched' sections within the chunk
        // And will include biome data if all sections have been 'touched'
        // This flags 65 blocks distributed across all the sections of the chunk, so that everything is sent, including biomes
        int height = getMaxHeight() / 16;
        for (int idx = 0; idx < 64; idx++) {
            world.notify(new BlockPosition(px + (idx / height), ((idx % height) * 16), pz), Blocks.AIR.getBlockData(), Blocks.STONE.getBlockData(), 3);
        }
        world.notify(new BlockPosition(px + 15, (height * 16) - 1, pz + 15), Blocks.AIR.getBlockData(), Blocks.STONE.getBlockData(), 3);

        return true;
    }

    @Override
    public boolean isChunkInUse(int x, int z) {
        return isChunkLoaded(x, z);
    }

    @Override
    public boolean loadChunk(int x, int z, boolean generate) {
        org.spigotmc.AsyncCatcher.catchOp( "chunk load"); // Spigot
        IChunkAccess chunk = world.getChunkProvider().getChunkAt(x, z, generate || isChunkGenerated(x, z) ? ChunkStatus.FULL : ChunkStatus.EMPTY, true);

        // If generate = false, but the chunk already exists, we will get this back.
        if (chunk instanceof ProtoChunkExtension) {
            // We then cycle through again to get the full chunk immediately, rather than after the ticket addition
            chunk = world.getChunkProvider().getChunkAt(x, z, ChunkStatus.FULL, true);
        }

        if (chunk instanceof net.minecraft.server.Chunk) {
            world.getChunkProvider().addTicket(TicketType.PLUGIN, new ChunkCoordIntPair(x, z), 1, Unit.INSTANCE);
            return true;
        }

        return false;
    }

    @Override
    public boolean isChunkLoaded(Chunk chunk) {
        return isChunkLoaded(chunk.getX(), chunk.getZ());
    }

    @Override
    public void loadChunk(Chunk chunk) {
        loadChunk(chunk.getX(), chunk.getZ());
        ((CraftChunk) getChunkAt(chunk.getX(), chunk.getZ())).getHandle().bukkitChunk = chunk;
    }

    @Override
    public boolean isChunkForceLoaded(int x, int z) {
        return getHandle().getForceLoadedChunks().contains(ChunkCoordIntPair.pair(x, z));
    }

    @Override
    public void setChunkForceLoaded(int x, int z, boolean forced) {
        getHandle().setForceLoaded(x, z, forced);
    }

    @Override
    public Collection<Chunk> getForceLoadedChunks() {
        Set<Chunk> chunks = new HashSet<>();

        for (long coord : getHandle().getForceLoadedChunks()) {
            chunks.add(getChunkAt(ChunkCoordIntPair.getX(coord), ChunkCoordIntPair.getZ(coord)));
        }

        return Collections.unmodifiableCollection(chunks);
    }

    public WorldServer getHandle() {
        return world;
    }

    @Override
    public org.bukkit.entity.Item dropItem(Location loc, ItemStack item) {
        Validate.notNull(item, "Cannot drop a Null item.");
        EntityItem entity = new EntityItem(world, loc.getX(), loc.getY(), loc.getZ(), CraftItemStack.asNMSCopy(item));
        entity.pickupDelay = 10;
        world.addEntity(entity, SpawnReason.CUSTOM);
        // TODO this is inconsistent with how Entity.getBukkitEntity() works.
        // However, this entity is not at the moment backed by a server entity class so it may be left.
        return new CraftItem(world.getServer(), entity);
    }

    @Override
    public org.bukkit.entity.Item dropItemNaturally(Location loc, ItemStack item) {
        double xs = (world.random.nextFloat() * 0.5F) + 0.25D;
        double ys = (world.random.nextFloat() * 0.5F) + 0.25D;
        double zs = (world.random.nextFloat() * 0.5F) + 0.25D;
        loc = loc.clone();
        loc.setX(loc.getX() + xs);
        loc.setY(loc.getY() + ys);
        loc.setZ(loc.getZ() + zs);
        return dropItem(loc, item);
    }

    @Override
    public Arrow spawnArrow(Location loc, Vector velocity, float speed, float spread) {
        return spawnArrow(loc, velocity, speed, spread, Arrow.class);
    }

    @Override
    public <T extends AbstractArrow> T spawnArrow(Location loc, Vector velocity, float speed, float spread, Class<T> clazz) {
        Validate.notNull(loc, "Can not spawn arrow with a null location");
        Validate.notNull(velocity, "Can not spawn arrow with a null velocity");
        Validate.notNull(clazz, "Can not spawn an arrow with no class");

        EntityArrow arrow;
        if (TippedArrow.class.isAssignableFrom(clazz)) {
            arrow = EntityTypes.ARROW.a(world);
            ((EntityTippedArrow) arrow).setType(CraftPotionUtil.fromBukkit(new PotionData(PotionType.WATER, false, false)));
        } else if (SpectralArrow.class.isAssignableFrom(clazz)) {
            arrow = EntityTypes.SPECTRAL_ARROW.a(world);
        } else if (Trident.class.isAssignableFrom(clazz)){
            arrow = EntityTypes.TRIDENT.a(world);
        } else {
            arrow = EntityTypes.ARROW.a(world);
        }

        arrow.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        arrow.shoot(velocity.getX(), velocity.getY(), velocity.getZ(), speed, spread);
        world.addEntity(arrow);
        return (T) arrow.getBukkitEntity();
    }

    @Override
    public Entity spawnEntity(Location loc, EntityType entityType) {
        return spawn(loc, entityType.getEntityClass());
    }

    @Override
    public LightningStrike strikeLightning(Location loc) {
        EntityLightning lightning = new EntityLightning(world, loc.getX(), loc.getY(), loc.getZ(), false);
        world.strikeLightning(lightning);
        return new CraftLightningStrike(server, lightning);
    }

    @Override
    public LightningStrike strikeLightningEffect(Location loc) {
        EntityLightning lightning = new EntityLightning(world, loc.getX(), loc.getY(), loc.getZ(), true);
        world.strikeLightning(lightning);
        return new CraftLightningStrike(server, lightning);
    }

    @Override
    public boolean generateTree(Location loc, TreeType type) {
        BlockPosition pos = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

        net.minecraft.server.WorldGenerator gen;
        switch (type) {
        case BIG_TREE:
            gen = WorldGenerator.FANCY_TREE;
            break;
        case BIRCH:
            gen = WorldGenerator.BIRCH_TREE;
            break;
        case REDWOOD:
            gen = WorldGenerator.SPRUCE_TREE;
            break;
        case TALL_REDWOOD:
            gen = WorldGenerator.PINE_TREE;
            break;
        case JUNGLE:
            gen = WorldGenerator.MEGA_JUNGLE_TREE;
            break;
        case SMALL_JUNGLE:
            gen = WorldGenerator.JUNGLE_TREE;
            break;
        case COCOA_TREE:
            gen = WorldGenerator.MEGA_JUNGLE_TREE;
            break;
        case JUNGLE_BUSH:
            gen = WorldGenerator.JUNGLE_GROUND_BUSH;
            break;
        case RED_MUSHROOM:
            gen = WorldGenerator.HUGE_RED_MUSHROOM;
            break;
        case BROWN_MUSHROOM:
            gen = WorldGenerator.HUGE_BROWN_MUSHROOM;
            break;
        case SWAMP:
            gen = WorldGenerator.SWAMP_TREE;
            break;
        case ACACIA:
            gen = WorldGenerator.SAVANNA_TREE;
            break;
        case DARK_OAK:
            gen = WorldGenerator.DARK_OAK_TREE;
            break;
        case MEGA_REDWOOD:
            gen = WorldGenerator.MEGA_PINE_TREE;
            break;
        case TALL_BIRCH:
            gen = WorldGenerator.SUPER_BIRCH_TREE;
            break;
        case CHORUS_PLANT:
            ((BlockChorusFlower) Blocks.CHORUS_FLOWER).a(world, pos, rand, 8);
            return true;
        case TREE:
        default:
            gen = WorldGenerator.NORMAL_TREE;
            break;
        }

        return gen.generate(world, world.worldProvider.getChunkGenerator(), rand, pos, new WorldGenFeatureEmptyConfiguration());
    }

    @Override
    public boolean generateTree(Location loc, TreeType type, BlockChangeDelegate delegate) {
        world.captureTreeGeneration = true;
        world.captureBlockStates = true;
        boolean grownTree = generateTree(loc, type);
        world.captureBlockStates = false;
        world.captureTreeGeneration = false;
        if (grownTree) { // Copy block data to delegate
            for (BlockState blockstate : world.capturedBlockStates) {
                BlockPosition position = ((CraftBlockState) blockstate).getPosition();
                net.minecraft.server.IBlockData oldBlock = world.getType(position);
                int flag = ((CraftBlockState) blockstate).getFlag();
                delegate.setBlockData(blockstate.getX(), blockstate.getY(), blockstate.getZ(), blockstate.getBlockData());
                net.minecraft.server.IBlockData newBlock = world.getType(position);
                world.notifyAndUpdatePhysics(position, null, oldBlock, newBlock, newBlock, flag);
            }
            world.capturedBlockStates.clear();
            return true;
        } else {
            world.capturedBlockStates.clear();
            return false;
        }
    }

    @Override
    public String getName() {
        return world.worldData.getName();
    }

    @Deprecated
    public long getId() {
        return world.worldData.getSeed();
    }

    @Override
    public UUID getUID() {
        return world.getDataManager().getUUID();
    }

    @Override
    public String toString() {
        return "CraftWorld{name=" + getName() + '}';
    }

    @Override
    public long getTime() {
        long time = getFullTime() % 24000;
        if (time < 0) time += 24000;
        return time;
    }

    @Override
    public void setTime(long time) {
        long margin = (time - getFullTime()) % 24000;
        if (margin < 0) margin += 24000;
        setFullTime(getFullTime() + margin);
    }

    @Override
    public long getFullTime() {
        return world.getDayTime();
    }

    @Override
    public void setFullTime(long time) {
        world.setDayTime(time);

        // Forces the client to update to the new time immediately
        for (Player p : getPlayers()) {
            CraftPlayer cp = (CraftPlayer) p;
            if (cp.getHandle().playerConnection == null) continue;

            cp.getHandle().playerConnection.sendPacket(new PacketPlayOutUpdateTime(cp.getHandle().world.getTime(), cp.getHandle().getPlayerTime(), cp.getHandle().world.getGameRules().getBoolean("doDaylightCycle")));
        }
    }

    @Override
    public boolean createExplosion(double x, double y, double z, float power) {
        return createExplosion(x, y, z, power, false, true);
    }

    @Override
    public boolean createExplosion(double x, double y, double z, float power, boolean setFire) {
        return createExplosion(x, y, z, power, setFire, true);
    }

    @Override
    public boolean createExplosion(double x, double y, double z, float power, boolean setFire, boolean breakBlocks) {
        return !world.createExplosion(null, x, y, z, power, setFire, breakBlocks ? Explosion.Effect.BREAK : Explosion.Effect.NONE).wasCanceled;
    }
    // Paper start
    public boolean createExplosion(Entity source, Location loc, float power, boolean setFire, boolean breakBlocks) {
        return !world.createExplosion(source != null ? ((org.bukkit.craftbukkit.entity.CraftEntity) source).getHandle() : null, loc.getX(), loc.getY(), loc.getZ(), power, setFire, breakBlocks ? Explosion.Effect.BREAK : Explosion.Effect.NONE).wasCanceled;
    }
    // Paper end

    @Override
    public boolean createExplosion(Location loc, float power) {
        return createExplosion(loc, power, false);
    }

    @Override
    public boolean createExplosion(Location loc, float power, boolean setFire) {
        return createExplosion(loc.getX(), loc.getY(), loc.getZ(), power, setFire);
    }

    @Override
    public Environment getEnvironment() {
        return environment;
    }

    @Override
    public Block getBlockAt(Location location) {
        return getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Override
    public int getHighestBlockYAt(Location location) {
        return getHighestBlockYAt(location.getBlockX(), location.getBlockZ());
    }

    @Override
    public Chunk getChunkAt(Location location) {
        return getChunkAt(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    @Override
    public ChunkGenerator getGenerator() {
        return generator;
    }

    @Override
    public List<BlockPopulator> getPopulators() {
        return populators;
    }

    @Override
    public Block getHighestBlockAt(int x, int z) {
        return getBlockAt(x, getHighestBlockYAt(x, z), z);
    }

    @Override
    public Block getHighestBlockAt(Location location) {
        return getHighestBlockAt(location.getBlockX(), location.getBlockZ());
    }

    @Override
    public Biome getBiome(int x, int z) {
        return CraftBlock.biomeBaseToBiome(this.world.getBiome(new BlockPosition(x, 0, z)));
    }

    @Override
    public void setBiome(int x, int z, Biome bio) {
        BiomeBase bb = CraftBlock.biomeToBiomeBase(bio);
        if (this.world.isLoaded(new BlockPosition(x, 0, z))) {
            net.minecraft.server.Chunk chunk = this.world.getChunkAtWorldCoords(new BlockPosition(x, 0, z));

            if (chunk != null) {
                BiomeBase[] biomevals = chunk.getBiomeIndex();
                biomevals[((z & 0xF) << 4) | (x & 0xF)] = bb;

                chunk.markDirty(); // SPIGOT-2890
            }
        }
    }

    @Override
    public double getTemperature(int x, int z) {
        return this.world.getBiome(new BlockPosition(x, 0, z)).getTemperature();
    }

    @Override
    public double getHumidity(int x, int z) {
        return this.world.getBiome(new BlockPosition(x, 0, z)).getHumidity();
    }

    @Override
    public List<Entity> getEntities() {
        List<Entity> list = new ArrayList<Entity>();

        for (Object o : world.entitiesById.values()) {
            if (o instanceof net.minecraft.server.Entity) {
                net.minecraft.server.Entity mcEnt = (net.minecraft.server.Entity) o;
                if (mcEnt.shouldBeRemoved) continue; // Paper
                Entity bukkitEntity = mcEnt.getBukkitEntity();

                // Assuming that bukkitEntity isn't null
                if (bukkitEntity != null && bukkitEntity.isValid()) {
                    list.add(bukkitEntity);
                }
            }
        }

        return list;
    }

    @Override
    public List<LivingEntity> getLivingEntities() {
        List<LivingEntity> list = new ArrayList<LivingEntity>();

        for (Object o : world.entitiesById.values()) {
            if (o instanceof net.minecraft.server.Entity) {
                net.minecraft.server.Entity mcEnt = (net.minecraft.server.Entity) o;
                if (mcEnt.shouldBeRemoved) continue; // Paper
                Entity bukkitEntity = mcEnt.getBukkitEntity();

                // Assuming that bukkitEntity isn't null
                if (bukkitEntity != null && bukkitEntity instanceof LivingEntity && bukkitEntity.isValid()) {
                    list.add((LivingEntity) bukkitEntity);
                }
            }
        }

        return list;
    }

    @Override
    @SuppressWarnings("unchecked")
    @Deprecated
    public <T extends Entity> Collection<T> getEntitiesByClass(Class<T>... classes) {
        return (Collection<T>)getEntitiesByClasses(classes);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Entity> Collection<T> getEntitiesByClass(Class<T> clazz) {
        Collection<T> list = new ArrayList<T>();

        for (Object entity: world.entitiesById.values()) {
            if (entity instanceof net.minecraft.server.Entity) {
                if (((net.minecraft.server.Entity) entity).shouldBeRemoved) continue; // Paper
                Entity bukkitEntity = ((net.minecraft.server.Entity) entity).getBukkitEntity();

                if (bukkitEntity == null) {
                    continue;
                }

                Class<?> bukkitClass = bukkitEntity.getClass();

                if (clazz.isAssignableFrom(bukkitClass)) {
                    list.add((T) bukkitEntity);
                }
            }
        }

        return list;
    }

    @Override
    public Collection<Entity> getEntitiesByClasses(Class<?>... classes) {
        Collection<Entity> list = new ArrayList<Entity>();

        for (Object entity: world.entitiesById.values()) {
            if (entity instanceof net.minecraft.server.Entity) {
                if (((net.minecraft.server.Entity) entity).shouldBeRemoved) continue; // Paper
                Entity bukkitEntity = ((net.minecraft.server.Entity) entity).getBukkitEntity();

                if (bukkitEntity == null) {
                    continue;
                }

                Class<?> bukkitClass = bukkitEntity.getClass();

                for (Class<?> clazz : classes) {
                    if (clazz.isAssignableFrom(bukkitClass)) {
                        list.add(bukkitEntity);
                        break;
                    }
                }
            }
        }

        return list;
    }

    @Override
    public Collection<Entity> getNearbyEntities(Location location, double x, double y, double z) {
        return this.getNearbyEntities(location, x, y, z, null);
    }

    @Override
    public Collection<Entity> getNearbyEntities(Location location, double x, double y, double z, Predicate<Entity> filter) {
        Validate.notNull(location, "Location is null!");
        Validate.isTrue(this.equals(location.getWorld()), "Location is from different world!");

        BoundingBox aabb = BoundingBox.of(location, x, y, z);
        return this.getNearbyEntities(aabb, filter);
    }

    @Override
    public Collection<Entity> getNearbyEntities(BoundingBox boundingBox) {
        return this.getNearbyEntities(boundingBox, null);
    }

    @Override
    public Collection<Entity> getNearbyEntities(BoundingBox boundingBox, Predicate<Entity> filter) {
        Validate.notNull(boundingBox, "Bounding box is null!");

        AxisAlignedBB bb = new AxisAlignedBB(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(), boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
        List<net.minecraft.server.Entity> entityList = getHandle().getEntities((net.minecraft.server.Entity) null, bb, null);
        List<Entity> bukkitEntityList = new ArrayList<org.bukkit.entity.Entity>(entityList.size());

        for (net.minecraft.server.Entity entity : entityList) {
            Entity bukkitEntity = entity.getBukkitEntity();
            if (filter == null || filter.test(bukkitEntity)) {
                bukkitEntityList.add(bukkitEntity);
            }
        }

        return bukkitEntityList;
    }

    @Override
    public RayTraceResult rayTraceEntities(Location start, Vector direction, double maxDistance) {
        return this.rayTraceEntities(start, direction, maxDistance, null);
    }

    @Override
    public RayTraceResult rayTraceEntities(Location start, Vector direction, double maxDistance, double raySize) {
        return this.rayTraceEntities(start, direction, maxDistance, raySize, null);
    }

    @Override
    public RayTraceResult rayTraceEntities(Location start, Vector direction, double maxDistance, Predicate<Entity> filter) {
        return this.rayTraceEntities(start, direction, maxDistance, 0.0D, filter);
    }

    @Override
    public RayTraceResult rayTraceEntities(Location start, Vector direction, double maxDistance, double raySize, Predicate<Entity> filter) {
        Validate.notNull(start, "Start location is null!");
        Validate.isTrue(this.equals(start.getWorld()), "Start location is from different world!");
        start.checkFinite();

        Validate.notNull(direction, "Direction is null!");
        direction.checkFinite();

        Validate.isTrue(direction.lengthSquared() > 0, "Direction's magnitude is 0!");

        if (maxDistance < 0.0D) {
            return null;
        }

        Vector startPos = start.toVector();
        Vector dir = direction.clone().normalize().multiply(maxDistance);
        BoundingBox aabb = BoundingBox.of(startPos, startPos).expandDirectional(dir).expand(raySize);
        Collection<Entity> entities = this.getNearbyEntities(aabb, filter);

        Entity nearestHitEntity = null;
        RayTraceResult nearestHitResult = null;
        double nearestDistanceSq = Double.MAX_VALUE;

        for (Entity entity : entities) {
            BoundingBox boundingBox = entity.getBoundingBox().expand(raySize);
            RayTraceResult hitResult = boundingBox.rayTrace(startPos, direction, maxDistance);

            if (hitResult != null) {
                double distanceSq = startPos.distanceSquared(hitResult.getHitPosition());

                if (distanceSq < nearestDistanceSq) {
                    nearestHitEntity = entity;
                    nearestHitResult = hitResult;
                    nearestDistanceSq = distanceSq;
                }
            }
        }

        return (nearestHitEntity == null) ? null : new RayTraceResult(nearestHitResult.getHitPosition(), nearestHitEntity, nearestHitResult.getHitBlockFace());
    }

    @Override
    public RayTraceResult rayTraceBlocks(Location start, Vector direction, double maxDistance) {
        return this.rayTraceBlocks(start, direction, maxDistance, FluidCollisionMode.NEVER, false);
    }

    @Override
    public RayTraceResult rayTraceBlocks(Location start, Vector direction, double maxDistance, FluidCollisionMode fluidCollisionMode) {
        return this.rayTraceBlocks(start, direction, maxDistance, fluidCollisionMode, false);
    }

    @Override
    public RayTraceResult rayTraceBlocks(Location start, Vector direction, double maxDistance, FluidCollisionMode fluidCollisionMode, boolean ignorePassableBlocks) {
        Validate.notNull(start, "Start location is null!");
        Validate.isTrue(this.equals(start.getWorld()), "Start location is from different world!");
        start.checkFinite();

        Validate.notNull(direction, "Direction is null!");
        direction.checkFinite();

        Validate.isTrue(direction.lengthSquared() > 0, "Direction's magnitude is 0!");
        Validate.notNull(fluidCollisionMode, "Fluid collision mode is null!");

        if (maxDistance < 0.0D) {
            return null;
        }

        Vector dir = direction.clone().normalize().multiply(maxDistance);
        Vec3D startPos = new Vec3D(start.getX(), start.getY(), start.getZ());
        Vec3D endPos = new Vec3D(start.getX() + dir.getX(), start.getY() + dir.getY(), start.getZ() + dir.getZ());
        MovingObjectPosition nmsHitResult = this.getHandle().rayTrace(new RayTrace(startPos, endPos, ignorePassableBlocks ? RayTrace.BlockCollisionOption.COLLIDER : RayTrace.BlockCollisionOption.OUTLINE, CraftFluidCollisionMode.toNMS(fluidCollisionMode), null));

        return CraftRayTraceResult.fromNMS(this, nmsHitResult);
    }

    @Override
    public RayTraceResult rayTrace(Location start, Vector direction, double maxDistance, FluidCollisionMode fluidCollisionMode, boolean ignorePassableBlocks, double raySize, Predicate<Entity> filter) {
        RayTraceResult blockHit = this.rayTraceBlocks(start, direction, maxDistance, fluidCollisionMode, ignorePassableBlocks);
        Vector startVec = null;
        double blockHitDistance = maxDistance;

        // limiting the entity search range if we found a block hit:
        if (blockHit != null) {
            startVec = start.toVector();
            blockHitDistance = startVec.distance(blockHit.getHitPosition());
        }

        RayTraceResult entityHit = this.rayTraceEntities(start, direction, blockHitDistance, raySize, filter);
        if (blockHit == null) {
            return entityHit;
        }

        if (entityHit == null) {
            return blockHit;
        }

        // Cannot be null as blockHit == null returns above
        double entityHitDistanceSquared = startVec.distanceSquared(entityHit.getHitPosition());
        if (entityHitDistanceSquared < (blockHitDistance * blockHitDistance)) {
            return entityHit;
        }

        return blockHit;
    }

    @Override
    public List<Player> getPlayers() {
        List<Player> list = new ArrayList<Player>(world.getPlayers().size());

        for (EntityHuman human : world.getPlayers()) {
            HumanEntity bukkitEntity = human.getBukkitEntity();

            if ((bukkitEntity != null) && (bukkitEntity instanceof Player)) {
                list.add((Player) bukkitEntity);
            }
        }

        return list;
    }

    // Paper start - getEntity by UUID API
    @Override
    public Entity getEntity(UUID uuid) {
        Validate.notNull(uuid, "UUID cannot be null");
        net.minecraft.server.Entity entity = world.getEntity(uuid);
        return entity == null ? null : entity.getBukkitEntity();
    }
    // Paper end

    @Override
    public void save() {
        this.server.checkSaveState();
        try {
            boolean oldSave = world.savingDisabled;

            world.savingDisabled = false;
            world.save(null, false, false);

            world.savingDisabled = oldSave;
        } catch (ExceptionWorldConflict ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean isAutoSave() {
        return !world.savingDisabled;
    }

    @Override
    public void setAutoSave(boolean value) {
        world.savingDisabled = !value;
    }

    @Override
    public void setDifficulty(Difficulty difficulty) {
        this.getHandle().worldData.setDifficulty(EnumDifficulty.getById(difficulty.getValue()));
    }

    @Override
    public Difficulty getDifficulty() {
        return Difficulty.getByValue(this.getHandle().getDifficulty().ordinal());
    }

    public BlockMetadataStore getBlockMetadata() {
        return blockMetadata;
    }

    @Override
    public boolean hasStorm() {
        return world.worldData.hasStorm();
    }

    @Override
    public void setStorm(boolean hasStorm) {
        world.worldData.setStorm(hasStorm);
        setWeatherDuration(0); // Reset weather duration (legacy behaviour)
    }

    @Override
    public int getWeatherDuration() {
        return world.worldData.getWeatherDuration();
    }

    @Override
    public void setWeatherDuration(int duration) {
        world.worldData.setWeatherDuration(duration);
    }

    @Override
    public boolean isThundering() {
        return world.worldData.isThundering();
    }

    @Override
    public void setThundering(boolean thundering) {
        world.worldData.setThundering(thundering);
        setThunderDuration(0); // Reset weather duration (legacy behaviour)
    }

    @Override
    public int getThunderDuration() {
        return world.worldData.getThunderDuration();
    }

    @Override
    public void setThunderDuration(int duration) {
        world.worldData.setThunderDuration(duration);
    }

    @Override
    public long getSeed() {
        return world.worldData.getSeed();
    }

    @Override
    public boolean getPVP() {
        return world.pvpMode;
    }

    @Override
    public void setPVP(boolean pvp) {
        world.pvpMode = pvp;
    }

    public void playEffect(Player player, Effect effect, int data) {
        playEffect(player.getLocation(), effect, data, 0);
    }

    @Override
    public void playEffect(Location location, Effect effect, int data) {
        playEffect(location, effect, data, 64);
    }

    @Override
    public <T> void playEffect(Location loc, Effect effect, T data) {
        playEffect(loc, effect, data, 64);
    }

    @Override
    public <T> void playEffect(Location loc, Effect effect, T data, int radius) {
        if (data != null) {
            Validate.isTrue(effect.getData() != null && effect.getData().isAssignableFrom(data.getClass()), "Wrong kind of data for this effect!");
        } else {
            Validate.isTrue(effect.getData() == null, "Wrong kind of data for this effect!");
        }

        int datavalue = data == null ? 0 : CraftEffect.getDataValue(effect, data);
        playEffect(loc, effect, datavalue, radius);
    }

    @Override
    public void playEffect(Location location, Effect effect, int data, int radius) {
        Validate.notNull(location, "Location cannot be null");
        Validate.notNull(effect, "Effect cannot be null");
        Validate.notNull(location.getWorld(), "World cannot be null");
        int packetData = effect.getId();
        PacketPlayOutWorldEvent packet = new PacketPlayOutWorldEvent(packetData, new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()), data, false);
        int distance;
        radius *= radius;

        for (Player player : getPlayers()) {
            if (((CraftPlayer) player).getHandle().playerConnection == null) continue;
            if (!location.getWorld().equals(player.getWorld())) continue;

            distance = (int) player.getLocation().distanceSquared(location);
            if (distance <= radius) {
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
            }
        }
    }

    @Override
    public <T extends Entity> T spawn(Location location, Class<T> clazz) throws IllegalArgumentException {
        return spawn(location, clazz, null, SpawnReason.CUSTOM);
    }

    @Override
    public <T extends Entity> T spawn(Location location, Class<T> clazz, Consumer<T> function) throws IllegalArgumentException {
        return spawn(location, clazz, function, SpawnReason.CUSTOM);
    }

    @Override
    public FallingBlock spawnFallingBlock(Location location, MaterialData data) throws IllegalArgumentException {
        Validate.notNull(data, "MaterialData cannot be null");
        return spawnFallingBlock(location, data.getItemType(), data.getData());
    }

    @Override
    public FallingBlock spawnFallingBlock(Location location, org.bukkit.Material material, byte data) throws IllegalArgumentException {
        Validate.notNull(location, "Location cannot be null");
        Validate.notNull(material, "Material cannot be null");
        Validate.isTrue(material.isBlock(), "Material must be a block");

        EntityFallingBlock entity = new EntityFallingBlock(world, location.getX(), location.getY(), location.getZ(), CraftMagicNumbers.getBlock(material).getBlockData());
        entity.ticksLived = 1;

        world.addEntity(entity, SpawnReason.CUSTOM);
        return (FallingBlock) entity.getBukkitEntity();
    }

    @Override
    public FallingBlock spawnFallingBlock(Location location, BlockData data) throws IllegalArgumentException {
        Validate.notNull(location, "Location cannot be null");
        Validate.notNull(data, "Material cannot be null");

        EntityFallingBlock entity = new EntityFallingBlock(world, location.getX(), location.getY(), location.getZ(), ((CraftBlockData) data).getState());
        entity.ticksLived = 1;

        world.addEntity(entity, SpawnReason.CUSTOM);
        return (FallingBlock) entity.getBukkitEntity();
    }

    @SuppressWarnings("unchecked")
    public net.minecraft.server.Entity createEntity(Location location, Class<? extends Entity> clazz) throws IllegalArgumentException {
        if (location == null || clazz == null) {
            throw new IllegalArgumentException("Location or entity class cannot be null");
        }

        net.minecraft.server.Entity entity = null;

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        float pitch = location.getPitch();
        float yaw = location.getYaw();

        // order is important for some of these
        if (Boat.class.isAssignableFrom(clazz)) {
            entity = new EntityBoat(world, x, y, z);
            entity.setPositionRotation(x, y, z, yaw, pitch);
            // Paper start
        } else if (org.bukkit.entity.Item.class.isAssignableFrom(clazz)) {
            entity = new EntityItem(world, x, y, z, new net.minecraft.server.ItemStack(net.minecraft.server.Item.getItemOf(net.minecraft.server.Blocks.DIRT)));
            // Paper end
        } else if (FallingBlock.class.isAssignableFrom(clazz)) {
            entity = new EntityFallingBlock(world, x, y, z, world.getType(new BlockPosition(x, y, z)));
        } else if (Projectile.class.isAssignableFrom(clazz)) {
            if (Snowball.class.isAssignableFrom(clazz)) {
                entity = new EntitySnowball(world, x, y, z);
            } else if (Egg.class.isAssignableFrom(clazz)) {
                entity = new EntityEgg(world, x, y, z);
            } else if (AbstractArrow.class.isAssignableFrom(clazz)) {
                if (TippedArrow.class.isAssignableFrom(clazz)) {
                    entity = EntityTypes.ARROW.a(world);
                    ((EntityTippedArrow) entity).setType(CraftPotionUtil.fromBukkit(new PotionData(PotionType.WATER, false, false)));
                } else if (SpectralArrow.class.isAssignableFrom(clazz)) {
                    entity = EntityTypes.SPECTRAL_ARROW.a(world);
                } else if (Trident.class.isAssignableFrom(clazz)) {
                    entity = EntityTypes.TRIDENT.a(world);
                } else {
                    entity = EntityTypes.ARROW.a(world);
                }
                entity.setPositionRotation(x, y, z, 0, 0);
            } else if (ThrownExpBottle.class.isAssignableFrom(clazz)) {
                entity = EntityTypes.EXPERIENCE_BOTTLE.a(world);
                entity.setPositionRotation(x, y, z, 0, 0);
            } else if (EnderPearl.class.isAssignableFrom(clazz)) {
                entity = EntityTypes.ENDER_PEARL.a(world);
                entity.setPositionRotation(x, y, z, 0, 0);
            } else if (ThrownPotion.class.isAssignableFrom(clazz)) {
                if (LingeringPotion.class.isAssignableFrom(clazz)) {
                    entity = new EntityPotion(world, x, y, z);
                    ((EntityPotion) entity).setItem(CraftItemStack.asNMSCopy(new ItemStack(org.bukkit.Material.LINGERING_POTION, 1)));
                } else {
                    entity = new EntityPotion(world, x, y, z);
                    ((EntityPotion) entity).setItem(CraftItemStack.asNMSCopy(new ItemStack(org.bukkit.Material.SPLASH_POTION, 1)));
                }
            } else if (Fireball.class.isAssignableFrom(clazz)) {
                if (SmallFireball.class.isAssignableFrom(clazz)) {
                    entity = EntityTypes.SMALL_FIREBALL.a(world);
                } else if (WitherSkull.class.isAssignableFrom(clazz)) {
                    entity = EntityTypes.WITHER_SKULL.a(world);
                } else if (DragonFireball.class.isAssignableFrom(clazz)) {
                    entity = EntityTypes.DRAGON_FIREBALL.a(world);
                } else {
                    entity = EntityTypes.FIREBALL.a(world);
                }
                entity.setPositionRotation(x, y, z, yaw, pitch);
                Vector direction = location.getDirection().multiply(10);
                ((EntityFireball) entity).setDirection(direction.getX(), direction.getY(), direction.getZ());
            } else if (ShulkerBullet.class.isAssignableFrom(clazz)) {
                entity = EntityTypes.SHULKER_BULLET.a(world);
                entity.setPositionRotation(x, y, z, yaw, pitch);
            } else if (LlamaSpit.class.isAssignableFrom(clazz)) {
                entity = EntityTypes.LLAMA_SPIT.a(world);
                entity.setPositionRotation(x, y, z, yaw, pitch);
            }
        } else if (Minecart.class.isAssignableFrom(clazz)) {
            if (PoweredMinecart.class.isAssignableFrom(clazz)) {
                entity = new EntityMinecartFurnace(world, x, y, z);
            } else if (StorageMinecart.class.isAssignableFrom(clazz)) {
                entity = new EntityMinecartChest(world, x, y, z);
            } else if (ExplosiveMinecart.class.isAssignableFrom(clazz)) {
                entity = new EntityMinecartTNT(world, x, y, z);
            } else if (HopperMinecart.class.isAssignableFrom(clazz)) {
                entity = new EntityMinecartHopper(world, x, y, z);
            } else if (SpawnerMinecart.class.isAssignableFrom(clazz)) {
                entity = new EntityMinecartMobSpawner(world, x, y, z);
            } else if (CommandMinecart.class.isAssignableFrom(clazz)) {
                entity = new EntityMinecartCommandBlock(world, x, y, z);
            } else { // Default to rideable minecart for pre-rideable compatibility
                entity = new EntityMinecartRideable(world, x, y, z);
            }
        } else if (EnderSignal.class.isAssignableFrom(clazz)) {
            entity = new EntityEnderSignal(world, x, y, z);
        } else if (EnderCrystal.class.isAssignableFrom(clazz)) {
            entity = EntityTypes.END_CRYSTAL.a(world);
            entity.setPositionRotation(x, y, z, 0, 0);
        } else if (LivingEntity.class.isAssignableFrom(clazz)) {
            if (Chicken.class.isAssignableFrom(clazz)) {
                entity = EntityTypes.CHICKEN.a(world);
            } else if (Cow.class.isAssignableFrom(clazz)) {
                if (MushroomCow.class.isAssignableFrom(clazz)) {
                    entity = EntityTypes.MOOSHROOM.a(world);
                } else {
                    entity = EntityTypes.COW.a(world);
                }
            } else if (Golem.class.isAssignableFrom(clazz)) {
                if (Snowman.class.isAssignableFrom(clazz)) {
                    entity = EntityTypes.SNOW_GOLEM.a(world);
                } else if (IronGolem.class.isAssignableFrom(clazz)) {
                    entity = EntityTypes.IRON_GOLEM.a(world);
                } else if (Shulker.class.isAssignableFrom(clazz)) {
                    entity = EntityTypes.SHULKER.a(world);
                }
            } else if (Creeper.class.isAssignableFrom(clazz)) {
                entity = EntityTypes.CREEPER.a(world);
            } else if (Ghast.class.isAssignableFrom(clazz)) {
                entity = EntityTypes.GHAST.a(world);
            } else if (Pig.class.isAssignableFrom(clazz)) {
                entity = EntityTypes.PIG.a(world);
            } else if (Player.class.isAssignableFrom(clazz)) {
                // need a net server handler for this one
            } else if (Sheep.class.isAssignableFrom(clazz)) {
                entity = EntityTypes.SHEEP.a(world);
            } else if (AbstractHorse.class.isAssignableFrom(clazz)) {
                if (ChestedHorse.class.isAssignableFrom(clazz)) {
                    if (Donkey.class.isAssignableFrom(clazz)) {
                        entity = EntityTypes.DONKEY.a(world);
                    } else if (Mule.class.isAssignableFrom(clazz)) {
                        entity = EntityTypes.MULE.a(world);
                    } else if (Llama.class.isAssignableFrom(clazz)) {
                        if (TraderLlama.class.isAssignableFrom(clazz)) {
                            entity = EntityTypes.TRADER_LLAMA.a(world);
                        } else {
                            entity = EntityTypes.LLAMA.a(world);
                        }
                    }
                } else if (SkeletonHorse.class.isAssignableFrom(clazz)) {
                    entity = EntityTypes.SKELETON_HORSE.a(world);
                } else if (ZombieHorse.class.isAssignableFrom(clazz)) {
                    entity = EntityTypes.ZOMBIE_HORSE.a(world);
                } else {
                    entity = EntityTypes.HORSE.a(world);
                }
            } else if (Skeleton.class.isAssignableFrom(clazz)) {
                if (Stray.class.isAssignableFrom(clazz)){
                    entity = EntityTypes.STRAY.a(world);
                } else if (WitherSkeleton.class.isAssignableFrom(clazz)) {
                    entity = EntityTypes.WITHER_SKELETON.a(world);
                } else {
                    entity = EntityTypes.SKELETON.a(world);
                }
            } else if (Slime.class.isAssignableFrom(clazz)) {
                if (MagmaCube.class.isAssignableFrom(clazz)) {
                    entity = EntityTypes.MAGMA_CUBE.a(world);
                } else {
                    entity = EntityTypes.SLIME.a(world);
                }
            } else if (Spider.class.isAssignableFrom(clazz)) {
                if (CaveSpider.class.isAssignableFrom(clazz)) {
                    entity = EntityTypes.CAVE_SPIDER.a(world);
                } else {
                    entity = EntityTypes.SPIDER.a(world);
                }
            } else if (Squid.class.isAssignableFrom(clazz)) {
                entity = EntityTypes.SQUID.a(world);
            } else if (Tameable.class.isAssignableFrom(clazz)) {
                if (Wolf.class.isAssignableFrom(clazz)) {
                    entity = EntityTypes.WOLF.a(world);
                } else if (Parrot.class.isAssignableFrom(clazz)) {
                    entity = EntityTypes.PARROT.a(world);
                } else if (Cat.class.isAssignableFrom(clazz)) {
                    entity = EntityTypes.CAT.a(world);
                }
            } else if (PigZombie.class.isAssignableFrom(clazz)) {
                entity = EntityTypes.ZOMBIE_PIGMAN.a(world);
            } else if (Zombie.class.isAssignableFrom(clazz)) {
                if (Husk.class.isAssignableFrom(clazz)) {
                    entity = EntityTypes.HUSK.a(world);
                } else if (ZombieVillager.class.isAssignableFrom(clazz)) {
                    entity = EntityTypes.ZOMBIE_VILLAGER.a(world);
                } else if (Drowned.class.isAssignableFrom(clazz)) {
                    entity = EntityTypes.DROWNED.a(world);
                } else {
                    entity = new EntityZombie(world);
                }
            } else if (Giant.class.isAssignableFrom(clazz)) {
                entity = EntityTypes.GIANT.a(world);
            } else if (Silverfish.class.isAssignableFrom(clazz)) {
                entity = EntityTypes.SILVERFISH.a(world);
            } else if (Enderman.class.isAssignableFrom(clazz)) {
                entity = EntityTypes.ENDERMAN.a(world);
            } else if (Blaze.class.isAssignableFrom(clazz)) {
                entity = EntityTypes.BLAZE.a(world);
            } else if (AbstractVillager.class.isAssignableFrom(clazz)) {
                if (Villager.class.isAssignableFrom(clazz)) {
                    entity = EntityTypes.VILLAGER.a(world);
                } else if (WanderingTrader.class.isAssignableFrom(clazz)) {
                    entity = EntityTypes.WANDERING_TRADER.a(world);
                }
            } else if (Witch.class.isAssignableFrom(clazz)) {
                entity = EntityTypes.WITCH.a(world);
            } else if (Wither.class.isAssignableFrom(clazz)) {
                entity = EntityTypes.WITHER.a(world);
            } else if (ComplexLivingEntity.class.isAssignableFrom(clazz)) {
                if (EnderDragon.class.isAssignableFrom(clazz)) {
                    entity = EntityTypes.ENDER_DRAGON.a(world);
                }
            } else if (Ambient.class.isAssignableFrom(clazz)) {
                if (Bat.class.isAssignableFrom(clazz)) {
                    entity = EntityTypes.BAT.a(world);
                }
            } else if (Rabbit.class.isAssignableFrom(clazz)) {
                entity = EntityTypes.RABBIT.a(world);
            } else if (Endermite.class.isAssignableFrom(clazz)) {
                entity = EntityTypes.ENDERMITE.a(world);
            } else if (Guardian.class.isAssignableFrom(clazz)) {
                if (ElderGuardian.class.isAssignableFrom(clazz)){
                    entity = EntityTypes.ELDER_GUARDIAN.a(world);
                } else {
                    entity = EntityTypes.GUARDIAN.a(world);
                }
            } else if (ArmorStand.class.isAssignableFrom(clazz)) {
                entity = new EntityArmorStand(world, x, y, z);
            } else if (PolarBear.class.isAssignableFrom(clazz)) {
                entity = EntityTypes.POLAR_BEAR.a(world);
            } else if (Vex.class.isAssignableFrom(clazz)) {
                entity = EntityTypes.VEX.a(world);
            } else if (Illager.class.isAssignableFrom(clazz)) {
                if (Spellcaster.class.isAssignableFrom(clazz)) {
                    if (Evoker.class.isAssignableFrom(clazz)) {
                        entity = EntityTypes.EVOKER.a(world);
                    } else if (Illusioner.class.isAssignableFrom(clazz)) {
                        entity = EntityTypes.ILLUSIONER.a(world);
                    }
                } else if (Vindicator.class.isAssignableFrom(clazz)) {
                    entity = EntityTypes.VINDICATOR.a(world);
                } else if (Pillager.class.isAssignableFrom(clazz)) {
                    entity = EntityTypes.PILLAGER.a(world);
                }
            } else if (Turtle.class.isAssignableFrom(clazz)) {
                entity = EntityTypes.TURTLE.a(world);
            } else if (Phantom.class.isAssignableFrom(clazz)) {
                entity = EntityTypes.PHANTOM.a(world);
            } else if (Fish.class.isAssignableFrom(clazz)) {
                if (Cod.class.isAssignableFrom(clazz)) {
                    entity = EntityTypes.COD.a(world);
                } else if (PufferFish.class.isAssignableFrom(clazz)) {
                    entity = EntityTypes.PUFFERFISH.a(world);
                } else if (Salmon.class.isAssignableFrom(clazz)) {
                    entity = EntityTypes.SALMON.a(world);
                } else if (TropicalFish.class.isAssignableFrom(clazz)) {
                    entity = EntityTypes.TROPICAL_FISH.a(world);
                }
            } else if (Dolphin.class.isAssignableFrom(clazz)) {
                entity = EntityTypes.DOLPHIN.a(world);
            } else if (Ocelot.class.isAssignableFrom(clazz)) {
                entity = EntityTypes.OCELOT.a(world);
            } else if (Ravager.class.isAssignableFrom(clazz)) {
                entity = EntityTypes.RAVAGER.a(world);
            } else if (Panda.class.isAssignableFrom(clazz)) {
                entity = EntityTypes.PANDA.a(world);
            } else if (Fox.class.isAssignableFrom(clazz)) {
                entity = EntityTypes.FOX.a(world);
            }

            if (entity != null) {
                entity.setLocation(x, y, z, yaw, pitch);
                entity.setHeadRotation(yaw); // SPIGOT-3587
            }
        } else if (Hanging.class.isAssignableFrom(clazz)) {
            BlockFace face = BlockFace.SELF;

            int width = 16; // 1 full block, also painting smallest size.
            int height = 16; // 1 full block, also painting smallest size.

            if (ItemFrame.class.isAssignableFrom(clazz)) {
                width = 12;
                height = 12;
            } else if (LeashHitch.class.isAssignableFrom(clazz)) {
                width = 9;
                height = 9;
            }

            BlockFace[] faces = new BlockFace[]{BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH, BlockFace.UP, BlockFace.DOWN};
            final BlockPosition pos = new BlockPosition((int) x, (int) y, (int) z);
            for (BlockFace dir : faces) {
                IBlockData nmsBlock = world.getType(pos.shift(CraftBlock.blockFaceToNotch(dir)));
                if (nmsBlock.getMaterial().isBuildable() || BlockDiodeAbstract.isDiode(nmsBlock)) {
                    boolean taken = false;
                    AxisAlignedBB bb = EntityHanging.calculateBoundingBox(null, pos, CraftBlock.blockFaceToNotch(dir).opposite(), width, height);
                    List<net.minecraft.server.Entity> list = (List<net.minecraft.server.Entity>) world.getEntities(null, bb);
                    for (Iterator<net.minecraft.server.Entity> it = list.iterator(); !taken && it.hasNext();) {
                        net.minecraft.server.Entity e = it.next();
                        if (e instanceof EntityHanging) {
                            taken = true; // Hanging entities do not like hanging entities which intersect them.
                        }
                    }

                    if (!taken) {
                        face = dir;
                        break;
                    }
                }
            }

            if (LeashHitch.class.isAssignableFrom(clazz)) {
                entity = new EntityLeash(world, new BlockPosition((int) x, (int) y, (int) z));
                entity.attachedToPlayer = true;
            } else {
                // No valid face found
                Preconditions.checkArgument(face != BlockFace.SELF, "Cannot spawn hanging entity for %s at %s (no free face)", clazz.getName(), location);

                EnumDirection dir = CraftBlock.blockFaceToNotch(face).opposite();
                if (Painting.class.isAssignableFrom(clazz)) {
                    entity = new EntityPainting(world, new BlockPosition((int) x, (int) y, (int) z), dir);
                } else if (ItemFrame.class.isAssignableFrom(clazz)) {
                    entity = new EntityItemFrame(world, new BlockPosition((int) x, (int) y, (int) z), dir);
                }
            }

            if (entity != null && !((EntityHanging) entity).survives()) {
                throw new IllegalArgumentException("Cannot spawn hanging entity for " + clazz.getName() + " at " + location);
            }
        } else if (TNTPrimed.class.isAssignableFrom(clazz)) {
            entity = new EntityTNTPrimed(world, x, y, z, null);
        } else if (ExperienceOrb.class.isAssignableFrom(clazz)) {
            entity = new EntityExperienceOrb(world, x, y, z, 0, org.bukkit.entity.ExperienceOrb.SpawnReason.CUSTOM, null, null); // Paper
        } else if (LightningStrike.class.isAssignableFrom(clazz)) {
            entity = new EntityLightning(world, x, y, z, false);
        } else if (Firework.class.isAssignableFrom(clazz)) {
            entity = new EntityFireworks(world, x, y, z, net.minecraft.server.ItemStack.a);
        } else if (AreaEffectCloud.class.isAssignableFrom(clazz)) {
            entity = new EntityAreaEffectCloud(world, x, y, z);
        } else if (EvokerFangs.class.isAssignableFrom(clazz)) {
            entity = new EntityEvokerFangs(world, x, y, z, (float) Math.toRadians(yaw), 0, null);
        }

        if (entity != null) {
            // Spigot start
            if (entity instanceof net.minecraft.server.EntityOcelot)
            {
                ( (net.minecraft.server.EntityOcelot) entity ).spawnBonus = false;
            }
            // Spigot end
            return entity;
        }

        throw new IllegalArgumentException("Cannot spawn an entity for " + clazz.getName());
    }

    @SuppressWarnings("unchecked")
    public <T extends Entity> T addEntity(net.minecraft.server.Entity entity, SpawnReason reason) throws IllegalArgumentException {
        return addEntity(entity, reason, null);
    }

    @SuppressWarnings("unchecked")
    public <T extends Entity> T addEntity(net.minecraft.server.Entity entity, SpawnReason reason, Consumer<T> function) throws IllegalArgumentException {
        Preconditions.checkArgument(entity != null, "Cannot spawn null entity");

        if (entity instanceof EntityInsentient) {
            ((EntityInsentient) entity).prepare(getHandle(), getHandle().getDamageScaler(new BlockPosition(entity)), EnumMobSpawn.COMMAND, (GroupDataEntity) null, null);
        }

        if (function != null) {
            function.accept((T) entity.getBukkitEntity());
        }

        world.addEntity(entity, reason);
        return (T) entity.getBukkitEntity();
    }

    public <T extends Entity> T spawn(Location location, Class<T> clazz, Consumer<T> function, SpawnReason reason) throws IllegalArgumentException {
        net.minecraft.server.Entity entity = createEntity(location, clazz);

        return addEntity(entity, reason, function);
    }

    @Override
    public ChunkSnapshot getEmptyChunkSnapshot(int x, int z, boolean includeBiome, boolean includeBiomeTempRain) {
        return CraftChunk.getEmptyChunkSnapshot(x, z, this, includeBiome, includeBiomeTempRain);
    }

    @Override
    public void setSpawnFlags(boolean allowMonsters, boolean allowAnimals) {
        world.setSpawnFlags(allowMonsters, allowAnimals);
    }

    @Override
    public boolean getAllowAnimals() {
        return world.getChunkProvider().allowAnimals;
    }

    @Override
    public boolean getAllowMonsters() {
        return world.getChunkProvider().allowMonsters;
    }

    @Override
    public int getMaxHeight() {
        return world.getBuildHeight();
    }

    @Override
    public int getSeaLevel() {
        return world.getSeaLevel();
    }

    @Override
    public boolean getKeepSpawnInMemory() {
        return world.keepSpawnInMemory;
    }

    @Override
    public void setKeepSpawnInMemory(boolean keepLoaded) {
        world.keepSpawnInMemory = keepLoaded;
        // Grab the worlds spawn chunk
        BlockPosition chunkcoordinates = this.world.getSpawn();
        if (keepLoaded) {
            world.getChunkProvider().addTicket(TicketType.START, new ChunkCoordIntPair(chunkcoordinates), 11, Unit.INSTANCE);
        } else {
            // TODO: doesn't work well if spawn changed....
            world.getChunkProvider().removeTicket(TicketType.START, new ChunkCoordIntPair(chunkcoordinates), 11, Unit.INSTANCE);
        }
    }

    @Override
    public int hashCode() {
        return getUID().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final CraftWorld other = (CraftWorld) obj;

        return this.getUID() == other.getUID();
    }

    @Override
    public File getWorldFolder() {
        return ((WorldNBTStorage) world.getDataManager()).getDirectory();
    }

    @Override
    public void sendPluginMessage(Plugin source, String channel, byte[] message) {
        StandardMessenger.validatePluginMessage(server.getMessenger(), source, channel, message);

        for (Player player : getPlayers()) {
            player.sendPluginMessage(source, channel, message);
        }
    }

    @Override
    public Set<String> getListeningPluginChannels() {
        Set<String> result = new HashSet<String>();

        for (Player player : getPlayers()) {
            result.addAll(player.getListeningPluginChannels());
        }

        return result;
    }

    @Override
    public org.bukkit.WorldType getWorldType() {
        return org.bukkit.WorldType.getByName(world.getWorldData().getType().name());
    }

    @Override
    public boolean canGenerateStructures() {
        return world.getWorldData().shouldGenerateMapFeatures();
    }

    @Override
    public long getTicksPerAnimalSpawns() {
        return world.ticksPerAnimalSpawns;
    }

    @Override
    public void setTicksPerAnimalSpawns(int ticksPerAnimalSpawns) {
        world.ticksPerAnimalSpawns = ticksPerAnimalSpawns;
    }

    @Override
    public long getTicksPerMonsterSpawns() {
        return world.ticksPerMonsterSpawns;
    }

    @Override
    public void setTicksPerMonsterSpawns(int ticksPerMonsterSpawns) {
        world.ticksPerMonsterSpawns = ticksPerMonsterSpawns;
    }

    @Override
    public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
        server.getWorldMetadata().setMetadata(this, metadataKey, newMetadataValue);
    }

    @Override
    public List<MetadataValue> getMetadata(String metadataKey) {
        return server.getWorldMetadata().getMetadata(this, metadataKey);
    }

    @Override
    public boolean hasMetadata(String metadataKey) {
        return server.getWorldMetadata().hasMetadata(this, metadataKey);
    }

    @Override
    public void removeMetadata(String metadataKey, Plugin owningPlugin) {
        server.getWorldMetadata().removeMetadata(this, metadataKey, owningPlugin);
    }

    @Override
    public int getMonsterSpawnLimit() {
        if (monsterSpawn < 0) {
            return server.getMonsterSpawnLimit();
        }

        return monsterSpawn;
    }

    @Override
    public void setMonsterSpawnLimit(int limit) {
        monsterSpawn = limit;
    }

    @Override
    public int getAnimalSpawnLimit() {
        if (animalSpawn < 0) {
            return server.getAnimalSpawnLimit();
        }

        return animalSpawn;
    }

    @Override
    public void setAnimalSpawnLimit(int limit) {
        animalSpawn = limit;
    }

    @Override
    public int getWaterAnimalSpawnLimit() {
        if (waterAnimalSpawn < 0) {
            return server.getWaterAnimalSpawnLimit();
        }

        return waterAnimalSpawn;
    }

    @Override
    public void setWaterAnimalSpawnLimit(int limit) {
        waterAnimalSpawn = limit;
    }

    @Override
    public int getAmbientSpawnLimit() {
        if (ambientSpawn < 0) {
            return server.getAmbientSpawnLimit();
        }

        return ambientSpawn;
    }

    @Override
    public void setAmbientSpawnLimit(int limit) {
        ambientSpawn = limit;
    }

    @Override
    public void playSound(Location loc, Sound sound, float volume, float pitch) {
        playSound(loc, sound, org.bukkit.SoundCategory.MASTER, volume, pitch);
    }

    @Override
    public void playSound(Location loc, String sound, float volume, float pitch) {
        playSound(loc, sound, org.bukkit.SoundCategory.MASTER, volume, pitch);
    }

    @Override
    public void playSound(Location loc, Sound sound, org.bukkit.SoundCategory category, float volume, float pitch) {
        if (loc == null || sound == null || category == null) return;

        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        getHandle().a(null, x, y, z, CraftSound.getSoundEffect(CraftSound.getSound(sound)), SoundCategory.valueOf(category.name()), volume, pitch); // PAIL: rename
    }

    @Override
    public void playSound(Location loc, String sound, org.bukkit.SoundCategory category, float volume, float pitch) {
        if (loc == null || sound == null || category == null) return;

        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        PacketPlayOutCustomSoundEffect packet = new PacketPlayOutCustomSoundEffect(new MinecraftKey(sound), SoundCategory.valueOf(category.name()), new Vec3D(x, y, z), volume, pitch);
        world.getMinecraftServer().getPlayerList().sendPacketNearby(null, x, y, z, volume > 1.0F ? 16.0F * volume : 16.0D, this.world, packet); // Paper - this.world.dimension -> this.world
    }

    @Override
    public String getGameRuleValue(String rule) {
        // In method contract for some reason
        if (rule == null) {
            return null;
        }

        GameRules.GameRuleValue value = getHandle().getGameRules().get(rule);
        return value != null ? value.getValue() : "";
    }

    @Override
    public boolean setGameRuleValue(String rule, String value) {
        // No null values allowed
        if (rule == null || value == null) return false;

        if (!isGameRule(rule)) return false;

        getHandle().getGameRules().set(rule, value, getHandle().getMinecraftServer());
        return true;
    }

    @Override
    public String[] getGameRules() {
        return GameRules.getGameRules().keySet().toArray(new String[GameRules.getGameRules().size()]);
    }

    @Override
    public boolean isGameRule(String rule) {
        Validate.isTrue(rule != null && !rule.isEmpty(), "Rule cannot be null nor empty");
        return GameRules.getGameRules().containsKey(rule);
    }

    @Override
    public <T> T getGameRuleValue(GameRule<T> rule) {
        Validate.notNull(rule, "GameRule cannot be null");
        return convert(rule, getHandle().getGameRules().get(rule.getName()));
    }

    @Override
    public <T> T getGameRuleDefault(GameRule<T> rule) {
        Validate.notNull(rule, "GameRule cannot be null");
        return convert(rule, GameRules.getGameRules().get(rule.getName()).a());
    }

    @Override
    public <T> boolean setGameRule(GameRule<T> rule, T newValue) {
        Validate.notNull(rule, "GameRule cannot be null");
        Validate.notNull(newValue, "GameRule value cannot be null");

        if (!isGameRule(rule.getName())) return false;

        getHandle().getGameRules().set(rule.getName(), newValue.toString(), getHandle().getMinecraftServer());
        return true;
    }

    private <T> T convert(GameRule<T> rule, GameRules.GameRuleValue value) {
        if (value == null) {
            return null;
        }

        switch (value.getType()) {
            case BOOLEAN_VALUE:
                return rule.getType().cast(value.getBooleanValue());
            case NUMERICAL_VALUE:
                return rule.getType().cast(value.getIntValue());
            default:
                throw new IllegalArgumentException("Invalid GameRule type (" + value.getType() + ") for GameRule " + rule.getName());
        }
    }

    @Override
    public WorldBorder getWorldBorder() {
        if (this.worldBorder == null) {
            this.worldBorder = new CraftWorldBorder(this);
        }

        return this.worldBorder;
    }

    @Override
    public void spawnParticle(Particle particle, Location location, int count) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count);
    }

    @Override
    public void spawnParticle(Particle particle, double x, double y, double z, int count) {
        spawnParticle(particle, x, y, z, count, null);
    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, T data) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, data);
    }

    @Override
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, T data) {
        spawnParticle(particle, x, y, z, count, 0, 0, 0, data);
    }

    @Override
    public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ);
    }

    @Override
    public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ) {
        spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, null);
    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, T data) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, data);
    }

    @Override
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, T data) {
        spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, 1, data);
    }

    @Override
    public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, extra);
    }

    @Override
    public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra) {
        spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, extra, null);
    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra, T data) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, extra, data);
    }

    @Override
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, T data) {
        spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, extra, data, false);
    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra, T data, boolean force) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, extra, data, force);
    }

    @Override
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, T data, boolean force) {
        // Paper start - Particle API Expansion
        spawnParticle(particle, null, null, x, y, z, count, offsetX, offsetY, offsetZ, extra, data, force);
    }
    public <T> void spawnParticle(Particle particle, List<Player> receivers, Player sender, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, T data, boolean force) {
        // Paper end
        if (data != null && !particle.getDataType().isInstance(data)) {
            throw new IllegalArgumentException("data should be " + particle.getDataType() + " got " + data.getClass());
        }
        getHandle().sendParticles(
                receivers == null ? getHandle().players : receivers.stream().map(player -> ((CraftPlayer) player).getHandle()).collect(java.util.stream.Collectors.toList()), // Paper -  Particle API Expansion
                sender != null ? ((CraftPlayer) sender).getHandle() : null, // Sender // Paper - Particle API Expansion
                CraftParticle.toNMS(particle, data), // Particle
                x, y, z, // Position
                count,  // Count
                offsetX, offsetY, offsetZ, // Random offset
                extra, // Speed?
                force
        );

    }

    @Override
    public Location locateNearestStructure(Location origin, StructureType structureType, int radius, boolean findUnexplored) {
        BlockPosition originPos = new BlockPosition(origin.getX(), origin.getY(), origin.getZ());
        BlockPosition nearest = getHandle().getChunkProvider().getChunkGenerator().findNearestMapFeature(getHandle(), structureType.getName(), originPos, radius, findUnexplored);
        return (nearest == null) ? null : new Location(this, nearest.getX(), nearest.getY(), nearest.getZ());
    }

    // Spigot start
    private final Spigot spigot = new Spigot()
    {

        @Override
        public LightningStrike strikeLightning(Location loc, boolean isSilent)
        {
            EntityLightning lightning = new EntityLightning( world, loc.getX(), loc.getY(), loc.getZ(), false, isSilent );
            world.strikeLightning( lightning );
            return new CraftLightningStrike( server, lightning );
        }

        @Override
        public LightningStrike strikeLightningEffect(Location loc, boolean isSilent)
        {
            EntityLightning lightning = new EntityLightning( world, loc.getX(), loc.getY(), loc.getZ(), true, isSilent );
            world.strikeLightning( lightning );
            return new CraftLightningStrike( server, lightning );
        }
    };

    public Spigot spigot()
    {
        return spigot;
    }
    // Spigot end
}
