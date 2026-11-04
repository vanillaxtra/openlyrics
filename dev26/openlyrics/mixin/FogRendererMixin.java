package dev.openlyrics.mixin;

import dev.openlyrics.sky.WorldSky;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({FogRenderer.class})
public abstract class FogRendererMixin {
   @Inject(
      method = {"setupFog(Lnet/minecraft/client/Camera;ILnet/minecraft/client/DeltaTracker;FLnet/minecraft/client/multiplayer/ClientLevel;)Lorg/joml/Vector4f;"},
      at = {@At("RETURN")},
      cancellable = true,
      require = 0
   )
   private void openlyrics$skyFog(
      Camera camera, int renderDistance, DeltaTracker tickCounter, float darken, ClientLevel level, CallbackInfoReturnable<Vector4f> cir
   ) {
      if (camera.getFluidInCamera() == FogType.NONE) {
         Vector4f v = (Vector4f)cir.getReturnValue();
         if (v != null && WorldSky.tintFog(v)) {
            cir.setReturnValue(v);
         }
      }
   }
}
