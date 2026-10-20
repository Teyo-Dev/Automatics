package bet.automatics.level.block.entity;

import bet.automatics.Automatics;
import bet.automatics.level.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Automatics.MOD_ID);

    public static final Supplier<BlockEntityType<MinerBlockEntity>> MINER =
            BLOCK_ENTITY_TYPES.register("miner", () -> new BlockEntityType<>(
                    MinerBlockEntity::new,
                    false,
                    ModBlocks.MINER.get())
            );

    public static final Supplier<BlockEntityType<StockCrateBlockEntity>> STOCK_CRATE =
            BLOCK_ENTITY_TYPES.register("stock_crate", () -> new BlockEntityType<>(
                    StockCrateBlockEntity::new,
                    false,
                    ModBlocks.STOCK_CRATE.get())
            );
}
