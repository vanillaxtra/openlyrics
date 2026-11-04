package dev.openlyrics.lyrics;

import dev.openlyrics.OpenLyrics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class LyricTiming {
   public final long[][] wordStart;
   public final double beatMs;
   public final double beatPhase;
   public final float confidence;
   public final float sylPerSec;
   private static final Map<List<NowPlaying.Line>, LyricTiming> CACHE = Collections.synchronizedMap(new IdentityHashMap<>());

   private LyricTiming(long[][] ws, double beat, double phase, float conf, float sps) {
      this.wordStart = ws;
      this.beatMs = beat;
      this.beatPhase = phase;
      this.confidence = conf;
      this.sylPerSec = sps;
   }

   public static LyricTiming of(List<NowPlaying.Line> lines) {
      if (lines != null && !lines.isEmpty()) {
         LyricTiming t = CACHE.get(lines);
         if (t == null) {
            t = build(lines);
            if (CACHE.size() > 20) {
               CACHE.clear();
            }

            CACHE.put(lines, t);
         }

         return t;
      } else {
         return null;
      }
   }

   public static void invalidate() {
      CACHE.clear();
   }

   static float weight(String w) {
      float s = NowPlaying.syllables(w);
      char last = w.charAt(w.length() - 1);
      if (last == ',' || last == '.' || last == '!' || last == '?' || last == ')') {
         s += 0.6F;
      }

      return s;
   }

   private static long nextStart(List<NowPlaying.Line> lines, int i) {
      return i + 1 < lines.size() ? lines.get(i + 1).timeMs() : lines.get(i).timeMs() + 8000L;
   }

   private static LyricTiming build(List<NowPlaying.Line> lines) {
      int n = lines.size();
      List<Double> paces = new ArrayList<>();

      for (int i = 0; i < n; i++) {
         NowPlaying.Line l = lines.get(i);
         if (l.words().length >= 3) {
            long gap = nextStart(lines, i) - l.timeMs();
            if (gap >= 900L && gap <= 9000L) {
               float weightSum = 0.0F;
               for (String w : l.words()) {
                  weightSum += weight(w);
               }

               if (weightSum > 0.0F) {
                  paces.add((gap / 1000.0) / weightSum);
               }
            }
         }
      }

      // 20th percentile of sec/weight so instrumental gaps don't slow the voice
      double pace;
      if (paces.size() >= 4) {
         Collections.sort(paces);
         pace = paces.get((int)(paces.size() * 0.2));
      } else {
         pace = 0.26;
      }

      pace = Math.max(0.15, Math.min(0.26, pace));
      pace = Math.max(0.10, Math.min(0.40, pace * (OpenLyrics.cfg().lyricsPacePct / 100.0)));
      float sps = (float)(1.0 / pace);
      List<Long> starts = new ArrayList<>();

      for (NowPlaying.Line l : lines) {
         if (!l.text().isBlank()) {
            starts.add(l.timeMs());
         }
      }

      double bestBeat = 0.0;
      double bestPhase = 0.0;
      float bestScore = 0.0F;
      if (starts.size() >= 6) {
         for (double bpm = 60.0; bpm <= 180.0; bpm += 0.25) {
            double p = 60000.0 / bpm;
            double sx = 0.0;
            double sy = 0.0;

            for (long t : starts) {
               double a = (Math.PI * 2) * (t % p) / p;
               sx += Math.cos(a);
               sy += Math.sin(a);
            }

            float r = (float)(Math.sqrt(sx * sx + sy * sy) / starts.size());
            float pref = bpm >= 80.0 && bpm <= 150.0 ? 1.0F : 0.93F;
            if (r * pref > bestScore) {
               bestScore = r * pref;
               bestBeat = p;
               double ang = Math.atan2(sy, sx);
               if (ang < 0.0) {
                  ang += Math.PI * 2;
               }

               bestPhase = ang / (Math.PI * 2) * p;
            }
         }
      }

      boolean useGrid = bestScore >= 0.45F && bestBeat > 0.0;
      long[][] ws = new long[n][];
      int estimated = 0;
      double sungSum = 0.0;

      for (int ix = 0; ix < n; ix++) {
         NowPlaying.Line lx = lines.get(ix);
         String[] words = lx.words();
         ws[ix] = new long[words.length];
         if (words.length != 0) {
            if (lx.wordTimes() != null && lx.wordTimes().length == words.length) {
               ws[ix] = (long[])lx.wordTimes().clone();
               spreadTiedWords(ws[ix], words);
            } else {
               long start = lx.timeMs();
               long gap = Math.max(300L, nextStart(lines, ix) - start);
               float total = 0.0F;

               for (String w : words) {
                  total += weight(w);
               }

               if (total <= 0.0F) {
                  total = words.length;
               }

               // sparse ~9 syl / 6s -> ~2.6s (was ~4.7s); dense 9/3.2s stays ~2.6-3.0s, never > gap*0.95
               double sungMs = Math.max(400.0, Math.min(gap * 0.95, total * pace * 1000.0 + 300.0));
               estimated++;
               sungSum += sungMs;
               double acc = 0.0;
               long prev = start - 1L;

               for (int k = 0; k < words.length; k++) {
                  double t = k == 0 ? start : start + acc / total * sungMs;
                  long ti = Math.max(prev + 40L, Math.round(t));
                  ti = Math.min(ti, start + (long)(gap * 0.97));
                  ws[ix][k] = ti;
                  prev = ti;
                  acc += weight(words[k]);
               }
            }

            runningMax(ws[ix]);
         }
      }

      if (OpenLyrics.LOG.isDebugEnabled()) {
         OpenLyrics.LOG.debug(
            "lyrics pace={} eligible={} avgSung={}ms",
            pace,
            paces.size(),
            estimated == 0 ? 0.0 : sungSum / estimated
         );
      }

      return new LyricTiming(ws, useGrid ? bestBeat : 0.0, bestPhase, useGrid ? Math.min(1.0F, bestScore) : 0.0F, sps);
   }

   // identical stamps from word-sync files; first word keeps t
   private static void spreadTiedWords(long[] s, String[] words) {
      int i = 0;
      while (i < s.length) {
         int j = i + 1;
         while (j < s.length && s[j] == s[i]) {
            j++;
         }

         if (j - i >= 2) {
            long t = s[i];
            long t2 = j < s.length ? s[j] : t + (long)(j - i) * 350L;
            long end = Math.min(t2, t + (long)(j - i) * 350L);
            if (end < t) {
               end = t;
            }

            float total = 0.0F;
            for (int k = i + 1; k < j; k++) {
               total += weight(words[k]);
            }

            if (total <= 0.0F) {
               total = j - i - 1;
            }

            double acc = 0.0;
            for (int k = i + 1; k < j; k++) {
               acc += weight(words[k]);
               s[k] = t + Math.round(acc / total * (end - t));
            }
         }

         i = j;
      }
   }

   private static void runningMax(long[] s) {
      long m = Long.MIN_VALUE;
      for (int i = 0; i < s.length; i++) {
         if (s[i] < m) {
            s[i] = m;
         } else {
            m = s[i];
         }
      }
   }

   private static int lastAtOrBefore(long[] s, long progress) {
      int lo = 0;
      int hi = s.length - 1;
      int k = -1;
      while (lo <= hi) {
         int m = lo + hi >>> 1;
         if (s[m] <= progress) {
            k = m;
            lo = m + 1;
         } else {
            hi = m - 1;
         }
      }

      return k;
   }

   public long wordStartMs(int line, int word) {
      if (line >= 0 && line < this.wordStart.length) {
         long[] s = this.wordStart[line];
         return word >= 0 && word < s.length ? s[word] : -1L;
      } else {
         return -1L;
      }
   }

   public int wordCount(int line) {
      return line >= 0 && line < this.wordStart.length ? this.wordStart[line].length : 0;
   }

   public float wordPosition(int line, long progress, long lineEnd) {
      if (line >= 0 && line < this.wordStart.length) {
         long[] s = this.wordStart[line];
         if (s.length == 0) {
            return -1.0F;
         } else {
            int k = lastAtOrBefore(s, progress);

            if (k < 0) {
               return 0.0F;
            } else if (k >= s.length - 1) {
               long end = Math.max(s[s.length - 1] + 200L, Math.min(lineEnd, s[s.length - 1] + 900L));
               return s.length - 1 + Math.min(0.999F, (float)(progress - s[s.length - 1]) / (float)(end - s[s.length - 1]));
            } else {
               return k + (float)(progress - s[k]) / (float)Math.max(1L, s[k + 1] - s[k]);
            }
         }
      } else {
         return -1.0F;
      }
   }

   public float beatPulse(long progress) {
      if (this.beatMs <= 0.0) {
         return 0.0F;
      } else {
         double ph = ((progress - this.beatPhase) % this.beatMs + this.beatMs) % this.beatMs;
         return (float)Math.exp(-ph / (this.beatMs * 0.18)) * this.confidence;
      }
   }
}
