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

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import buildcraft.core.utils.StringUtil;

class RenderRecipe extends GuiContainer {
	public String name;
	private final HashMap<String, ItemStack> incredientList = new HashMap<String, ItemStack>();

	public RenderRecipe(String name) {
		super(new ContainerCraft());
		this.name = name;
		mc = Minecraft.getMinecraft();
		fontRenderer = mc.fontRenderer;

		if (TileEntityRenderer.instance.renderEngine == null) {
			TileEntityRenderer.instance.renderEngine = mc.renderEngine;
			Iterator iterator = TileEntityRenderer.instance.specialRendererMap.values().iterator();

			while (iterator.hasNext()) {
				TileEntitySpecialRenderer tileentityspecialrenderer = (TileEntitySpecialRenderer) iterator.next();
				tileentityspecialrenderer.setTileEntityRenderer(TileEntityRenderer.instance);
			}
		}

		xSize = 176;
		ySize = 155;

		width = xSize * 3;
		height = ySize * 3;
	}

	public ContainerCraft getCraftingContainer() {
		return (ContainerCraft) this.inventorySlots;
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();
		int k = this.guiLeft;
		int l = this.guiTop;
		this.drawGuiContainerBackgroundLayer(par3, par1, par2);
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		RenderHelper.disableStandardItemLighting();
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		RenderHelper.enableGUIStandardItemLighting();
		GL11.glPushMatrix();
		GL11.glTranslatef(k, l, 0.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		short short1 = 240;
		short short2 = 240;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, short1 / 1.0F, short2 / 1.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int i1;

		// crafting result
		drawSlotInventory((Slot) inventorySlots.inventorySlots.get(0));

		incredientList.clear();

		for (int j1 = 1; j1 < inventorySlots.inventorySlots.size(); ++j1) {
			Slot slot = (Slot) inventorySlots.inventorySlots.get(j1);
			drawSlotInventory(slot);
		}

		this.drawGuiContainerForegroundLayer(par1, par2);

		GL11.glPopMatrix();

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		RenderHelper.enableStandardItemLighting();
	}

	/**
	 * Draws an itemstack at a specific position
	 */
	protected void drawItemStackAtPosition(ItemStack itemstack, int x, int y) {
		if (itemstack == null)
			return;

		this.zLevel = 100.0F;
		itemRenderer.zLevel = 100.0F;

		String s = null;
		if (itemstack.stackSize > 1)
			s = "" + itemstack.stackSize;

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		itemRenderer.renderItemAndEffectIntoGUI(fontRenderer, mc.renderEngine, itemstack, x, y);
		itemRenderer.renderItemOverlayIntoGUI(fontRenderer, mc.renderEngine, itemstack, x, y, s);

		itemRenderer.zLevel = 0.0F;
		this.zLevel = 0.0F;
	}

	/**
	 * Draws an inventory slot
	 */
	@Override
	protected void drawSlotInventory(Slot slot) {
		ItemStack itemstack = slot.getStack();

		if (itemstack == null)
			return;

		itemstack.stackSize = 1;

		if (!incredientList.containsKey(itemstack.getDisplayName()))
			incredientList.put(itemstack.getDisplayName(), itemstack);

		drawItemStackAtPosition(itemstack, slot.xDisplayPosition, slot.yDisplayPosition);
	}

	protected int getCenteredOffset(String string, int xWidth) {
		return (xWidth - fontRenderer.getStringWidth(string)) / 2;
	}

	@Override
	public void drawGuiContainerForegroundLayer(int i, int j) {
		super.drawGuiContainerForegroundLayer(i, j);

		String title = StringUtil.localize(getCraftingContainer().craftResult.getStackInSlot(0).getDisplayName());
		fontRenderer.drawString(title, getCenteredOffset(title, xSize), 5, 0x404040);

		float scale = 3 / 4F;

		// since we scale by 1/2, we have to start on *2
		int[] baseX = { (int) (10 * (1F / scale)), (int) (100 * (1F / scale)) };
		int baseY = (int) (76 * (1F / scale));

		GL11.glScalef(scale, scale, 1.0F);

		int item = 0;
		int y = baseY;
		for (Entry<String, ItemStack> entry : incredientList.entrySet()) {

			int x = baseX[incredientList.size() < 3 ? 0 : (item < incredientList.size() / 2 ? 0 : 1)];
			if (incredientList.size() < 3 ? false : (item == incredientList.size() / 2))
				y = baseY;

			fontRenderer.drawString(fontRenderer.trimStringToWidth(entry.getKey(), (int) ((100 - 10 - 18) * (1F / scale))), x + 18, y + 4, 0x404040);
			drawItemStackAtPosition(entry.getValue(), x, y);
			y += 18;
			item++;
		}
		GL11.glScalef(1F / scale, 1F / scale, 1.0F);
	}

	@Override
	public void drawBackground(int par1) {

	}

	public void draw() {

		File dir = new File(Minecraft.getMinecraftDir(), "recipes");
		if (!dir.exists() && !dir.mkdirs()) {
			throw new RuntimeException("The recipes directory could not be created: " + dir);
		}

		File file = new File(Minecraft.getMinecraftDir(), "recipes/" + name + ".png");

		if (file.exists())
			return;

		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glPushClientAttrib(GL11.GL_ALL_CLIENT_ATTRIB_BITS);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glViewport(0, 0, width, height);
		GL11.glOrtho(0.0D, xSize, ySize, 0.0D, 1000.0D, 3000.0D);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
		GL11.glLineWidth(1.0F);

		GL11.glEnable(GL11.GL_COLOR_MATERIAL);

		try {
			drawScreen(0, 0, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		int[] pixels = new int[width * height];
		int bindex;

		ByteBuffer fb = ByteBuffer.allocateDirect(width * height * 3);

		GL11.glReadPixels(0, 0, width, height, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, fb);
		GL11.glPopMatrix();
		GL11.glPopAttrib();
		GL11.glPopClientAttrib();
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		try {
			Display.swapBuffers();
		} catch (LWJGLException e1) {
			e1.printStackTrace();
		}

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int i = (x + (width * y)) * 3;
				int r = fb.get(i) & 0xFF;
				int g = fb.get(i + 1) & 0xFF;
				int b = fb.get(i + 2) & 0xFF;
				image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
			}
		}

		try {
			ImageIO.write(image, "png", file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		this.mc.renderEngine.bindTexture("/arg/crafting.png");
		drawTexturedModalRect(0, 0, 0, 0, xSize, ySize);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}
}