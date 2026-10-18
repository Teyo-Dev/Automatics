package bet.automatics;

import bet.automatics.datagen.ModBlockTagsProvider;
import bet.automatics.datagen.ModModelProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = Automatics.MOD_ID)
public class AutomaticsDataGen {
    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();

        var lookupProvider = event.getWorldLookupProvider();

        generator.addProvider(true, new ModModelProvider(packOutput));
        generator.addProvider(true, new ModBlockTagsProvider(packOutput, lookupProvider));
    }
}
