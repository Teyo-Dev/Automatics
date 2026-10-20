package bet.automatics.level.block;

import bet.automatics.Automatics;
import bet.automatics.level.block.custom.MinerBlock;
import bet.automatics.level.block.custom.StockCrateBlock;
import bet.automatics.level.block.custom.TickerBlock;
import bet.automatics.level.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public class ModBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Automatics.MOD_ID);

    // All blocks
    public static final DeferredBlock<Block> MINER = registerBlock(
            "miner",
            properties -> new MinerBlock(properties
                    .mapColor(MapColor.COLOR_GRAY)
                    .noOcclusion()
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(2.0F, 6.0F))
    );

    public static final DeferredBlock<Block> TICKER = registerBlock(
            "ticker",
            properties -> new TickerBlock(properties
                    .mapColor(MapColor.COLOR_GRAY)
                    .noOcclusion()
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(2.0F, 6.0F))
    );

    public static final DeferredBlock<Block> STOCK_CRATE = registerBlock(
            "stock_crate",
            properties -> new StockCrateBlock(properties
                    .mapColor(MapColor.RAW_IRON)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2.5F)
                    .sound(SoundType.IRON).ignitedByLava()));


    private static <T extends Block> DeferredBlock <T> registerBlock(String name, Function<BlockBehaviour.Properties, T> function) {
        DeferredBlock<T> toReturn = BLOCKS.registerBlock(name, function);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.registerItem(name, properties -> new BlockItem(block.get(), properties.useBlockDescriptionPrefix()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}


