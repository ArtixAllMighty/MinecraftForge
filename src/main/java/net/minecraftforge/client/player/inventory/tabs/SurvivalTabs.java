package net.minecraftforge.client.player.inventory.tabs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * @author Subaraki
 * 
 *         class named after CreativeTabs, but for Survival
 */
public abstract class SurvivalTabs
{

	private static ListMultimap<Class<? extends GuiContainer>, SurvivalTabs> tabRegistry = ArrayListMultimap.create();
	private static HashMap<Class<? extends GuiContainer>, Integer> tabIndexForContainer = new HashMap<Class<? extends GuiContainer>, Integer>();

	private ItemStack iconStack = null;
	private ResourceLocation iconResLoc = null;
	private int tabIndex = 0;
	private String name = null;

	public static final SurvivalTabs VANILLA_INVENTORY_TAB = new SurvivalTabs("inventory", new ItemStack(Blocks.CHEST), GuiInventory.class) {
		@Override
		public void onTabClicked(EntityPlayerSP player)
		{
			Minecraft.getMinecraft().displayGuiScreen(new GuiInventory(player));
		}

		@Override
		public Class<? extends GuiContainer> getTabContainer()
		{
			return GuiInventory.class;
		}
	};

	public SurvivalTabs(String name, ItemStack icon, Class<? extends GuiContainer> parentContainer)
	{

		this.name = name;
		this.tabIndex = getNextID(parentContainer);
		this.iconStack = icon;
		tabRegistry.put(parentContainer, this);
	}

	public SurvivalTabs(String name, ResourceLocation icon, Class<? extends GuiContainer> parentContainer)
	{

		this.name = name;
		this.tabIndex = getNextID(parentContainer);
		this.iconResLoc = icon;
		tabRegistry.put(parentContainer, this);
	}

	public void draw(GuiContainer guiContainer, boolean isSelectedtab, int guiLeft, int guiTop, int xSize, int ySize, RenderItem itemRender,
			FontRenderer fontRendererObj)
	{

		boolean isFirstRow = isTopRow();
		int column = getColumn();
		int texX = column * 28;
		int texY = 0;
		int x = guiLeft + 28 * column;
		int y = guiTop;

		if (isSelectedtab)
			texY += 32;

		if (column == 5) // set the last tab to the extreme right of the gui
			x = guiLeft + xSize - 28;

		else if (column > 0)
			x += column;

		if (isFirstRow)
			y = y - 28;
		else
		{
			texY += 64;
			y = y + (ySize - 4);
		}
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.disableLighting();
		GlStateManager.color(1F, 1F, 1F); // Forge: Reset color in case Items change it.
		GlStateManager.enableBlend(); // Forge: Make sure blend is enabled else tabs show a white border.
		guiContainer.drawTexturedModalRect(x, y, texX, texY, 28, 32);
		x = x + 6;
		y = y + 8 + (isFirstRow ? 1 : -1);
		GlStateManager.enableLighting();
		if (getIconStack() != null)
		{
			ItemStack itemstack = getIconStack();
			itemRender.renderItemAndEffectIntoGUI(itemstack, x, y);
			itemRender.renderItemOverlays(fontRendererObj, itemstack, x, y);
		}
		if(getIconResLoc() != null){
			Minecraft.getMinecraft().getTextureManager().bindTexture(getIconResLoc());
			Gui.drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, 16, 16, 16f, 16f);
		}
		GlStateManager.disableLighting();
	}

	private int getNextID(Class<? extends GuiContainer> guiContainer)
	{
		if (tabIndexForContainer.containsKey(guiContainer))
		{
			int newIndex = (tabIndexForContainer.get(guiContainer) + 1);
			tabIndexForContainer.put(guiContainer, newIndex);
			return newIndex;
		}

		tabIndexForContainer.put(guiContainer, 0);
		return 0;
	}

	/** returns the index relative to the gui */
	public int getTabIndex()
	{
		return tabIndex;
	}

	/** wether this tab is on the top row or not */
	public boolean isTopRow()
	{
		return getTabIndex() > 11 ? ((getTabIndex() - 12) % 12) < 6 : getTabIndex() < 6;
	}

	/** returns index % 6 to determine column */
	public int getColumn()
	{
		return getTabIndex() > 11 ? ((getTabIndex() - 12) % 12) % 6 : getTabIndex() % 6;
	}

	/**
	 * @return wether this tab's parent gui is the current gui
	 */
	public boolean isActiveTab(Class<? extends GuiContainer> guiContainer)
	{
		return guiContainer != null && guiContainer.equals(getTabContainer());
	}

	/**
	 * @return the tabindex / 12
	 */
	public int getTabPage()
	{
		return tabIndex / 12;
	}

	/**
	 * @return the localized name for this tab
	 */
	public String getLocalizedName()
	{
		return "guiGroup." + name;
	}

	public ResourceLocation getIconResLoc()
	{
		return iconResLoc;
	}

	public ItemStack getIconStack()
	{
		return iconStack;
	}

	/**
	 * @return a list of the appended tabs for the given guicontainer
	 */
	public static List<SurvivalTabs> getTabsForContainer(Class<? extends GuiContainer> guiContainer)
	{
		return tabRegistry.containsKey(guiContainer) ? tabRegistry.get(guiContainer) : new ArrayList<SurvivalTabs>();
	}

	/**
	 * Appends a list of tabs from a parent gui to another gui to create the illusion of continuity
	 * 
	 * @param original
	 *            : the tabs to copy from, if any registered
	 * @param guiContainer
	 *            : the GuiContainer the list will be added too.
	 */
	public static void createContinuity(Class<? extends GuiContainer> original, Class<? extends GuiContainer> guiContainer)
	{

		if (tabRegistry.containsKey(original))
		{
			for (SurvivalTabs tab : tabRegistry.get(original))
				tabRegistry.put(guiContainer, tab);
		}
	}

	/**
	 * process interaction when the tab is clicked. should be used to send a packet to the server and open a gui
	 */
	public abstract void onTabClicked(EntityPlayerSP player);

	/**
	 * returns the GuiContainer this tab should be linked too. not affiliated with opening the gui. can be null if no gui should lead to this tab. mostly used to know when a tab is
	 * on an affiliated gui, and draw the tab texture in of the others.
	 */
	public abstract Class<? extends GuiContainer> getTabContainer();
}
