package dev.openlyrics.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.openlyrics.cosmetic.PhoneMenu;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({FirstPersonHandsAndItemsRenderer.class})
public abstract class HandMixin {
   @Inject(
      method = {"submitHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/PlayerRenderState;Lnet/minecraft/client/renderer/state/level/FirstPersonHandsAndItemsRenderState;)V"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void openlyrics$hideHand(
      float partialTick, PoseStack matrices, SubmitNodeCollector queue, PlayerRenderState player, FirstPersonHandsAndItemsRenderState hands, CallbackInfo ci
   ) {
      if (PhoneMenu.hidesHand()) {
         ci.cancel();
      }
   }
}
