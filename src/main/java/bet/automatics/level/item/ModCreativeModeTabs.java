package bet.automatics.level.item;

import bet.automatics.Automatics;
import bet.automatics.level.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Automatics.MOD_ID);


    public static final Supplier<CreativeModeTab> AUTOMATICS_ITEMS = CREATIVE_MODE_TABS.register("automatics_items",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.MINER.get()))
                    .title(Component.translatable("itemGroup.automatics_items"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModBlocks.MINER);
                        output.accept(ModBlocks.TICKER);
                    })
                    .build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
