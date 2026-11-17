package com.mrfuzzihead.artifice.render.connectedtexture;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;

import com.mrfuzzihead.artifice.render.TextureHandler;

public class RenderBlocksCTM extends RenderBlocks {

    Tessellator tessellator;
    double[] X = new double[26];
    double[] Y = new double[26];
    double[] Z = new double[26];
    double[] U = new double[26];
    double[] V = new double[26];
    int[] L = new int[26];
    float[] R = new float[26];
    float[] G = new float[26];
    float[] B = new float[26];
    TextureSubmap submap;
    TextureSubmap submapSmall;
    RenderBlocks rendererOld;
    int bx;
    int by;
    int bz;

    public RenderBlocksCTM() {
        this.resetVertices();
    }

    public boolean renderStandardBlock(Block block, int x, int y, int z) {
        this.bx = x;
        this.by = y;
        this.bz = z;
        this.tessellator = Tessellator.instance;
        this.tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);
        this.tessellator.addTranslation(x, y, z);
        boolean res = super.renderStandardBlock(block, x, y, z);
        this.tessellator.addTranslation(-x, -y, -z);
        return res;
    }

    void setupSides(int a, int b, int c, int d, int xa, int xb, int xc, int xd, int e) {
        this.L[a] = this.brightnessBottomLeft;
        this.L[b] = this.brightnessBottomRight;
        this.L[c] = this.brightnessTopRight;
        this.L[d] = this.brightnessTopLeft;
        this.L[e] = (this.brightnessBottomLeft + this.brightnessTopLeft
            + this.brightnessTopRight
            + this.brightnessBottomRight) / 4;
        this.L[xa] = (this.L[a] + this.L[b]) / 2;
        this.L[xb] = (this.L[b] + this.L[c]) / 2;
        this.L[xc] = (this.L[c] + this.L[d]) / 2;
        this.L[xd] = (this.L[d] + this.L[a]) / 2;
        this.R[a] = this.colorRedBottomLeft;
        this.R[b] = this.colorRedBottomRight;
        this.R[c] = this.colorRedTopRight;
        this.R[d] = this.colorRedTopLeft;
        this.R[e] = (this.colorRedBottomLeft + this.colorRedTopLeft + this.colorRedTopRight + this.colorRedBottomRight)
            / 4.0F;
        this.R[xa] = (this.R[a] + this.R[b]) / 2.0F;
        this.R[xb] = (this.R[b] + this.R[c]) / 2.0F;
        this.R[xc] = (this.R[c] + this.R[d]) / 2.0F;
        this.R[xd] = (this.R[d] + this.R[a]) / 2.0F;
        this.G[a] = this.colorGreenBottomLeft;
        this.G[b] = this.colorGreenBottomRight;
        this.G[c] = this.colorGreenTopRight;
        this.G[d] = this.colorGreenTopLeft;
        this.G[e] = (this.colorGreenBottomLeft + this.colorGreenTopLeft
            + this.colorGreenTopRight
            + this.colorGreenBottomRight) / 4.0F;
        this.G[xa] = (this.G[a] + this.G[b]) / 2.0F;
        this.G[xb] = (this.G[b] + this.G[c]) / 2.0F;
        this.G[xc] = (this.G[c] + this.G[d]) / 2.0F;
        this.G[xd] = (this.G[d] + this.G[a]) / 2.0F;
        this.B[a] = this.colorBlueBottomLeft;
        this.B[b] = this.colorBlueBottomRight;
        this.B[c] = this.colorBlueTopRight;
        this.B[d] = this.colorBlueTopLeft;
        this.B[e] = (this.colorBlueBottomLeft + this.colorBlueTopLeft
            + this.colorBlueTopRight
            + this.colorBlueBottomRight) / 4.0F;
        this.B[xa] = (this.B[a] + this.B[b]) / 2.0F;
        this.B[xb] = (this.B[b] + this.B[c]) / 2.0F;
        this.B[xc] = (this.B[c] + this.B[d]) / 2.0F;
        this.B[xd] = (this.B[d] + this.B[a]) / 2.0F;
    }

    void side(int a, int b, int c, int d, int iconIndex, boolean flip) {
        IIcon icon = iconIndex >= 16 ? this.submapSmall.icons[iconIndex - 16] : this.submap.icons[iconIndex];
        double u0 = icon.getMaxU();
        double u1 = icon.getMinU();
        double v0 = icon.getMaxV();
        double v1 = icon.getMinV();
        this.U[a] = flip ? u1 : u1;
        this.U[b] = flip ? u0 : u1;
        this.U[c] = flip ? u0 : u0;
        this.U[d] = flip ? u1 : u0;
        this.V[a] = flip ? v1 : v1;
        this.V[b] = flip ? v1 : v0;
        this.V[c] = flip ? v0 : v0;
        this.V[d] = flip ? v0 : v1;
        this.vert(a);
        this.vert(b);
        this.vert(c);
        this.vert(d);
    }

    void vert(int index) {
        if (this.enableAO) {
            this.tessellator.setColorOpaque_F(this.R[index], this.G[index], this.B[index]);
            this.tessellator.setBrightness(this.L[index]);
        }

        this.tessellator.addVertexWithUV(this.X[index], this.Y[index], this.Z[index], this.U[index], this.V[index]);
    }

    public void renderFaceXNeg(Block block, double x, double y, double z, IIcon icon) {
        int meta = this.blockAccess.getBlockMetadata(this.bx, this.by, this.bz);
        ConnectedTexture texture = TextureHandler.getConnectedTexture(block, meta, 4);
        if (texture == null) {
            this.rendererOld.setOverrideBlockTexture(block.getIcon(4, meta));
        } else {
            this.submap = texture.submap;
            this.submapSmall = texture.submapSmall;
        }

        if (this.rendererOld != null && this.rendererOld.hasOverrideBlockTexture()) {
            IIcon i = this.rendererOld.overrideBlockTexture;
            this.tessellator.addVertexWithUV(0.0, 1.0, 0.0, i.getMinU(), i.getMinV());
            this.tessellator.addVertexWithUV(0.0, 0.0, 0.0, i.getMinU(), i.getMaxV());
            this.tessellator.addVertexWithUV(0.0, 0.0, 1.0, i.getMaxU(), i.getMaxV());
            this.tessellator.addVertexWithUV(0.0, 1.0, 1.0, i.getMaxU(), i.getMinV());
        } else {
            int[] tex = CTM.getSubmapIndices(this.blockAccess, this.bx, this.by, this.bz, 4);
            this.setupSides(1, 0, 4, 5, 14, 19, 17, 23, 9);
            this.side(1, 14, 9, 23, tex[0], false);
            this.side(23, 9, 17, 5, tex[1], false);
            this.side(9, 19, 4, 17, tex[3], false);
            this.side(14, 0, 19, 9, tex[2], false);
        }

        if (texture == null) {
            this.rendererOld.clearOverrideBlockTexture();
        }
    }

    public void renderFaceXPos(Block block, double x, double y, double z, IIcon icon) {
        int meta = this.blockAccess.getBlockMetadata(this.bx, this.by, this.bz);
        ConnectedTexture texture = TextureHandler.getConnectedTexture(block, meta, 5);
        if (texture == null) {
            this.rendererOld.setOverrideBlockTexture(block.getIcon(5, meta));
        } else {
            this.submap = texture.submap;
            this.submapSmall = texture.submapSmall;
        }

        if (this.rendererOld != null && this.rendererOld.hasOverrideBlockTexture()) {
            IIcon i = this.rendererOld.overrideBlockTexture;
            this.tessellator.addVertexWithUV(1.0, 1.0, 1.0, i.getMaxU(), i.getMinV());
            this.tessellator.addVertexWithUV(1.0, 0.0, 1.0, i.getMaxU(), i.getMaxV());
            this.tessellator.addVertexWithUV(1.0, 0.0, 0.0, i.getMinU(), i.getMaxV());
            this.tessellator.addVertexWithUV(1.0, 1.0, 0.0, i.getMinU(), i.getMinV());
        } else {
            int[] tex = CTM.getSubmapIndices(this.blockAccess, this.bx, this.by, this.bz, 5);
            this.setupSides(3, 2, 6, 7, 15, 25, 16, 21, 11);
            this.side(11, 21, 3, 15, tex[3], false);
            this.side(16, 7, 21, 11, tex[2], false);
            this.side(25, 11, 15, 2, tex[1], false);
            this.side(6, 16, 11, 25, tex[0], false);
        }

        if (texture == null) {
            this.rendererOld.clearOverrideBlockTexture();
        }
    }

    public void renderFaceZNeg(Block block, double x, double y, double z, IIcon icon) {
        int meta = this.blockAccess.getBlockMetadata(this.bx, this.by, this.bz);
        ConnectedTexture texture = TextureHandler.getConnectedTexture(block, meta, 2);
        if (texture == null) {
            this.rendererOld.setOverrideBlockTexture(block.getIcon(2, meta));
        } else {
            this.submap = texture.submap;
            this.submapSmall = texture.submapSmall;
        }

        if (this.rendererOld != null && this.rendererOld.hasOverrideBlockTexture()) {
            IIcon i = this.rendererOld.overrideBlockTexture;
            this.tessellator.addVertexWithUV(1.0, 1.0, 0.0, i.getMaxU(), i.getMinV());
            this.tessellator.addVertexWithUV(1.0, 0.0, 0.0, i.getMaxU(), i.getMaxV());
            this.tessellator.addVertexWithUV(0.0, 0.0, 0.0, i.getMinU(), i.getMaxV());
            this.tessellator.addVertexWithUV(0.0, 1.0, 0.0, i.getMinU(), i.getMinV());
        } else {
            int[] tex = CTM.getSubmapIndices(this.blockAccess, this.bx, this.by, this.bz, 2);
            this.setupSides(2, 3, 0, 1, 15, 18, 14, 22, 8);
            this.side(2, 15, 8, 22, tex[0], false);
            this.side(15, 3, 18, 8, tex[2], false);
            this.side(8, 18, 0, 14, tex[3], false);
            this.side(22, 8, 14, 1, tex[1], false);
        }

        if (texture == null) {
            this.rendererOld.clearOverrideBlockTexture();
        }
    }

    public void renderFaceZPos(Block block, double x, double y, double z, IIcon icon) {
        int meta = this.blockAccess.getBlockMetadata(this.bx, this.by, this.bz);
        ConnectedTexture texture = TextureHandler.getConnectedTexture(block, meta, 3);
        if (texture == null) {
            this.rendererOld.setOverrideBlockTexture(block.getIcon(3, meta));
        } else {
            this.submap = texture.submap;
            this.submapSmall = texture.submapSmall;
        }

        if (this.rendererOld != null && this.rendererOld.hasOverrideBlockTexture()) {
            IIcon i = this.rendererOld.overrideBlockTexture;
            this.tessellator.addVertexWithUV(0.0, 1.0, 1.0, i.getMinU(), i.getMinV());
            this.tessellator.addVertexWithUV(0.0, 0.0, 1.0, i.getMinU(), i.getMaxV());
            this.tessellator.addVertexWithUV(1.0, 0.0, 1.0, i.getMaxU(), i.getMaxV());
            this.tessellator.addVertexWithUV(1.0, 1.0, 1.0, i.getMaxU(), i.getMinV());
        } else {
            int[] tex = CTM.getSubmapIndices(this.blockAccess, this.bx, this.by, this.bz, 3);
            this.setupSides(4, 7, 6, 5, 20, 16, 24, 17, 10);
            this.side(17, 4, 20, 10, tex[2], false);
            this.side(5, 17, 10, 24, tex[0], false);
            this.side(24, 10, 16, 6, tex[1], false);
            this.side(10, 20, 7, 16, tex[3], false);
        }

        if (texture == null) {
            this.rendererOld.clearOverrideBlockTexture();
        }
    }

    public void renderFaceYNeg(Block block, double x, double y, double z, IIcon icon) {
        int meta = this.blockAccess.getBlockMetadata(this.bx, this.by, this.bz);
        ConnectedTexture texture = TextureHandler.getConnectedTexture(block, meta, 0);
        if (texture == null) {
            this.rendererOld.setOverrideBlockTexture(block.getIcon(0, meta));
        } else {
            this.submap = texture.submap;
            this.submapSmall = texture.submapSmall;
        }

        if (this.rendererOld != null && this.rendererOld.hasOverrideBlockTexture()) {
            IIcon i = this.rendererOld.overrideBlockTexture;
            this.tessellator.addVertexWithUV(0.0, 0.0, 1.0, i.getMinU(), i.getMaxV());
            this.tessellator.addVertexWithUV(0.0, 0.0, 0.0, i.getMinU(), i.getMinV());
            this.tessellator.addVertexWithUV(1.0, 0.0, 0.0, i.getMaxU(), i.getMinV());
            this.tessellator.addVertexWithUV(1.0, 0.0, 1.0, i.getMaxU(), i.getMaxV());
        } else {
            int[] tex = CTM.getSubmapIndices(this.blockAccess, this.bx, this.by, this.bz, 0);
            this.setupSides(0, 3, 7, 4, 18, 21, 20, 19, 13);
            this.side(13, 21, 7, 20, tex[3], true);
            this.side(19, 13, 20, 4, tex[2], true);
            this.side(0, 18, 13, 19, tex[0], true);
            this.side(18, 3, 21, 13, tex[1], true);
        }

        if (texture == null) {
            this.rendererOld.clearOverrideBlockTexture();
        }
    }

    public void renderFaceYPos(Block block, double x, double y, double z, IIcon icon) {
        int meta = this.blockAccess.getBlockMetadata(this.bx, this.by, this.bz);
        ConnectedTexture texture = TextureHandler.getConnectedTexture(block, meta, 1);
        if (texture == null) {
            this.rendererOld.setOverrideBlockTexture(block.getIcon(1, meta));
        } else {
            this.submap = texture.submap;
            this.submapSmall = texture.submapSmall;
        }

        if (this.rendererOld != null && this.rendererOld.hasOverrideBlockTexture()) {
            IIcon i = this.rendererOld.overrideBlockTexture;
            this.tessellator.addVertexWithUV(0.0, 1.0, 0.0, i.getMinU(), i.getMinV());
            this.tessellator.addVertexWithUV(0.0, 1.0, 1.0, i.getMinU(), i.getMaxV());
            this.tessellator.addVertexWithUV(1.0, 1.0, 1.0, i.getMaxU(), i.getMaxV());
            this.tessellator.addVertexWithUV(1.0, 1.0, 0.0, i.getMaxU(), i.getMinV());
        } else {
            int[] tex = CTM.getSubmapIndices(this.blockAccess, this.bx, this.by, this.bz, 1);
            this.setupSides(2, 1, 5, 6, 22, 23, 24, 25, 12);
            this.side(12, 24, 6, 25, tex[3], false);
            this.side(22, 12, 25, 2, tex[1], false);
            this.side(1, 23, 12, 22, tex[0], false);
            this.side(23, 5, 24, 12, tex[2], false);
        }

        if (texture == null) {
            this.rendererOld.clearOverrideBlockTexture();
        }
    }

    void resetVertices() {
        this.X[0] = 0.0;
        this.Z[0] = 0.0;
        this.Y[0] = 0.0;
        this.X[1] = 0.0;
        this.Z[1] = 0.0;
        this.Y[1] = 1.0;
        this.X[2] = 1.0;
        this.Z[2] = 0.0;
        this.Y[2] = 1.0;
        this.X[3] = 1.0;
        this.Z[3] = 0.0;
        this.Y[3] = 0.0;
        this.X[4] = 0.0;
        this.Z[4] = 1.0;
        this.Y[4] = 0.0;
        this.X[5] = 0.0;
        this.Z[5] = 1.0;
        this.Y[5] = 1.0;
        this.X[6] = 1.0;
        this.Z[6] = 1.0;
        this.Y[6] = 1.0;
        this.X[7] = 1.0;
        this.Z[7] = 1.0;
        this.Y[7] = 0.0;
        this.X[8] = 0.5;
        this.Z[8] = 0.0;
        this.Y[8] = 0.5;
        this.X[9] = 0.0;
        this.Z[9] = 0.5;
        this.Y[9] = 0.5;
        this.X[10] = 0.5;
        this.Z[10] = 1.0;
        this.Y[10] = 0.5;
        this.X[11] = 1.0;
        this.Z[11] = 0.5;
        this.Y[11] = 0.5;
        this.X[12] = 0.5;
        this.Z[12] = 0.5;
        this.Y[12] = 1.0;
        this.X[13] = 0.5;
        this.Z[13] = 0.5;
        this.Y[13] = 0.0;
        this.X[14] = 0.0;
        this.Z[14] = 0.0;
        this.Y[14] = 0.5;
        this.X[15] = 1.0;
        this.Z[15] = 0.0;
        this.Y[15] = 0.5;
        this.X[16] = 1.0;
        this.Z[16] = 1.0;
        this.Y[16] = 0.5;
        this.X[17] = 0.0;
        this.Z[17] = 1.0;
        this.Y[17] = 0.5;
        this.X[18] = 0.5;
        this.Z[18] = 0.0;
        this.Y[18] = 0.0;
        this.X[19] = 0.0;
        this.Z[19] = 0.5;
        this.Y[19] = 0.0;
        this.X[20] = 0.5;
        this.Z[20] = 1.0;
        this.Y[20] = 0.0;
        this.X[21] = 1.0;
        this.Z[21] = 0.5;
        this.Y[21] = 0.0;
        this.X[22] = 0.5;
        this.Z[22] = 0.0;
        this.Y[22] = 1.0;
        this.X[23] = 0.0;
        this.Z[23] = 0.5;
        this.Y[23] = 1.0;
        this.X[24] = 0.5;
        this.Z[24] = 1.0;
        this.Y[24] = 1.0;
        this.X[25] = 1.0;
        this.Z[25] = 0.5;
        this.Y[25] = 1.0;
    }
}
