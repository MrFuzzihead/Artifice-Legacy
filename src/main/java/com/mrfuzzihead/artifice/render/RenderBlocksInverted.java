package com.mrfuzzihead.artifice.render;

import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public class RenderBlocksInverted {

    public boolean renderStandardBlock(RenderBlocks renderer, Block par1Block, int par2, int par3, int par4) {
        int l = par1Block.colorMultiplier(renderer.blockAccess, par2, par3, par4);
        float f = (l >> 16 & 0xFF) / 255.0F;
        float f1 = (l >> 8 & 0xFF) / 255.0F;
        float f2 = (l & 0xFF) / 255.0F;
        if (EntityRenderer.anaglyphEnable) {
            float f3 = (f * 30.0F + f1 * 59.0F + f2 * 11.0F) / 100.0F;
            float f4 = (f * 30.0F + f1 * 70.0F) / 100.0F;
            float f5 = (f * 30.0F + f2 * 70.0F) / 100.0F;
            f = f3;
            f1 = f4;
            f2 = f5;
        }

        return Minecraft.isAmbientOcclusionEnabled() && par1Block.getLightValue() == 0
            ? (renderer.partialRenderBounds ? this.func_102027_b(renderer, par1Block, par2, par3, par4, f, f1, f2)
                : this.renderStandardBlockWithAmbientOcclusion(renderer, par1Block, par2, par3, par4, f, f1, f2))
            : this.renderStandardBlockWithColorMultiplier(renderer, par1Block, par2, par3, par4, f, f1, f2);
    }

    public boolean func_102027_b(RenderBlocks renderer, Block par1Block, int par2, int par3, int par4, float par5,
        float par6, float par7) {
        renderer.enableAO = true;
        boolean flag = false;
        float f3 = 0.0F;
        float f4 = 0.0F;
        float f5 = 0.0F;
        float f6 = 0.0F;
        boolean flag1 = true;
        int l = par1Block.getMixedBrightnessForBlock(renderer.blockAccess, par2, par3, par4);
        Tessellator tessellator = Tessellator.instance;
        tessellator.setBrightness(983055);
        if (renderer.getBlockIcon(par1Block)
            .getIconName()
            .equals("grass_top")) {
            flag1 = false;
        } else if (renderer.hasOverrideBlockTexture()) {
            flag1 = false;
        }

        if (renderer.renderAllFaces || par1Block.shouldSideBeRendered(renderer.blockAccess, par2, par3 - 1, par4, 0)) {
            if (renderer.renderMinY <= 0.0) {
                par3--;
            }

            renderer.aoBrightnessXYNN = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2 - 1, par3, par4);
            renderer.aoBrightnessYZNN = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3, par4 - 1);
            renderer.aoBrightnessYZNP = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3, par4 + 1);
            renderer.aoBrightnessXYPN = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2 + 1, par3, par4);
            renderer.aoLightValueScratchXYNN = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2 - 1, par3, par4);
            renderer.aoLightValueScratchYZNN = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3, par4 - 1);
            renderer.aoLightValueScratchYZNP = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3, par4 + 1);
            renderer.aoLightValueScratchXYPN = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2 + 1, par3, par4);
            boolean flag3 = renderer.blockAccess.getBlock(par2 + 1, par3 - 1, par4)
                .getCanBlockGrass();
            boolean flag2 = renderer.blockAccess.getBlock(par2 - 1, par3 - 1, par4)
                .getCanBlockGrass();
            boolean flag5 = renderer.blockAccess.getBlock(par2, par3 - 1, par4 + 1)
                .getCanBlockGrass();
            boolean flag4 = renderer.blockAccess.getBlock(par2, par3 - 1, par4 - 1)
                .getCanBlockGrass();
            if (!flag4 && !flag2) {
                renderer.aoLightValueScratchXYZNNN = renderer.aoLightValueScratchXYNN;
                renderer.aoBrightnessXYZNNN = renderer.aoBrightnessXYNN;
            } else {
                renderer.aoLightValueScratchXYZNNN = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2 - 1, par3, par4 - 1);
                renderer.aoBrightnessXYZNNN = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2 - 1, par3, par4 - 1);
            }

            if (!flag5 && !flag2) {
                renderer.aoLightValueScratchXYZNNP = renderer.aoLightValueScratchXYNN;
                renderer.aoBrightnessXYZNNP = renderer.aoBrightnessXYNN;
            } else {
                renderer.aoLightValueScratchXYZNNP = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2 - 1, par3, par4 + 1);
                renderer.aoBrightnessXYZNNP = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2 - 1, par3, par4 + 1);
            }

            if (!flag4 && !flag3) {
                renderer.aoLightValueScratchXYZPNN = renderer.aoLightValueScratchXYPN;
                renderer.aoBrightnessXYZPNN = renderer.aoBrightnessXYPN;
            } else {
                renderer.aoLightValueScratchXYZPNN = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2 + 1, par3, par4 - 1);
                renderer.aoBrightnessXYZPNN = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2 + 1, par3, par4 - 1);
            }

            if (!flag5 && !flag3) {
                renderer.aoLightValueScratchXYZPNP = renderer.aoLightValueScratchXYPN;
                renderer.aoBrightnessXYZPNP = renderer.aoBrightnessXYPN;
            } else {
                renderer.aoLightValueScratchXYZPNP = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2 + 1, par3, par4 + 1);
                renderer.aoBrightnessXYZPNP = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2 + 1, par3, par4 + 1);
            }

            if (renderer.renderMinY <= 0.0) {
                par3++;
            }

            int i1 = l;
            if (renderer.renderMinY <= 0.0 || !renderer.blockAccess.getBlock(par2, par3 - 1, par4)
                .isOpaqueCube()) {
                i1 = par1Block.getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 - 1, par4);
            }

            float f7 = this.getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 - 1, par4);
            f3 = (renderer.aoLightValueScratchXYZNNP + renderer.aoLightValueScratchXYNN
                + renderer.aoLightValueScratchYZNP
                + f7) / 4.0F;
            f6 = (renderer.aoLightValueScratchYZNP + f7
                + renderer.aoLightValueScratchXYZPNP
                + renderer.aoLightValueScratchXYPN) / 4.0F;
            f5 = (f7 + renderer.aoLightValueScratchYZNN
                + renderer.aoLightValueScratchXYPN
                + renderer.aoLightValueScratchXYZPNN) / 4.0F;
            f4 = (renderer.aoLightValueScratchXYNN + renderer.aoLightValueScratchXYZNNN
                + f7
                + renderer.aoLightValueScratchYZNN) / 4.0F;
            renderer.brightnessTopLeft = renderer
                .getAoBrightness(renderer.aoBrightnessXYZNNP, renderer.aoBrightnessXYNN, renderer.aoBrightnessYZNP, i1);
            renderer.brightnessTopRight = renderer
                .getAoBrightness(renderer.aoBrightnessYZNP, renderer.aoBrightnessXYZPNP, renderer.aoBrightnessXYPN, i1);
            renderer.brightnessBottomRight = renderer
                .getAoBrightness(renderer.aoBrightnessYZNN, renderer.aoBrightnessXYPN, renderer.aoBrightnessXYZPNN, i1);
            renderer.brightnessBottomLeft = renderer
                .getAoBrightness(renderer.aoBrightnessXYNN, renderer.aoBrightnessXYZNNN, renderer.aoBrightnessYZNN, i1);
            if (flag1) {
                renderer.colorRedTopLeft = renderer.colorRedBottomLeft = renderer.colorRedBottomRight = renderer.colorRedTopRight = par5
                    * 0.5F;
                renderer.colorGreenTopLeft = renderer.colorGreenBottomLeft = renderer.colorGreenBottomRight = renderer.colorGreenTopRight = par6
                    * 0.5F;
                renderer.colorBlueTopLeft = renderer.colorBlueBottomLeft = renderer.colorBlueBottomRight = renderer.colorBlueTopRight = par7
                    * 0.5F;
            } else {
                renderer.colorRedTopLeft = renderer.colorRedBottomLeft = renderer.colorRedBottomRight = renderer.colorRedTopRight = 0.5F;
                renderer.colorGreenTopLeft = renderer.colorGreenBottomLeft = renderer.colorGreenBottomRight = renderer.colorGreenTopRight = 0.5F;
                renderer.colorBlueTopLeft = renderer.colorBlueBottomLeft = renderer.colorBlueBottomRight = renderer.colorBlueTopRight = 0.5F;
            }

            renderer.colorRedTopLeft *= f3;
            renderer.colorGreenTopLeft *= f3;
            renderer.colorBlueTopLeft *= f3;
            renderer.colorRedBottomLeft *= f4;
            renderer.colorGreenBottomLeft *= f4;
            renderer.colorBlueBottomLeft *= f4;
            renderer.colorRedBottomRight *= f5;
            renderer.colorGreenBottomRight *= f5;
            renderer.colorBlueBottomRight *= f5;
            renderer.colorRedTopRight *= f6;
            renderer.colorGreenTopRight *= f6;
            renderer.colorBlueTopRight *= f6;
            this.renderBottomFace(
                renderer,
                par1Block,
                par2,
                par3,
                par4,
                renderer.getBlockIcon(par1Block, renderer.blockAccess, par2, par3, par4, 0));
            flag = true;
        }

        if (renderer.renderAllFaces || par1Block.shouldSideBeRendered(renderer.blockAccess, par2, par3 + 1, par4, 1)) {
            if (renderer.renderMaxY >= 1.0) {
                par3++;
            }

            renderer.aoBrightnessXYNP = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2 - 1, par3, par4);
            renderer.aoBrightnessXYPP = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2 + 1, par3, par4);
            renderer.aoBrightnessYZPN = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3, par4 - 1);
            renderer.aoBrightnessYZPP = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3, par4 + 1);
            renderer.aoLightValueScratchXYNP = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2 - 1, par3, par4);
            renderer.aoLightValueScratchXYPP = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2 + 1, par3, par4);
            renderer.aoLightValueScratchYZPN = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3, par4 - 1);
            renderer.aoLightValueScratchYZPP = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3, par4 + 1);
            boolean flag3x = renderer.blockAccess.getBlock(par2 + 1, par3 + 1, par4)
                .getCanBlockGrass();
            boolean flag2x = renderer.blockAccess.getBlock(par2 - 1, par3 + 1, par4)
                .getCanBlockGrass();
            boolean flag5x = renderer.blockAccess.getBlock(par2, par3 + 1, par4 + 1)
                .getCanBlockGrass();
            boolean flag4x = renderer.blockAccess.getBlock(par2, par3 + 1, par4 - 1)
                .getCanBlockGrass();
            if (!flag4x && !flag2x) {
                renderer.aoLightValueScratchXYZNPN = renderer.aoLightValueScratchXYNP;
                renderer.aoBrightnessXYZNPN = renderer.aoBrightnessXYNP;
            } else {
                renderer.aoLightValueScratchXYZNPN = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2 - 1, par3, par4 - 1);
                renderer.aoBrightnessXYZNPN = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2 - 1, par3, par4 - 1);
            }

            if (!flag4x && !flag3x) {
                renderer.aoLightValueScratchXYZPPN = renderer.aoLightValueScratchXYPP;
                renderer.aoBrightnessXYZPPN = renderer.aoBrightnessXYPP;
            } else {
                renderer.aoLightValueScratchXYZPPN = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2 + 1, par3, par4 - 1);
                renderer.aoBrightnessXYZPPN = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2 + 1, par3, par4 - 1);
            }

            if (!flag5x && !flag2x) {
                renderer.aoLightValueScratchXYZNPP = renderer.aoLightValueScratchXYNP;
                renderer.aoBrightnessXYZNPP = renderer.aoBrightnessXYNP;
            } else {
                renderer.aoLightValueScratchXYZNPP = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2 - 1, par3, par4 + 1);
                renderer.aoBrightnessXYZNPP = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2 - 1, par3, par4 + 1);
            }

            if (!flag5x && !flag3x) {
                renderer.aoLightValueScratchXYZPPP = renderer.aoLightValueScratchXYPP;
                renderer.aoBrightnessXYZPPP = renderer.aoBrightnessXYPP;
            } else {
                renderer.aoLightValueScratchXYZPPP = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2 + 1, par3, par4 + 1);
                renderer.aoBrightnessXYZPPP = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2 + 1, par3, par4 + 1);
            }

            if (renderer.renderMaxY >= 1.0) {
                par3--;
            }

            int i1x = l;
            if (renderer.renderMaxY >= 1.0 || !renderer.blockAccess.getBlock(par2, par3 + 1, par4)
                .isOpaqueCube()) {
                i1x = par1Block.getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 + 1, par4);
            }

            float f7 = this.getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 + 1, par4);
            f6 = (renderer.aoLightValueScratchXYZNPP + renderer.aoLightValueScratchXYNP
                + renderer.aoLightValueScratchYZPP
                + f7) / 4.0F;
            f3 = (renderer.aoLightValueScratchYZPP + f7
                + renderer.aoLightValueScratchXYZPPP
                + renderer.aoLightValueScratchXYPP) / 4.0F;
            f4 = (f7 + renderer.aoLightValueScratchYZPN
                + renderer.aoLightValueScratchXYPP
                + renderer.aoLightValueScratchXYZPPN) / 4.0F;
            f5 = (renderer.aoLightValueScratchXYNP + renderer.aoLightValueScratchXYZNPN
                + f7
                + renderer.aoLightValueScratchYZPN) / 4.0F;
            renderer.brightnessTopRight = renderer.getAoBrightness(
                renderer.aoBrightnessXYZNPP,
                renderer.aoBrightnessXYNP,
                renderer.aoBrightnessYZPP,
                i1x);
            renderer.brightnessTopLeft = renderer.getAoBrightness(
                renderer.aoBrightnessYZPP,
                renderer.aoBrightnessXYZPPP,
                renderer.aoBrightnessXYPP,
                i1x);
            renderer.brightnessBottomLeft = renderer.getAoBrightness(
                renderer.aoBrightnessYZPN,
                renderer.aoBrightnessXYPP,
                renderer.aoBrightnessXYZPPN,
                i1x);
            renderer.brightnessBottomRight = renderer.getAoBrightness(
                renderer.aoBrightnessXYNP,
                renderer.aoBrightnessXYZNPN,
                renderer.aoBrightnessYZPN,
                i1x);
            renderer.colorRedTopLeft = renderer.colorRedBottomLeft = renderer.colorRedBottomRight = renderer.colorRedTopRight = par5;
            renderer.colorGreenTopLeft = renderer.colorGreenBottomLeft = renderer.colorGreenBottomRight = renderer.colorGreenTopRight = par6;
            renderer.colorBlueTopLeft = renderer.colorBlueBottomLeft = renderer.colorBlueBottomRight = renderer.colorBlueTopRight = par7;
            renderer.colorRedTopLeft *= f3;
            renderer.colorGreenTopLeft *= f3;
            renderer.colorBlueTopLeft *= f3;
            renderer.colorRedBottomLeft *= f4;
            renderer.colorGreenBottomLeft *= f4;
            renderer.colorBlueBottomLeft *= f4;
            renderer.colorRedBottomRight *= f5;
            renderer.colorGreenBottomRight *= f5;
            renderer.colorBlueBottomRight *= f5;
            renderer.colorRedTopRight *= f6;
            renderer.colorGreenTopRight *= f6;
            renderer.colorBlueTopRight *= f6;
            this.renderTopFace(
                renderer,
                par1Block,
                par2,
                par3,
                par4,
                renderer.getBlockIcon(par1Block, renderer.blockAccess, par2, par3, par4, 1));
            flag = true;
        }

        if (renderer.renderAllFaces || par1Block.shouldSideBeRendered(renderer.blockAccess, par2, par3, par4 - 1, 2)) {
            if (renderer.renderMinZ <= 0.0) {
                par4--;
            }

            renderer.aoLightValueScratchXZNN = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2 - 1, par3, par4);
            renderer.aoLightValueScratchYZNN = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 - 1, par4);
            renderer.aoLightValueScratchYZPN = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 + 1, par4);
            renderer.aoLightValueScratchXZPN = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2 + 1, par3, par4);
            renderer.aoBrightnessXZNN = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2 - 1, par3, par4);
            renderer.aoBrightnessYZNN = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 - 1, par4);
            renderer.aoBrightnessYZPN = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 + 1, par4);
            renderer.aoBrightnessXZPN = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2 + 1, par3, par4);
            boolean flag3xx = renderer.blockAccess.getBlock(par2 + 1, par3, par4 - 1)
                .getCanBlockGrass();
            boolean flag2xx = renderer.blockAccess.getBlock(par2 - 1, par3, par4 - 1)
                .getCanBlockGrass();
            boolean flag5xx = renderer.blockAccess.getBlock(par2, par3 + 1, par4 - 1)
                .getCanBlockGrass();
            boolean flag4xx = renderer.blockAccess.getBlock(par2, par3 - 1, par4 - 1)
                .getCanBlockGrass();
            if (!flag2xx && !flag4xx) {
                renderer.aoLightValueScratchXYZNNN = renderer.aoLightValueScratchXZNN;
                renderer.aoBrightnessXYZNNN = renderer.aoBrightnessXZNN;
            } else {
                renderer.aoLightValueScratchXYZNNN = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2 - 1, par3 - 1, par4);
                renderer.aoBrightnessXYZNNN = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2 - 1, par3 - 1, par4);
            }

            if (!flag2xx && !flag5xx) {
                renderer.aoLightValueScratchXYZNPN = renderer.aoLightValueScratchXZNN;
                renderer.aoBrightnessXYZNPN = renderer.aoBrightnessXZNN;
            } else {
                renderer.aoLightValueScratchXYZNPN = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2 - 1, par3 + 1, par4);
                renderer.aoBrightnessXYZNPN = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2 - 1, par3 + 1, par4);
            }

            if (!flag3xx && !flag4xx) {
                renderer.aoLightValueScratchXYZPNN = renderer.aoLightValueScratchXZPN;
                renderer.aoBrightnessXYZPNN = renderer.aoBrightnessXZPN;
            } else {
                renderer.aoLightValueScratchXYZPNN = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2 + 1, par3 - 1, par4);
                renderer.aoBrightnessXYZPNN = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2 + 1, par3 - 1, par4);
            }

            if (!flag3xx && !flag5xx) {
                renderer.aoLightValueScratchXYZPPN = renderer.aoLightValueScratchXZPN;
                renderer.aoBrightnessXYZPPN = renderer.aoBrightnessXZPN;
            } else {
                renderer.aoLightValueScratchXYZPPN = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2 + 1, par3 + 1, par4);
                renderer.aoBrightnessXYZPPN = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2 + 1, par3 + 1, par4);
            }

            if (renderer.renderMinZ <= 0.0) {
                par4++;
            }

            int i1x = l;
            if (renderer.renderMinZ <= 0.0 || !renderer.blockAccess.getBlock(par2, par3, par4 - 1)
                .isOpaqueCube()) {
                i1x = par1Block.getMixedBrightnessForBlock(renderer.blockAccess, par2, par3, par4 - 1);
            }

            float f7 = this.getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3, par4 - 1);
            float f9 = (renderer.aoLightValueScratchXZNN + renderer.aoLightValueScratchXYZNPN
                + f7
                + renderer.aoLightValueScratchYZPN) / 4.0F;
            float f8 = (f7 + renderer.aoLightValueScratchYZPN
                + renderer.aoLightValueScratchXZPN
                + renderer.aoLightValueScratchXYZPPN) / 4.0F;
            float f11 = (renderer.aoLightValueScratchYZNN + f7
                + renderer.aoLightValueScratchXYZPNN
                + renderer.aoLightValueScratchXZPN) / 4.0F;
            float f10 = (renderer.aoLightValueScratchXYZNNN + renderer.aoLightValueScratchXZNN
                + renderer.aoLightValueScratchYZNN
                + f7) / 4.0F;
            f3 = (float) (f9 * renderer.renderMaxY * (1.0 - renderer.renderMinX)
                + f8 * renderer.renderMinY * renderer.renderMinX
                + f11 * (1.0 - renderer.renderMaxY) * renderer.renderMinX
                + f10 * (1.0 - renderer.renderMaxY) * (1.0 - renderer.renderMinX));
            f4 = (float) (f9 * renderer.renderMaxY * (1.0 - renderer.renderMaxX)
                + f8 * renderer.renderMaxY * renderer.renderMaxX
                + f11 * (1.0 - renderer.renderMaxY) * renderer.renderMaxX
                + f10 * (1.0 - renderer.renderMaxY) * (1.0 - renderer.renderMaxX));
            f5 = (float) (f9 * renderer.renderMinY * (1.0 - renderer.renderMaxX)
                + f8 * renderer.renderMinY * renderer.renderMaxX
                + f11 * (1.0 - renderer.renderMinY) * renderer.renderMaxX
                + f10 * (1.0 - renderer.renderMinY) * (1.0 - renderer.renderMaxX));
            f6 = (float) (f9 * renderer.renderMinY * (1.0 - renderer.renderMinX)
                + f8 * renderer.renderMinY * renderer.renderMinX
                + f11 * (1.0 - renderer.renderMinY) * renderer.renderMinX
                + f10 * (1.0 - renderer.renderMinY) * (1.0 - renderer.renderMinX));
            int k1 = renderer.getAoBrightness(
                renderer.aoBrightnessXZNN,
                renderer.aoBrightnessXYZNPN,
                renderer.aoBrightnessYZPN,
                i1x);
            int j1 = renderer.getAoBrightness(
                renderer.aoBrightnessYZPN,
                renderer.aoBrightnessXZPN,
                renderer.aoBrightnessXYZPPN,
                i1x);
            int i2 = renderer.getAoBrightness(
                renderer.aoBrightnessYZNN,
                renderer.aoBrightnessXYZPNN,
                renderer.aoBrightnessXZPN,
                i1x);
            int l1 = renderer.getAoBrightness(
                renderer.aoBrightnessXYZNNN,
                renderer.aoBrightnessXZNN,
                renderer.aoBrightnessYZNN,
                i1x);
            renderer.brightnessTopLeft = renderer.mixAoBrightness(
                k1,
                j1,
                i2,
                l1,
                renderer.renderMaxY * (1.0 - renderer.renderMinX),
                renderer.renderMaxY * renderer.renderMinX,
                (1.0 - renderer.renderMaxY) * renderer.renderMinX,
                (1.0 - renderer.renderMaxY) * (1.0 - renderer.renderMinX));
            renderer.brightnessBottomLeft = renderer.mixAoBrightness(
                k1,
                j1,
                i2,
                l1,
                renderer.renderMaxY * (1.0 - renderer.renderMaxX),
                renderer.renderMaxY * renderer.renderMaxX,
                (1.0 - renderer.renderMaxY) * renderer.renderMaxX,
                (1.0 - renderer.renderMaxY) * (1.0 - renderer.renderMaxX));
            renderer.brightnessBottomRight = renderer.mixAoBrightness(
                k1,
                j1,
                i2,
                l1,
                renderer.renderMinY * (1.0 - renderer.renderMaxX),
                renderer.renderMinY * renderer.renderMaxX,
                (1.0 - renderer.renderMinY) * renderer.renderMaxX,
                (1.0 - renderer.renderMinY) * (1.0 - renderer.renderMaxX));
            renderer.brightnessTopRight = renderer.mixAoBrightness(
                k1,
                j1,
                i2,
                l1,
                renderer.renderMinY * (1.0 - renderer.renderMinX),
                renderer.renderMinY * renderer.renderMinX,
                (1.0 - renderer.renderMinY) * renderer.renderMinX,
                (1.0 - renderer.renderMinY) * (1.0 - renderer.renderMinX));
            if (flag1) {
                renderer.colorRedTopLeft = renderer.colorRedBottomLeft = renderer.colorRedBottomRight = renderer.colorRedTopRight = par5
                    * 0.8F;
                renderer.colorGreenTopLeft = renderer.colorGreenBottomLeft = renderer.colorGreenBottomRight = renderer.colorGreenTopRight = par6
                    * 0.8F;
                renderer.colorBlueTopLeft = renderer.colorBlueBottomLeft = renderer.colorBlueBottomRight = renderer.colorBlueTopRight = par7
                    * 0.8F;
            } else {
                renderer.colorRedTopLeft = renderer.colorRedBottomLeft = renderer.colorRedBottomRight = renderer.colorRedTopRight = 0.8F;
                renderer.colorGreenTopLeft = renderer.colorGreenBottomLeft = renderer.colorGreenBottomRight = renderer.colorGreenTopRight = 0.8F;
                renderer.colorBlueTopLeft = renderer.colorBlueBottomLeft = renderer.colorBlueBottomRight = renderer.colorBlueTopRight = 0.8F;
            }

            renderer.colorRedTopLeft *= f3;
            renderer.colorGreenTopLeft *= f3;
            renderer.colorBlueTopLeft *= f3;
            renderer.colorRedBottomLeft *= f4;
            renderer.colorGreenBottomLeft *= f4;
            renderer.colorBlueBottomLeft *= f4;
            renderer.colorRedBottomRight *= f5;
            renderer.colorGreenBottomRight *= f5;
            renderer.colorBlueBottomRight *= f5;
            renderer.colorRedTopRight *= f6;
            renderer.colorGreenTopRight *= f6;
            renderer.colorBlueTopRight *= f6;
            IIcon icon = renderer.getBlockIcon(par1Block, renderer.blockAccess, par2, par3, par4, 2);
            this.renderEastFace(renderer, par1Block, par2, par3, par4, icon);
            if (RenderBlocks.fancyGrass && icon.getIconName()
                .equals("grass_side") && !renderer.hasOverrideBlockTexture()) {
                renderer.colorRedTopLeft *= par5;
                renderer.colorRedBottomLeft *= par5;
                renderer.colorRedBottomRight *= par5;
                renderer.colorRedTopRight *= par5;
                renderer.colorGreenTopLeft *= par6;
                renderer.colorGreenBottomLeft *= par6;
                renderer.colorGreenBottomRight *= par6;
                renderer.colorGreenTopRight *= par6;
                renderer.colorBlueTopLeft *= par7;
                renderer.colorBlueBottomLeft *= par7;
                renderer.colorBlueBottomRight *= par7;
                renderer.colorBlueTopRight *= par7;
                this.renderEastFace(renderer, par1Block, par2, par3, par4, BlockGrass.getIconSideOverlay());
            }

            flag = true;
        }

        if (renderer.renderAllFaces || par1Block.shouldSideBeRendered(renderer.blockAccess, par2, par3, par4 + 1, 3)) {
            if (renderer.renderMaxZ >= 1.0) {
                par4++;
            }

            renderer.aoLightValueScratchXZNP = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2 - 1, par3, par4);
            renderer.aoLightValueScratchXZPP = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2 + 1, par3, par4);
            renderer.aoLightValueScratchYZNP = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 - 1, par4);
            renderer.aoLightValueScratchYZPP = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 + 1, par4);
            renderer.aoBrightnessXZNP = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2 - 1, par3, par4);
            renderer.aoBrightnessXZPP = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2 + 1, par3, par4);
            renderer.aoBrightnessYZNP = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 - 1, par4);
            renderer.aoBrightnessYZPP = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 + 1, par4);
            boolean flag3xxx = renderer.blockAccess.getBlock(par2 + 1, par3, par4 + 1)
                .getCanBlockGrass();
            boolean flag2xxx = renderer.blockAccess.getBlock(par2 - 1, par3, par4 + 1)
                .getCanBlockGrass();
            boolean flag5xxx = renderer.blockAccess.getBlock(par2, par3 + 1, par4 + 1)
                .getCanBlockGrass();
            boolean flag4xxx = renderer.blockAccess.getBlock(par2, par3 - 1, par4 + 1)
                .getCanBlockGrass();
            if (!flag2xxx && !flag4xxx) {
                renderer.aoLightValueScratchXYZNNP = renderer.aoLightValueScratchXZNP;
                renderer.aoBrightnessXYZNNP = renderer.aoBrightnessXZNP;
            } else {
                renderer.aoLightValueScratchXYZNNP = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2 - 1, par3 - 1, par4);
                renderer.aoBrightnessXYZNNP = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2 - 1, par3 - 1, par4);
            }

            if (!flag2xxx && !flag5xxx) {
                renderer.aoLightValueScratchXYZNPP = renderer.aoLightValueScratchXZNP;
                renderer.aoBrightnessXYZNPP = renderer.aoBrightnessXZNP;
            } else {
                renderer.aoLightValueScratchXYZNPP = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2 - 1, par3 + 1, par4);
                renderer.aoBrightnessXYZNPP = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2 - 1, par3 + 1, par4);
            }

            if (!flag3xxx && !flag4xxx) {
                renderer.aoLightValueScratchXYZPNP = renderer.aoLightValueScratchXZPP;
                renderer.aoBrightnessXYZPNP = renderer.aoBrightnessXZPP;
            } else {
                renderer.aoLightValueScratchXYZPNP = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2 + 1, par3 - 1, par4);
                renderer.aoBrightnessXYZPNP = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2 + 1, par3 - 1, par4);
            }

            if (!flag3xxx && !flag5xxx) {
                renderer.aoLightValueScratchXYZPPP = renderer.aoLightValueScratchXZPP;
                renderer.aoBrightnessXYZPPP = renderer.aoBrightnessXZPP;
            } else {
                renderer.aoLightValueScratchXYZPPP = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2 + 1, par3 + 1, par4);
                renderer.aoBrightnessXYZPPP = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2 + 1, par3 + 1, par4);
            }

            if (renderer.renderMaxZ >= 1.0) {
                par4--;
            }

            int i1xx = l;
            if (renderer.renderMaxZ >= 1.0 || !renderer.blockAccess.getBlock(par2, par3, par4 + 1)
                .isOpaqueCube()) {
                i1xx = par1Block.getMixedBrightnessForBlock(renderer.blockAccess, par2, par3, par4 + 1);
            }

            float f7x = this.getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3, par4 + 1);
            float f9x = (renderer.aoLightValueScratchXZNP + renderer.aoLightValueScratchXYZNPP
                + f7x
                + renderer.aoLightValueScratchYZPP) / 4.0F;
            float f8x = (f7x + renderer.aoLightValueScratchYZPP
                + renderer.aoLightValueScratchXZPP
                + renderer.aoLightValueScratchXYZPPP) / 4.0F;
            float f11x = (renderer.aoLightValueScratchYZNP + f7x
                + renderer.aoLightValueScratchXYZPNP
                + renderer.aoLightValueScratchXZPP) / 4.0F;
            float f10x = (renderer.aoLightValueScratchXYZNNP + renderer.aoLightValueScratchXZNP
                + renderer.aoLightValueScratchYZNP
                + f7x) / 4.0F;
            f3 = (float) (f9x * renderer.renderMaxY * (1.0 - renderer.renderMinX)
                + f8x * renderer.renderMaxY * renderer.renderMinX
                + f11x * (1.0 - renderer.renderMaxY) * renderer.renderMinX
                + f10x * (1.0 - renderer.renderMaxY) * (1.0 - renderer.renderMinX));
            f4 = (float) (f9x * renderer.renderMinY * (1.0 - renderer.renderMinX)
                + f8x * renderer.renderMinY * renderer.renderMinX
                + f11x * (1.0 - renderer.renderMinY) * renderer.renderMinX
                + f10x * (1.0 - renderer.renderMinY) * (1.0 - renderer.renderMinX));
            f5 = (float) (f9x * renderer.renderMinY * (1.0 - renderer.renderMaxX)
                + f8x * renderer.renderMinY * renderer.renderMaxX
                + f11x * (1.0 - renderer.renderMinY) * renderer.renderMaxX
                + f10x * (1.0 - renderer.renderMinY) * (1.0 - renderer.renderMaxX));
            f6 = (float) (f9x * renderer.renderMaxY * (1.0 - renderer.renderMaxX)
                + f8x * renderer.renderMaxY * renderer.renderMaxX
                + f11x * (1.0 - renderer.renderMaxY) * renderer.renderMaxX
                + f10x * (1.0 - renderer.renderMaxY) * (1.0 - renderer.renderMaxX));
            int k1x = renderer.getAoBrightness(
                renderer.aoBrightnessXZNP,
                renderer.aoBrightnessXYZNPP,
                renderer.aoBrightnessYZPP,
                i1xx);
            int j1x = renderer.getAoBrightness(
                renderer.aoBrightnessYZPP,
                renderer.aoBrightnessXZPP,
                renderer.aoBrightnessXYZPPP,
                i1xx);
            int i2x = renderer.getAoBrightness(
                renderer.aoBrightnessYZNP,
                renderer.aoBrightnessXYZPNP,
                renderer.aoBrightnessXZPP,
                i1xx);
            int l1x = renderer.getAoBrightness(
                renderer.aoBrightnessXYZNNP,
                renderer.aoBrightnessXZNP,
                renderer.aoBrightnessYZNP,
                i1xx);
            renderer.brightnessTopLeft = renderer.mixAoBrightness(
                k1x,
                l1x,
                i2x,
                j1x,
                renderer.renderMaxY * (1.0 - renderer.renderMinX),
                (1.0 - renderer.renderMaxY) * (1.0 - renderer.renderMinX),
                (1.0 - renderer.renderMaxY) * renderer.renderMinX,
                renderer.renderMaxY * renderer.renderMinX);
            renderer.brightnessBottomLeft = renderer.mixAoBrightness(
                k1x,
                l1x,
                i2x,
                j1x,
                renderer.renderMinY * (1.0 - renderer.renderMinX),
                (1.0 - renderer.renderMinY) * (1.0 - renderer.renderMinX),
                (1.0 - renderer.renderMinY) * renderer.renderMinX,
                renderer.renderMinY * renderer.renderMinX);
            renderer.brightnessBottomRight = renderer.mixAoBrightness(
                k1x,
                l1x,
                i2x,
                j1x,
                renderer.renderMinY * (1.0 - renderer.renderMaxX),
                (1.0 - renderer.renderMinY) * (1.0 - renderer.renderMaxX),
                (1.0 - renderer.renderMinY) * renderer.renderMaxX,
                renderer.renderMinY * renderer.renderMaxX);
            renderer.brightnessTopRight = renderer.mixAoBrightness(
                k1x,
                l1x,
                i2x,
                j1x,
                renderer.renderMaxY * (1.0 - renderer.renderMaxX),
                (1.0 - renderer.renderMaxY) * (1.0 - renderer.renderMaxX),
                (1.0 - renderer.renderMaxY) * renderer.renderMaxX,
                renderer.renderMaxY * renderer.renderMaxX);
            if (flag1) {
                renderer.colorRedTopLeft = renderer.colorRedBottomLeft = renderer.colorRedBottomRight = renderer.colorRedTopRight = par5
                    * 0.8F;
                renderer.colorGreenTopLeft = renderer.colorGreenBottomLeft = renderer.colorGreenBottomRight = renderer.colorGreenTopRight = par6
                    * 0.8F;
                renderer.colorBlueTopLeft = renderer.colorBlueBottomLeft = renderer.colorBlueBottomRight = renderer.colorBlueTopRight = par7
                    * 0.8F;
            } else {
                renderer.colorRedTopLeft = renderer.colorRedBottomLeft = renderer.colorRedBottomRight = renderer.colorRedTopRight = 0.8F;
                renderer.colorGreenTopLeft = renderer.colorGreenBottomLeft = renderer.colorGreenBottomRight = renderer.colorGreenTopRight = 0.8F;
                renderer.colorBlueTopLeft = renderer.colorBlueBottomLeft = renderer.colorBlueBottomRight = renderer.colorBlueTopRight = 0.8F;
            }

            renderer.colorRedTopLeft *= f3;
            renderer.colorGreenTopLeft *= f3;
            renderer.colorBlueTopLeft *= f3;
            renderer.colorRedBottomLeft *= f4;
            renderer.colorGreenBottomLeft *= f4;
            renderer.colorBlueBottomLeft *= f4;
            renderer.colorRedBottomRight *= f5;
            renderer.colorGreenBottomRight *= f5;
            renderer.colorBlueBottomRight *= f5;
            renderer.colorRedTopRight *= f6;
            renderer.colorGreenTopRight *= f6;
            renderer.colorBlueTopRight *= f6;
            IIcon icon = renderer.getBlockIcon(par1Block, renderer.blockAccess, par2, par3, par4, 3);
            this.renderWestFace(
                renderer,
                par1Block,
                par2,
                par3,
                par4,
                renderer.getBlockIcon(par1Block, renderer.blockAccess, par2, par3, par4, 3));
            if (RenderBlocks.fancyGrass && icon.getIconName()
                .equals("grass_side") && !renderer.hasOverrideBlockTexture()) {
                renderer.colorRedTopLeft *= par5;
                renderer.colorRedBottomLeft *= par5;
                renderer.colorRedBottomRight *= par5;
                renderer.colorRedTopRight *= par5;
                renderer.colorGreenTopLeft *= par6;
                renderer.colorGreenBottomLeft *= par6;
                renderer.colorGreenBottomRight *= par6;
                renderer.colorGreenTopRight *= par6;
                renderer.colorBlueTopLeft *= par7;
                renderer.colorBlueBottomLeft *= par7;
                renderer.colorBlueBottomRight *= par7;
                renderer.colorBlueTopRight *= par7;
                this.renderWestFace(renderer, par1Block, par2, par3, par4, BlockGrass.getIconSideOverlay());
            }

            flag = true;
        }

        if (renderer.renderAllFaces || par1Block.shouldSideBeRendered(renderer.blockAccess, par2 - 1, par3, par4, 4)) {
            if (renderer.renderMinX <= 0.0) {
                par2--;
            }

            renderer.aoLightValueScratchXYNN = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 - 1, par4);
            renderer.aoLightValueScratchXZNN = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3, par4 - 1);
            renderer.aoLightValueScratchXZNP = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3, par4 + 1);
            renderer.aoLightValueScratchXYNP = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 + 1, par4);
            renderer.aoBrightnessXYNN = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 - 1, par4);
            renderer.aoBrightnessXZNN = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3, par4 - 1);
            renderer.aoBrightnessXZNP = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3, par4 + 1);
            renderer.aoBrightnessXYNP = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 + 1, par4);
            boolean flag3xxxx = renderer.blockAccess.getBlock(par2 - 1, par3 + 1, par4)
                .getCanBlockGrass();
            boolean flag2xxxx = renderer.blockAccess.getBlock(par2 - 1, par3 - 1, par4)
                .getCanBlockGrass();
            boolean flag5xxxx = renderer.blockAccess.getBlock(par2 - 1, par3, par4 - 1)
                .getCanBlockGrass();
            boolean flag4xxxx = renderer.blockAccess.getBlock(par2 - 1, par3, par4 + 1)
                .getCanBlockGrass();
            if (!flag5xxxx && !flag2xxxx) {
                renderer.aoLightValueScratchXYZNNN = renderer.aoLightValueScratchXZNN;
                renderer.aoBrightnessXYZNNN = renderer.aoBrightnessXZNN;
            } else {
                renderer.aoLightValueScratchXYZNNN = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 - 1, par4 - 1);
                renderer.aoBrightnessXYZNNN = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 - 1, par4 - 1);
            }

            if (!flag4xxxx && !flag2xxxx) {
                renderer.aoLightValueScratchXYZNNP = renderer.aoLightValueScratchXZNP;
                renderer.aoBrightnessXYZNNP = renderer.aoBrightnessXZNP;
            } else {
                renderer.aoLightValueScratchXYZNNP = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 - 1, par4 + 1);
                renderer.aoBrightnessXYZNNP = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 - 1, par4 + 1);
            }

            if (!flag5xxxx && !flag3xxxx) {
                renderer.aoLightValueScratchXYZNPN = renderer.aoLightValueScratchXZNN;
                renderer.aoBrightnessXYZNPN = renderer.aoBrightnessXZNN;
            } else {
                renderer.aoLightValueScratchXYZNPN = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 + 1, par4 - 1);
                renderer.aoBrightnessXYZNPN = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 + 1, par4 - 1);
            }

            if (!flag4xxxx && !flag3xxxx) {
                renderer.aoLightValueScratchXYZNPP = renderer.aoLightValueScratchXZNP;
                renderer.aoBrightnessXYZNPP = renderer.aoBrightnessXZNP;
            } else {
                renderer.aoLightValueScratchXYZNPP = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 + 1, par4 + 1);
                renderer.aoBrightnessXYZNPP = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 + 1, par4 + 1);
            }

            if (renderer.renderMinX <= 0.0) {
                par2++;
            }

            int i1xxx = l;
            if (renderer.renderMinX <= 0.0 || !renderer.blockAccess.getBlock(par2 - 1, par3, par4)
                .isOpaqueCube()) {
                i1xxx = par1Block.getMixedBrightnessForBlock(renderer.blockAccess, par2 - 1, par3, par4);
            }

            float f7xx = this.getAmbientOcclusionLightValue(renderer.blockAccess, par2 - 1, par3, par4);
            float f9xx = (renderer.aoLightValueScratchXYNN + renderer.aoLightValueScratchXYZNNP
                + f7xx
                + renderer.aoLightValueScratchXZNP) / 4.0F;
            float f8xx = (f7xx + renderer.aoLightValueScratchXZNP
                + renderer.aoLightValueScratchXYNP
                + renderer.aoLightValueScratchXYZNPP) / 4.0F;
            float f11xx = (renderer.aoLightValueScratchXZNN + f7xx
                + renderer.aoLightValueScratchXYZNPN
                + renderer.aoLightValueScratchXYNP) / 4.0F;
            float f10xx = (renderer.aoLightValueScratchXYZNNN + renderer.aoLightValueScratchXYNN
                + renderer.aoLightValueScratchXZNN
                + f7xx) / 4.0F;
            f3 = (float) (f8xx * renderer.renderMaxY * renderer.renderMaxZ
                + f11xx * renderer.renderMaxY * (1.0 - renderer.renderMaxZ)
                + f10xx * (1.0 - renderer.renderMaxY) * (1.0 - renderer.renderMaxZ)
                + f9xx * (1.0 - renderer.renderMaxY) * renderer.renderMaxZ);
            f4 = (float) (f8xx * renderer.renderMaxY * renderer.renderMinZ
                + f11xx * renderer.renderMaxY * (1.0 - renderer.renderMinZ)
                + f10xx * (1.0 - renderer.renderMaxY) * (1.0 - renderer.renderMinZ)
                + f9xx * (1.0 - renderer.renderMaxY) * renderer.renderMinZ);
            f5 = (float) (f8xx * renderer.renderMinY * renderer.renderMinZ
                + f11xx * renderer.renderMinY * (1.0 - renderer.renderMinZ)
                + f10xx * (1.0 - renderer.renderMinY) * (1.0 - renderer.renderMinZ)
                + f9xx * (1.0 - renderer.renderMinY) * renderer.renderMinZ);
            f6 = (float) (f8xx * renderer.renderMinY * renderer.renderMaxZ
                + f11xx * renderer.renderMinY * (1.0 - renderer.renderMaxZ)
                + f10xx * (1.0 - renderer.renderMinY) * (1.0 - renderer.renderMaxZ)
                + f9xx * (1.0 - renderer.renderMinY) * renderer.renderMaxZ);
            int k1xx = renderer.getAoBrightness(
                renderer.aoBrightnessXYNN,
                renderer.aoBrightnessXYZNNP,
                renderer.aoBrightnessXZNP,
                i1xxx);
            int j1xx = renderer.getAoBrightness(
                renderer.aoBrightnessXZNP,
                renderer.aoBrightnessXYNP,
                renderer.aoBrightnessXYZNPP,
                i1xxx);
            int i2xx = renderer.getAoBrightness(
                renderer.aoBrightnessXZNN,
                renderer.aoBrightnessXYZNPN,
                renderer.aoBrightnessXYNP,
                i1xxx);
            int l1xx = renderer.getAoBrightness(
                renderer.aoBrightnessXYZNNN,
                renderer.aoBrightnessXYNN,
                renderer.aoBrightnessXZNN,
                i1xxx);
            renderer.brightnessTopLeft = renderer.mixAoBrightness(
                j1xx,
                i2xx,
                l1xx,
                k1xx,
                renderer.renderMaxY * renderer.renderMaxZ,
                renderer.renderMaxY * (1.0 - renderer.renderMaxZ),
                (1.0 - renderer.renderMaxY) * (1.0 - renderer.renderMaxZ),
                (1.0 - renderer.renderMaxY) * renderer.renderMaxZ);
            renderer.brightnessBottomLeft = renderer.mixAoBrightness(
                j1xx,
                i2xx,
                l1xx,
                k1xx,
                renderer.renderMaxY * renderer.renderMinZ,
                renderer.renderMaxY * (1.0 - renderer.renderMinZ),
                (1.0 - renderer.renderMaxY) * (1.0 - renderer.renderMinZ),
                (1.0 - renderer.renderMaxY) * renderer.renderMinZ);
            renderer.brightnessBottomRight = renderer.mixAoBrightness(
                j1xx,
                i2xx,
                l1xx,
                k1xx,
                renderer.renderMinY * renderer.renderMinZ,
                renderer.renderMinY * (1.0 - renderer.renderMinZ),
                (1.0 - renderer.renderMinY) * (1.0 - renderer.renderMinZ),
                (1.0 - renderer.renderMinY) * renderer.renderMinZ);
            renderer.brightnessTopRight = renderer.mixAoBrightness(
                j1xx,
                i2xx,
                l1xx,
                k1xx,
                renderer.renderMinY * renderer.renderMaxZ,
                renderer.renderMinY * (1.0 - renderer.renderMaxZ),
                (1.0 - renderer.renderMinY) * (1.0 - renderer.renderMaxZ),
                (1.0 - renderer.renderMinY) * renderer.renderMaxZ);
            if (flag1) {
                renderer.colorRedTopLeft = renderer.colorRedBottomLeft = renderer.colorRedBottomRight = renderer.colorRedTopRight = par5
                    * 0.6F;
                renderer.colorGreenTopLeft = renderer.colorGreenBottomLeft = renderer.colorGreenBottomRight = renderer.colorGreenTopRight = par6
                    * 0.6F;
                renderer.colorBlueTopLeft = renderer.colorBlueBottomLeft = renderer.colorBlueBottomRight = renderer.colorBlueTopRight = par7
                    * 0.6F;
            } else {
                renderer.colorRedTopLeft = renderer.colorRedBottomLeft = renderer.colorRedBottomRight = renderer.colorRedTopRight = 0.6F;
                renderer.colorGreenTopLeft = renderer.colorGreenBottomLeft = renderer.colorGreenBottomRight = renderer.colorGreenTopRight = 0.6F;
                renderer.colorBlueTopLeft = renderer.colorBlueBottomLeft = renderer.colorBlueBottomRight = renderer.colorBlueTopRight = 0.6F;
            }

            renderer.colorRedTopLeft *= f3;
            renderer.colorGreenTopLeft *= f3;
            renderer.colorBlueTopLeft *= f3;
            renderer.colorRedBottomLeft *= f4;
            renderer.colorGreenBottomLeft *= f4;
            renderer.colorBlueBottomLeft *= f4;
            renderer.colorRedBottomRight *= f5;
            renderer.colorGreenBottomRight *= f5;
            renderer.colorBlueBottomRight *= f5;
            renderer.colorRedTopRight *= f6;
            renderer.colorGreenTopRight *= f6;
            renderer.colorBlueTopRight *= f6;
            IIcon icon = renderer.getBlockIcon(par1Block, renderer.blockAccess, par2, par3, par4, 4);
            this.renderNorthFace(renderer, par1Block, par2, par3, par4, icon);
            if (RenderBlocks.fancyGrass && icon.getIconName()
                .equals("grass_side") && !renderer.hasOverrideBlockTexture()) {
                renderer.colorRedTopLeft *= par5;
                renderer.colorRedBottomLeft *= par5;
                renderer.colorRedBottomRight *= par5;
                renderer.colorRedTopRight *= par5;
                renderer.colorGreenTopLeft *= par6;
                renderer.colorGreenBottomLeft *= par6;
                renderer.colorGreenBottomRight *= par6;
                renderer.colorGreenTopRight *= par6;
                renderer.colorBlueTopLeft *= par7;
                renderer.colorBlueBottomLeft *= par7;
                renderer.colorBlueBottomRight *= par7;
                renderer.colorBlueTopRight *= par7;
                this.renderNorthFace(renderer, par1Block, par2, par3, par4, BlockGrass.getIconSideOverlay());
            }

            flag = true;
        }

        if (renderer.renderAllFaces || par1Block.shouldSideBeRendered(renderer.blockAccess, par2 + 1, par3, par4, 5)) {
            if (renderer.renderMaxX >= 1.0) {
                par2++;
            }

            renderer.aoLightValueScratchXYPN = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 - 1, par4);
            renderer.aoLightValueScratchXZPN = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3, par4 - 1);
            renderer.aoLightValueScratchXZPP = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3, par4 + 1);
            renderer.aoLightValueScratchXYPP = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 + 1, par4);
            renderer.aoBrightnessXYPN = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 - 1, par4);
            renderer.aoBrightnessXZPN = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3, par4 - 1);
            renderer.aoBrightnessXZPP = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3, par4 + 1);
            renderer.aoBrightnessXYPP = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 + 1, par4);
            boolean flag3xxxxx = renderer.blockAccess.getBlock(par2 + 1, par3 + 1, par4)
                .getCanBlockGrass();
            boolean flag2xxxxx = renderer.blockAccess.getBlock(par2 + 1, par3 - 1, par4)
                .getCanBlockGrass();
            boolean flag5xxxxx = renderer.blockAccess.getBlock(par2 + 1, par3, par4 + 1)
                .getCanBlockGrass();
            boolean flag4xxxxx = renderer.blockAccess.getBlock(par2 + 1, par3, par4 - 1)
                .getCanBlockGrass();
            if (!flag2xxxxx && !flag4xxxxx) {
                renderer.aoLightValueScratchXYZPNN = renderer.aoLightValueScratchXZPN;
                renderer.aoBrightnessXYZPNN = renderer.aoBrightnessXZPN;
            } else {
                renderer.aoLightValueScratchXYZPNN = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 - 1, par4 - 1);
                renderer.aoBrightnessXYZPNN = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 - 1, par4 - 1);
            }

            if (!flag2xxxxx && !flag5xxxxx) {
                renderer.aoLightValueScratchXYZPNP = renderer.aoLightValueScratchXZPP;
                renderer.aoBrightnessXYZPNP = renderer.aoBrightnessXZPP;
            } else {
                renderer.aoLightValueScratchXYZPNP = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 - 1, par4 + 1);
                renderer.aoBrightnessXYZPNP = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 - 1, par4 + 1);
            }

            if (!flag3xxxxx && !flag4xxxxx) {
                renderer.aoLightValueScratchXYZPPN = renderer.aoLightValueScratchXZPN;
                renderer.aoBrightnessXYZPPN = renderer.aoBrightnessXZPN;
            } else {
                renderer.aoLightValueScratchXYZPPN = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 + 1, par4 - 1);
                renderer.aoBrightnessXYZPPN = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 + 1, par4 - 1);
            }

            if (!flag3xxxxx && !flag5xxxxx) {
                renderer.aoLightValueScratchXYZPPP = renderer.aoLightValueScratchXZPP;
                renderer.aoBrightnessXYZPPP = renderer.aoBrightnessXZPP;
            } else {
                renderer.aoLightValueScratchXYZPPP = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 + 1, par4 + 1);
                renderer.aoBrightnessXYZPPP = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 + 1, par4 + 1);
            }

            if (renderer.renderMaxX >= 1.0) {
                par2--;
            }

            int i1xxxx = l;
            if (renderer.renderMaxX >= 1.0 || !renderer.blockAccess.getBlock(par2 + 1, par3, par4)
                .isOpaqueCube()) {
                i1xxxx = par1Block.getMixedBrightnessForBlock(renderer.blockAccess, par2 + 1, par3, par4);
            }

            float f7xxx = this.getAmbientOcclusionLightValue(renderer.blockAccess, par2 + 1, par3, par4);
            float f9xxx = (renderer.aoLightValueScratchXYPN + renderer.aoLightValueScratchXYZPNP
                + f7xxx
                + renderer.aoLightValueScratchXZPP) / 4.0F;
            float f8xxx = (renderer.aoLightValueScratchXYZPNN + renderer.aoLightValueScratchXYPN
                + renderer.aoLightValueScratchXZPN
                + f7xxx) / 4.0F;
            float f11xxx = (renderer.aoLightValueScratchXZPN + f7xxx
                + renderer.aoLightValueScratchXYZPPN
                + renderer.aoLightValueScratchXYPP) / 4.0F;
            float f10xxx = (f7xxx + renderer.aoLightValueScratchXZPP
                + renderer.aoLightValueScratchXYPP
                + renderer.aoLightValueScratchXYZPPP) / 4.0F;
            f3 = (float) (f9xxx * (1.0 - renderer.renderMinY) * renderer.renderMaxZ
                + f8xxx * (1.0 - renderer.renderMinY) * (1.0 - renderer.renderMaxZ)
                + f11xxx * renderer.renderMinY * (1.0 - renderer.renderMaxZ)
                + f10xxx * renderer.renderMinY * renderer.renderMaxZ);
            f4 = (float) (f9xxx * (1.0 - renderer.renderMinY) * renderer.renderMinZ
                + f8xxx * (1.0 - renderer.renderMinY) * (1.0 - renderer.renderMinZ)
                + f11xxx * renderer.renderMinY * (1.0 - renderer.renderMinZ)
                + f10xxx * renderer.renderMinY * renderer.renderMinZ);
            f5 = (float) (f9xxx * (1.0 - renderer.renderMaxY) * renderer.renderMinZ
                + f8xxx * (1.0 - renderer.renderMaxY) * (1.0 - renderer.renderMinZ)
                + f11xxx * renderer.renderMaxY * (1.0 - renderer.renderMinZ)
                + f10xxx * renderer.renderMaxY * renderer.renderMinZ);
            f6 = (float) (f9xxx * (1.0 - renderer.renderMaxY) * renderer.renderMaxZ
                + f8xxx * (1.0 - renderer.renderMaxY) * (1.0 - renderer.renderMaxZ)
                + f11xxx * renderer.renderMaxY * (1.0 - renderer.renderMaxZ)
                + f10xxx * renderer.renderMaxY * renderer.renderMaxZ);
            int k1xxx = renderer.getAoBrightness(
                renderer.aoBrightnessXYPN,
                renderer.aoBrightnessXYZPNP,
                renderer.aoBrightnessXZPP,
                i1xxxx);
            int j1xxx = renderer.getAoBrightness(
                renderer.aoBrightnessXZPP,
                renderer.aoBrightnessXYPP,
                renderer.aoBrightnessXYZPPP,
                i1xxxx);
            int i2xxx = renderer.getAoBrightness(
                renderer.aoBrightnessXZPN,
                renderer.aoBrightnessXYZPPN,
                renderer.aoBrightnessXYPP,
                i1xxxx);
            int l1xxx = renderer.getAoBrightness(
                renderer.aoBrightnessXYZPNN,
                renderer.aoBrightnessXYPN,
                renderer.aoBrightnessXZPN,
                i1xxxx);
            renderer.brightnessTopLeft = renderer.mixAoBrightness(
                k1xxx,
                l1xxx,
                i2xxx,
                j1xxx,
                (1.0 - renderer.renderMinY) * renderer.renderMaxZ,
                (1.0 - renderer.renderMinY) * (1.0 - renderer.renderMaxZ),
                renderer.renderMinY * (1.0 - renderer.renderMaxZ),
                renderer.renderMinY * renderer.renderMaxZ);
            renderer.brightnessBottomLeft = renderer.mixAoBrightness(
                k1xxx,
                l1xxx,
                i2xxx,
                j1xxx,
                (1.0 - renderer.renderMinY) * renderer.renderMinZ,
                (1.0 - renderer.renderMinY) * (1.0 - renderer.renderMinZ),
                renderer.renderMinY * (1.0 - renderer.renderMinZ),
                renderer.renderMinY * renderer.renderMinZ);
            renderer.brightnessBottomRight = renderer.mixAoBrightness(
                k1xxx,
                l1xxx,
                i2xxx,
                j1xxx,
                (1.0 - renderer.renderMaxY) * renderer.renderMinZ,
                (1.0 - renderer.renderMaxY) * (1.0 - renderer.renderMinZ),
                renderer.renderMaxY * (1.0 - renderer.renderMinZ),
                renderer.renderMaxY * renderer.renderMinZ);
            renderer.brightnessTopRight = renderer.mixAoBrightness(
                k1xxx,
                l1xxx,
                i2xxx,
                j1xxx,
                (1.0 - renderer.renderMaxY) * renderer.renderMaxZ,
                (1.0 - renderer.renderMaxY) * (1.0 - renderer.renderMaxZ),
                renderer.renderMaxY * (1.0 - renderer.renderMaxZ),
                renderer.renderMaxY * renderer.renderMaxZ);
            if (flag1) {
                renderer.colorRedTopLeft = renderer.colorRedBottomLeft = renderer.colorRedBottomRight = renderer.colorRedTopRight = par5
                    * 0.6F;
                renderer.colorGreenTopLeft = renderer.colorGreenBottomLeft = renderer.colorGreenBottomRight = renderer.colorGreenTopRight = par6
                    * 0.6F;
                renderer.colorBlueTopLeft = renderer.colorBlueBottomLeft = renderer.colorBlueBottomRight = renderer.colorBlueTopRight = par7
                    * 0.6F;
            } else {
                renderer.colorRedTopLeft = renderer.colorRedBottomLeft = renderer.colorRedBottomRight = renderer.colorRedTopRight = 0.6F;
                renderer.colorGreenTopLeft = renderer.colorGreenBottomLeft = renderer.colorGreenBottomRight = renderer.colorGreenTopRight = 0.6F;
                renderer.colorBlueTopLeft = renderer.colorBlueBottomLeft = renderer.colorBlueBottomRight = renderer.colorBlueTopRight = 0.6F;
            }

            renderer.colorRedTopLeft *= f3;
            renderer.colorGreenTopLeft *= f3;
            renderer.colorBlueTopLeft *= f3;
            renderer.colorRedBottomLeft *= f4;
            renderer.colorGreenBottomLeft *= f4;
            renderer.colorBlueBottomLeft *= f4;
            renderer.colorRedBottomRight *= f5;
            renderer.colorGreenBottomRight *= f5;
            renderer.colorBlueBottomRight *= f5;
            renderer.colorRedTopRight *= f6;
            renderer.colorGreenTopRight *= f6;
            renderer.colorBlueTopRight *= f6;
            IIcon icon = renderer.getBlockIcon(par1Block, renderer.blockAccess, par2, par3, par4, 5);
            this.renderSouthFace(renderer, par1Block, par2, par3, par4, icon);
            if (RenderBlocks.fancyGrass && icon.getIconName()
                .equals("grass_side") && !renderer.hasOverrideBlockTexture()) {
                renderer.colorRedTopLeft *= par5;
                renderer.colorRedBottomLeft *= par5;
                renderer.colorRedBottomRight *= par5;
                renderer.colorRedTopRight *= par5;
                renderer.colorGreenTopLeft *= par6;
                renderer.colorGreenBottomLeft *= par6;
                renderer.colorGreenBottomRight *= par6;
                renderer.colorGreenTopRight *= par6;
                renderer.colorBlueTopLeft *= par7;
                renderer.colorBlueBottomLeft *= par7;
                renderer.colorBlueBottomRight *= par7;
                renderer.colorBlueTopRight *= par7;
                this.renderSouthFace(renderer, par1Block, par2, par3, par4, BlockGrass.getIconSideOverlay());
            }

            flag = true;
        }

        renderer.enableAO = false;
        return flag;
    }

    private float getAmbientOcclusionLightValue(IBlockAccess blockAccess, int par2, int par3, int par4) {
        return blockAccess.getBlock(par2, par3, par4)
            .getAmbientOcclusionLightValue();
    }

    public boolean renderStandardBlockWithAmbientOcclusion(RenderBlocks renderer, Block par1Block, int par2, int par3,
        int par4, float par5, float par6, float par7) {
        renderer.enableAO = true;
        boolean flag = false;
        float f3 = 0.0F;
        float f4 = 0.0F;
        float f5 = 0.0F;
        float f6 = 0.0F;
        boolean flag1 = true;
        int l = par1Block.getMixedBrightnessForBlock(renderer.blockAccess, par2, par3, par4);
        Tessellator tessellator = Tessellator.instance;
        tessellator.setBrightness(983055);
        if (renderer.getBlockIcon(par1Block)
            .getIconName()
            .equals("grass_top")) {
            flag1 = false;
        } else if (renderer.hasOverrideBlockTexture()) {
            flag1 = false;
        }

        if (renderer.renderAllFaces || par1Block.shouldSideBeRendered(renderer.blockAccess, par2, par3 - 1, par4, 0)) {
            if (renderer.renderMinY <= 0.0) {
                par3--;
            }

            renderer.aoBrightnessXYNN = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2 - 1, par3, par4);
            renderer.aoBrightnessYZNN = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3, par4 - 1);
            renderer.aoBrightnessYZNP = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3, par4 + 1);
            renderer.aoBrightnessXYPN = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2 + 1, par3, par4);
            renderer.aoLightValueScratchXYNN = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2 - 1, par3, par4);
            renderer.aoLightValueScratchYZNN = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3, par4 - 1);
            renderer.aoLightValueScratchYZNP = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3, par4 + 1);
            renderer.aoLightValueScratchXYPN = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2 + 1, par3, par4);
            boolean flag3 = renderer.blockAccess.getBlock(par2 + 1, par3 - 1, par4)
                .getCanBlockGrass();
            boolean flag2 = renderer.blockAccess.getBlock(par2 - 1, par3 - 1, par4)
                .getCanBlockGrass();
            boolean flag5 = renderer.blockAccess.getBlock(par2, par3 - 1, par4 + 1)
                .getCanBlockGrass();
            boolean flag4 = renderer.blockAccess.getBlock(par2, par3 - 1, par4 - 1)
                .getCanBlockGrass();
            if (!flag4 && !flag2) {
                renderer.aoLightValueScratchXYZNNN = renderer.aoLightValueScratchXYNN;
                renderer.aoBrightnessXYZNNN = renderer.aoBrightnessXYNN;
            } else {
                renderer.aoLightValueScratchXYZNNN = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2 - 1, par3, par4 - 1);
                renderer.aoBrightnessXYZNNN = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2 - 1, par3, par4 - 1);
            }

            if (!flag5 && !flag2) {
                renderer.aoLightValueScratchXYZNNP = renderer.aoLightValueScratchXYNN;
                renderer.aoBrightnessXYZNNP = renderer.aoBrightnessXYNN;
            } else {
                renderer.aoLightValueScratchXYZNNP = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2 - 1, par3, par4 + 1);
                renderer.aoBrightnessXYZNNP = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2 - 1, par3, par4 + 1);
            }

            if (!flag4 && !flag3) {
                renderer.aoLightValueScratchXYZPNN = renderer.aoLightValueScratchXYPN;
                renderer.aoBrightnessXYZPNN = renderer.aoBrightnessXYPN;
            } else {
                renderer.aoLightValueScratchXYZPNN = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2 + 1, par3, par4 - 1);
                renderer.aoBrightnessXYZPNN = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2 + 1, par3, par4 - 1);
            }

            if (!flag5 && !flag3) {
                renderer.aoLightValueScratchXYZPNP = renderer.aoLightValueScratchXYPN;
                renderer.aoBrightnessXYZPNP = renderer.aoBrightnessXYPN;
            } else {
                renderer.aoLightValueScratchXYZPNP = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2 + 1, par3, par4 + 1);
                renderer.aoBrightnessXYZPNP = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2 + 1, par3, par4 + 1);
            }

            if (renderer.renderMinY <= 0.0) {
                par3++;
            }

            int i1 = l;
            if (renderer.renderMinY <= 0.0 || !renderer.blockAccess.getBlock(par2, par3 - 1, par4)
                .isOpaqueCube()) {
                i1 = par1Block.getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 - 1, par4);
            }

            float f7 = this.getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 - 1, par4);
            f3 = (renderer.aoLightValueScratchXYZNNP + renderer.aoLightValueScratchXYNN
                + renderer.aoLightValueScratchYZNP
                + f7) / 4.0F;
            f6 = (renderer.aoLightValueScratchYZNP + f7
                + renderer.aoLightValueScratchXYZPNP
                + renderer.aoLightValueScratchXYPN) / 4.0F;
            f5 = (f7 + renderer.aoLightValueScratchYZNN
                + renderer.aoLightValueScratchXYPN
                + renderer.aoLightValueScratchXYZPNN) / 4.0F;
            f4 = (renderer.aoLightValueScratchXYNN + renderer.aoLightValueScratchXYZNNN
                + f7
                + renderer.aoLightValueScratchYZNN) / 4.0F;
            renderer.brightnessTopLeft = renderer
                .getAoBrightness(renderer.aoBrightnessXYZNNP, renderer.aoBrightnessXYNN, renderer.aoBrightnessYZNP, i1);
            renderer.brightnessTopRight = renderer
                .getAoBrightness(renderer.aoBrightnessYZNP, renderer.aoBrightnessXYZPNP, renderer.aoBrightnessXYPN, i1);
            renderer.brightnessBottomRight = renderer
                .getAoBrightness(renderer.aoBrightnessYZNN, renderer.aoBrightnessXYPN, renderer.aoBrightnessXYZPNN, i1);
            renderer.brightnessBottomLeft = renderer
                .getAoBrightness(renderer.aoBrightnessXYNN, renderer.aoBrightnessXYZNNN, renderer.aoBrightnessYZNN, i1);
            if (flag1) {
                renderer.colorRedTopLeft = renderer.colorRedBottomLeft = renderer.colorRedBottomRight = renderer.colorRedTopRight = par5
                    * 0.5F;
                renderer.colorGreenTopLeft = renderer.colorGreenBottomLeft = renderer.colorGreenBottomRight = renderer.colorGreenTopRight = par6
                    * 0.5F;
                renderer.colorBlueTopLeft = renderer.colorBlueBottomLeft = renderer.colorBlueBottomRight = renderer.colorBlueTopRight = par7
                    * 0.5F;
            } else {
                renderer.colorRedTopLeft = renderer.colorRedBottomLeft = renderer.colorRedBottomRight = renderer.colorRedTopRight = 0.5F;
                renderer.colorGreenTopLeft = renderer.colorGreenBottomLeft = renderer.colorGreenBottomRight = renderer.colorGreenTopRight = 0.5F;
                renderer.colorBlueTopLeft = renderer.colorBlueBottomLeft = renderer.colorBlueBottomRight = renderer.colorBlueTopRight = 0.5F;
            }

            renderer.colorRedTopLeft *= f3;
            renderer.colorGreenTopLeft *= f3;
            renderer.colorBlueTopLeft *= f3;
            renderer.colorRedBottomLeft *= f4;
            renderer.colorGreenBottomLeft *= f4;
            renderer.colorBlueBottomLeft *= f4;
            renderer.colorRedBottomRight *= f5;
            renderer.colorGreenBottomRight *= f5;
            renderer.colorBlueBottomRight *= f5;
            renderer.colorRedTopRight *= f6;
            renderer.colorGreenTopRight *= f6;
            renderer.colorBlueTopRight *= f6;
            this.renderBottomFace(
                renderer,
                par1Block,
                par2,
                par3,
                par4,
                renderer.getBlockIcon(par1Block, renderer.blockAccess, par2, par3, par4, 0));
            flag = true;
        }

        if (renderer.renderAllFaces || par1Block.shouldSideBeRendered(renderer.blockAccess, par2, par3 + 1, par4, 1)) {
            if (renderer.renderMaxY >= 1.0) {
                par3++;
            }

            renderer.aoBrightnessXYNP = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2 - 1, par3, par4);
            renderer.aoBrightnessXYPP = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2 + 1, par3, par4);
            renderer.aoBrightnessYZPN = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3, par4 - 1);
            renderer.aoBrightnessYZPP = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3, par4 + 1);
            renderer.aoLightValueScratchXYNP = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2 - 1, par3, par4);
            renderer.aoLightValueScratchXYPP = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2 + 1, par3, par4);
            renderer.aoLightValueScratchYZPN = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3, par4 - 1);
            renderer.aoLightValueScratchYZPP = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3, par4 + 1);
            boolean flag3x = renderer.blockAccess.getBlock(par2 + 1, par3 + 1, par4)
                .getCanBlockGrass();
            boolean flag2x = renderer.blockAccess.getBlock(par2 - 1, par3 + 1, par4)
                .getCanBlockGrass();
            boolean flag5x = renderer.blockAccess.getBlock(par2, par3 + 1, par4 + 1)
                .getCanBlockGrass();
            boolean flag4x = renderer.blockAccess.getBlock(par2, par3 + 1, par4 - 1)
                .getCanBlockGrass();
            if (!flag4x && !flag2x) {
                renderer.aoLightValueScratchXYZNPN = renderer.aoLightValueScratchXYNP;
                renderer.aoBrightnessXYZNPN = renderer.aoBrightnessXYNP;
            } else {
                renderer.aoLightValueScratchXYZNPN = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2 - 1, par3, par4 - 1);
                renderer.aoBrightnessXYZNPN = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2 - 1, par3, par4 - 1);
            }

            if (!flag4x && !flag3x) {
                renderer.aoLightValueScratchXYZPPN = renderer.aoLightValueScratchXYPP;
                renderer.aoBrightnessXYZPPN = renderer.aoBrightnessXYPP;
            } else {
                renderer.aoLightValueScratchXYZPPN = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2 + 1, par3, par4 - 1);
                renderer.aoBrightnessXYZPPN = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2 + 1, par3, par4 - 1);
            }

            if (!flag5x && !flag2x) {
                renderer.aoLightValueScratchXYZNPP = renderer.aoLightValueScratchXYNP;
                renderer.aoBrightnessXYZNPP = renderer.aoBrightnessXYNP;
            } else {
                renderer.aoLightValueScratchXYZNPP = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2 - 1, par3, par4 + 1);
                renderer.aoBrightnessXYZNPP = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2 - 1, par3, par4 + 1);
            }

            if (!flag5x && !flag3x) {
                renderer.aoLightValueScratchXYZPPP = renderer.aoLightValueScratchXYPP;
                renderer.aoBrightnessXYZPPP = renderer.aoBrightnessXYPP;
            } else {
                renderer.aoLightValueScratchXYZPPP = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2 + 1, par3, par4 + 1);
                renderer.aoBrightnessXYZPPP = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2 + 1, par3, par4 + 1);
            }

            if (renderer.renderMaxY >= 1.0) {
                par3--;
            }

            int i1x = l;
            if (renderer.renderMaxY >= 1.0 || !renderer.blockAccess.getBlock(par2, par3 + 1, par4)
                .isOpaqueCube()) {
                i1x = par1Block.getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 + 1, par4);
            }

            float f7 = this.getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 + 1, par4);
            f6 = (renderer.aoLightValueScratchXYZNPP + renderer.aoLightValueScratchXYNP
                + renderer.aoLightValueScratchYZPP
                + f7) / 4.0F;
            f3 = (renderer.aoLightValueScratchYZPP + f7
                + renderer.aoLightValueScratchXYZPPP
                + renderer.aoLightValueScratchXYPP) / 4.0F;
            f4 = (f7 + renderer.aoLightValueScratchYZPN
                + renderer.aoLightValueScratchXYPP
                + renderer.aoLightValueScratchXYZPPN) / 4.0F;
            f5 = (renderer.aoLightValueScratchXYNP + renderer.aoLightValueScratchXYZNPN
                + f7
                + renderer.aoLightValueScratchYZPN) / 4.0F;
            renderer.brightnessTopRight = renderer.getAoBrightness(
                renderer.aoBrightnessXYZNPP,
                renderer.aoBrightnessXYNP,
                renderer.aoBrightnessYZPP,
                i1x);
            renderer.brightnessTopLeft = renderer.getAoBrightness(
                renderer.aoBrightnessYZPP,
                renderer.aoBrightnessXYZPPP,
                renderer.aoBrightnessXYPP,
                i1x);
            renderer.brightnessBottomLeft = renderer.getAoBrightness(
                renderer.aoBrightnessYZPN,
                renderer.aoBrightnessXYPP,
                renderer.aoBrightnessXYZPPN,
                i1x);
            renderer.brightnessBottomRight = renderer.getAoBrightness(
                renderer.aoBrightnessXYNP,
                renderer.aoBrightnessXYZNPN,
                renderer.aoBrightnessYZPN,
                i1x);
            renderer.colorRedTopLeft = renderer.colorRedBottomLeft = renderer.colorRedBottomRight = renderer.colorRedTopRight = par5;
            renderer.colorGreenTopLeft = renderer.colorGreenBottomLeft = renderer.colorGreenBottomRight = renderer.colorGreenTopRight = par6;
            renderer.colorBlueTopLeft = renderer.colorBlueBottomLeft = renderer.colorBlueBottomRight = renderer.colorBlueTopRight = par7;
            renderer.colorRedTopLeft *= f3;
            renderer.colorGreenTopLeft *= f3;
            renderer.colorBlueTopLeft *= f3;
            renderer.colorRedBottomLeft *= f4;
            renderer.colorGreenBottomLeft *= f4;
            renderer.colorBlueBottomLeft *= f4;
            renderer.colorRedBottomRight *= f5;
            renderer.colorGreenBottomRight *= f5;
            renderer.colorBlueBottomRight *= f5;
            renderer.colorRedTopRight *= f6;
            renderer.colorGreenTopRight *= f6;
            renderer.colorBlueTopRight *= f6;
            this.renderTopFace(
                renderer,
                par1Block,
                par2,
                par3,
                par4,
                renderer.getBlockIcon(par1Block, renderer.blockAccess, par2, par3, par4, 1));
            flag = true;
        }

        if (renderer.renderAllFaces || par1Block.shouldSideBeRendered(renderer.blockAccess, par2, par3, par4 - 1, 2)) {
            if (renderer.renderMinZ <= 0.0) {
                par4--;
            }

            renderer.aoLightValueScratchXZNN = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2 - 1, par3, par4);
            renderer.aoLightValueScratchYZNN = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 - 1, par4);
            renderer.aoLightValueScratchYZPN = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 + 1, par4);
            renderer.aoLightValueScratchXZPN = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2 + 1, par3, par4);
            renderer.aoBrightnessXZNN = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2 - 1, par3, par4);
            renderer.aoBrightnessYZNN = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 - 1, par4);
            renderer.aoBrightnessYZPN = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 + 1, par4);
            renderer.aoBrightnessXZPN = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2 + 1, par3, par4);
            boolean flag3xx = renderer.blockAccess.getBlock(par2 + 1, par3, par4 - 1)
                .getCanBlockGrass();
            boolean flag2xx = renderer.blockAccess.getBlock(par2 - 1, par3, par4 - 1)
                .getCanBlockGrass();
            boolean flag5xx = renderer.blockAccess.getBlock(par2, par3 + 1, par4 - 1)
                .getCanBlockGrass();
            boolean flag4xx = renderer.blockAccess.getBlock(par2, par3 - 1, par4 - 1)
                .getCanBlockGrass();
            if (!flag2xx && !flag4xx) {
                renderer.aoLightValueScratchXYZNNN = renderer.aoLightValueScratchXZNN;
                renderer.aoBrightnessXYZNNN = renderer.aoBrightnessXZNN;
            } else {
                renderer.aoLightValueScratchXYZNNN = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2 - 1, par3 - 1, par4);
                renderer.aoBrightnessXYZNNN = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2 - 1, par3 - 1, par4);
            }

            if (!flag2xx && !flag5xx) {
                renderer.aoLightValueScratchXYZNPN = renderer.aoLightValueScratchXZNN;
                renderer.aoBrightnessXYZNPN = renderer.aoBrightnessXZNN;
            } else {
                renderer.aoLightValueScratchXYZNPN = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2 - 1, par3 + 1, par4);
                renderer.aoBrightnessXYZNPN = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2 - 1, par3 + 1, par4);
            }

            if (!flag3xx && !flag4xx) {
                renderer.aoLightValueScratchXYZPNN = renderer.aoLightValueScratchXZPN;
                renderer.aoBrightnessXYZPNN = renderer.aoBrightnessXZPN;
            } else {
                renderer.aoLightValueScratchXYZPNN = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2 + 1, par3 - 1, par4);
                renderer.aoBrightnessXYZPNN = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2 + 1, par3 - 1, par4);
            }

            if (!flag3xx && !flag5xx) {
                renderer.aoLightValueScratchXYZPPN = renderer.aoLightValueScratchXZPN;
                renderer.aoBrightnessXYZPPN = renderer.aoBrightnessXZPN;
            } else {
                renderer.aoLightValueScratchXYZPPN = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2 + 1, par3 + 1, par4);
                renderer.aoBrightnessXYZPPN = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2 + 1, par3 + 1, par4);
            }

            if (renderer.renderMinZ <= 0.0) {
                par4++;
            }

            int i1x = l;
            if (renderer.renderMinZ <= 0.0 || !renderer.blockAccess.getBlock(par2, par3, par4 - 1)
                .isOpaqueCube()) {
                i1x = par1Block.getMixedBrightnessForBlock(renderer.blockAccess, par2, par3, par4 - 1);
            }

            float f7 = this.getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3, par4 - 1);
            f3 = (renderer.aoLightValueScratchXZNN + renderer.aoLightValueScratchXYZNPN
                + f7
                + renderer.aoLightValueScratchYZPN) / 4.0F;
            f4 = (f7 + renderer.aoLightValueScratchYZPN
                + renderer.aoLightValueScratchXZPN
                + renderer.aoLightValueScratchXYZPPN) / 4.0F;
            f5 = (renderer.aoLightValueScratchYZNN + f7
                + renderer.aoLightValueScratchXYZPNN
                + renderer.aoLightValueScratchXZPN) / 4.0F;
            f6 = (renderer.aoLightValueScratchXYZNNN + renderer.aoLightValueScratchXZNN
                + renderer.aoLightValueScratchYZNN
                + f7) / 4.0F;
            renderer.brightnessTopLeft = renderer.getAoBrightness(
                renderer.aoBrightnessXZNN,
                renderer.aoBrightnessXYZNPN,
                renderer.aoBrightnessYZPN,
                i1x);
            renderer.brightnessBottomLeft = renderer.getAoBrightness(
                renderer.aoBrightnessYZPN,
                renderer.aoBrightnessXZPN,
                renderer.aoBrightnessXYZPPN,
                i1x);
            renderer.brightnessBottomRight = renderer.getAoBrightness(
                renderer.aoBrightnessYZNN,
                renderer.aoBrightnessXYZPNN,
                renderer.aoBrightnessXZPN,
                i1x);
            renderer.brightnessTopRight = renderer.getAoBrightness(
                renderer.aoBrightnessXYZNNN,
                renderer.aoBrightnessXZNN,
                renderer.aoBrightnessYZNN,
                i1x);
            if (flag1) {
                renderer.colorRedTopLeft = renderer.colorRedBottomLeft = renderer.colorRedBottomRight = renderer.colorRedTopRight = par5
                    * 0.8F;
                renderer.colorGreenTopLeft = renderer.colorGreenBottomLeft = renderer.colorGreenBottomRight = renderer.colorGreenTopRight = par6
                    * 0.8F;
                renderer.colorBlueTopLeft = renderer.colorBlueBottomLeft = renderer.colorBlueBottomRight = renderer.colorBlueTopRight = par7
                    * 0.8F;
            } else {
                renderer.colorRedTopLeft = renderer.colorRedBottomLeft = renderer.colorRedBottomRight = renderer.colorRedTopRight = 0.8F;
                renderer.colorGreenTopLeft = renderer.colorGreenBottomLeft = renderer.colorGreenBottomRight = renderer.colorGreenTopRight = 0.8F;
                renderer.colorBlueTopLeft = renderer.colorBlueBottomLeft = renderer.colorBlueBottomRight = renderer.colorBlueTopRight = 0.8F;
            }

            renderer.colorRedTopLeft *= f3;
            renderer.colorGreenTopLeft *= f3;
            renderer.colorBlueTopLeft *= f3;
            renderer.colorRedBottomLeft *= f4;
            renderer.colorGreenBottomLeft *= f4;
            renderer.colorBlueBottomLeft *= f4;
            renderer.colorRedBottomRight *= f5;
            renderer.colorGreenBottomRight *= f5;
            renderer.colorBlueBottomRight *= f5;
            renderer.colorRedTopRight *= f6;
            renderer.colorGreenTopRight *= f6;
            renderer.colorBlueTopRight *= f6;
            IIcon icon = renderer.getBlockIcon(par1Block, renderer.blockAccess, par2, par3, par4, 2);
            this.renderEastFace(renderer, par1Block, par2, par3, par4, icon);
            if (RenderBlocks.fancyGrass && icon.getIconName()
                .equals("grass_side") && !renderer.hasOverrideBlockTexture()) {
                renderer.colorRedTopLeft *= par5;
                renderer.colorRedBottomLeft *= par5;
                renderer.colorRedBottomRight *= par5;
                renderer.colorRedTopRight *= par5;
                renderer.colorGreenTopLeft *= par6;
                renderer.colorGreenBottomLeft *= par6;
                renderer.colorGreenBottomRight *= par6;
                renderer.colorGreenTopRight *= par6;
                renderer.colorBlueTopLeft *= par7;
                renderer.colorBlueBottomLeft *= par7;
                renderer.colorBlueBottomRight *= par7;
                renderer.colorBlueTopRight *= par7;
                this.renderEastFace(renderer, par1Block, par2, par3, par4, BlockGrass.getIconSideOverlay());
            }

            flag = true;
        }

        if (renderer.renderAllFaces || par1Block.shouldSideBeRendered(renderer.blockAccess, par2, par3, par4 + 1, 3)) {
            if (renderer.renderMaxZ >= 1.0) {
                par4++;
            }

            renderer.aoLightValueScratchXZNP = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2 - 1, par3, par4);
            renderer.aoLightValueScratchXZPP = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2 + 1, par3, par4);
            renderer.aoLightValueScratchYZNP = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 - 1, par4);
            renderer.aoLightValueScratchYZPP = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 + 1, par4);
            renderer.aoBrightnessXZNP = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2 - 1, par3, par4);
            renderer.aoBrightnessXZPP = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2 + 1, par3, par4);
            renderer.aoBrightnessYZNP = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 - 1, par4);
            renderer.aoBrightnessYZPP = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 + 1, par4);
            boolean flag3xxx = renderer.blockAccess.getBlock(par2 + 1, par3, par4 + 1)
                .getCanBlockGrass();
            boolean flag2xxx = renderer.blockAccess.getBlock(par2 - 1, par3, par4 + 1)
                .getCanBlockGrass();
            boolean flag5xxx = renderer.blockAccess.getBlock(par2, par3 + 1, par4 + 1)
                .getCanBlockGrass();
            boolean flag4xxx = renderer.blockAccess.getBlock(par2, par3 - 1, par4 + 1)
                .getCanBlockGrass();
            if (!flag2xxx && !flag4xxx) {
                renderer.aoLightValueScratchXYZNNP = renderer.aoLightValueScratchXZNP;
                renderer.aoBrightnessXYZNNP = renderer.aoBrightnessXZNP;
            } else {
                renderer.aoLightValueScratchXYZNNP = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2 - 1, par3 - 1, par4);
                renderer.aoBrightnessXYZNNP = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2 - 1, par3 - 1, par4);
            }

            if (!flag2xxx && !flag5xxx) {
                renderer.aoLightValueScratchXYZNPP = renderer.aoLightValueScratchXZNP;
                renderer.aoBrightnessXYZNPP = renderer.aoBrightnessXZNP;
            } else {
                renderer.aoLightValueScratchXYZNPP = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2 - 1, par3 + 1, par4);
                renderer.aoBrightnessXYZNPP = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2 - 1, par3 + 1, par4);
            }

            if (!flag3xxx && !flag4xxx) {
                renderer.aoLightValueScratchXYZPNP = renderer.aoLightValueScratchXZPP;
                renderer.aoBrightnessXYZPNP = renderer.aoBrightnessXZPP;
            } else {
                renderer.aoLightValueScratchXYZPNP = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2 + 1, par3 - 1, par4);
                renderer.aoBrightnessXYZPNP = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2 + 1, par3 - 1, par4);
            }

            if (!flag3xxx && !flag5xxx) {
                renderer.aoLightValueScratchXYZPPP = renderer.aoLightValueScratchXZPP;
                renderer.aoBrightnessXYZPPP = renderer.aoBrightnessXZPP;
            } else {
                renderer.aoLightValueScratchXYZPPP = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2 + 1, par3 + 1, par4);
                renderer.aoBrightnessXYZPPP = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2 + 1, par3 + 1, par4);
            }

            if (renderer.renderMaxZ >= 1.0) {
                par4--;
            }

            int i1xx = l;
            if (renderer.renderMaxZ >= 1.0 || !renderer.blockAccess.getBlock(par2, par3, par4 + 1)
                .isOpaqueCube()) {
                i1xx = par1Block.getMixedBrightnessForBlock(renderer.blockAccess, par2, par3, par4 + 1);
            }

            float f7x = this.getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3, par4 + 1);
            f3 = (renderer.aoLightValueScratchXZNP + renderer.aoLightValueScratchXYZNPP
                + f7x
                + renderer.aoLightValueScratchYZPP) / 4.0F;
            f6 = (f7x + renderer.aoLightValueScratchYZPP
                + renderer.aoLightValueScratchXZPP
                + renderer.aoLightValueScratchXYZPPP) / 4.0F;
            f5 = (renderer.aoLightValueScratchYZNP + f7x
                + renderer.aoLightValueScratchXYZPNP
                + renderer.aoLightValueScratchXZPP) / 4.0F;
            f4 = (renderer.aoLightValueScratchXYZNNP + renderer.aoLightValueScratchXZNP
                + renderer.aoLightValueScratchYZNP
                + f7x) / 4.0F;
            renderer.brightnessTopLeft = renderer.getAoBrightness(
                renderer.aoBrightnessXZNP,
                renderer.aoBrightnessXYZNPP,
                renderer.aoBrightnessYZPP,
                i1xx);
            renderer.brightnessTopRight = renderer.getAoBrightness(
                renderer.aoBrightnessYZPP,
                renderer.aoBrightnessXZPP,
                renderer.aoBrightnessXYZPPP,
                i1xx);
            renderer.brightnessBottomRight = renderer.getAoBrightness(
                renderer.aoBrightnessYZNP,
                renderer.aoBrightnessXYZPNP,
                renderer.aoBrightnessXZPP,
                i1xx);
            renderer.brightnessBottomLeft = renderer.getAoBrightness(
                renderer.aoBrightnessXYZNNP,
                renderer.aoBrightnessXZNP,
                renderer.aoBrightnessYZNP,
                i1xx);
            if (flag1) {
                renderer.colorRedTopLeft = renderer.colorRedBottomLeft = renderer.colorRedBottomRight = renderer.colorRedTopRight = par5
                    * 0.8F;
                renderer.colorGreenTopLeft = renderer.colorGreenBottomLeft = renderer.colorGreenBottomRight = renderer.colorGreenTopRight = par6
                    * 0.8F;
                renderer.colorBlueTopLeft = renderer.colorBlueBottomLeft = renderer.colorBlueBottomRight = renderer.colorBlueTopRight = par7
                    * 0.8F;
            } else {
                renderer.colorRedTopLeft = renderer.colorRedBottomLeft = renderer.colorRedBottomRight = renderer.colorRedTopRight = 0.8F;
                renderer.colorGreenTopLeft = renderer.colorGreenBottomLeft = renderer.colorGreenBottomRight = renderer.colorGreenTopRight = 0.8F;
                renderer.colorBlueTopLeft = renderer.colorBlueBottomLeft = renderer.colorBlueBottomRight = renderer.colorBlueTopRight = 0.8F;
            }

            renderer.colorRedTopLeft *= f3;
            renderer.colorGreenTopLeft *= f3;
            renderer.colorBlueTopLeft *= f3;
            renderer.colorRedBottomLeft *= f4;
            renderer.colorGreenBottomLeft *= f4;
            renderer.colorBlueBottomLeft *= f4;
            renderer.colorRedBottomRight *= f5;
            renderer.colorGreenBottomRight *= f5;
            renderer.colorBlueBottomRight *= f5;
            renderer.colorRedTopRight *= f6;
            renderer.colorGreenTopRight *= f6;
            renderer.colorBlueTopRight *= f6;
            IIcon icon = renderer.getBlockIcon(par1Block, renderer.blockAccess, par2, par3, par4, 3);
            this.renderWestFace(
                renderer,
                par1Block,
                par2,
                par3,
                par4,
                renderer.getBlockIcon(par1Block, renderer.blockAccess, par2, par3, par4, 3));
            if (RenderBlocks.fancyGrass && icon.getIconName()
                .equals("grass_side") && !renderer.hasOverrideBlockTexture()) {
                renderer.colorRedTopLeft *= par5;
                renderer.colorRedBottomLeft *= par5;
                renderer.colorRedBottomRight *= par5;
                renderer.colorRedTopRight *= par5;
                renderer.colorGreenTopLeft *= par6;
                renderer.colorGreenBottomLeft *= par6;
                renderer.colorGreenBottomRight *= par6;
                renderer.colorGreenTopRight *= par6;
                renderer.colorBlueTopLeft *= par7;
                renderer.colorBlueBottomLeft *= par7;
                renderer.colorBlueBottomRight *= par7;
                renderer.colorBlueTopRight *= par7;
                this.renderWestFace(renderer, par1Block, par2, par3, par4, BlockGrass.getIconSideOverlay());
            }

            flag = true;
        }

        if (renderer.renderAllFaces || par1Block.shouldSideBeRendered(renderer.blockAccess, par2 - 1, par3, par4, 4)) {
            if (renderer.renderMinX <= 0.0) {
                par2--;
            }

            renderer.aoLightValueScratchXYNN = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 - 1, par4);
            renderer.aoLightValueScratchXZNN = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3, par4 - 1);
            renderer.aoLightValueScratchXZNP = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3, par4 + 1);
            renderer.aoLightValueScratchXYNP = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 + 1, par4);
            renderer.aoBrightnessXYNN = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 - 1, par4);
            renderer.aoBrightnessXZNN = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3, par4 - 1);
            renderer.aoBrightnessXZNP = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3, par4 + 1);
            renderer.aoBrightnessXYNP = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 + 1, par4);
            boolean flag3xxxx = renderer.blockAccess.getBlock(par2 - 1, par3 + 1, par4)
                .getCanBlockGrass();
            boolean flag2xxxx = renderer.blockAccess.getBlock(par2 - 1, par3 - 1, par4)
                .getCanBlockGrass();
            boolean flag5xxxx = renderer.blockAccess.getBlock(par2 - 1, par3, par4 - 1)
                .getCanBlockGrass();
            boolean flag4xxxx = renderer.blockAccess.getBlock(par2 - 1, par3, par4 + 1)
                .getCanBlockGrass();
            if (!flag5xxxx && !flag2xxxx) {
                renderer.aoLightValueScratchXYZNNN = renderer.aoLightValueScratchXZNN;
                renderer.aoBrightnessXYZNNN = renderer.aoBrightnessXZNN;
            } else {
                renderer.aoLightValueScratchXYZNNN = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 - 1, par4 - 1);
                renderer.aoBrightnessXYZNNN = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 - 1, par4 - 1);
            }

            if (!flag4xxxx && !flag2xxxx) {
                renderer.aoLightValueScratchXYZNNP = renderer.aoLightValueScratchXZNP;
                renderer.aoBrightnessXYZNNP = renderer.aoBrightnessXZNP;
            } else {
                renderer.aoLightValueScratchXYZNNP = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 - 1, par4 + 1);
                renderer.aoBrightnessXYZNNP = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 - 1, par4 + 1);
            }

            if (!flag5xxxx && !flag3xxxx) {
                renderer.aoLightValueScratchXYZNPN = renderer.aoLightValueScratchXZNN;
                renderer.aoBrightnessXYZNPN = renderer.aoBrightnessXZNN;
            } else {
                renderer.aoLightValueScratchXYZNPN = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 + 1, par4 - 1);
                renderer.aoBrightnessXYZNPN = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 + 1, par4 - 1);
            }

            if (!flag4xxxx && !flag3xxxx) {
                renderer.aoLightValueScratchXYZNPP = renderer.aoLightValueScratchXZNP;
                renderer.aoBrightnessXYZNPP = renderer.aoBrightnessXZNP;
            } else {
                renderer.aoLightValueScratchXYZNPP = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 + 1, par4 + 1);
                renderer.aoBrightnessXYZNPP = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 + 1, par4 + 1);
            }

            if (renderer.renderMinX <= 0.0) {
                par2++;
            }

            int i1xxx = l;
            if (renderer.renderMinX <= 0.0 || !renderer.blockAccess.getBlock(par2 - 1, par3, par4)
                .isOpaqueCube()) {
                i1xxx = par1Block.getMixedBrightnessForBlock(renderer.blockAccess, par2 - 1, par3, par4);
            }

            float f7xx = this.getAmbientOcclusionLightValue(renderer.blockAccess, par2 - 1, par3, par4);
            f6 = (renderer.aoLightValueScratchXYNN + renderer.aoLightValueScratchXYZNNP
                + f7xx
                + renderer.aoLightValueScratchXZNP) / 4.0F;
            f3 = (f7xx + renderer.aoLightValueScratchXZNP
                + renderer.aoLightValueScratchXYNP
                + renderer.aoLightValueScratchXYZNPP) / 4.0F;
            f4 = (renderer.aoLightValueScratchXZNN + f7xx
                + renderer.aoLightValueScratchXYZNPN
                + renderer.aoLightValueScratchXYNP) / 4.0F;
            f5 = (renderer.aoLightValueScratchXYZNNN + renderer.aoLightValueScratchXYNN
                + renderer.aoLightValueScratchXZNN
                + f7xx) / 4.0F;
            renderer.brightnessTopRight = renderer.getAoBrightness(
                renderer.aoBrightnessXYNN,
                renderer.aoBrightnessXYZNNP,
                renderer.aoBrightnessXZNP,
                i1xxx);
            renderer.brightnessTopLeft = renderer.getAoBrightness(
                renderer.aoBrightnessXZNP,
                renderer.aoBrightnessXYNP,
                renderer.aoBrightnessXYZNPP,
                i1xxx);
            renderer.brightnessBottomLeft = renderer.getAoBrightness(
                renderer.aoBrightnessXZNN,
                renderer.aoBrightnessXYZNPN,
                renderer.aoBrightnessXYNP,
                i1xxx);
            renderer.brightnessBottomRight = renderer.getAoBrightness(
                renderer.aoBrightnessXYZNNN,
                renderer.aoBrightnessXYNN,
                renderer.aoBrightnessXZNN,
                i1xxx);
            if (flag1) {
                renderer.colorRedTopLeft = renderer.colorRedBottomLeft = renderer.colorRedBottomRight = renderer.colorRedTopRight = par5
                    * 0.6F;
                renderer.colorGreenTopLeft = renderer.colorGreenBottomLeft = renderer.colorGreenBottomRight = renderer.colorGreenTopRight = par6
                    * 0.6F;
                renderer.colorBlueTopLeft = renderer.colorBlueBottomLeft = renderer.colorBlueBottomRight = renderer.colorBlueTopRight = par7
                    * 0.6F;
            } else {
                renderer.colorRedTopLeft = renderer.colorRedBottomLeft = renderer.colorRedBottomRight = renderer.colorRedTopRight = 0.6F;
                renderer.colorGreenTopLeft = renderer.colorGreenBottomLeft = renderer.colorGreenBottomRight = renderer.colorGreenTopRight = 0.6F;
                renderer.colorBlueTopLeft = renderer.colorBlueBottomLeft = renderer.colorBlueBottomRight = renderer.colorBlueTopRight = 0.6F;
            }

            renderer.colorRedTopLeft *= f3;
            renderer.colorGreenTopLeft *= f3;
            renderer.colorBlueTopLeft *= f3;
            renderer.colorRedBottomLeft *= f4;
            renderer.colorGreenBottomLeft *= f4;
            renderer.colorBlueBottomLeft *= f4;
            renderer.colorRedBottomRight *= f5;
            renderer.colorGreenBottomRight *= f5;
            renderer.colorBlueBottomRight *= f5;
            renderer.colorRedTopRight *= f6;
            renderer.colorGreenTopRight *= f6;
            renderer.colorBlueTopRight *= f6;
            IIcon icon = renderer.getBlockIcon(par1Block, renderer.blockAccess, par2, par3, par4, 4);
            this.renderNorthFace(renderer, par1Block, par2, par3, par4, icon);
            if (RenderBlocks.fancyGrass && icon.getIconName()
                .equals("grass_side") && !renderer.hasOverrideBlockTexture()) {
                renderer.colorRedTopLeft *= par5;
                renderer.colorRedBottomLeft *= par5;
                renderer.colorRedBottomRight *= par5;
                renderer.colorRedTopRight *= par5;
                renderer.colorGreenTopLeft *= par6;
                renderer.colorGreenBottomLeft *= par6;
                renderer.colorGreenBottomRight *= par6;
                renderer.colorGreenTopRight *= par6;
                renderer.colorBlueTopLeft *= par7;
                renderer.colorBlueBottomLeft *= par7;
                renderer.colorBlueBottomRight *= par7;
                renderer.colorBlueTopRight *= par7;
                this.renderNorthFace(renderer, par1Block, par2, par3, par4, BlockGrass.getIconSideOverlay());
            }

            flag = true;
        }

        if (renderer.renderAllFaces || par1Block.shouldSideBeRendered(renderer.blockAccess, par2 + 1, par3, par4, 5)) {
            if (renderer.renderMaxX >= 1.0) {
                par2++;
            }

            renderer.aoLightValueScratchXYPN = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 - 1, par4);
            renderer.aoLightValueScratchXZPN = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3, par4 - 1);
            renderer.aoLightValueScratchXZPP = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3, par4 + 1);
            renderer.aoLightValueScratchXYPP = this
                .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 + 1, par4);
            renderer.aoBrightnessXYPN = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 - 1, par4);
            renderer.aoBrightnessXZPN = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3, par4 - 1);
            renderer.aoBrightnessXZPP = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3, par4 + 1);
            renderer.aoBrightnessXYPP = par1Block
                .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 + 1, par4);
            boolean flag3xxxxx = renderer.blockAccess.getBlock(par2 + 1, par3 + 1, par4)
                .getCanBlockGrass();
            boolean flag2xxxxx = renderer.blockAccess.getBlock(par2 + 1, par3 - 1, par4)
                .getCanBlockGrass();
            boolean flag5xxxxx = renderer.blockAccess.getBlock(par2 + 1, par3, par4 + 1)
                .getCanBlockGrass();
            boolean flag4xxxxx = renderer.blockAccess.getBlock(par2 + 1, par3, par4 - 1)
                .getCanBlockGrass();
            if (!flag2xxxxx && !flag4xxxxx) {
                renderer.aoLightValueScratchXYZPNN = renderer.aoLightValueScratchXZPN;
                renderer.aoBrightnessXYZPNN = renderer.aoBrightnessXZPN;
            } else {
                renderer.aoLightValueScratchXYZPNN = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 - 1, par4 - 1);
                renderer.aoBrightnessXYZPNN = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 - 1, par4 - 1);
            }

            if (!flag2xxxxx && !flag5xxxxx) {
                renderer.aoLightValueScratchXYZPNP = renderer.aoLightValueScratchXZPP;
                renderer.aoBrightnessXYZPNP = renderer.aoBrightnessXZPP;
            } else {
                renderer.aoLightValueScratchXYZPNP = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 - 1, par4 + 1);
                renderer.aoBrightnessXYZPNP = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 - 1, par4 + 1);
            }

            if (!flag3xxxxx && !flag4xxxxx) {
                renderer.aoLightValueScratchXYZPPN = renderer.aoLightValueScratchXZPN;
                renderer.aoBrightnessXYZPPN = renderer.aoBrightnessXZPN;
            } else {
                renderer.aoLightValueScratchXYZPPN = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 + 1, par4 - 1);
                renderer.aoBrightnessXYZPPN = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 + 1, par4 - 1);
            }

            if (!flag3xxxxx && !flag5xxxxx) {
                renderer.aoLightValueScratchXYZPPP = renderer.aoLightValueScratchXZPP;
                renderer.aoBrightnessXYZPPP = renderer.aoBrightnessXZPP;
            } else {
                renderer.aoLightValueScratchXYZPPP = this
                    .getAmbientOcclusionLightValue(renderer.blockAccess, par2, par3 + 1, par4 + 1);
                renderer.aoBrightnessXYZPPP = par1Block
                    .getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 + 1, par4 + 1);
            }

            if (renderer.renderMaxX >= 1.0) {
                par2--;
            }

            int i1xxxx = l;
            if (renderer.renderMaxX >= 1.0 || !renderer.blockAccess.getBlock(par2 + 1, par3, par4)
                .isOpaqueCube()) {
                i1xxxx = par1Block.getMixedBrightnessForBlock(renderer.blockAccess, par2 + 1, par3, par4);
            }

            float f7xxx = this.getAmbientOcclusionLightValue(renderer.blockAccess, par2 + 1, par3, par4);
            f3 = (renderer.aoLightValueScratchXYPN + renderer.aoLightValueScratchXYZPNP
                + f7xxx
                + renderer.aoLightValueScratchXZPP) / 4.0F;
            f4 = (renderer.aoLightValueScratchXYZPNN + renderer.aoLightValueScratchXYPN
                + renderer.aoLightValueScratchXZPN
                + f7xxx) / 4.0F;
            f5 = (renderer.aoLightValueScratchXZPN + f7xxx
                + renderer.aoLightValueScratchXYZPPN
                + renderer.aoLightValueScratchXYPP) / 4.0F;
            f6 = (f7xxx + renderer.aoLightValueScratchXZPP
                + renderer.aoLightValueScratchXYPP
                + renderer.aoLightValueScratchXYZPPP) / 4.0F;
            renderer.brightnessTopLeft = renderer.getAoBrightness(
                renderer.aoBrightnessXYPN,
                renderer.aoBrightnessXYZPNP,
                renderer.aoBrightnessXZPP,
                i1xxxx);
            renderer.brightnessTopRight = renderer.getAoBrightness(
                renderer.aoBrightnessXZPP,
                renderer.aoBrightnessXYPP,
                renderer.aoBrightnessXYZPPP,
                i1xxxx);
            renderer.brightnessBottomRight = renderer.getAoBrightness(
                renderer.aoBrightnessXZPN,
                renderer.aoBrightnessXYZPPN,
                renderer.aoBrightnessXYPP,
                i1xxxx);
            renderer.brightnessBottomLeft = renderer.getAoBrightness(
                renderer.aoBrightnessXYZPNN,
                renderer.aoBrightnessXYPN,
                renderer.aoBrightnessXZPN,
                i1xxxx);
            if (flag1) {
                renderer.colorRedTopLeft = renderer.colorRedBottomLeft = renderer.colorRedBottomRight = renderer.colorRedTopRight = par5
                    * 0.6F;
                renderer.colorGreenTopLeft = renderer.colorGreenBottomLeft = renderer.colorGreenBottomRight = renderer.colorGreenTopRight = par6
                    * 0.6F;
                renderer.colorBlueTopLeft = renderer.colorBlueBottomLeft = renderer.colorBlueBottomRight = renderer.colorBlueTopRight = par7
                    * 0.6F;
            } else {
                renderer.colorRedTopLeft = renderer.colorRedBottomLeft = renderer.colorRedBottomRight = renderer.colorRedTopRight = 0.6F;
                renderer.colorGreenTopLeft = renderer.colorGreenBottomLeft = renderer.colorGreenBottomRight = renderer.colorGreenTopRight = 0.6F;
                renderer.colorBlueTopLeft = renderer.colorBlueBottomLeft = renderer.colorBlueBottomRight = renderer.colorBlueTopRight = 0.6F;
            }

            renderer.colorRedTopLeft *= f3;
            renderer.colorGreenTopLeft *= f3;
            renderer.colorBlueTopLeft *= f3;
            renderer.colorRedBottomLeft *= f4;
            renderer.colorGreenBottomLeft *= f4;
            renderer.colorBlueBottomLeft *= f4;
            renderer.colorRedBottomRight *= f5;
            renderer.colorGreenBottomRight *= f5;
            renderer.colorBlueBottomRight *= f5;
            renderer.colorRedTopRight *= f6;
            renderer.colorGreenTopRight *= f6;
            renderer.colorBlueTopRight *= f6;
            IIcon icon = renderer.getBlockIcon(par1Block, renderer.blockAccess, par2, par3, par4, 5);
            this.renderSouthFace(renderer, par1Block, par2, par3, par4, icon);
            if (RenderBlocks.fancyGrass && icon.getIconName()
                .equals("grass_side") && !renderer.hasOverrideBlockTexture()) {
                renderer.colorRedTopLeft *= par5;
                renderer.colorRedBottomLeft *= par5;
                renderer.colorRedBottomRight *= par5;
                renderer.colorRedTopRight *= par5;
                renderer.colorGreenTopLeft *= par6;
                renderer.colorGreenBottomLeft *= par6;
                renderer.colorGreenBottomRight *= par6;
                renderer.colorGreenTopRight *= par6;
                renderer.colorBlueTopLeft *= par7;
                renderer.colorBlueBottomLeft *= par7;
                renderer.colorBlueBottomRight *= par7;
                renderer.colorBlueTopRight *= par7;
                this.renderSouthFace(renderer, par1Block, par2, par3, par4, BlockGrass.getIconSideOverlay());
            }

            flag = true;
        }

        renderer.enableAO = false;
        return flag;
    }

    public boolean renderStandardBlockWithColorMultiplier(RenderBlocks renderer, Block par1Block, int par2, int par3,
        int par4, float par5, float par6, float par7) {
        renderer.enableAO = false;
        Tessellator tessellator = Tessellator.instance;
        boolean flag = false;
        float f3 = 0.5F;
        float f4 = 1.0F;
        float f5 = 0.8F;
        float f6 = 0.6F;
        float f7 = f4 * par5;
        float f8 = f4 * par6;
        float f9 = f4 * par7;
        float f10 = f3;
        float f11 = f5;
        float f12 = f6;
        float f13 = f3;
        float f14 = f5;
        float f15 = f6;
        float f16 = f3;
        float f17 = f5;
        float f18 = f6;
        if (par1Block != Blocks.grass) {
            f10 = f3 * par5;
            f11 = f5 * par5;
            f12 = f6 * par5;
            f13 = f3 * par6;
            f14 = f5 * par6;
            f15 = f6 * par6;
            f16 = f3 * par7;
            f17 = f5 * par7;
            f18 = f6 * par7;
        }

        int l = par1Block.getMixedBrightnessForBlock(renderer.blockAccess, par2, par3, par4);
        if (renderer.renderAllFaces || par1Block.shouldSideBeRendered(renderer.blockAccess, par2, par3 - 1, par4, 0)) {
            tessellator.setBrightness(
                renderer.renderMinY > 0.0 ? l
                    : par1Block.getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 - 1, par4));
            tessellator.setColorOpaque_F(f10, f13, f16);
            this.renderBottomFace(
                renderer,
                par1Block,
                par2,
                par3,
                par4,
                renderer.getBlockIcon(par1Block, renderer.blockAccess, par2, par3, par4, 0));
            flag = true;
        }

        if (renderer.renderAllFaces || par1Block.shouldSideBeRendered(renderer.blockAccess, par2, par3 + 1, par4, 1)) {
            tessellator.setBrightness(
                renderer.renderMaxY < 1.0 ? l
                    : par1Block.getMixedBrightnessForBlock(renderer.blockAccess, par2, par3 + 1, par4));
            tessellator.setColorOpaque_F(f7, f8, f9);
            this.renderTopFace(
                renderer,
                par1Block,
                par2,
                par3,
                par4,
                renderer.getBlockIcon(par1Block, renderer.blockAccess, par2, par3, par4, 1));
            flag = true;
        }

        if (renderer.renderAllFaces || par1Block.shouldSideBeRendered(renderer.blockAccess, par2, par3, par4 - 1, 2)) {
            tessellator.setBrightness(
                renderer.renderMinZ > 0.0 ? l
                    : par1Block.getMixedBrightnessForBlock(renderer.blockAccess, par2, par3, par4 - 1));
            tessellator.setColorOpaque_F(f11, f14, f17);
            IIcon icon = renderer.getBlockIcon(par1Block, renderer.blockAccess, par2, par3, par4, 2);
            this.renderEastFace(renderer, par1Block, par2, par3, par4, icon);
            if (RenderBlocks.fancyGrass && icon.getIconName()
                .equals("grass_side") && !renderer.hasOverrideBlockTexture()) {
                tessellator.setColorOpaque_F(f11 * par5, f14 * par6, f17 * par7);
                this.renderEastFace(renderer, par1Block, par2, par3, par4, BlockGrass.getIconSideOverlay());
            }

            flag = true;
        }

        if (renderer.renderAllFaces || par1Block.shouldSideBeRendered(renderer.blockAccess, par2, par3, par4 + 1, 3)) {
            tessellator.setBrightness(
                renderer.renderMaxZ < 1.0 ? l
                    : par1Block.getMixedBrightnessForBlock(renderer.blockAccess, par2, par3, par4 + 1));
            tessellator.setColorOpaque_F(f11, f14, f17);
            IIcon icon = renderer.getBlockIcon(par1Block, renderer.blockAccess, par2, par3, par4, 3);
            this.renderWestFace(renderer, par1Block, par2, par3, par4, icon);
            if (RenderBlocks.fancyGrass && icon.getIconName()
                .equals("grass_side") && !renderer.hasOverrideBlockTexture()) {
                tessellator.setColorOpaque_F(f11 * par5, f14 * par6, f17 * par7);
                this.renderWestFace(renderer, par1Block, par2, par3, par4, BlockGrass.getIconSideOverlay());
            }

            flag = true;
        }

        if (renderer.renderAllFaces || par1Block.shouldSideBeRendered(renderer.blockAccess, par2 - 1, par3, par4, 4)) {
            tessellator.setBrightness(
                renderer.renderMinX > 0.0 ? l
                    : par1Block.getMixedBrightnessForBlock(renderer.blockAccess, par2 - 1, par3, par4));
            tessellator.setColorOpaque_F(f12, f15, f18);
            IIcon icon = renderer.getBlockIcon(par1Block, renderer.blockAccess, par2, par3, par4, 4);
            this.renderNorthFace(renderer, par1Block, par2, par3, par4, icon);
            if (RenderBlocks.fancyGrass && icon.getIconName()
                .equals("grass_side") && !renderer.hasOverrideBlockTexture()) {
                tessellator.setColorOpaque_F(f12 * par5, f15 * par6, f18 * par7);
                this.renderNorthFace(renderer, par1Block, par2, par3, par4, BlockGrass.getIconSideOverlay());
            }

            flag = true;
        }

        if (renderer.renderAllFaces || par1Block.shouldSideBeRendered(renderer.blockAccess, par2 + 1, par3, par4, 5)) {
            tessellator.setBrightness(
                renderer.renderMaxX < 1.0 ? l
                    : par1Block.getMixedBrightnessForBlock(renderer.blockAccess, par2 + 1, par3, par4));
            tessellator.setColorOpaque_F(f12, f15, f18);
            IIcon icon = renderer.getBlockIcon(par1Block, renderer.blockAccess, par2, par3, par4, 5);
            this.renderSouthFace(renderer, par1Block, par2, par3, par4, icon);
            if (RenderBlocks.fancyGrass && icon.getIconName()
                .equals("grass_side") && !renderer.hasOverrideBlockTexture()) {
                tessellator.setColorOpaque_F(f12 * par5, f15 * par6, f18 * par7);
                this.renderSouthFace(renderer, par1Block, par2, par3, par4, BlockGrass.getIconSideOverlay());
            }

            flag = true;
        }

        return flag;
    }

    public void renderBottomFace(RenderBlocks renderer, Block par1Block, double par2, double par4, double par6,
        IIcon par8Icon) {
        Tessellator tessellator = Tessellator.instance;
        if (renderer.hasOverrideBlockTexture()) {
            par8Icon = renderer.overrideBlockTexture;
        }

        double d3 = par8Icon.getInterpolatedU(renderer.renderMinX * 16.0);
        double d4 = par8Icon.getInterpolatedU(renderer.renderMaxX * 16.0);
        double d5 = par8Icon.getInterpolatedV(renderer.renderMinZ * 16.0);
        double d6 = par8Icon.getInterpolatedV(renderer.renderMaxZ * 16.0);
        if (renderer.renderMinX < 0.0 || renderer.renderMaxX > 1.0) {
            d3 = par8Icon.getMinU();
            d4 = par8Icon.getMaxU();
        }

        if (renderer.renderMinZ < 0.0 || renderer.renderMaxZ > 1.0) {
            d5 = par8Icon.getMinV();
            d6 = par8Icon.getMaxV();
        }

        double d7 = d4;
        double d8 = d3;
        double d9 = d5;
        double d10 = d6;
        if (renderer.uvRotateBottom == 2) {
            d3 = par8Icon.getInterpolatedU(renderer.renderMinZ * 16.0);
            d5 = par8Icon.getInterpolatedV(16.0 - renderer.renderMaxX * 16.0);
            d4 = par8Icon.getInterpolatedU(renderer.renderMaxZ * 16.0);
            d6 = par8Icon.getInterpolatedV(16.0 - renderer.renderMinX * 16.0);
            d9 = d5;
            d10 = d6;
            d7 = d3;
            d8 = d4;
            d5 = d6;
            d6 = d5;
        } else if (renderer.uvRotateBottom == 1) {
            d3 = par8Icon.getInterpolatedU(16.0 - renderer.renderMaxZ * 16.0);
            d5 = par8Icon.getInterpolatedV(renderer.renderMinX * 16.0);
            d4 = par8Icon.getInterpolatedU(16.0 - renderer.renderMinZ * 16.0);
            d6 = par8Icon.getInterpolatedV(renderer.renderMaxX * 16.0);
            d7 = d4;
            d8 = d3;
            d3 = d4;
            d4 = d3;
            d9 = d6;
            d10 = d5;
        } else if (renderer.uvRotateBottom == 3) {
            d3 = par8Icon.getInterpolatedU(16.0 - renderer.renderMinX * 16.0);
            d4 = par8Icon.getInterpolatedU(16.0 - renderer.renderMaxX * 16.0);
            d5 = par8Icon.getInterpolatedV(16.0 - renderer.renderMinZ * 16.0);
            d6 = par8Icon.getInterpolatedV(16.0 - renderer.renderMaxZ * 16.0);
            d7 = d4;
            d8 = d3;
            d9 = d5;
            d10 = d6;
        }

        double d11 = par2 + renderer.renderMinX;
        double d12 = par2 + renderer.renderMaxX;
        double d13 = par4 + renderer.renderMinY;
        double d14 = par6 + renderer.renderMinZ;
        double d15 = par6 + renderer.renderMaxZ;
        if (renderer.enableAO) {
            tessellator
                .setColorOpaque_F(renderer.colorRedTopRight, renderer.colorGreenTopRight, renderer.colorBlueTopRight);
            tessellator.setBrightness(renderer.brightnessTopRight);
            tessellator.addVertexWithUV(d12, d13, d15, d4, d6);
            tessellator.setColorOpaque_F(
                renderer.colorRedBottomRight,
                renderer.colorGreenBottomRight,
                renderer.colorBlueBottomRight);
            tessellator.setBrightness(renderer.brightnessBottomRight);
            tessellator.addVertexWithUV(d12, d13, d14, d7, d9);
            tessellator.setColorOpaque_F(
                renderer.colorRedBottomLeft,
                renderer.colorGreenBottomLeft,
                renderer.colorBlueBottomLeft);
            tessellator.setBrightness(renderer.brightnessBottomLeft);
            tessellator.addVertexWithUV(d11, d13, d14, d3, d5);
            tessellator
                .setColorOpaque_F(renderer.colorRedTopLeft, renderer.colorGreenTopLeft, renderer.colorBlueTopLeft);
            tessellator.setBrightness(renderer.brightnessTopLeft);
            tessellator.addVertexWithUV(d11, d13, d15, d8, d10);
        } else {
            tessellator.addVertexWithUV(d12, d13, d15, d4, d6);
            tessellator.addVertexWithUV(d12, d13, d14, d7, d9);
            tessellator.addVertexWithUV(d11, d13, d14, d3, d5);
            tessellator.addVertexWithUV(d11, d13, d15, d8, d10);
        }
    }

    public void renderTopFace(RenderBlocks renderer, Block par1Block, double par2, double par4, double par6,
        IIcon par8Icon) {
        Tessellator tessellator = Tessellator.instance;
        if (renderer.hasOverrideBlockTexture()) {
            par8Icon = renderer.overrideBlockTexture;
        }

        double d3 = par8Icon.getInterpolatedU(renderer.renderMinX * 16.0);
        double d4 = par8Icon.getInterpolatedU(renderer.renderMaxX * 16.0);
        double d5 = par8Icon.getInterpolatedV(renderer.renderMinZ * 16.0);
        double d6 = par8Icon.getInterpolatedV(renderer.renderMaxZ * 16.0);
        if (renderer.renderMinX < 0.0 || renderer.renderMaxX > 1.0) {
            d3 = par8Icon.getMinU();
            d4 = par8Icon.getMaxU();
        }

        if (renderer.renderMinZ < 0.0 || renderer.renderMaxZ > 1.0) {
            d5 = par8Icon.getMinV();
            d6 = par8Icon.getMaxV();
        }

        double d7 = d4;
        double d8 = d3;
        double d9 = d5;
        double d10 = d6;
        if (renderer.uvRotateTop == 1) {
            d3 = par8Icon.getInterpolatedU(renderer.renderMinZ * 16.0);
            d5 = par8Icon.getInterpolatedV(16.0 - renderer.renderMaxX * 16.0);
            d4 = par8Icon.getInterpolatedU(renderer.renderMaxZ * 16.0);
            d6 = par8Icon.getInterpolatedV(16.0 - renderer.renderMinX * 16.0);
            d9 = d5;
            d10 = d6;
            d7 = d3;
            d8 = d4;
            d5 = d6;
            d6 = d5;
        } else if (renderer.uvRotateTop == 2) {
            d3 = par8Icon.getInterpolatedU(16.0 - renderer.renderMaxZ * 16.0);
            d5 = par8Icon.getInterpolatedV(renderer.renderMinX * 16.0);
            d4 = par8Icon.getInterpolatedU(16.0 - renderer.renderMinZ * 16.0);
            d6 = par8Icon.getInterpolatedV(renderer.renderMaxX * 16.0);
            d7 = d4;
            d8 = d3;
            d3 = d4;
            d4 = d3;
            d9 = d6;
            d10 = d5;
        } else if (renderer.uvRotateTop == 3) {
            d3 = par8Icon.getInterpolatedU(16.0 - renderer.renderMinX * 16.0);
            d4 = par8Icon.getInterpolatedU(16.0 - renderer.renderMaxX * 16.0);
            d5 = par8Icon.getInterpolatedV(16.0 - renderer.renderMinZ * 16.0);
            d6 = par8Icon.getInterpolatedV(16.0 - renderer.renderMaxZ * 16.0);
            d7 = d4;
            d8 = d3;
            d9 = d5;
            d10 = d6;
        }

        double d11 = par2 + renderer.renderMinX;
        double d12 = par2 + renderer.renderMaxX;
        double d13 = par4 + renderer.renderMaxY;
        double d14 = par6 + renderer.renderMinZ;
        double d15 = par6 + renderer.renderMaxZ;
        if (renderer.enableAO) {
            tessellator
                .setColorOpaque_F(renderer.colorRedTopRight, renderer.colorGreenTopRight, renderer.colorBlueTopRight);
            tessellator.setBrightness(renderer.brightnessTopRight);
            tessellator.addVertexWithUV(d11, d13, d15, d8, d10);
            tessellator.setColorOpaque_F(
                renderer.colorRedBottomRight,
                renderer.colorGreenBottomRight,
                renderer.colorBlueBottomRight);
            tessellator.setBrightness(renderer.brightnessBottomRight);
            tessellator.addVertexWithUV(d11, d13, d14, d3, d5);
            tessellator.setColorOpaque_F(
                renderer.colorRedBottomLeft,
                renderer.colorGreenBottomLeft,
                renderer.colorBlueBottomLeft);
            tessellator.setBrightness(renderer.brightnessBottomLeft);
            tessellator.addVertexWithUV(d12, d13, d14, d7, d9);
            tessellator
                .setColorOpaque_F(renderer.colorRedTopLeft, renderer.colorGreenTopLeft, renderer.colorBlueTopLeft);
            tessellator.setBrightness(renderer.brightnessTopLeft);
            tessellator.addVertexWithUV(d12, d13, d15, d4, d6);
        } else {
            tessellator.addVertexWithUV(d11, d13, d15, d8, d10);
            tessellator.addVertexWithUV(d11, d13, d14, d3, d5);
            tessellator.addVertexWithUV(d12, d13, d14, d7, d9);
            tessellator.addVertexWithUV(d12, d13, d15, d4, d6);
        }
    }

    public void renderEastFace(RenderBlocks renderer, Block par1Block, double par2, double par4, double par6,
        IIcon par8Icon) {
        Tessellator tessellator = Tessellator.instance;
        if (renderer.hasOverrideBlockTexture()) {
            par8Icon = renderer.overrideBlockTexture;
        }

        double d3 = par8Icon.getInterpolatedU(renderer.renderMinX * 16.0);
        double d4 = par8Icon.getInterpolatedU(renderer.renderMaxX * 16.0);
        double d5 = par8Icon.getInterpolatedV(16.0 - renderer.renderMaxY * 16.0);
        double d6 = par8Icon.getInterpolatedV(16.0 - renderer.renderMinY * 16.0);
        if (renderer.flipTexture) {
            double d7 = d3;
            d3 = d4;
            d4 = d7;
        }

        if (renderer.renderMinX < 0.0 || renderer.renderMaxX > 1.0) {
            d3 = par8Icon.getMinU();
            d4 = par8Icon.getMaxU();
        }

        if (renderer.renderMinY < 0.0 || renderer.renderMaxY > 1.0) {
            d5 = par8Icon.getMinV();
            d6 = par8Icon.getMaxV();
        }

        double d7 = d4;
        double d8 = d3;
        double d9 = d5;
        double d10 = d6;
        if (renderer.uvRotateEast == 2) {
            d3 = par8Icon.getInterpolatedU(renderer.renderMinY * 16.0);
            d5 = par8Icon.getInterpolatedV(16.0 - renderer.renderMinX * 16.0);
            d4 = par8Icon.getInterpolatedU(renderer.renderMaxY * 16.0);
            d6 = par8Icon.getInterpolatedV(16.0 - renderer.renderMaxX * 16.0);
            d9 = d5;
            d10 = d6;
            d7 = d3;
            d8 = d4;
            d5 = d6;
            d6 = d5;
        } else if (renderer.uvRotateEast == 1) {
            d3 = par8Icon.getInterpolatedU(16.0 - renderer.renderMaxY * 16.0);
            d5 = par8Icon.getInterpolatedV(renderer.renderMaxX * 16.0);
            d4 = par8Icon.getInterpolatedU(16.0 - renderer.renderMinY * 16.0);
            d6 = par8Icon.getInterpolatedV(renderer.renderMinX * 16.0);
            d7 = d4;
            d8 = d3;
            d3 = d4;
            d4 = d3;
            d9 = d6;
            d10 = d5;
        } else if (renderer.uvRotateEast == 3) {
            d3 = par8Icon.getInterpolatedU(16.0 - renderer.renderMinX * 16.0);
            d4 = par8Icon.getInterpolatedU(16.0 - renderer.renderMaxX * 16.0);
            d5 = par8Icon.getInterpolatedV(renderer.renderMaxY * 16.0);
            d6 = par8Icon.getInterpolatedV(renderer.renderMinY * 16.0);
            d7 = d4;
            d8 = d3;
            d9 = d5;
            d10 = d6;
        }

        double d11 = par2 + renderer.renderMinX;
        double d12 = par2 + renderer.renderMaxX;
        double d13 = par4 + renderer.renderMinY;
        double d14 = par4 + renderer.renderMaxY;
        double d15 = par6 + renderer.renderMinZ;
        if (renderer.enableAO) {
            tessellator
                .setColorOpaque_F(renderer.colorRedTopRight, renderer.colorGreenTopRight, renderer.colorBlueTopRight);
            tessellator.setBrightness(renderer.brightnessTopRight);
            tessellator.addVertexWithUV(d11, d13, d15, d4, d6);
            tessellator.setColorOpaque_F(
                renderer.colorRedBottomRight,
                renderer.colorGreenBottomRight,
                renderer.colorBlueBottomRight);
            tessellator.setBrightness(renderer.brightnessBottomRight);
            tessellator.addVertexWithUV(d12, d13, d15, d8, d10);
            tessellator.setColorOpaque_F(
                renderer.colorRedBottomLeft,
                renderer.colorGreenBottomLeft,
                renderer.colorBlueBottomLeft);
            tessellator.setBrightness(renderer.brightnessBottomLeft);
            tessellator.addVertexWithUV(d12, d14, d15, d3, d5);
            tessellator
                .setColorOpaque_F(renderer.colorRedTopLeft, renderer.colorGreenTopLeft, renderer.colorBlueTopLeft);
            tessellator.setBrightness(renderer.brightnessTopLeft);
            tessellator.addVertexWithUV(d11, d14, d15, d7, d9);
        } else {
            tessellator.addVertexWithUV(d11, d13, d15, d4, d6);
            tessellator.addVertexWithUV(d12, d13, d15, d8, d10);
            tessellator.addVertexWithUV(d12, d14, d15, d3, d5);
            tessellator.addVertexWithUV(d11, d14, d15, d7, d9);
        }
    }

    public void renderWestFace(RenderBlocks renderer, Block par1Block, double par2, double par4, double par6,
        IIcon par8Icon) {
        Tessellator tessellator = Tessellator.instance;
        if (renderer.hasOverrideBlockTexture()) {
            par8Icon = renderer.overrideBlockTexture;
        }

        double d3 = par8Icon.getInterpolatedU(renderer.renderMinX * 16.0);
        double d4 = par8Icon.getInterpolatedU(renderer.renderMaxX * 16.0);
        double d5 = par8Icon.getInterpolatedV(16.0 - renderer.renderMaxY * 16.0);
        double d6 = par8Icon.getInterpolatedV(16.0 - renderer.renderMinY * 16.0);
        if (renderer.flipTexture) {
            double d7 = d3;
            d3 = d4;
            d4 = d7;
        }

        if (renderer.renderMinX < 0.0 || renderer.renderMaxX > 1.0) {
            d3 = par8Icon.getMinU();
            d4 = par8Icon.getMaxU();
        }

        if (renderer.renderMinY < 0.0 || renderer.renderMaxY > 1.0) {
            d5 = par8Icon.getMinV();
            d6 = par8Icon.getMaxV();
        }

        double d7 = d4;
        double d8 = d3;
        double d9 = d5;
        double d10 = d6;
        if (renderer.uvRotateWest == 1) {
            d3 = par8Icon.getInterpolatedU(renderer.renderMinY * 16.0);
            d6 = par8Icon.getInterpolatedV(16.0 - renderer.renderMinX * 16.0);
            d4 = par8Icon.getInterpolatedU(renderer.renderMaxY * 16.0);
            d5 = par8Icon.getInterpolatedV(16.0 - renderer.renderMaxX * 16.0);
            d9 = d5;
            d10 = d6;
            d7 = d3;
            d8 = d4;
            d5 = d6;
            d6 = d5;
        } else if (renderer.uvRotateWest == 2) {
            d3 = par8Icon.getInterpolatedU(16.0 - renderer.renderMaxY * 16.0);
            d5 = par8Icon.getInterpolatedV(renderer.renderMinX * 16.0);
            d4 = par8Icon.getInterpolatedU(16.0 - renderer.renderMinY * 16.0);
            d6 = par8Icon.getInterpolatedV(renderer.renderMaxX * 16.0);
            d7 = d4;
            d8 = d3;
            d3 = d4;
            d4 = d3;
            d9 = d6;
            d10 = d5;
        } else if (renderer.uvRotateWest == 3) {
            d3 = par8Icon.getInterpolatedU(16.0 - renderer.renderMinX * 16.0);
            d4 = par8Icon.getInterpolatedU(16.0 - renderer.renderMaxX * 16.0);
            d5 = par8Icon.getInterpolatedV(renderer.renderMaxY * 16.0);
            d6 = par8Icon.getInterpolatedV(renderer.renderMinY * 16.0);
            d7 = d4;
            d8 = d3;
            d9 = d5;
            d10 = d6;
        }

        double d11 = par2 + renderer.renderMinX;
        double d12 = par2 + renderer.renderMaxX;
        double d13 = par4 + renderer.renderMinY;
        double d14 = par4 + renderer.renderMaxY;
        double d15 = par6 + renderer.renderMaxZ;
        if (renderer.enableAO) {
            tessellator
                .setColorOpaque_F(renderer.colorRedTopRight, renderer.colorGreenTopRight, renderer.colorBlueTopRight);
            tessellator.setBrightness(renderer.brightnessTopRight);
            tessellator.addVertexWithUV(d12, d14, d15, d7, d9);
            tessellator.setColorOpaque_F(
                renderer.colorRedBottomRight,
                renderer.colorGreenBottomRight,
                renderer.colorBlueBottomRight);
            tessellator.setBrightness(renderer.brightnessBottomRight);
            tessellator.addVertexWithUV(d12, d13, d15, d4, d6);
            tessellator.setColorOpaque_F(
                renderer.colorRedBottomLeft,
                renderer.colorGreenBottomLeft,
                renderer.colorBlueBottomLeft);
            tessellator.setBrightness(renderer.brightnessBottomLeft);
            tessellator.addVertexWithUV(d11, d13, d15, d8, d10);
            tessellator
                .setColorOpaque_F(renderer.colorRedTopLeft, renderer.colorGreenTopLeft, renderer.colorBlueTopLeft);
            tessellator.setBrightness(renderer.brightnessTopLeft);
            tessellator.addVertexWithUV(d11, d14, d15, d3, d5);
        } else {
            tessellator.addVertexWithUV(d12, d14, d15, d7, d9);
            tessellator.addVertexWithUV(d12, d13, d15, d4, d6);
            tessellator.addVertexWithUV(d11, d13, d15, d8, d10);
            tessellator.addVertexWithUV(d11, d14, d15, d3, d5);
        }
    }

    public void renderNorthFace(RenderBlocks renderer, Block par1Block, double par2, double par4, double par6,
        IIcon par8Icon) {
        Tessellator tessellator = Tessellator.instance;
        if (renderer.hasOverrideBlockTexture()) {
            par8Icon = renderer.overrideBlockTexture;
        }

        double d3 = par8Icon.getInterpolatedU(renderer.renderMinZ * 16.0);
        double d4 = par8Icon.getInterpolatedU(renderer.renderMaxZ * 16.0);
        double d5 = par8Icon.getInterpolatedV(16.0 - renderer.renderMaxY * 16.0);
        double d6 = par8Icon.getInterpolatedV(16.0 - renderer.renderMinY * 16.0);
        if (renderer.flipTexture) {
            double d7 = d3;
            d3 = d4;
            d4 = d7;
        }

        if (renderer.renderMinZ < 0.0 || renderer.renderMaxZ > 1.0) {
            d3 = par8Icon.getMinU();
            d4 = par8Icon.getMaxU();
        }

        if (renderer.renderMinY < 0.0 || renderer.renderMaxY > 1.0) {
            d5 = par8Icon.getMinV();
            d6 = par8Icon.getMaxV();
        }

        double d7 = d4;
        double d8 = d3;
        double d9 = d5;
        double d10 = d6;
        if (renderer.uvRotateNorth == 1) {
            d3 = par8Icon.getInterpolatedU(renderer.renderMinY * 16.0);
            d5 = par8Icon.getInterpolatedV(16.0 - renderer.renderMaxZ * 16.0);
            d4 = par8Icon.getInterpolatedU(renderer.renderMaxY * 16.0);
            d6 = par8Icon.getInterpolatedV(16.0 - renderer.renderMinZ * 16.0);
            d9 = d5;
            d10 = d6;
            d7 = d3;
            d8 = d4;
            d5 = d6;
            d6 = d5;
        } else if (renderer.uvRotateNorth == 2) {
            d3 = par8Icon.getInterpolatedU(16.0 - renderer.renderMaxY * 16.0);
            d5 = par8Icon.getInterpolatedV(renderer.renderMinZ * 16.0);
            d4 = par8Icon.getInterpolatedU(16.0 - renderer.renderMinY * 16.0);
            d6 = par8Icon.getInterpolatedV(renderer.renderMaxZ * 16.0);
            d7 = d4;
            d8 = d3;
            d3 = d4;
            d4 = d3;
            d9 = d6;
            d10 = d5;
        } else if (renderer.uvRotateNorth == 3) {
            d3 = par8Icon.getInterpolatedU(16.0 - renderer.renderMinZ * 16.0);
            d4 = par8Icon.getInterpolatedU(16.0 - renderer.renderMaxZ * 16.0);
            d5 = par8Icon.getInterpolatedV(renderer.renderMaxY * 16.0);
            d6 = par8Icon.getInterpolatedV(renderer.renderMinY * 16.0);
            d7 = d4;
            d8 = d3;
            d9 = d5;
            d10 = d6;
        }

        double d11 = par2 + renderer.renderMinX;
        double d12 = par4 + renderer.renderMinY;
        double d13 = par4 + renderer.renderMaxY;
        double d14 = par6 + renderer.renderMinZ;
        double d15 = par6 + renderer.renderMaxZ;
        if (renderer.enableAO) {
            tessellator
                .setColorOpaque_F(renderer.colorRedTopRight, renderer.colorGreenTopRight, renderer.colorBlueTopRight);
            tessellator.setBrightness(renderer.brightnessTopRight);
            tessellator.addVertexWithUV(d11, d12, d15, d4, d6);
            tessellator.setColorOpaque_F(
                renderer.colorRedBottomRight,
                renderer.colorGreenBottomRight,
                renderer.colorBlueBottomRight);
            tessellator.setBrightness(renderer.brightnessBottomRight);
            tessellator.addVertexWithUV(d11, d12, d14, d8, d10);
            tessellator.setColorOpaque_F(
                renderer.colorRedBottomLeft,
                renderer.colorGreenBottomLeft,
                renderer.colorBlueBottomLeft);
            tessellator.setBrightness(renderer.brightnessBottomLeft);
            tessellator.addVertexWithUV(d11, d13, d14, d3, d5);
            tessellator
                .setColorOpaque_F(renderer.colorRedTopLeft, renderer.colorGreenTopLeft, renderer.colorBlueTopLeft);
            tessellator.setBrightness(renderer.brightnessTopLeft);
            tessellator.addVertexWithUV(d11, d13, d15, d7, d9);
        } else {
            tessellator.addVertexWithUV(d11, d12, d15, d4, d6);
            tessellator.addVertexWithUV(d11, d12, d14, d8, d10);
            tessellator.addVertexWithUV(d11, d13, d14, d3, d5);
            tessellator.addVertexWithUV(d11, d13, d15, d7, d9);
        }
    }

    public void renderSouthFace(RenderBlocks renderer, Block par1Block, double par2, double par4, double par6,
        IIcon par8Icon) {
        Tessellator tessellator = Tessellator.instance;
        if (renderer.hasOverrideBlockTexture()) {
            par8Icon = renderer.overrideBlockTexture;
        }

        double d3 = par8Icon.getInterpolatedU(renderer.renderMinZ * 16.0);
        double d4 = par8Icon.getInterpolatedU(renderer.renderMaxZ * 16.0);
        double d5 = par8Icon.getInterpolatedV(16.0 - renderer.renderMaxY * 16.0);
        double d6 = par8Icon.getInterpolatedV(16.0 - renderer.renderMinY * 16.0);
        if (renderer.flipTexture) {
            double d7 = d3;
            d3 = d4;
            d4 = d7;
        }

        if (renderer.renderMinZ < 0.0 || renderer.renderMaxZ > 1.0) {
            d3 = par8Icon.getMinU();
            d4 = par8Icon.getMaxU();
        }

        if (renderer.renderMinY < 0.0 || renderer.renderMaxY > 1.0) {
            d5 = par8Icon.getMinV();
            d6 = par8Icon.getMaxV();
        }

        double d7 = d4;
        double d8 = d3;
        double d9 = d5;
        double d10 = d6;
        if (renderer.uvRotateSouth == 2) {
            d3 = par8Icon.getInterpolatedU(renderer.renderMinY * 16.0);
            d5 = par8Icon.getInterpolatedV(16.0 - renderer.renderMinZ * 16.0);
            d4 = par8Icon.getInterpolatedU(renderer.renderMaxY * 16.0);
            d6 = par8Icon.getInterpolatedV(16.0 - renderer.renderMaxZ * 16.0);
            d9 = d5;
            d10 = d6;
            d7 = d3;
            d8 = d4;
            d5 = d6;
            d6 = d5;
        } else if (renderer.uvRotateSouth == 1) {
            d3 = par8Icon.getInterpolatedU(16.0 - renderer.renderMaxY * 16.0);
            d5 = par8Icon.getInterpolatedV(renderer.renderMaxZ * 16.0);
            d4 = par8Icon.getInterpolatedU(16.0 - renderer.renderMinY * 16.0);
            d6 = par8Icon.getInterpolatedV(renderer.renderMinZ * 16.0);
            d7 = d4;
            d8 = d3;
            d3 = d4;
            d4 = d3;
            d9 = d6;
            d10 = d5;
        } else if (renderer.uvRotateSouth == 3) {
            d3 = par8Icon.getInterpolatedU(16.0 - renderer.renderMinZ * 16.0);
            d4 = par8Icon.getInterpolatedU(16.0 - renderer.renderMaxZ * 16.0);
            d5 = par8Icon.getInterpolatedV(renderer.renderMaxY * 16.0);
            d6 = par8Icon.getInterpolatedV(renderer.renderMinY * 16.0);
            d7 = d4;
            d8 = d3;
            d9 = d5;
            d10 = d6;
        }

        double d11 = par2 + renderer.renderMaxX;
        double d12 = par4 + renderer.renderMinY;
        double d13 = par4 + renderer.renderMaxY;
        double d14 = par6 + renderer.renderMinZ;
        double d15 = par6 + renderer.renderMaxZ;
        if (renderer.enableAO) {
            tessellator
                .setColorOpaque_F(renderer.colorRedTopRight, renderer.colorGreenTopRight, renderer.colorBlueTopRight);
            tessellator.setBrightness(renderer.brightnessTopRight);
            tessellator.addVertexWithUV(d11, d13, d15, d3, d5);
            tessellator.setColorOpaque_F(
                renderer.colorRedBottomRight,
                renderer.colorGreenBottomRight,
                renderer.colorBlueBottomRight);
            tessellator.setBrightness(renderer.brightnessBottomRight);
            tessellator.addVertexWithUV(d11, d13, d14, d7, d9);
            tessellator.setColorOpaque_F(
                renderer.colorRedBottomLeft,
                renderer.colorGreenBottomLeft,
                renderer.colorBlueBottomLeft);
            tessellator.setBrightness(renderer.brightnessBottomLeft);
            tessellator.addVertexWithUV(d11, d12, d14, d4, d6);
            tessellator
                .setColorOpaque_F(renderer.colorRedTopLeft, renderer.colorGreenTopLeft, renderer.colorBlueTopLeft);
            tessellator.setBrightness(renderer.brightnessTopLeft);
            tessellator.addVertexWithUV(d11, d12, d15, d8, d10);
        } else {
            tessellator.addVertexWithUV(d11, d13, d15, d3, d5);
            tessellator.addVertexWithUV(d11, d13, d14, d7, d9);
            tessellator.addVertexWithUV(d11, d12, d14, d4, d6);
            tessellator.addVertexWithUV(d11, d12, d15, d8, d10);
        }
    }
}
