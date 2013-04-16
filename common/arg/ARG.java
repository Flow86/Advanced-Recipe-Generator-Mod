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
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeFireworks;
import net.minecraft.item.crafting.RecipesArmorDyes;
import net.minecraft.item.crafting.RecipesMapCloning;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
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

	public static boolean[] mapLoaded = { false, false };
	public static boolean mapGenerated = false;

	@ForgeSubscribe(priority = EventPriority.LOWEST)
	@SideOnly(Side.CLIENT)
	public void createRecipeImages(TextureStitchEvent.Post evt) {

		if (evt.map == Minecraft.getMinecraft().renderEngine.textureMapBlocks)
			mapLoaded[0] = true;
		if (evt.map == Minecraft.getMinecraft().renderEngine.textureMapItems)
			mapLoaded[1] = true;

		if (mapLoaded[0] && mapLoaded[1]) {
			if (mapGenerated)
				return;
			mapGenerated = true;

			int position = evt.map.getTexture().getTextureData().position();
			ByteBuffer buff = evt.map.getTexture().getTextureData().duplicate();
			evt.map.getTexture().uploadTexture();

			Block.chest.registerIcons(Minecraft.getMinecraft().renderEngine.textureMapBlocks);

			for (Object orecipe : CraftingManager.getInstance().getRecipeList()) {
				IRecipe irecipe = (IRecipe) orecipe;

				if ((irecipe instanceof RecipesArmorDyes) || (irecipe instanceof RecipeFireworks) || (irecipe instanceof RecipesMapCloning))
					continue;

				if (irecipe.getRecipeOutput() == null) {
					System.out.println("Skip recipe without output: " + irecipe.getClass().getSimpleName());
					continue;
				}

				Render render = new Render(irecipe.getRecipeOutput().getDisplayName());

				ItemStack[] recipeInput = null;
				try {
					recipeInput = getRecipeArray(irecipe);
					if (recipeInput == null)
						continue;
				} catch (Exception e) {
					e.printStackTrace();
				}

				for (int i = 0; i < irecipe.getRecipeSize(); ++i)
					render.getCraftingContainer().craftMatrix.setInventorySlotContents(i, recipeInput[i + 1]);

				render.getCraftingContainer().craftResult.setInventorySlotContents(0, recipeInput[0]);
				render.draw();

			}

			evt.map.getTexture().getTextureData().clear();
			evt.map.getTexture().getTextureData().put(buff);
			evt.map.getTexture().getTextureData().position(position);
		}
	}

	private ItemStack[] getRecipeArray(IRecipe irecipe) throws IllegalArgumentException, SecurityException, NoSuchFieldException {
		if (irecipe.getRecipeSize() > 9) {
			return null;
		}
		ItemStack[] recipeArray = new ItemStack[10];
		recipeArray[0] = irecipe.getRecipeOutput();

		if ((irecipe instanceof ShapedRecipes)) {
			ShapedRecipes shapedRecipe = (ShapedRecipes) irecipe;

			ItemStack[] recipeInput = shapedRecipe.recipeItems;

			for (int slot = 0; slot < recipeInput.length; slot++) {
				ItemStack item = recipeInput[slot];

				if ((item != null) && ((item.getItemDamage() == -1) || (item.getItemDamage() == 32767))) {
					item = item.copy();
					item.setItemDamage(0);
				}

				int x = slot % shapedRecipe.recipeWidth;
				int y = slot / shapedRecipe.recipeWidth;
				recipeArray[(x + y * shapedRecipe.recipeHeight) + 1] = item;
			}

		} else if ((irecipe instanceof ShapelessRecipes)) {
			ShapelessRecipes shapelessRecipe = (ShapelessRecipes) irecipe;

			List recipeInput = shapelessRecipe.recipeItems;

			for (int slot = 0; slot < recipeInput.size(); slot++) {
				ItemStack item = (ItemStack) recipeInput.get(slot);

				if ((item != null) && (item.getItemDamage() == -1)) {
					item = item.copy();
					item.setItemDamage(0);
				}

				recipeArray[slot + 1] = item;
			}

		} else if ((irecipe instanceof ShapedOreRecipe)) {
			ShapedOreRecipe shapedOreRecipe = (ShapedOreRecipe) irecipe;

			Object[] recipeInput = shapedOreRecipe.getInput();

			for (int slot = 0; slot < recipeInput.length; slot++) {
				Object recipeSlot = recipeInput[slot];

				if (recipeSlot == null)
					continue;

				if (recipeSlot instanceof ArrayList) {
					ArrayList list = (ArrayList) recipeSlot;
					if (list.size() > 1) {
						System.out.println("ERROR: Slot-Array " + (slot + 1) + " has more then one item: " + list);
						return null;
					}
					recipeSlot = list.get(0);
				}

				if (recipeSlot instanceof ItemStack) {
					ItemStack item = (ItemStack) recipeSlot;

					if ((item != null) && (item.getItemDamage() == -1)) {
						item = item.copy();
						item.setItemDamage(0);
					}

					recipeArray[slot + 1] = item;

				} else {
					System.out.println("Slot " + (slot + 1) + " is type " + recipeSlot.getClass().getSimpleName());
					return null;
				}
			}

		} else if ((irecipe instanceof ShapelessOreRecipe)) {
			ShapelessOreRecipe shapelessOreRecipe = (ShapelessOreRecipe) irecipe;

			List recipeInput = shapelessOreRecipe.getInput();

			for (int slot = 0; slot < recipeInput.size(); slot++) {
				Object recipeSlot = recipeInput.get(slot);

				if (recipeSlot == null)
					continue;

				if (recipeSlot instanceof ArrayList) {
					ArrayList list = (ArrayList) recipeSlot;
					if (list.size() > 1) {
						System.out.println("ERROR: Slot-Array " + (slot + 1) + " has more then one item: " + list);
						return null;
					}
					recipeSlot = list.get(0);
				}

				if (recipeSlot instanceof ItemStack) {
					ItemStack item = (ItemStack) recipeSlot;

					if ((item != null) && (item.getItemDamage() == -1)) {
						item = item.copy();
						item.setItemDamage(0);
					}

					recipeArray[slot + 1] = item;

				} else {
					System.out.println("Slot " + (slot + 1) + " is type " + recipeSlot.getClass().getSimpleName());
					return null;
				}
			}

		} else {
			System.out.println("Unknown Type: " + irecipe.getClass().getSimpleName());
			return null;
		}

		return recipeArray;
	}
}
