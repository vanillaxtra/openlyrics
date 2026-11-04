package dev.openlyrics.hud;

import dev.openlyrics.compat.Mc;
import dev.openlyrics.OpenLyrics;
import dev.openlyrics.config.Config;
import dev.openlyrics.gui.LyricsScreen;
import dev.openlyrics.lyrics.NowPlaying;
import dev.openlyrics.lyrics.SpotifyClient;
import dev.openlyrics.lyrics.Translator;
import dev.openlyrics.util.AutoPerf;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;

public final class Island {
   private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");
   private static final Island.Spring W = new Island.Spring(60.0F, 170.0F, 19.0F);
   private static final Island.Spring H = new Island.Spring(14.0F, 170.0F, 19.0F);
   private static final Island.Spring SCROLL = new Island.Spring(0.0F, 90.0F, 18.0F);
   private static final Island.Spring SHOW = new Island.Spring(0.0F, 120.0F, 20.0F);
   private static String note = null;
   private static boolean noteOk = true;
   private static long noteTime = 0L;
   private static String curKey = "";
   private static long switchTime = 0L;
   private static Island.Kind prevKind = Island.Kind.IDLE;
   private static String prevText = "";
   private static Island.Kind curKind = Island.Kind.IDLE;
   private static String curText = "";
   private static boolean landHighlight;
   private static long lastNanos = System.nanoTime();

   private Island() {
   }

   public static void notify(String text, boolean ok) {
      note = text;
      noteOk = ok;
      noteTime = System.currentTimeMillis();
   }

   public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
      Minecraft mc = Minecraft.getInstance();
      Config c = OpenLyrics.cfg();
      if (!Mc.hideGui(mc) && mc.player != null && c.islandEnabled) {
         if (!(Mc.screen(mc) instanceof LyricsScreen)) {
            AutoPerf.begin();

            try {
               renderIsland(g, mc, c);
            } finally {
               AutoPerf.end();
            }
         }
      }
   }

   private static void renderIsland(GuiGraphicsExtractor g, Minecraft mc, Config c) {
      long nowNs = System.nanoTime();
      float dt = Math.min(0.05F, (float)(nowNs - lastNanos) / 1.0E9F);
      lastNanos = nowNs;
      long now = System.currentTimeMillis();
      int sw = g.guiWidth();
      int cx = sw / 2;
      int top = 5;
      float old = Gfx.alpha;
      Gfx.alpha = 1.0F;
      int acc = Theme.accent();
      boolean noteActive = note != null && now - noteTime < 2400L;
      NowPlaying np = SpotifyClient.now();
      boolean hasSong = c.lyricsIsland && np.hasTrack();
      long prog = np.progressNow(c.lyricsOffsetMs);
      int li = hasSong && np.lyricsState == NowPlaying.LyricsState.SYNCED && np.playing ? np.lineIndex(prog) : -1;
      NowPlaying.Line line = li >= 0 ? np.lines.get(li) : null;
      boolean hasLine = line != null && !line.text().isBlank() && prog <= np.lineEnd(li) + 50L;
      Island.Kind kind;
      String text;
      String key;
      if (noteActive) {
         kind = Island.Kind.NOTE;
         text = note;
         key = "n" + noteTime;
      } else if (hasLine) {
         kind = Island.Kind.LYRIC;
         text = line.text();
         key = "l" + np.trackId + li;
      } else if (hasSong) {
         kind = Island.Kind.SONG;
         text = np.title + (np.artist.isBlank() ? "" : "  ·  " + np.artist);
         key = "s" + np.trackId;
      } else {
         kind = Island.Kind.IDLE;
         text = "";
         key = "i";
      }

      if (!key.equals(curKey)) {
         prevKind = curKind;
         prevText = curText;
         curKey = key;
         curKind = kind;
         curText = text;
         switchTime = now;
         landHighlight = kind == Island.Kind.LYRIC;
      }

      int musicW = (int)Math.max(200.0F, Math.min(300.0F, sw * 0.42F));
      float targetW;
      float targetH;
      switch (kind) {
         case NOTE:
            targetW = Math.min(musicW, Gfx.w(text) + 40);
            targetH = 18.0F;
            break;
         case SONG:
         case LYRIC:
            targetW = Math.max(110, Math.min(musicW, Gfx.w(text) + 58));
            targetH = 18.0F;
            break;
         default:
            targetW = 40.0F;
            targetH = 14.0F;
      }

      boolean idleHidden = kind == Island.Kind.IDLE && !c.showClock && !c.showPing;
      float show = SHOW.update(idleHidden ? 0.0F : 1.0F, dt);
      float w = W.update(targetW, dt);
      float h = H.update(targetH, dt);
      if (show < 0.02F) {
         Gfx.alpha = old;
      } else {
         int pw = Math.max(12, Math.round(w));
         int ph = Math.max(8, Math.round(h));
         int px = cx - pw / 2;
         int r = ph / 2;
         int midY = top + ph / 2;
         Gfx.alpha = show;
         if (c.showClock) {
            String t = LocalTime.now().format(HM);
            Gfx.textBold(g, t, px - 10 - Gfx.w(t, Gfx.BOLD), midY - 4, 16777215, 0.95F, true);
         }

         int rx = px + pw + 10;
         if (c.showPing && mc.getConnection() != null) {
            PlayerInfo pi = mc.getConnection().getPlayerInfo(mc.player.getUUID());
            int ping = pi == null ? 0 : pi.getLatency();
            int bars = ping <= 0 ? 0 : (ping < 60 ? 4 : (ping < 120 ? 3 : (ping < 250 ? 2 : 1)));
            float[] ax = new float[9];
            float[] ay = new float[9];

            for (int i = 0; i < 9; i++) {
               double an = Math.PI + i * Math.PI / 8.0;
               ax[i] = rx + 3.5F + (float)Math.cos(an) * 3.0F;
               ay[i] = midY + 0.5F + (float)Math.sin(an) * 3.0F;
            }

            Gfx.aaStroke(g, ax, ay, 1.1F, 10326968, 0.9F);
            String ps = Integer.toString(ping);
            Gfx.textBold(g, ps, rx + 9, midY - 4, 16777215, 0.95F, true);
            int bx = rx + 9 + Gfx.w(ps, Gfx.BOLD) + 5;

            for (int i = 0; i < 4; i++) {
               int bh = 2 + i * 2;
               Gfx.aaRound(g, bx + i * 3, midY + 4 - bh, 2.0F, bh, 1.0F, 16777215, i < bars ? 0.95F : 0.22F);
            }

            rx = bx + 16;
         }

         if (c.showFps) {
            Gfx.text(g, mc.getFps() + " fps", rx, midY - 4, 10326968, 0.85F, true);
         }

         Gfx.aaRound(g, px - 1, top - 1, pw + 2, ph + 2, r + 1, 15658737, 0.7F);
         Gfx.aaRound(g, px, top, pw, ph, r, 1052688, 1.0F);
         g.enableScissor(px + 2, top, px + pw - 2, top + ph);
         float e = kind == Island.Kind.LYRIC ? 1.0F : Anim.outCubic(Anim.since(switchTime, 380L));
         if (e < 1.0F && prevKind != Island.Kind.IDLE && !prevText.isEmpty()) {
            float oa = show * (1.0F - e);
            Gfx.alpha = oa * 0.5F;
            drawSimple(g, prevKind, prevText, px - Math.round(e * 3.0F), top, pw, ph, acc, np, now, false);
            drawSimple(g, prevKind, prevText, px + Math.round(e * 3.0F), top, pw, ph, acc, np, now, false);
         }

         Gfx.alpha = show * e;
         switch (kind) {
            case IDLE:
               float br = 0.5F + 0.5F * (float)Math.sin(now / 900.0);
               Gfx.aaCircle(g, cx, top + h / 2.0F, 2.2F, acc, 0.55F + 0.35F * br);
               break;
            case LYRIC:
               drawLyric(g, line, np, li, prog, px, top, pw, ph, acc, dt, e);
               break;
            default:
               drawSimple(g, kind, text, px, top, pw, ph, acc, np, now, true);
         }

         g.disableScissor();
         if (kind == Island.Kind.LYRIC && c.translate) {
            Translator.prefetchUpcoming(np.lines, prog);
            String tr = Translator.get(line.text());

            if (tr != null) {
               Gfx.alpha = show * e;
               g.pose().pushMatrix();
               g.pose().translate(cx, top + ph + 4);
               g.pose().scale(0.85F, 0.85F);
               Gfx.center(g, Gfx.ellipsize(tr, Math.round(pw / 0.85F)), 0, 0, 14275058, 0.9F, true);
               g.pose().popMatrix();
            }
         }

         Gfx.alpha = old;
      }
   }

   private static void cover(GuiGraphicsExtractor g, int cx, int cy, int acc, boolean playing, long now) {
      if (!Cover.draw(g, cx - 6, cy - 6, 12, 1.0F)) {
         Gfx.aaCircle(g, cx, cy, 6.0F, Gfx.lerpColor(acc, 2760266, 0.35F), 1.0F);
         Gfx.aaCircle(g, cx, cy, 4.6F, Gfx.lerpColor(acc, 16777215, 0.25F), 0.9F);
         double an = playing ? now / 450.0 : 0.8;
         Gfx.aaCircle(g, (float)(cx + Math.cos(an) * 3.0), (float)(cy + Math.sin(an) * 3.0), 1.1F, 16777215, 0.75F);
         Gfx.aaCircle(g, cx, cy, 1.4F, 1380895, 1.0F);
      }
   }

   private static void drawSimple(GuiGraphicsExtractor g, Island.Kind kind, String text, int px, int top, int pw, int ph, int acc, NowPlaying np, long now, boolean live) {
      int midY = top + ph / 2;
      int ty = midY - 4;
      if (kind == Island.Kind.NOTE) {
         int col = noteOk ? 6022299 : 16735354;
         float pop = live ? Anim.outBack(Anim.since(noteTime, 380L)) : 1.0F;
         float ccx = px + 11;
         float ccy = top + ph / 2.0F;
         Gfx.aaCircle(g, ccx, ccy, 5.0F * pop, col, 1.0F);
         if (noteOk) {
            Gfx.aaStroke(g, new float[]{ccx - 2.3F, ccx - 0.6F, ccx + 2.4F}, new float[]{ccy + 0.1F, ccy + 1.8F, ccy - 1.8F}, 1.3F * pop, 16777215, 1.0F);
         } else {
            Gfx.aaRound(g, ccx - 2.5F, ccy - 0.7F, 5.0F, 1.4F, 0.7F, 16777215, 1.0F);
         }

         Gfx.text(g, Gfx.ellipsize(text, pw - 30), px + 21, ty, 16777215, 1.0F, false);
      } else if (kind == Island.Kind.SONG || kind == Island.Kind.LYRIC) {
         cover(g, px + 11, midY, acc, np.playing, now);
         equalizer(g, px + pw - 20, midY, acc, np.playing, now);
         Gfx.text(g, Gfx.ellipsize(text, pw - 50), px + 22, ty, 16777215, kind == Island.Kind.SONG ? 0.95F : 0.45F, false);
      }
   }

   private static void equalizer(GuiGraphicsExtractor g, int x, int cy, int acc, boolean playing, long now) {
      if (!AutoPerf.allow(4)) {
         playing = false;
      }

      double t = now / 1000.0;
      NowPlaying np = SpotifyClient.now();
      float beat = np.beatPulse(np.progressNow(OpenLyrics.cfg().lyricsOffsetMs));

      for (int i = 0; i < 4; i++) {
         float hh = playing ? (float)(3.0 + 3.5 * Math.abs(Math.sin(t * (2.6 + i * 1.1) + i * 1.7)) + 3.5 * beat * (i % 2 == 0 ? 1.0 : 0.7)) : 2.5F;
         int bh = Math.round(hh);
         Gfx.aaRound(g, x + i * 3, cy - hh / 2.0F, 2.0F, hh, 1.0F, acc, 0.95F);
      }
   }

   private static void drawLyric(
      GuiGraphicsExtractor g, NowPlaying.Line line, NowPlaying np, int li, long prog, int px, int top, int pw, int ph, int acc, float dt, float in
   ) {
      long now = System.currentTimeMillis();
      int midY = top + ph / 2;
      cover(g, px + 11, midY, acc, np.playing, now);
      equalizer(g, px + pw - 20, midY, acc, np.playing, now);
      int tx = px + 22;
      int tw = pw - 22 - 26;
      String[] words = line.words();
      float wp = np.wordPosition(li, prog);
      int cw = (int)Math.floor(wp);
      int space = Gfx.w(" ");
      int[] wx = new int[words.length];
      int[] ww = new int[words.length];
      int x = 0;

      for (int i = 0; i < words.length; i++) {
         wx[i] = x;
         ww[i] = Gfx.w(words[i]);
         x += ww[i] + space;
      }

      int full = x - space;
      boolean on = cw >= 0 && cw < words.length;
      int curX = on ? wx[cw] : 0;
      int curW = on ? ww[cw] : 0;
      float sx = full > tw ? Math.max(0.0F, Math.min((float)(full - tw), curX + curW / 2.0F - tw * 0.55F)) : 0.0F;
      if (landHighlight) {
         landHighlight = false;
         SCROLL.snap(sx);
      }
      float scroll = SCROLL.update(sx, dt);
      int base = tx - Math.round(scroll);
      int ty = midY - 4;
      g.enableScissor(tx - 2, top, tx + tw + 2, top + ph);
      if (on) {
         float pad = Math.min(1.5F, space * 0.4F);
         Gfx.aaRound(g, base + curX - pad, top + 4, curW + pad * 2.0F, ph - 8, 2.5F, acc, 1.0F);
      }

      for (int i = 0; i < words.length; i++) {
         boolean sung = i <= cw;
         Gfx.text(g, words[i], base + wx[i], ty, 16777215, sung ? 1.0F : 0.42F, false);
      }

      g.disableScissor();
   }

   private static enum Kind {
      IDLE,
      NOTE,
      SONG,
      LYRIC;
   }

   private static final class Spring {
      float x;
      float v;
      final float k;
      final float d;

      Spring(float x, float stiffness, float damping) {
         this.x = x;
         this.k = stiffness;
         this.d = damping;
      }

      float update(float target, float dt) {
         int steps = Math.max(1, (int)Math.ceil(dt / 0.008F));
         float h = dt / steps;

         for (int i = 0; i < steps; i++) {
            float a = this.k * (target - this.x) - this.d * this.v;
            this.v += a * h;
            this.x = this.x + this.v * h;
         }

         return this.x;
      }

      void snap(float t) {
         this.x = t;
         this.v = 0.0F;
      }
   }
}
