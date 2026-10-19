package bet.automatics.level.block.custom;

import bet.automatics.level.block.state.properties.ModBlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;
import org.jspecify.annotations.Nullable;

public class TickerBlock extends DiodeBlock {
    public static final BooleanProperty ACTIVATED = ModBlockStateProperties.ACTIVATED;
    public static final IntegerProperty DELAY = BlockStateProperties.DELAY;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final VoxelShape SHAPE = Block.column(16.0, 0.0, 2.0);

    public TickerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(DELAY, 1).setValue(ACTIVATED, false).setValue(POWERED, false)
        );
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!player.getAbilities().mayBuild) {
            return InteractionResult.PASS;
        }

        level.setBlockAndUpdate(pos, state.cycle(DELAY));
        return InteractionResult.SUCCESS;
    }

    @Override
    protected int getDelay(BlockState state) {
        return state.getValue(DELAY) * 2;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (directionToNeighbour == Direction.DOWN && !this.canSurviveOn(level, neighbourPos, neighbourState)) {
            return Blocks.AIR.defaultBlockState();
        } else {
            return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
        }
    }

    @Override
    protected void checkTickOnNeighbor(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;

        // update block when powered
        if (this.shouldTurnOn(level, pos, state)) {
            // update activated to be true if not already
            if (!state.getValue(ACTIVATED))
                level.setBlock(pos, state.setValue(ACTIVATED, true), 3);
            // if not already ticking then start cycle
            if (!level.getBlockTicks().hasScheduledTick(pos, this))
                level.scheduleTick(pos, this, this.getDelay(state), TickPriority.NORMAL);

        } else {
            BlockState off = state.setValue(ACTIVATED, false).setValue(POWERED, false);
            if (off != state) {
                level.setBlock(pos, off, UPDATE_NEIGHBORS | UPDATE_CLIENTS);
                this.updateNeighborsInFront(level, pos, off);
            }
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {

        // update block if self isn't powered and return
        if (!this.shouldTurnOn(level, pos, state)) {
            BlockState off = state.setValue(ACTIVATED, false).setValue(POWERED, false);
            level.setBlock(pos, off, UPDATE_NEIGHBORS | UPDATE_CLIENTS);
            this.updateNeighborsInFront(level, pos, off);

            return;
        }

        BlockState next = state.setValue(ACTIVATED, true).setValue(POWERED, !state.getValue(POWERED));
        level.setBlock(pos, next, UPDATE_NEIGHBORS | UPDATE_CLIENTS);
        this.updateNeighborsInFront(level, pos, next);
        level.scheduleTick(pos, this, next.getValue(POWERED) ? 1 : Math.max(1, getDelay(next) - 1), TickPriority.NORMAL);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (!level.isClientSide()) this.checkTickOnNeighbor(level, pos, state);
    }

    @Override
    protected boolean shouldRedstoneWireConnectTo(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        Direction repeaterDirection = state.getValue(FACING);
        return repeaterDirection == direction || repeaterDirection.getOpposite() == direction;
    }

    @Override
    protected boolean sideInputDiodesOnly() {
        return true;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED)) {
            Direction direction = state.getValue(FACING);
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.2;
            double y = pos.getY() + 0.4 + (random.nextDouble() - 0.5) * 0.2;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.2;
            float offset = -5.0F;
            if (random.nextBoolean()) {
                offset = state.getValue(DELAY) * 2 - 1;
            }

            offset /= 16.0F;
            double xo = offset * direction.getStepX();
            double zo = offset * direction.getStepZ();
            level.addParticle(DustParticleOptions.REDSTONE, x + xo, y, z + zo, 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, DELAY, ACTIVATED, POWERED);
    }
}
