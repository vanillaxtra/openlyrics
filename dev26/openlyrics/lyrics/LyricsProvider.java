package dev.openlyrics.lyrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LyricsProvider {
   private static final String BASE = "https://lrclib.net/api";
   private static final Pattern LRC = Pattern.compile("\\[(\\d{1,3}):(\\d{1,2}(?:[.:]\\d{1,3})?)]");
   private static final Map<String, List<NowPlaying.Line>> CACHE = new HashMap<>();
   private static final Map<String, Integer> OFFSET_CACHE = new HashMap<>();
   private static final Pattern WORD_TAG = Pattern.compile("<(\\d{1,3}):(\\d{1,2}(?:[.:]\\d{1,3})?)>");
   private static final Pattern OFFSET = Pattern.compile("\\[offset:\\s*([+-]?\\d+)\\s*]", Pattern.CASE_INSENSITIVE);
   private static int fileOffsetMs;

   private LyricsProvider() {
   }

   public static synchronized List<NowPlaying.Line> cached(String trackId) {
      return CACHE.get(trackId);
   }

   public static synchronized int cachedOffset(String trackId) {
      Integer o = OFFSET_CACHE.get(trackId);
      return o == null ? fileOffsetMs : o;
   }

   public static int fileOffset() {
      return fileOffsetMs;
   }

   public static List<NowPlaying.Line> fetch(String trackId, String title, String artist, String album, long durationMs) throws Exception {
      List<NowPlaying.Line> c = cached(trackId);
      if (c != null) {
         return c;
      } else {
         List<NowPlaying.Line> lines = null;
         String url = "https://lrclib.net/api/get?track_name="
            + enc(title)
            + "&artist_name="
            + enc(artist)
            + (album != null && !album.isBlank() ? "&album_name=" + enc(album) : "")
            + (durationMs > 0L ? "&duration=" + Math.round(durationMs / 1000.0) : "");
         Http.Response r = Http.get(url, Map.of());
         if (r.code() == 200) {
            lines = fromRecord(JsonParser.parseString(r.body()).getAsJsonObject());
         }

         if (lines == null) {
            String clean = title.replaceAll("\\s*[-–]\\s.*$", "").replaceAll("\\s*[(\\[].*?[)\\]]", "").trim();
            Http.Response s = Http.get("https://lrclib.net/api/search?track_name=" + enc(clean) + "&artist_name=" + enc(artist), Map.of());
            if (s.code() == 200) {
               JsonArray arr = JsonParser.parseString(s.body()).getAsJsonArray();
               double best = Double.MAX_VALUE;

               for (JsonElement e : arr) {
                  JsonObject o = e.getAsJsonObject();
                  List<NowPlaying.Line> cand = fromRecord(o);
                  if (cand != null) {
                     double d = durationMs <= 0L
                        ? 0.0
                        : (o.has("duration") && !o.get("duration").isJsonNull() ? Math.abs(o.get("duration").getAsDouble() - durationMs / 1000.0) : 30.0);
                     if (flag(o, "hasWordSync")) {
                        d -= 4.0;
                     }

                     if (d < best && d < 8.0) {
                        best = d;
                        lines = cand;
                     }
                  }
               }
            }
         }
         if (lines != null && lines.isEmpty()) {
            lines = null;
         }

         synchronized (LyricsProvider.class) {
            CACHE.put(trackId, lines == null ? Collections.emptyList() : lines);
            OFFSET_CACHE.put(trackId, fileOffsetMs);
            if (CACHE.size() > 60) {
               CACHE.clear();
               OFFSET_CACHE.clear();
            }

            return lines;
         }
      }
   }

   public static List<NowPlaying.Line> parse(String lrc) {
      List<NowPlaying.Line> out = new ArrayList<>();
      fileOffsetMs = 0;

      for (String raw : lrc.split("\\r?\\n")) {
         Matcher off = OFFSET.matcher(raw);
         if (off.find()) {
            try {
               fileOffsetMs = Integer.parseInt(off.group(1));
            } catch (NumberFormatException var7) {
            }
            continue;
         }

         Matcher m = LRC.matcher(raw);
         List<Long> times = new ArrayList<>();

         int end;
         for (end = 0; m.find() && m.start() == end; end = m.end()) {
            long min = Long.parseLong(m.group(1));
            double sec = Double.parseDouble(m.group(2).replace(':', '.'));
            times.add(min * 60000L + Math.round(sec * 1000.0));
         }

         if (!times.isEmpty()) {
            String text = raw.substring(end).trim();

            for (long t : times) {
               out.add(text.indexOf(60) >= 0 ? enhancedLine(t, text) : line(t, text));
            }
         }
      }

      out.sort((a, b) -> Long.compare(a.timeMs(), b.timeMs()));
      return out;
   }

   static NowPlaying.Line enhancedLine(long t, String text) {
      Matcher m = WORD_TAG.matcher(text);
      List<String> words = new ArrayList<>();
      List<Long> times = new ArrayList<>();
      int last = -1;

      long lastT;
      for (lastT = t; m.find(); last = m.end()) {
         if (last >= 0) {
            addWords(text.substring(last, m.start()), lastT, words, times);
         }

         lastT = Long.parseLong(m.group(1)) * 60000L + Math.round(Double.parseDouble(m.group(2).replace(':', '.')) * 1000.0);
      }

      if (last >= 0) {
         addWords(text.substring(last), lastT, words, times);
      }

      String clean = WORD_TAG.matcher(text).replaceAll("").replaceAll("\\s+", " ").trim();
      if (words.isEmpty()) {
         return line(t, clean);
      } else {
         long[] wt = new long[times.size()];

         for (int i = 0; i < wt.length; i++) {
            wt[i] = Math.max(t, times.get(i));
         }

         return new NowPlaying.Line(t, clean, words.toArray(new String[0]), wt);
      }
   }

   private static void addWords(String chunk, long time, List<String> words, List<Long> times) {
      for (String w : chunk.trim().split("\\s+")) {
         if (!w.isBlank()) {
            words.add(w);
            times.add(time);
         }
      }
   }

   public static NowPlaying.Line line(long t, String text) {
      String[] words = text.isBlank() ? new String[0] : text.trim().split("\\s+");
      return new NowPlaying.Line(t, text, words);
   }

   private static List<NowPlaying.Line> fromRecord(JsonObject o) {
      if (flag(o, "hasWordSync")) {
         List<NowPlaying.Line> words = parseLyricsfile(str(o, "lyricsfile"));
         if (words != null) {
            return words;
         }
      }

      String synced = str(o, "syncedLyrics");
      if (synced != null) {
         List<NowPlaying.Line> lrc = parse(synced);
         if (lrc != null && !lrc.isEmpty()) {
            return lrc;
         }
      }

      return parseLyricsfile(str(o, "lyricsfile"));
   }

   static List<NowPlaying.Line> parseLyricsfile(String yaml) {
      if (yaml == null || yaml.isBlank()) {
         return null;
      }

      fileOffsetMs = 0;
      List<NowPlaying.Line> out = new ArrayList<>();
      String lineText = null;
      long lineStart = -1L;
      List<String> words = new ArrayList<>();
      List<Long> times = new ArrayList<>();
      String pendingWord = null;
      boolean inLines = false;
      boolean inWords = false;
      int lineDash = -1;

      for (String raw : yaml.split("\\r?\\n")) {
         if (raw.isBlank()) {
            continue;
         }

         int ind = 0;
         while (ind < raw.length() && raw.charAt(ind) == ' ') {
            ind++;
         }

         String s = raw.substring(ind);
         if (s.startsWith("#")) {
            continue;
         }

         if (ind == 0 && !s.startsWith("-")) {
            if (s.startsWith("lines:")) {
               flushLine(out, lineText, lineStart, words, times);
               lineText = null;
               lineStart = -1L;
               inLines = true;
               inWords = false;
               lineDash = -1;
            } else if (inLines) {
               break;
            } else if (isKey(s, "offset_ms")) {
               long ms = parseMs(keyVal(s, "offset_ms"));
               if (ms != -1L) {
                  fileOffsetMs = (int)Math.max(-5000L, Math.min(5000L, ms));
               }
            }
            continue;
         }

         if (!inLines) {
            if (isKey(s, "offset_ms")) {
               long ms = parseMs(keyVal(s, "offset_ms"));
               if (ms != -1L) {
                  fileOffsetMs = (int)Math.max(-5000L, Math.min(5000L, ms));
               }
            }
            continue;
         }

         boolean dash = s.startsWith("- ");
         String body = dash ? s.substring(2).trim() : s;
         if (dash && (lineDash < 0 || ind <= lineDash) && isKey(body, "text")) {
            flushLine(out, lineText, lineStart, words, times);
            lineDash = ind;
            inWords = false;
            pendingWord = null;
            lineText = yamlScalar(keyVal(body, "text"));
            lineStart = -1L;
         } else if (isKey(body, "start_ms")) {
            long ms = parseMs(keyVal(body, "start_ms"));
            if (inWords && pendingWord != null) {
               addWords(pendingWord, ms, words, times);
               pendingWord = null;
            } else if (!inWords) {
               lineStart = ms;
            }
         } else if (isKey(body, "words")) {
            inWords = true;
         } else if (inWords && dash && isKey(body, "text")) {
            pendingWord = yamlScalar(keyVal(body, "text"));
         }
      }

      flushLine(out, lineText, lineStart, words, times);
      return out.isEmpty() ? null : out;
   }

   private static void flushLine(List<NowPlaying.Line> out, String text, long start, List<String> words, List<Long> times) {
      if (text != null && !text.isBlank() && start >= 0L) {
         if (words.size() >= 1 && words.size() == times.size()) {
            String[] w = words.toArray(new String[0]);
            long[] wt = new long[times.size()];
            for (int i = 0; i < wt.length; i++) {
               wt[i] = Math.max(start, times.get(i));
            }
            out.add(new NowPlaying.Line(start, text, w, wt));
         } else {
            out.add(line(start, text));
         }
      }

      words.clear();
      times.clear();
   }

   private static boolean isKey(String body, String key) {
      return body.startsWith(key) && body.length() > key.length() && body.charAt(key.length()) == ':';
   }

   private static String keyVal(String body, String key) {
      return body.substring(key.length() + 1).trim();
   }

   private static String yamlScalar(String v) {
      if (v.startsWith("|") || v.startsWith(">")) {
         return "";
      }

      if (v.length() >= 2) {
         char q = v.charAt(0);
         if ((q == '\'' || q == '"') && v.charAt(v.length() - 1) == q) {
            String inner = v.substring(1, v.length() - 1);
            return q == '\'' ? inner.replace("''", "'") : inner.replace("\\\"", "\"");
         }
      }

      int hash = v.indexOf(" #");
      return hash > 0 ? v.substring(0, hash).trim() : v;
   }

   private static long parseMs(String v) {
      try {
         return Long.parseLong(yamlScalar(v).replace("_", ""));
      } catch (NumberFormatException e) {
         return -1L;
      }
   }

   private static boolean flag(JsonObject o, String k) {
      return o.has(k) && !o.get(k).isJsonNull() && o.get(k).getAsBoolean();
   }

   private static String str(JsonObject o, String k) {
      if (o.has(k) && !o.get(k).isJsonNull()) {
         String v = o.get(k).getAsString();
         return v.isBlank() ? null : v;
      } else {
         return null;
      }
   }

   static String enc(String s) {
      return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
   }

   public static List<NowPlaying.Line> demo() {
      String[] text = new String[]{
         "leave the ground, the sky still knows this song",
         "every word hangs quiet over the world",
         "say it back if you remember how",
         "I was here before the chorus found me",
         "hold the note until the night lets go",
         "nothing loud, just the line I'm on",
         "walk it off, the melody stays put",
         "if you hear it too, you're not alone",
         "wait it out, the last word always comes",
         "and then it fades like it was never sung"
      };
      List<NowPlaying.Line> l = new ArrayList<>();
      long t = 1500L;

      for (String s : text) {
         l.add(line(t, s));
         t += 3400L;
      }

      l.add(line(t, ""));
      return l;
   }
}
