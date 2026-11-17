package com.mrfuzzihead.artifice.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import com.mrfuzzihead.artifice.ArtificeCore;
import com.mrfuzzihead.artifice.render.TextureHandler;

import cofh.lib.util.helpers.MathHelper;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemCraftKit extends ItemArtifice {

    private IIcon icon;

    public ItemCraftKit() {
        this.setHasSubtypes(false);
        this.setMaxStackSize(1);
        this.setUnlocalizedName("artifice.craftkit");
        GameRegistry.registerItem(this, this.getUnlocalizedName());
    }

    @Override
    public IIcon getIconFromDamage(int meta) {
        return this.icon;
    }

    @Override
    public void registerIcons(IIconRegister reg) {
        this.icon = TextureHandler.registerIcon(reg, "craftkit", "misc");
    }

    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        int x = MathHelper.floor(player.posX);
        int y = MathHelper.floor(player.posY);
        int z = MathHelper.floor(player.posZ);
        player.openGui(ArtificeCore.instance, 0, world, x, y, z);
        return stack;
    }
}
