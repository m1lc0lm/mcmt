package net.minecraft.server;

import java.util.Random;
import javax.annotation.Nullable;

public abstract class TileEntityLootable extends TileEntityContainer {

    @Nullable
    public MinecraftKey lootTable; public MinecraftKey getLootTableKey() { return this.lootTable; } public void setLootTable(final MinecraftKey key) { this.lootTable = key; } // Paper - OBFHELPER
    public long lootTableSeed; public long getSeed() { return this.lootTableSeed; } public void setSeed(final long seed) { this.lootTableSeed = seed; } // Paper - OBFHELPER
    public final com.destroystokyo.paper.loottable.PaperLootableInventoryData lootableData = new com.destroystokyo.paper.loottable.PaperLootableInventoryData(new com.destroystokyo.paper.loottable.PaperTileEntityLootableInventory(this)); // Paper

    protected TileEntityLootable(TileEntityTypes<?> tileentitytypes) {
        super(tileentitytypes);
    }

    public static void a(IBlockAccess iblockaccess, Random random, BlockPosition blockposition, MinecraftKey minecraftkey) {
        TileEntity tileentity = iblockaccess.getTileEntity(blockposition);

        if (tileentity instanceof TileEntityLootable) {
            ((TileEntityLootable) tileentity).setLootTable(minecraftkey, random.nextLong());
        }

    }

    protected boolean d(NBTTagCompound nbttagcompound) {
        this.lootableData.loadNbt(nbttagcompound); // Paper
        if (nbttagcompound.hasKeyOfType("LootTable", 8)) {
            this.lootTable = new MinecraftKey(nbttagcompound.getString("LootTable"));
            this.lootTableSeed = nbttagcompound.getLong("LootTableSeed");
            return false; // Paper - always load the items, table may still remain
        } else {
            return false;
        }
    }

    protected boolean e(NBTTagCompound nbttagcompound) {
        this.lootableData.saveNbt(nbttagcompound); // Paper
        if (this.lootTable == null) {
            return false;
        } else {
            nbttagcompound.setString("LootTable", this.lootTable.toString());
            if (this.lootTableSeed != 0L) {
                nbttagcompound.setLong("LootTableSeed", this.lootTableSeed);
            }

            return false; // Paper - always save the items, table may still remain
        }
    }

    public void d(@Nullable EntityHuman entityhuman) {
        if (this.lootableData.shouldReplenish(entityhuman) && this.world.getMinecraftServer() != null) { // Paper
            LootTable loottable = this.world.getMinecraftServer().getLootTableRegistry().getLootTable(this.lootTable);

            this.lootableData.processRefill(entityhuman); // Paper
            LootTableInfo.Builder loottableinfo_builder = (new LootTableInfo.Builder((WorldServer) this.world)).set(LootContextParameters.POSITION, new BlockPosition(this.position)).a(this.lootTableSeed);

            if (entityhuman != null) {
                loottableinfo_builder.a(entityhuman.eb()).set(LootContextParameters.THIS_ENTITY, entityhuman);
            }

            loottable.fillInventory(this, loottableinfo_builder.build(LootContextParameterSets.CHEST));
        }

    }

    public void setLootTable(MinecraftKey minecraftkey, long i) {
        this.lootTable = minecraftkey;
        this.lootTableSeed = i;
    }

    @Override
    public ItemStack getItem(int i) {
        this.d((EntityHuman) null);
        return (ItemStack) this.f().get(i);
    }

    @Override
    public ItemStack splitStack(int i, int j) {
        this.d((EntityHuman) null);
        ItemStack itemstack = ContainerUtil.a(this.f(), i, j);

        if (!itemstack.isEmpty()) {
            this.update();
        }

        return itemstack;
    }

    @Override
    public ItemStack splitWithoutUpdate(int i) {
        this.d((EntityHuman) null);
        return ContainerUtil.a(this.f(), i);
    }

    @Override
    public void setItem(int i, ItemStack itemstack) {
        this.d((EntityHuman) null);
        this.f().set(i, itemstack);
        if (itemstack.getCount() > this.getMaxStackSize()) {
            itemstack.setCount(this.getMaxStackSize());
        }

        this.update();
    }

    @Override
    public boolean a(EntityHuman entityhuman) {
        return this.world.getTileEntity(this.position) != this ? false : entityhuman.e((double) this.position.getX() + 0.5D, (double) this.position.getY() + 0.5D, (double) this.position.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void clear() {
        this.f().clear();
    }

    protected abstract NonNullList<ItemStack> f();

    protected abstract void a(NonNullList<ItemStack> nonnulllist);

    @Override
    public boolean e(EntityHuman entityhuman) {
        return super.e(entityhuman) && (this.lootTable == null || !entityhuman.isSpectator());
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerinventory, EntityHuman entityhuman) {
        if (this.e(entityhuman)) {
            this.d(playerinventory.player);
            return this.createContainer(i, playerinventory);
        } else {
            return null;
        }
    }
}
