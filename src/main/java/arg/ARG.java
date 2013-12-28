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

import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
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

import com.google.common.collect.Maps;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = "Advanced-Recipe-Generator", name = "Advanced-Recipe-Generator", version = "@ARG_VERSION@")
public class ARG
{
	public static final String VERSION = "@ARG_VERSION@";

	@Instance("Advanced-Recipe-Generator")
	public static ARG instance;

	@EventHandler
	public void load(FMLInitializationEvent evt)
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	public static int[] mapLoaded =
	{ 0, 0 };
	public static boolean mapGenerated = false;

	@ForgeSubscribe(priority = EventPriority.LOWEST)
	@SideOnly(Side.CLIENT)
	public void createRecipeImages(TextureStitchEvent.Post evt)
	{

		mapLoaded[evt.map.textureType]++;

		System.out.println("mapLoaded: " + mapLoaded[0] + ", " + mapLoaded[1] + " => " + mapGenerated);

		if (mapLoaded[0] > 0 && mapLoaded[0] == mapLoaded[1])
		{
			if (mapGenerated)
				return;
			mapGenerated = true;

			System.out.println("Generating Recipes!");

			TextureManager tm = Minecraft.getMinecraft().getTextureManager();

			// save since we get a ConcurrentModificationException in TextureManager.func_110549_a otherwise

			Map mapTextureObjects = ObfuscationReflectionHelper.getPrivateValue(TextureManager.class, tm, "mapTextureObjects", "field_110585_a");

			Map new_mapTextureObjects = Maps.newHashMap();
			new_mapTextureObjects.putAll(mapTextureObjects);
			ObfuscationReflectionHelper.setPrivateValue(TextureManager.class, tm, new_mapTextureObjects, "mapTextureObjects", "field_110585_a");

			for (Object orecipe : CraftingManager.getInstance().getRecipeList())
			{
				IRecipe irecipe = (IRecipe) orecipe;

				if ((irecipe instanceof RecipesArmorDyes) || (irecipe instanceof RecipeFireworks) || (irecipe instanceof RecipesMapCloning))
					continue;

				if (irecipe.getRecipeOutput() == null)
				{
					System.out.println("Skip recipe without output: " + irecipe.getClass().getSimpleName());
					continue;
				}

				RenderRecipe render = new RenderRecipe(irecipe.getRecipeOutput().getDisplayName());

				ItemStack[] recipeInput = null;
				try
				{
					recipeInput = RecipeHelper.getRecipeArray(irecipe);
					if (recipeInput == null)
						continue;
				} catch (Exception e)
				{
					e.printStackTrace();
				}

				try
				{
					for (int i = 0; i < recipeInput.length - 1; ++i)
						render.getCraftingContainer().craftMatrix.setInventorySlotContents(i, recipeInput[i + 1]);

					render.getCraftingContainer().craftResult.setInventorySlotContents(0, recipeInput[0]);
					render.draw();
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}

			// restore map since we get a ConcurrentModificationException in TextureManager.func_110549_a otherwise
			ObfuscationReflectionHelper.setPrivateValue(TextureManager.class, tm, mapTextureObjects, "mapTextureObjects", "field_110585_a");

			System.out.println("Finished Generation of Recipes in " + Minecraft.getMinecraft().mcDataDir + "/recipes/");
		}
	}
}
