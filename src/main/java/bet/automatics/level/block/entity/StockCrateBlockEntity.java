package bet.automatics.level.block.entity;

import bet.automatics.Automatics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.ticks.ContainerSingleItem;
import org.jspecify.annotations.Nullable;

import java.util.List;

/// Note at myself how tf did I get this to work

public class StockCrateBlockEntity extends BlockEntity implements ContainerSingleItem.BlockContainerSingleItem, RandomizableContainer {
    private static final int CAPACITY = 3200;
    private ItemStack storedItem = ItemStack.EMPTY;
    private int storedCount = 0;
    protected @Nullable ResourceKey<LootTable> lootTable;
    protected long lootTableSeed;

    protected StockCrateBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
    }

    public StockCrateBlockEntity(BlockPos worldPosition, BlockState blockState) {
        this(ModBlockEntityTypes.STOCK_CRATE.get(), worldPosition, blockState);
    }

    public boolean canInsert(ItemStack stack) {
        return !stack.isEmpty() && storedCount < CAPACITY && (storedItem.isEmpty() || ItemStack.isSameItemSameComponents(storedItem, stack));
    }

    public int getStoredCount() {
        return storedCount;
    }

    public boolean insertOne(ItemStack stack) {
        if (!canInsert(stack)) {
            return false;
        }

        if (storedItem.isEmpty()) {
            storedItem = stack.copyWithCount(1);
        }

        storedCount++;
        setChanged();
        return true;
    }

    private void syncStoredCount() {
        int amount = storedItem.getCount();
        if (amount >= 1) {
            storedCount += amount - 1;
            storedItem.setCount(1);
        }
    }

    public List<ItemStack> getContentsForDrops() {
        List<ItemStack> loot = new java.util.ArrayList<>();

        if (storedItem.isEmpty() || storedCount <= 0) {
            return loot;
        }

        int remaining = storedCount;
        int maxStackSize = storedItem.getMaxStackSize();

        while (remaining > 0) {
            int amount = Math.min(remaining, maxStackSize);
            loot.add(storedItem.copyWithCount(amount));
            remaining -= amount;
        }

        return loot;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        if (!this.trySaveLootTable(output) && !this.storedItem.isEmpty()) {
            output.store("item", ItemStack.CODEC, this.storedItem);
            output.putInt("storedCount", storedCount);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        if (!this.tryLoadLootTable(input)) {
            this.storedItem = input.read("item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
            this.storedCount = input.getInt("storedCount").orElse(storedItem.getCount());

        } else {
            storedCount = Math.min(storedCount, CAPACITY);
            this.storedItem = ItemStack.EMPTY;
        }
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @Nullable ResourceKey<LootTable> getLootTable() {
        return this.lootTable;
    }

    @Override
    public void setLootTable(@Nullable ResourceKey<LootTable> lootTable) {
        this.lootTable = lootTable;
    }

    @Override
    public long getLootTableSeed() {
        return this.lootTableSeed;
    }

    @Override
    public void setLootTableSeed(long lootTableSeed) {
        this.lootTableSeed = lootTableSeed;
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(this.storedItem)));
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        this.storedItem = components.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyOne();
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        super.removeComponentsFromTag(output);
        output.discard("storedItem");
    }

    @Override
    public BlockEntity getContainerBlockEntity() {
        return this;
    }

    @Override
    public ItemStack getTheItem() {
        this.unpackLootTable(null);
        Automatics.LOGGER.info("getted item, {}", storedItem.copyWithCount(storedCount));
        Automatics.LOGGER.info("current item, {}", storedItem);
        return storedItem;
    }

    @Override
    public ItemStack splitTheItem(int count) {
        this.unpackLootTable(null);

        if (storedItem.isEmpty() || storedCount <= 0) {
            return ItemStack.EMPTY;
        }

        int amount = Math.min(count, storedCount);
        ItemStack result = storedItem.copyWithCount(amount);

        storedCount -= amount;

        if (storedCount <= 0) {
            storedCount = 0;
            storedItem = ItemStack.EMPTY;
        }

        return result;
    }

    @Override
    public void setItem(int slot, ItemStack stack, boolean insideTransaction) {
        if (slot == 0) {
            if (insideTransaction) {
                this.storedItem = stack;
            } else {
                setTheItem(stack);
            }
        }
    }

    @Override
    public void setTheItem(ItemStack itemStack) {
        this.unpackLootTable(null);
        this.storedItem = itemStack;
        this.storedCount = Math.min(itemStack.getCount(), CAPACITY);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack itemStack) {
         return storedCount < CAPACITY || this.getItem(slot).isEmpty();
    }

    @Override
    public void setChanged() {
        super.setChanged();

        syncStoredCount();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }

        for (ItemStack stack : getContentsForDrops()) {
            Block.popResource(this.level, pos, stack);
        }

        storedItem = ItemStack.EMPTY;
        storedCount = 0;
    }
}
