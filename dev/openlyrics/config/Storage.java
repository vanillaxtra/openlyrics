package dev.openlyrics.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.openlyrics.OpenLyrics;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Supplier;
import net.fabricmc.loader.api.FabricLoader;

public final class Storage {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

   private Storage() {
   }

   public static Path dir() {
      return FabricLoader.getInstance().getConfigDir().resolve("openlyrics");
   }

   public static void migrateOldConfigs() {
      try {
         Path old = FabricLoader.getInstance().getConfigDir().resolve("neonauction");
         if (!Files.isDirectory(old) || Files.exists(dir().resolve("config.json"))) {
            return;
         }

         Files.createDirectories(dir());
         Path oldCfg = Files.exists(old.resolve("config-v13-backup.json")) ? old.resolve("config-v13-backup.json") : old.resolve("config.json");
         if (Files.exists(oldCfg)) {
            Files.copy(oldCfg, dir().resolve("config.json"));
         }

         for (String f : new String[]{"spotify.json", "favorites.json"}) {
            Path src = old.resolve(f);
            if (Files.exists(src) && !Files.exists(dir().resolve(f))) {
               Files.copy(src, dir().resolve(f));
            }
         }

         OpenLyrics.LOG.info("Settings imported from config/neonauction");
      } catch (Exception var7) {
         OpenLyrics.LOG.warn("Could not import neonauction settings", var7);
      }
   }

   public static void migrateFromLegacy() {
      if (Files.exists(dir().resolve("config.json"))) {
         return;
      }

      for (String name : new String[]{"neonlyrics", "openlyrics"}) {
         try {
            Path old = FabricLoader.getInstance().getConfigDir().resolve(name);
            if (!Files.isDirectory(old)) {
               continue;
            }

            Files.createDirectories(dir());
            if (Files.exists(old.resolve("config.json"))) {
               Files.copy(old.resolve("config.json"), dir().resolve("config.json"));
            }

            for (String f : new String[]{"spotify.json", "favorites.json"}) {
               Path src = old.resolve(f);
               if (Files.exists(src) && !Files.exists(dir().resolve(f))) {
                  Files.copy(src, dir().resolve(f));
               }
            }

            OpenLyrics.LOG.info("Settings imported from config/{}", name);
            return;
         } catch (Exception var7) {
            OpenLyrics.LOG.warn("Could not import config from {}", name, var7);
         }
      }
   }

   public static <T> T load(String file, Type type, Supplier<T> fallback) {
      Path p = dir().resolve(file);
      if (!Files.exists(p)) {
         return fallback.get();
      } else {
         try {
            Object var6;
            try (Reader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
               T t = (T)GSON.fromJson(r, type);
               var6 = t != null ? t : fallback.get();
            }

            return (T)var6;
         } catch (Exception var9) {
            OpenLyrics.LOG.warn("Could not load {}, using defaults", file, var9);
            return fallback.get();
         }
      }
   }

   public static void save(String file, Object data) {
      try {
         Files.createDirectories(dir());
         Path p = dir().resolve(file);
         Path tmp = dir().resolve(file + ".tmp");
         byte[] json = GSON.toJson(data).getBytes(StandardCharsets.UTF_8);
         Files.write(tmp, json);

         try {
            Files.move(tmp, p, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
         } catch (Exception var8) {
            Files.copy(tmp, p, StandardCopyOption.REPLACE_EXISTING);
            Files.deleteIfExists(tmp);
         }
      } catch (Exception var9) {
         OpenLyrics.LOG.warn("Could not save {}", file, var9);
      }
   }
}
