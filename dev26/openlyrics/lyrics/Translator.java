package dev.openlyrics.lyrics;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.openlyrics.OpenLyrics;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Translator {
   private static final Map<String, String> CACHE = Collections.synchronizedMap(new LinkedHashMap<String, String>(256, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<String, String> e) {
         return this.size() > 400;
      }
   });
   private static final Set<String> PENDING = Collections.synchronizedSet(new HashSet<>());
   private static final ExecutorService EXEC = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "openlyrics-Translate");
      t.setDaemon(true);
      return t;
   });
   private static volatile long blockedUntil = 0L;

   private Translator() {
   }

   public static String get(String line) {
      if (OpenLyrics.cfg().translate && line != null && !line.isBlank()) {
         String key = OpenLyrics.cfg().translateTo + "|" + line;
         String v = CACHE.get(key);
         if (v != null) {
            return v.isEmpty() ? null : v;
         } else {
            request(line);
            return null;
         }
      } else {
         return null;
      }
   }

   public static void prefetch(String line) {
      if (OpenLyrics.cfg().translate && line != null && !line.isBlank()) {
         get(line);
      }
   }

   public static void prefetchUpcoming(List<NowPlaying.Line> lines, long progress) {
      if (!OpenLyrics.cfg().translate || lines == null || lines.isEmpty()) {
         return;
      }
      int i = 0;
      while (i < lines.size() && lines.get(i).timeMs() + 400L < progress) {
         i++;
      }
      for (int n = 0; i < lines.size() && n < 12; i++) {
         String t = lines.get(i).text();
         if (t != null && !t.isBlank()) {
            prefetch(t);
            n++;
         }
      }
   }

   private static void request(String line) {
      String to = OpenLyrics.cfg().translateTo;
      String key = to + "|" + line;
      if (System.currentTimeMillis() >= blockedUntil && PENDING.add(key)) {
         EXEC.execute(() -> {
            try {
               String url = "https://api.mymemory.translated.net/get?q=" + LyricsProvider.enc(line) + "&langpair=en%7C" + LyricsProvider.enc(to);
               Http.Response r = Http.get(url, Map.of());
               if (r.code() != 429) {
                  String out = "";
                  if (r.code() == 200) {
                     JsonObject o = JsonParser.parseString(r.body()).getAsJsonObject();
                     if (o.has("responseData") && o.get("responseData").isJsonObject()) {
                        JsonObject d = o.getAsJsonObject("responseData");
                        if (d.has("translatedText") && !d.get("translatedText").isJsonNull()) {
                           out = d.get("translatedText").getAsString().trim();
                        }
                     }
                  }

                  if (out.equalsIgnoreCase(line.trim()) || out.toUpperCase().contains("MYMEMORY WARNING")) {
                     out = "";
                  }

                  CACHE.put(key, out);
                  Thread.sleep(350L);
                  return;
               }

               blockedUntil = System.currentTimeMillis() + 60000L;
            } catch (Exception var11) {
               blockedUntil = System.currentTimeMillis() + 15000L;
               return;
            } finally {
               PENDING.remove(key);
            }
         });
      }
   }
}
