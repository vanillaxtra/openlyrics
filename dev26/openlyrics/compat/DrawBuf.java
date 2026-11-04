package dev.openlyrics.compat;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4fc;

public interface DrawBuf {
   VertexConsumer get(RenderType type);

   void end();

   void end(RenderType type);

   void text(Font font, Component text, float x, float y, int color, boolean shadow, Matrix4fc pose, DisplayMode mode, int bg, int light);
}
