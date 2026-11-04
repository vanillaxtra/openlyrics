package dev.openlyrics.compat;

import com.mojang.blaze3d.platform.InputConstants.Type;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.awt.Desktop;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.Level;
import org.joml.Matrix4fc;

public final class Mc {
   private static boolean hide;

   private Mc() {
   }

   public static Screen screen(Minecraft mc) {
      return mc.gui.screen();
   }

   public static void setScreen(Minecraft mc, Screen s) {
      mc.gui.setScreen(s);
   }

   public static boolean hideGui(Minecraft mc) {
      return hide;
   }

   public static void toggleHideGui(Minecraft mc) {
      hide = !hide;
   }

   public static Camera camera(Minecraft mc) {
      return mc.gameRenderer.mainCamera();
   }

   public static void tell(Minecraft mc, Component c) {
      if (mc.gui != null) {
         mc.gui.hud.getChat().addClientSystemMessage(c);
      }
   }

   public static void openUri(String url) {
      try {
         Desktop.getDesktop().browse(URI.create(url));
      } catch (Throwable ignored) {
      }
   }

   public static int light(Level level, BlockPos pos) {
      return LightCoordsUtil.getLightCoords(level, pos);
   }

   public static Type keyType() {
      return Type.KEYBOARD;
   }

   public static void registerWorld(Consumer<LevelRenderContext> r) {
      LevelRenderEvents.COLLECT_SUBMITS.register(r::accept);
   }

   public static void drawText(Font font, Component text, float x, float y, int color, boolean shadow, Matrix4fc pose, DrawBuf buf, DisplayMode mode, int bg, int light) {
      buf.text(font, text, x, y, color, shadow, pose, mode, bg, light);
   }

   public static DrawBuf buffers(LevelRenderContext ctx) {
      return new Rec(ctx.submitNodeCollector());
   }

   // records verts then submits; replay uses identity pose because callers already transform
   private static final class Rec implements DrawBuf, VertexConsumer {
      private final SubmitNodeCollector nodes;
      private final List<Consumer<VertexConsumer>> ops = new ArrayList<>();
      private RenderType cur;

      Rec(SubmitNodeCollector nodes) {
         this.nodes = nodes;
      }

      public VertexConsumer get(RenderType type) {
         if (this.cur != null && this.cur != type) {
            this.flush();
         }
         this.cur = type;
         return this;
      }

      public void end() {
         this.flush();
      }

      public void end(RenderType type) {
         if (this.cur == type) {
            this.flush();
         }
      }

      public void text(Font font, Component text, float x, float y, int color, boolean shadow, Matrix4fc pose, DisplayMode mode, int bg, int light) {
         this.flush();
         PoseStack ps = new PoseStack();
         ps.last().pose().set(pose);
         this.nodes.submitText(ps, x, y, text.getVisualOrderText(), shadow, mode, light, color, bg, 0);
      }

      private void flush() {
         if (this.cur == null || this.ops.isEmpty()) {
            this.ops.clear();
            this.cur = null;
            return;
         }
         RenderType t = this.cur;
         List<Consumer<VertexConsumer>> copy = new ArrayList<>(this.ops);
         this.ops.clear();
         this.cur = null;
         this.nodes.submitCustomGeometry(new PoseStack(), t, (pose, vc) -> {
            for (Consumer<VertexConsumer> op : copy) {
               op.accept(vc);
            }
         });
      }

      public VertexConsumer addVertex(float x, float y, float z) {
         this.ops.add(v -> v.addVertex(x, y, z));
         return this;
      }

      public VertexConsumer setColor(int r, int g, int b, int a) {
         this.ops.add(v -> v.setColor(r, g, b, a));
         return this;
      }

      public VertexConsumer setColor(int argb) {
         this.ops.add(v -> v.setColor(argb));
         return this;
      }

      public VertexConsumer setUv(float u, float v) {
         this.ops.add(c -> c.setUv(u, v));
         return this;
      }

      public VertexConsumer setUv1(int u, int v) {
         this.ops.add(c -> c.setUv1(u, v));
         return this;
      }

      public VertexConsumer setUv2(int u, int v) {
         this.ops.add(c -> c.setUv2(u, v));
         return this;
      }

      public VertexConsumer setUv3(float u, float v) {
         this.ops.add(c -> c.setUv3(u, v));
         return this;
      }

      public VertexConsumer setNormal(float x, float y, float z) {
         this.ops.add(c -> c.setNormal(x, y, z));
         return this;
      }

      public VertexConsumer setLineWidth(float w) {
         this.ops.add(c -> c.setLineWidth(w));
         return this;
      }
   }
}
