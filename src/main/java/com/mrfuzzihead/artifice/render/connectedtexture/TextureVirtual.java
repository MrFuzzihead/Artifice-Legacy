package shukaro.artifice.render.connectedtexture;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.IIcon;

public class TextureVirtual implements IIcon {
   int ox;
   int oy;
   float u0;
   float u1;
   float v0;
   float v1;
   String name;
   IIcon icon;

   TextureVirtual(IIcon parent, int w, int h, int x, int y) {
      this.icon = parent;
      this.u0 = this.icon.getInterpolatedU(16.0 * x / w);
      this.u1 = this.icon.getInterpolatedU(16.0 * (x + 1) / w);
      this.v0 = this.icon.getInterpolatedV(16.0 * y / h);
      this.v1 = this.icon.getInterpolatedV(16.0 * (y + 1) / h);
      this.name = this.icon.getIconName() + "|" + x + "." + y;
      this.ox = this.icon.getIconWidth();
      this.oy = this.icon.getIconHeight();
   }

   @SideOnly(Side.CLIENT)
   public float getMinU() {
      return this.u0;
   }

   @SideOnly(Side.CLIENT)
   public float getMaxU() {
      return this.u1;
   }

   @SideOnly(Side.CLIENT)
   public float getInterpolatedU(double d0) {
      return (float)(this.u0 + (this.u1 - this.u0) * d0 / 16.0);
   }

   @SideOnly(Side.CLIENT)
   public float getMinV() {
      return this.v0;
   }

   @SideOnly(Side.CLIENT)
   public float getMaxV() {
      return this.v1;
   }

   @SideOnly(Side.CLIENT)
   public float getInterpolatedV(double d0) {
      return (float)(this.v0 + (this.v1 - this.v0) * d0 / 16.0);
   }

   @SideOnly(Side.CLIENT)
   public String getIconName() {
      return this.name;
   }

   @SideOnly(Side.CLIENT)
   public int getIconWidth() {
      return this.ox;
   }

   @SideOnly(Side.CLIENT)
   public int getIconHeight() {
      return this.oy;
   }
}
