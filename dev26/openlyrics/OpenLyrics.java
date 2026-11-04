package dev.openlyrics;

import dev.openlyrics.compat.Mc;
import dev.openlyrics.config.Config;
import dev.openlyrics.config.Storage;
import dev.openlyrics.cosmetic.PhoneMenu;
import dev.openlyrics.hud.BeatGlow;
import dev.openlyrics.hud.GlassHotbar;
import dev.openlyrics.hud.Island;
import dev.openlyrics.hud.StartupIntro;
import dev.openlyrics.lyrics.Favorites;
import dev.openlyrics.lyrics.SpotifyClient;
import dev.openlyrics.sky.SkyRenderer;
import dev.openlyrics.sky.WorldSky;
import dev.openlyrics.util.AutoPerf;
import dev.openlyrics.util.Fx;
import dev.openlyrics.util.Profiles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OpenLyrics implements ClientModInitializer {
   public static final String MOD_ID = "openlyrics";
   public static final Logger LOG = LoggerFactory.getLogger("openlyrics");
   private static Config config = new Config();
   public static KeyMapping keyMenu;
   public static KeyMapping keySkip;
   public static KeyMapping keyPrev;
   public static KeyMapping keyPause;
   public static KeyMapping keyLyrics;
   public static KeyMapping keySync;
   public static KeyMapping keyFav;
   public static KeyMapping keyClip;
   public static KeyMapping keyProfile;

   public static Config cfg() {
      return config;
   }

   public static void saveConfig() {
      config.clamp();
      Storage.save("config.json", config);
   }

   public void onInitializeClient() {
      Storage.migrateOldConfigs();
      Storage.migrateFromLegacy();
      config = Storage.load("config.json", Config.class, Config::new);
      config.clamp();
      saveConfig();
      Category cat = Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));
      keyMenu = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.openlyrics.openmenu", Mc.keyType(), 74, cat));
      keySkip = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.openlyrics.skip", Mc.keyType(), 298, cat));
      keyPrev = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.openlyrics.prev", Mc.keyType(), 263, cat));
      keyPause = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.openlyrics.pause", Mc.keyType(), 80, cat));
      keyFav = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.openlyrics.fav", Mc.keyType(), 78, cat));
      keyClip = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.openlyrics.clip", Mc.keyType(), 299, cat));
      keyProfile = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.openlyrics.profile", Mc.keyType(), 297, cat));
      keySync = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.openlyrics.sync", Mc.keyType(), 82, cat));
      keyLyrics = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.openlyrics.lyrics", Mc.keyType(), 76, cat));
      HudElementRegistry.addFirst(Identifier.fromNamespaceAndPath(MOD_ID, "sky"), SkyRenderer::render);
      HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "island"), Island::render);
      HudElementRegistry.replaceElement(VanillaHudElements.HOTBAR, GlassHotbar::wrap);
      HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "beatglow"), BeatGlow::render);
      Mc.registerWorld(WorldSky::render);
      SpotifyClient.init();
      StartupIntro.register();
      ClientTickEvents.END_CLIENT_TICK.register(this::tick);
      ClientTickEvents.START_CLIENT_TICK.register(PhoneMenu::startTick);
      LOG.info("openlyrics");
   }

   private void tick(Minecraft mc) {
      AutoPerf.tick(mc);
      PhoneMenu.tick(mc);
      SkyRenderer.tick(mc);

      while (keyMenu.consumeClick()) {
         if (Mc.screen(mc) == null) {
            PhoneMenu.open(mc);
         }
      }

      while (keySkip.consumeClick()) {
         SpotifyClient.control(SpotifyClient.Cmd.NEXT);
      }

      while (keyPrev.consumeClick()) {
         SpotifyClient.control(SpotifyClient.Cmd.PREVIOUS);
      }

      while (keyPause.consumeClick()) {
         SpotifyClient.control(SpotifyClient.Cmd.PLAYPAUSE);
      }

      while (keySync.consumeClick()) {
         SpotifyClient.syncHere();
      }

      while (keyFav.consumeClick()) {
         Favorites.saveCurrent();
      }

      while (keyClip.consumeClick()) {
         Mc.toggleHideGui(mc);
      }

      while (keyProfile.consumeClick()) {
         Profiles.toggle();
      }

      while (keyLyrics.consumeClick()) {
         config.lyricsSky = !config.lyricsSky;
         saveConfig();
         Island.notify(Fx.t(config.lyricsSky ? "openlyrics.media.lyricson" : "openlyrics.media.lyricsoff"), config.lyricsSky);
      }
   }
}
