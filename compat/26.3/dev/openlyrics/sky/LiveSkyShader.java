package dev.openlyrics.sky;

import dev.openlyrics.config.Config;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import org.joml.Matrix4f;

public final class LiveSkyShader {
   private LiveSkyShader() {
   }

   public static boolean ready() {
      return false;
   }

   public static void disable(Throwable t) {
   }

   public static RenderType type() {
      return RenderTypes.debugQuads();
   }

   public static void params(SkyPreset p, int a1, int a2, double t, Config c, Matrix4f pose) {
   }
}
