package net.minecraft.server;

import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Supplier;
// CraftBukkit start
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.inventory.InventoryHolder;
// CraftBukkit end
import co.aikar.timings.MinecraftTimings; // Paper
import co.aikar.timings.Timing; // Paper

public abstract class TileEntity implements KeyedObject { // Paper

    public Timing tickTimer = MinecraftTimings.getTileEntityTimings(this); // Paper
    // CraftBukkit start - data containers
    private static final CraftPersistentDataTypeRegistry DATA_TYPE_REGISTRY = new CraftPersistentDataTypeRegistry();
    public final CraftPersistentDataContainer persistentDataContainer = new CraftPersistentDataContainer(DATA_TYPE_REGISTRY);
    // CraftBukkit end
    private static final Logger LOGGER = LogManager.getLogger();
    boolean isLoadingStructure = false; // Paper
    private final TileEntityTypes<?> b; public TileEntityTypes getTileEntityType() { return b; } // Paper - OBFHELPER
    @Nullable
    protected World world;
    protected BlockPosition position;
    protected boolean f;
    @Nullable
    private IBlockData c;
    private boolean g;

    public TileEntity(TileEntityTypes<?> tileentitytypes) {
        this.position = BlockPosition.ZERO;
        this.b = tileentitytypes;
    }

    // Paper start
    private String tileEntityKeyString = null;
    private MinecraftKey tileEntityKey = null;

    @Override
    public MinecraftKey getMinecraftKey() {
        if (tileEntityKey == null) {
            tileEntityKey = TileEntityTypes.a(this.getTileEntityType());
            tileEntityKeyString = tileEntityKey != null ? tileEntityKey.toString() : null;
        }
        return tileEntityKey;
    }

    @Override
    public String getMinecraftKeyString() {
        getMinecraftKey(); // Try to load if it doesn't exists.
        return tileEntityKeyString;
    }

    private java.lang.ref.WeakReference<Chunk> currentChunk = null;
    public Chunk getCurrentChunk() {
        final Chunk chunk = currentChunk != null ? currentChunk.get() : null;
        return chunk != null && chunk.isLoaded() ? chunk : null;
    }
    public void setCurrentChunk(Chunk chunk) {
        this.currentChunk = chunk != null ? new java.lang.ref.WeakReference<>(chunk) : null;
    }
    // Paper end

    @Nullable
    public World getWorld() {
        return this.world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public boolean hasWorld() {
        return this.world != null;
    }

    public void load(NBTTagCompound nbttagcompound) {
        this.position = new BlockPosition(nbttagcompound.getInt("x"), nbttagcompound.getInt("y"), nbttagcompound.getInt("z"));
        // CraftBukkit start - read container
        NBTTagCompound persistentDataTag = nbttagcompound.getCompound("PublicBukkitValues");
        if (persistentDataTag != null) {
            this.persistentDataContainer.putAll(persistentDataTag);
        }
        // CraftBukkit end
    }

    public NBTTagCompound save(NBTTagCompound nbttagcompound) {
        return this.d(nbttagcompound);
    }

    private NBTTagCompound d(NBTTagCompound nbttagcompound) {
        MinecraftKey minecraftkey = TileEntityTypes.a(this.q());

        if (minecraftkey == null) {
            throw new RuntimeException(this.getClass() + " is missing a mapping! This is a bug!");
        } else {
            nbttagcompound.setString("id", minecraftkey.toString());
            nbttagcompound.setInt("x", this.position.getX());
            nbttagcompound.setInt("y", this.position.getY());
            nbttagcompound.setInt("z", this.position.getZ());
            // CraftBukkit start - store container
            if (!this.persistentDataContainer.isEmpty()) {
                nbttagcompound.set("PublicBukkitValues", this.persistentDataContainer.toTagCompound());
            }
            // CraftBukkit end
            return nbttagcompound;
        }
    }

    // CraftBukkit start
    @Nullable
    public static TileEntity create(NBTTagCompound nbttagcompound) {
        return create(nbttagcompound, null);
    }

    @Nullable
    public static TileEntity create(NBTTagCompound nbttagcompound, @Nullable World world) {
        // CraftBukkit end
        String s = nbttagcompound.getString("id");

        return (TileEntity) IRegistry.BLOCK_ENTITY_TYPE.getOptional(new MinecraftKey(s)).map((tileentitytypes) -> {
            try {
                return tileentitytypes.a();
            } catch (Throwable throwable) {
                TileEntity.LOGGER.error("Failed to create block entity {}", s, throwable);
                return null;
            }
        }).map((tileentity) -> {
            try {
                tileentity.setWorld(world); // CraftBukkit
                tileentity.load(nbttagcompound);
                return tileentity;
            } catch (Throwable throwable) {
                TileEntity.LOGGER.error("Failed to load data for block entity {}", s, throwable);
                return null;
            }
        }).orElseGet(() -> {
            TileEntity.LOGGER.warn("Skipping BlockEntity with id {}", s);
            return null;
        });
    }

    public void update() {
        if (this.world != null) {
            this.c = this.world.getType(this.position);
            this.world.b(this.position, this);
            if (!this.c.isAir()) {
                this.world.updateAdjacentComparators(this.position, this.c.getBlock());
            }
        }

    }

    public BlockPosition getPosition() {
        return this.position;
    }

    public IBlockData getBlock() {
        if (this.c == null) {
            this.c = this.world.getType(this.position);
        }

        return this.c;
    }

    @Nullable
    public PacketPlayOutTileEntityData getUpdatePacket() {
        return null;
    }

    public NBTTagCompound b() {
        return this.d(new NBTTagCompound());
    }

    public boolean isRemoved() {
        return this.f;
    }

    public void W_() {
        this.f = true;
    }

    public void n() {
        this.f = false;
    }

    public boolean setProperty(int i, int j) {
        return false;
    }

    public void invalidateBlockCache() {
        this.c = null;
    }

    public void a(CrashReportSystemDetails crashreportsystemdetails) {
        crashreportsystemdetails.a("Name", () -> {
            return IRegistry.BLOCK_ENTITY_TYPE.getKey(this.q()) + " // " + this.getClass().getCanonicalName();
        });
        if (this.world != null) {
            // Paper start - Prevent TileEntity and Entity crashes
            IBlockData block = this.getBlock();
            if (block != null) {
                CrashReportSystemDetails.a(crashreportsystemdetails, this.position, block);
            }
            // Paper end
            CrashReportSystemDetails.a(crashreportsystemdetails, this.position, this.world.getType(this.position));
        }
    }

    public void setPosition(BlockPosition blockposition) {
        this.position = blockposition.immutableCopy();
    }

    public boolean isFilteredNBT() {
        return false;
    }

    public void a(EnumBlockRotation enumblockrotation) {}

    public void a(EnumBlockMirror enumblockmirror) {}

    public TileEntityTypes<?> q() {
        return this.b;
    }

    public void r() {
        if (!this.g) {
            this.g = true;
            TileEntity.LOGGER.warn("Block entity invalid: {} @ {}", new Supplier[]{() -> {
                        return IRegistry.BLOCK_ENTITY_TYPE.getKey(this.q());
                    }, this::getPosition});
        }
    }

    // CraftBukkit start - add method
    public InventoryHolder getOwner() {
        if (world == null) return null;
        // Spigot start
        org.bukkit.block.Block block = world.getWorld().getBlockAt(position.getX(), position.getY(), position.getZ());
        if (block == null) {
            org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.WARNING, "No block for owner at %s %d %d %d", new Object[]{world.getWorld(), position.getX(), position.getY(), position.getZ()});
            return null;
        }
        // Spigot end
        org.bukkit.block.BlockState state = block.getState();
        if (state instanceof InventoryHolder) return (InventoryHolder) state;
        return null;
    }
    // CraftBukkit end
}
