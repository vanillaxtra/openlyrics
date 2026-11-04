package dev.openlyrics.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.openlyrics.cosmetic.PhoneMenu;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemInHandRenderer.class})
public abstract class HandMixin {
   @Inject(
      method = {"renderHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/player/LocalPlayer;I)V"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void openlyrics$hideHand(
      float partialTick, PoseStack matrices, SubmitNodeCollector queue, LocalPlayer player, int light, CallbackInfo ci
   ) {
      if (PhoneMenu.hidesHand()) {
         ci.cancel();
      }
   }
}
