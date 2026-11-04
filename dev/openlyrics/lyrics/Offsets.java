package dev.openlyrics.lyrics;

import com.google.gson.reflect.TypeToken;
import dev.openlyrics.config.Storage;
import java.util.HashMap;
import java.util.Map;

final class Offsets {
   private static Map<String, Integer> map;

   private Offsets() {
   }

   private static synchronized Map<String, Integer> map() {
      if (map == null) {
         map = Storage.load("offsets.json", new TypeToken<HashMap<String, Integer>>() {
         }.getType(), HashMap::new);
         if (map == null) {
            map = new HashMap<>();
         }
      }

      return map;
   }

   static synchronized boolean has(String id) {
      return id != null && map().containsKey(id);
   }

   static synchronized int get(String id, int fallback) {
      Integer v = id == null ? null : map().get(id);
      return v == null ? fallback : v;
   }

   static synchronized void put(String id, int ms) {
      if (id != null && !id.isBlank()) {
         map().put(id, Math.max(-5000, Math.min(5000, ms)));
         Storage.save("offsets.json", map);
      }
   }
}
