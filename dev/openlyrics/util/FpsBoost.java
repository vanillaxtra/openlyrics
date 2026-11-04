package dev.openlyrics.util;

import dev.openlyrics.OpenLyrics;
import dev.openlyrics.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;

public final class FpsBoost {
   private FpsBoost() {
   }

   public static void apply(boolean on) {
      MinecraftClient mc = MinecraftClient.getInstance();
      Config c = OpenLyrics.cfg();

      try {
         if (on) {
            c.boostBefore = save(mc.options.getEntityShadows()) + ";" + save(mc.options.getParticles()) + ";" + save(mc.options.getBiomeBlendRadius());
            set(mc.options.getEntityShadows(), "false");
            set(mc.options.getParticles(), "MINIMAL");
            set(mc.options.getBiomeBlendRadius(), "0");
            c.quality = 0;
         } else if (c.boostBefore != null && !c.boostBefore.isEmpty()) {
            String[] v = c.boostBefore.split(";", -1);
            if (v.length >= 3) {
               set(mc.options.getEntityShadows(), v[0]);
               set(mc.options.getParticles(), v[1]);
               set(mc.options.getBiomeBlendRadius(), v[2]);
            }

            c.boostBefore = "";
            if (c.quality == 0) {
               c.quality = 1;
            }
         }

         mc.options.write();
      } catch (Exception var4) {
         OpenLyrics.LOG.warn("could not apply fps boost", var4);
      }

      c.fpsBoost = on;
      OpenLyrics.saveConfig();
   }

   private static String save(SimpleOption<?> o) {
      Object v = o.getValue();
      return v instanceof Enum<?> e ? e.name() : String.valueOf(v);
   }

   private static void set(SimpleOption o, String value) {
      Object cur = o.getValue();
      Object nv;
      if (cur instanceof Boolean) {
         nv = Boolean.parseBoolean(value);
      } else if (cur instanceof Integer) {
         nv = Integer.parseInt(value);
      } else {
         if (!(cur instanceof Enum<?> e)) {
            return;
         }

         nv = Enum.valueOf(e.getDeclaringClass(), value);
      }

      o.setValue(nv);
   }
}
