package dev.openlyrics.compat;

import com.mojang.blaze3d.platform.InputConstants.Type;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.level.Level;
import org.joml.Matrix4fc;

public final class Mc {
   private Mc() {
   }

   public static Screen screen(Minecraft mc) {
      return mc.screen;
   }

   public static void setScreen(Minecraft mc, Screen s) {
      mc.setScreen(s);
   }

   public static boolean hideGui(Minecraft mc) {
      return mc.options.hideGui;
   }

   public static void toggleHideGui(Minecraft mc) {
      mc.options.hideGui = !mc.options.hideGui;
   }

   public static Camera camera(Minecraft mc) {
      return mc.gameRenderer.getMainCamera();
   }

   public static void tell(Minecraft mc, Component c) {
      if (mc.gui != null) {
         mc.gui.getChat().addClientSystemMessage(c);
      }
   }

   public static void openUri(String url) {
      Util.getPlatform().openUri(url);
   }

   public static int light(Level level, BlockPos pos) {
      return LevelRenderer.getLightCoords(level, pos);
   }

   public static Type keyType() {
      return Type.KEYSYM;
   }

   public static void registerWorld(Consumer<LevelRenderContext> r) {
      LevelRenderEvents.AFTER_SOLID_FEATURES.register(r::accept);
   }

   public static void drawText(Font font, Component text, float x, float y, int color, boolean shadow, Matrix4fc pose, DrawBuf buf, DisplayMode mode, int bg, int light) {
      buf.text(font, text, x, y, color, shadow, pose, mode, bg, light);
   }

   public static DrawBuf buffers(LevelRenderContext ctx) {
      return new Src(ctx.bufferSource());
   }

   private static final class Src implements DrawBuf {
      private final MultiBufferSource src;

      Src(MultiBufferSource src) {
         this.src = src;
      }

      public VertexConsumer get(RenderType type) {
         return this.src.getBuffer(type);
      }

      public void end() {
         if (this.src instanceof BufferSource b) {
            b.endBatch();
         }
      }

      public void end(RenderType type) {
         if (this.src instanceof BufferSource b) {
            b.endBatch(type);
         }
      }

      public void text(Font font, Component text, float x, float y, int color, boolean shadow, Matrix4fc pose, DisplayMode mode, int bg, int light) {
         font.drawInBatch(text, x, y, color, shadow, pose, this.src, mode, bg, light);
      }
   }
}
