package net.misemise.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.misemise.item.ModItems;

import java.util.concurrent.CompletableFuture;
 
public class StackBoxRecipeProvider extends FabricRecipeProvider {

    public StackBoxRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }
 
    @Override
    protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup wrapperLookup, RecipeExporter recipeExporter) {
        return new RecipeGenerator(wrapperLookup, recipeExporter) {
            @Override
            public void generate() {
                // StackBox Recipe: 8 Chests surrounding empty center
                // Recipe pattern:
                //   C C C
                //   C   C
                //   C C C
                // C = Chest
                createShaped(RecipeCategory.TOOLS, ModItems.STACK_BOX)
                        .pattern("CCC")
                        .pattern("C C")
                        .pattern("CCC")
                        .input('C', Items.CHEST)
                        .criterion(hasItem(Items.CHEST), conditionsFromItem(Items.CHEST))
                        .offerTo(exporter);
            }
        };
    }

    @Override
    public String getName() {
        return "StackBox Recipes";
    }
}
