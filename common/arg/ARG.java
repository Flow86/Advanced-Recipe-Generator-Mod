/** 
 * Copyright (C) 2013 Flow86
 * 
 * AdvancedRecipeGenerator is open-source.
 *
 * It is distributed under the terms of my Open Source License. 
 * It grants rights to read, modify, compile or run the code. 
 * It does *NOT* grant the right to redistribute this software or its 
 * modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package arg;

import java.nio.ByteBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeFireworks;
import net.minecraft.item.crafting.RecipesArmorDyes;
import net.minecraft.item.crafting.RecipesMapCloning;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = "Advanced-Recipe-Generator", name = "Advanced-Recipe-Generator", version = "@ARG_VERSION@")
public class ARG {
	public static final String VERSION = "@ARG_VERSION@";

	@Instance("Advanced-Recipe-Generator")
	public static ARG instance;

	@Init
	public void load(FMLInitializationEvent evt) {
		MinecraftForge.EVENT_BUS.register(this);
	}

	public static int[] mapLoaded = { 0, 0 };
	public static boolean mapGenerated = false;

	@ForgeSubscribe(priority = EventPriority.LOWEST)
	@SideOnly(Side.CLIENT)
	public void createRecipeImages(TextureStitchEvent.Post evt) {

		if (evt.map == Minecraft.getMinecraft().renderEngine.textureMapBlocks)
			mapLoaded[0]++;
		if (evt.map == Minecraft.getMinecraft().renderEngine.textureMapItems)
			mapLoaded[1]++;

		System.out.println("mapLoaded: " + mapLoaded[0] + ", " + mapLoaded[1] + " => " + mapGenerated);

		if (mapLoaded[0] > 0 && mapLoaded[0] == mapLoaded[1]) {
			if (mapGenerated)
				return;
			mapGenerated = true;

			System.out.println("Generating Recipes!");

			int position = evt.map.getTexture().getTextureData().position();
			ByteBuffer buff = evt.map.getTexture().getTextureData().duplicate();

			evt.map.getTexture().uploadTexture();

			for (Object orecipe : CraftingManager.getInstance().getRecipeList()) {
				IRecipe irecipe = (IRecipe) orecipe;

				if ((irecipe instanceof RecipesArmorDyes) || (irecipe instanceof RecipeFireworks) || (irecipe instanceof RecipesMapCloning))
					continue;

				if (irecipe.getRecipeOutput() == null) {
					System.out.println("Skip recipe without output: " + irecipe.getClass().getSimpleName());
					continue;
				}

				RenderRecipe render = new RenderRecipe(irecipe.getRecipeOutput().getDisplayName());

				ItemStack[] recipeInput = null;
				try {
					recipeInput = RecipeHelper.getRecipeArray(irecipe);
					if (recipeInput == null)
						continue;
				} catch (Exception e) {
					e.printStackTrace();
				}

				for (int i = 0; i < recipeInput.length - 1; ++i)
					render.getCraftingContainer().craftMatrix.setInventorySlotContents(i, recipeInput[i + 1]);

				render.getCraftingContainer().craftResult.setInventorySlotContents(0, recipeInput[0]);
				render.draw();

			}

			evt.map.getTexture().getTextureData().clear();
			evt.map.getTexture().getTextureData().put(buff);
			evt.map.getTexture().getTextureData().position(position);
		}
	}
}
