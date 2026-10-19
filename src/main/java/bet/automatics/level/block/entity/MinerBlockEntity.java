package bet.automatics.level.block.entity;

import bet.automatics.inventory.MinerMenu;
import bet.automatics.level.block.custom.MinerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class MinerBlockEntity  extends RandomizableContainerBlockEntity {
    private static final Component DEFAULT_NAME = Component.translatable("container.automatics.miner");
    private NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);

    private int miningProgress = 0;
    private int miningRequired = 3;

    // which block currently has the breaking overlay.
    private BlockPos breakingPos;

    protected MinerBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
    }

    public MinerBlockEntity(BlockPos worldPosition, BlockState blockState) {
        this(ModBlockEntityTypes.MINER.get(), worldPosition, blockState);
    }

    @Override
    protected Component getDefaultName() {
        return DEFAULT_NAME;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(input)) {
            ContainerHelper.loadAllItems(input, this.items);
        }

        this.miningProgress = input.getIntOr("MiningProgress", 0);
        this.miningRequired = Math.max(1, input.getIntOr("MiningRequired", 3));

        // Prevent invalid progress values.
        this.miningProgress = Math.clamp(this.miningProgress, 0, this.miningRequired);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!this.trySaveLootTable(output)) {
            ContainerHelper.saveAllItems(output, this.items);
        }

        output.putInt("MiningProgress", this.miningProgress);
        output.putInt("MiningRequired", this.miningRequired);
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new MinerMenu(containerId, inventory, this);
    }

    @Override
    public int getContainerSize() {
        return 9;
    }

    public void mineFrontBlock(ServerLevel level) {
        if (this.isRemoved()) {
            return;
        }

        Direction facing = this.getBlockState().getValue(MinerBlock.FACING);
        BlockPos targetPos = this.getBlockPos().relative(facing);

        BlockState targetState = level.getBlockState(targetPos);

        if (targetState.isAir() || targetState.getDestroySpeed(level, targetPos) < 0 || targetState.getBlock() == Blocks.BEDROCK) {
            resetMiningProgress();
            return;
        }
        // Reset progress if the target block changes.
        if (this.breakingPos != null && !this.breakingPos.equals(targetPos)) {
            clearBreakingOverlay(level);
            this.miningProgress = 0;
        }

        this.breakingPos = targetPos.immutable();
        this.miningRequired = getRequiredPulses(targetState, level, targetPos);

        this.miningProgress++;

        int stage = Math.min(9, (int) ((float) this.miningProgress / this.miningRequired * 10));

        level.destroyBlockProgress(this.getBlockPos().hashCode(), targetPos, stage);

        level.playSound(
                null,
                targetPos,
                targetState.getSoundType().getHitSound(),
                SoundSource.BLOCKS,
                0.8F,
                1.0F
        );

        if (this.miningProgress < this.miningRequired) {
            this.setChanged();
            return;
        }

        // get loot
        var drops = Block.getDrops(
                targetState,
                level,
                targetPos,
                level.getBlockEntity(targetPos)
        );

        clearBreakingOverlay(level);

        // destroy block
        level.destroyBlock(targetPos, false);

        // add loot to inventory
        for (ItemStack drop : drops) {
            ItemStack remaining = drop.copy();

            for (int slot = 0; slot < this.getContainerSize() && !remaining.isEmpty(); slot++) {
                ItemStack current = this.getItem(slot);

                if (current.isEmpty()) {
                    this.setItem(slot, remaining.copy());
                    remaining.setCount(0);
                } else if (ItemStack.isSameItemSameComponents(current, remaining)) {
                    int capacity = Math.min(
                            this.getMaxStackSize(),
                            current.getMaxStackSize()
                    ) - current.getCount();

                    if (capacity > 0) {
                        int moved = Math.min(capacity, remaining.getCount());
                        current.grow(moved);
                        remaining.shrink(moved);
                    }
                }
            }

            // if inventory is full drop loot
            if (!remaining.isEmpty()) {
                Block.popResource(level, this.getBlockPos().relative(facing), remaining);
            }
        }

        resetMiningProgress();
        this.setChanged();
    }

    private int getRequiredPulses(BlockState state, Level level, BlockPos pos) {
        float hardness = state.getDestroySpeed(level, pos);

        if (hardness < 0) {
            return Integer.MAX_VALUE; // Unbreakable
        }

        return Math.max(1, (int) Math.ceil(hardness * 3));
    }

    private void clearBreakingOverlay(ServerLevel level) {
        if (this.breakingPos != null) {
            level.destroyBlockProgress(
                    this.getBlockPos().hashCode(),
                    this.breakingPos,
                    -1
            );

            this.breakingPos = null;
        }
    }

    public int getMiningProgress() {
        return miningProgress;
    }

    public int getMiningRequired() {
        return miningRequired;
    }

    public void resetMiningProgress() {
        this.miningProgress = 0;
        this.miningRequired = 3;
        this.setChanged();
    }
}
