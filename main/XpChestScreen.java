package com.totis.totismod.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.totis.totismod.Constants;
import com.totis.totismod.server.blockentity.menu.PiggyBankMenu;
import com.totis.totismod.server.blockentity.menu.XpChestMenu;
import com.totis.totismod.server.network.ModMessages;
import com.totis.totismod.server.network.packets.PacketGivePlayerXpSync2S;
import com.totis.totismod.server.network.packets.PacketXPSync2S;
import com.totis.totismod.utils.TotisJavaUtils;
import com.totis.totismod.utils.TotisMathUtils;
import com.totis.totismod.utils.TotisPlayerUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.SliderButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.widget.ForgeSlider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerXpEvent;

@SuppressWarnings("all")
public class XpChestScreen extends AbstractContainerScreen<XpChestMenu> {

	public static final ResourceLocation TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/gui/xpchest_gui.png");

	private Player player;
	private ForgeSlider slider;
	private Button withdrawButton;
	private Button depositButton;
	private EditBox box;

	public XpChestScreen(XpChestMenu menu, Inventory inv, Component component) {
		super(menu, inv, component);
		this.player = inv.player;
		inventoryLabelY = -1000;
	}

	@Override
	protected void init() {
		super.init();
		int x = (width - imageWidth) / 2;
		int y = (height - imageHeight) / 2;
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		/*this.slider = addRenderableWidget(new ForgeSlider(x+18,y+23,153,20,new TextComponent("Exp: "),TextComponent.EMPTY,
				0,100,100,1,1,false));*/
		this.box = new EditBox(font, x + 53, y + 15, 76, 15, TextComponent.EMPTY);
		this.box.setCanLoseFocus(false);
		this.box.setTextColor(-1);
		this.box.setTextColorUneditable(-1);
		this.box.setBordered(true);
		this.box.setMaxLength(50);
		//this.box.setSuggestion("Introduce a 4 characters password");
		this.box.setValue("");
		this.addWidget(this.box);
		this.setInitialFocus(this.box);
		this.withdrawButton = addRenderableWidget(new Button(x+57,y+59,75,20,new TextComponent("Withdraw"), button -> {
			try {
				int xp = menu.blockEntity.getXpStored();
				player.sendMessage(new TextComponent(""+xp), player.getUUID());
				/*if(xp >= 1) {
					int withdrawedXp = Math.round(TotisMathUtils.calculatePercent(Integer.parseInt(this.box.getValue()), xp));
					menu.setXpStored(-xp);
					//ModMessages.sendToServer(new PacketXPSync2S(-xp, menu.getBlockPos()));
				} else {
					player.sendMessage(new TextComponent(ChatFormatting.RED + "Error: no XP to withdraw"), player.getUUID());
				}*/
			} catch(Exception e) {}
		}));
		this.depositButton = addRenderableWidget(new Button(x+57,y+81,75,20,new TextComponent("Deposit"), button2 -> {
			try {
				int xp = TotisPlayerUtils.getPlayerXP(player);
				if(xp >= 1) {
					//if(this.box.getValue().length() < 1) return;
					int depositedXp = 20;//Math.round(TotisMathUtils.calculatePercent(Integer.parseInt(this.box.getValue()), xp));
					ModMessages.sendToServer(new PacketXPSync2S(depositedXp, menu.blockEntity.getBlockPos()));
					menu.blockEntity.setXpStored(depositedXp);
					int previousLevel = player.experienceLevel;
					//MinecraftForge.EVENT_BUS.post(new PlayerXpEvent.XpChange(player, -depositedXp));

					ModMessages.sendToServer(new PacketGivePlayerXpSync2S(-depositedXp)); //Removes the xp from the player
					//TotisPlayerUtils.addPlayerXP(player, -depositedXp);


					//if(previousLevel != player.experienceLevel) MinecraftForge.EVENT_BUS.post(new PlayerXpEvent.LevelChange(player, player.experienceLevel));

					//player.giveExperienceLevels(-depositedXp);
				} else {
					player.sendMessage(new TextComponent(ChatFormatting.RED + "Error: no XP to deposit"), player.getUUID());
				}
			} catch(NumberFormatException e) {
				player.sendMessage(new TextComponent(ChatFormatting.RED + "You only can put integer values!"), player.getUUID());
			}
		}));
	}

	@Override
	protected void containerTick() {
		super.containerTick();
	}

	@Override
	public void removed() {
		super.removed();
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
	}

	@Override
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
		if (pKeyCode == 256) {
			this.minecraft.player.closeContainer();
		}
		return !this.box.keyPressed(pKeyCode, pScanCode, pModifiers) && !this.box.canConsumeInput() ? super.keyPressed(pKeyCode, pScanCode, pModifiers) : true;
	}

	@Override
	protected void renderLabels(PoseStack stack, int mouseX, int mouseY) {
		super.renderLabels(stack, mouseX, mouseY);
	}
	
	@Override
	protected void renderBg(PoseStack stack, float partialTick, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        this.blit(stack, x, y, 0, 0, imageWidth+42, imageHeight);
	}
	
	@Override
    public void render(PoseStack pPoseStack, int mouseX, int mouseY, float delta) {
        renderBackground(pPoseStack);
        super.render(pPoseStack, mouseX, mouseY, delta);
		this.renderFg(pPoseStack, mouseX, mouseY, delta);
        renderTooltip(pPoseStack, mouseX, mouseY);
    }

	public void renderFg(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
		this.box.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
	}
}
