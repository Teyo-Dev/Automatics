package bet.automatics.level.block.custom;

import bet.automatics.level.block.entity.MinerBlockEntity;
import bet.automatics.level.block.entity.StockCrateBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class StockCrateBlock extends BaseEntityBlock {
    //btw uses some of the code is very similar DecoratedPotBlockEntity
    public StockCrateBlock(Properties properties) {
        super(properties);
    }

    // show count of items
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof StockCrateBlockEntity crate) {
            player.sendOverlayMessage(Component.literal(String.valueOf(crate.getStoredCount())));
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof StockCrateBlockEntity crate) {
            if (level.isClientSide()) {return InteractionResult.SUCCESS;}

            if (!itemStack.isEmpty() && crate.insertOne(itemStack)) {
                player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));

                itemStack.consumeAndReturn(1, player);

                float pitchBend = (float) crate.getStoredCount() / 3000.0F;

                level.playSound(null, pos, SoundEvents.DECORATED_POT_INSERT, SoundSource.BLOCKS, 1.0F, 0.7F + 0.5F * pitchBend);

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.DUST_PLUME, pos.getX() + 0.2, pos.getY() + 1.2, pos.getZ() + 0.2, 7, 0.0, 0.0, 0.0, 0.0);
                }

                crate.setChanged();
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.TRY_WITH_EMPTY_HAND;
            }
        } else {
            return InteractionResult.PASS;
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) {
        return new StockCrateBlockEntity(worldPosition, blockState);
    }
}
