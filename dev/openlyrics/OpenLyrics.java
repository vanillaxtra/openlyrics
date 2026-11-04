package dev.openlyrics;

import dev.openlyrics.config.Config;
import dev.openlyrics.config.Storage;
import dev.openlyrics.cosmetic.DeathFx;
import dev.openlyrics.cosmetic.PhoneMenu;
import dev.openlyrics.hud.BeatGlow;
import dev.openlyrics.hud.Crosshair;
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
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding.Category;
import net.minecraft.client.util.InputUtil.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OpenLyrics implements ClientModInitializer {
   public static final String MOD_ID = "openlyrics";
   public static final Logger LOG = LoggerFactory.getLogger("openlyrics");
   private static Config config = new Config();
   public static KeyBinding keyMenu;
   public static KeyBinding keySkip;
   public static KeyBinding keyPrev;
   public static KeyBinding keyPause;
   public static KeyBinding keyLyrics;
   public static KeyBinding keySync;
   public static KeyBinding keyFav;
   public static KeyBinding keyClip;
   public static KeyBinding keyProfile;

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
      Category cat = Category.create(Identifier.of(MOD_ID, "main"));
      keyMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.openlyrics.openmenu", Type.KEYSYM, 74, cat));
      keySkip = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.openlyrics.skip", Type.KEYSYM, 298, cat));
      keyPrev = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.openlyrics.prev", Type.KEYSYM, 263, cat));
      keyPause = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.openlyrics.pause", Type.KEYSYM, 80, cat));
      keyFav = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.openlyrics.fav", Type.KEYSYM, 78, cat));
      keyClip = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.openlyrics.clip", Type.KEYSYM, 299, cat));
      keyProfile = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.openlyrics.profile", Type.KEYSYM, 297, cat));
      keySync = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.openlyrics.sync", Type.KEYSYM, 82, cat));
      keyLyrics = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.openlyrics.lyrics", Type.KEYSYM, 76, cat));
      HudElementRegistry.addFirst(Identifier.of(MOD_ID, "sky"), SkyRenderer::render);
      HudElementRegistry.addLast(Identifier.of(MOD_ID, "island"), Island::render);
      HudElementRegistry.replaceElement(VanillaHudElements.HOTBAR, GlassHotbar::wrap);
      HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, Crosshair::wrap);
      HudElementRegistry.addLast(Identifier.of(MOD_ID, "beatglow"), BeatGlow::render);
      WorldRenderEvents.AFTER_ENTITIES.register(WorldSky::render);
      SpotifyClient.init();
      StartupIntro.register();
      AttackEntityCallback.EVENT.register((AttackEntityCallback)(player, level, hand, entity, hit) -> {
         DeathFx.onAttack(entity);
         return ActionResult.PASS;
      });
      ClientTickEvents.END_CLIENT_TICK.register(this::tick);
      ClientTickEvents.START_CLIENT_TICK.register(PhoneMenu::startTick);
      LOG.info("openlyrics");
   }

   private void tick(MinecraftClient mc) {
      AutoPerf.tick(mc);
      PhoneMenu.tick(mc);
      DeathFx.tick(mc);
      SkyRenderer.tick(mc);

      while (keyMenu.wasPressed()) {
         if (mc.currentScreen == null) {
            PhoneMenu.open(mc);
         }
      }

      while (keySkip.wasPressed()) {
         SpotifyClient.control(SpotifyClient.Cmd.NEXT);
      }

      while (keyPrev.wasPressed()) {
         SpotifyClient.control(SpotifyClient.Cmd.PREVIOUS);
      }

      while (keyPause.wasPressed()) {
         SpotifyClient.control(SpotifyClient.Cmd.PLAYPAUSE);
      }

      while (keySync.wasPressed()) {
         SpotifyClient.syncHere();
      }

      while (keyFav.wasPressed()) {
         Favorites.saveCurrent();
      }

      while (keyClip.wasPressed()) {
         mc.options.hudHidden = !mc.options.hudHidden;
      }

      while (keyProfile.wasPressed()) {
         Profiles.toggle();
      }

      while (keyLyrics.wasPressed()) {
         config.lyricsSky = !config.lyricsSky;
         saveConfig();
         Island.notify(Fx.t(config.lyricsSky ? "openlyrics.media.lyricson" : "openlyrics.media.lyricsoff"), config.lyricsSky);
      }
   }
}
