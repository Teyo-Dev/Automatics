package bet.automatics.datagen;

import bet.automatics.Automatics;
import bet.automatics.level.block.ModBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.data.PackOutput;

public class ModModelProvider extends ModelProvider {
    public ModModelProvider(PackOutput output) {
        super(output, Automatics.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        // Items
        //itemModels.generateFlatItem(ModItems.ITEM.get(), ModelTemplates.FLAT_ITEM);

        // Blocks
        blockModels.createTrivialBlock(ModBlocks.BREAKER.get(), TexturedModel.COLUMN_ALT);
    }
}
