package bet.automatics;

import bet.automatics.level.block.ModBlocks;
import bet.automatics.level.block.entity.ModBlockEntityTypes;
import bet.automatics.level.item.ModCreativeModeTabs;
import bet.automatics.level.item.ModItems;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Automatics.MOD_ID)
public class Automatics {
    public static final String MOD_ID = "automatics";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Automatics(IEventBus eventBus, ModContainer modContainer) {
        ModBlocks.register(eventBus);
        ModItems.register(eventBus);
        ModCreativeModeTabs.register(eventBus);
        ModBlockEntityTypes.BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
