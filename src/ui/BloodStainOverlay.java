package ui;

import main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * BloodStainOverlay — menampilkan noda darah di tepi layar saat player kena hit.
 *
 * - Sprite dipilih random dari bloodStain1–6
 * - Posisi random di tepi/sudut layar, tidak di tengah
 * - Opacity sesuai damage yang diterima (makin besar damage, makin pekat)
 * - Perlahan fade out setelah muncul
 */
public class BloodStainOverlay {

    private final GamePanel gp;
    private final Random random = new Random();

    private BufferedImage[] sprites;
    private static final int SPRITE_COUNT = 6;

    // Daftar noda aktif di layar
    private final ArrayList<Stain> activeStains = new ArrayList<>();

    // Berapa damage maksimal yang dianggap "full opacity" (disesuaikan dengan damage zombie)
    private static final float MAX_DAMAGE_FOR_FULL_OPACITY = 40f;

    public BloodStainOverlay(GamePanel gp) {
        this.gp = gp;
        loadSprites();
    }

    private void loadSprites() {
        sprites = new BufferedImage[SPRITE_COUNT];
        for (int i = 0; i < SPRITE_COUNT; i++) {
            try {
                sprites[i] = ImageIO.read(
                        getClass().getResourceAsStream("/uiGraphics/bloodStain/bloodStain" + (i + 1) + ".png"));
            } catch (Exception e) {
                System.out.println("[BloodStain] Gagal load sprite " + (i + 1));
            }
        }
    }

    /**
     * Panggil ini saat player menerima damage.
     * @param damage Jumlah damage yang diterima
     */
    public void onPlayerHit(int damage) {
        // Opacity awal proporsional dengan damage (min 0.35, max 0.90)
        float rawOpacity = Math.min(damage / MAX_DAMAGE_FOR_FULL_OPACITY, 1f);
        float startOpacity = 0.35f + rawOpacity * 0.55f;

        // Spawn 1–2 noda tergantung damage
        int count = (damage >= 25) ? 2 : 1;
        for (int i = 0; i < count; i++) {
            activeStains.add(createStain(startOpacity));
        }
    }

    private Stain createStain(float startOpacity) {
        // Pilih sprite random
        BufferedImage sprite = null;
        for (int attempt = 0; attempt < 10; attempt++) {
            BufferedImage candidate = sprites[random.nextInt(SPRITE_COUNT)];
            if (candidate != null) { sprite = candidate; break; }
        }

        // Ukuran noda — random antara 1/4 sampai 1/2 lebar layar
        int minSize = gp.screenWidht / 5;
        int maxSize = gp.screenWidht / 3;
        int size    = minSize + random.nextInt(maxSize - minSize);

        // ── Posisi: di tepi layar, jangan di tengah ──────────────────────────
        // Bagi layar jadi zona: kiri, kanan, atas, bawah, dan 4 sudut
        // Pusat zona ada di tepi, posisi akhir sedikit masuk ke layar
        int screenW = gp.screenWidht;
        int screenH = gp.screenHeight;

        // Margin: seberapa jauh dari tepi layar pusat sprite bisa berada
        int marginX = size / 3;
        int marginY = size / 3;

        // Zona: 0=kiri, 1=kanan, 2=atas, 3=bawah, 4=kiri-atas, 5=kanan-atas, 6=kiri-bawah, 7=kanan-bawah
        int zone = random.nextInt(8);
        int x, y;

        switch (zone) {
            case 0:  // kiri
                x = -size / 2 + random.nextInt(marginX);
                y = marginY + random.nextInt(Math.max(1, screenH - size - marginY * 2));
                break;
            case 1:  // kanan
                x = screenW - size / 2 - random.nextInt(marginX);
                y = marginY + random.nextInt(Math.max(1, screenH - size - marginY * 2));
                break;
            case 2:  // atas
                x = marginX + random.nextInt(Math.max(1, screenW - size - marginX * 2));
                y = -size / 2 + random.nextInt(marginY);
                break;
            case 3:  // bawah
                x = marginX + random.nextInt(Math.max(1, screenW - size - marginX * 2));
                y = screenH - size / 2 - random.nextInt(marginY);
                break;
            case 4:  // sudut kiri-atas
                x = -size / 3 + random.nextInt(marginX);
                y = -size / 3 + random.nextInt(marginY);
                break;
            case 5:  // sudut kanan-atas
                x = screenW - size * 2 / 3 - random.nextInt(marginX);
                y = -size / 3 + random.nextInt(marginY);
                break;
            case 6:  // sudut kiri-bawah
                x = -size / 3 + random.nextInt(marginX);
                y = screenH - size * 2 / 3 - random.nextInt(marginY);
                break;
            default: // sudut kanan-bawah
                x = screenW - size * 2 / 3 - random.nextInt(marginX);
                y = screenH - size * 2 / 3 - random.nextInt(marginY);
                break;
        }

        // Rotasi random agar tidak monoton
        double rotation = random.nextDouble() * Math.PI * 2;

        // Durasi hidup: 120–180 frame (2–3 detik) sebelum mulai fade
        int holdFrames = 90 + random.nextInt(60);
        // Durasi fade out: 60–90 frame (1–1.5 detik)
        int fadeFrames = 60 + random.nextInt(30);

        return new Stain(sprite, x, y, size, rotation, startOpacity, holdFrames, fadeFrames);
    }

    /** Panggil di GamePanel.update() */
    public void update() {
        Iterator<Stain> it = activeStains.iterator();
        while (it.hasNext()) {
            Stain s = it.next();
            s.update();
            if (s.isDone()) it.remove();
        }
    }

    /** Panggil di GamePanel.paintComponent() SETELAH semua game object, SEBELUM HUD lain */
    public void draw(Graphics2D g2) {
        for (Stain s : activeStains) {
            s.draw(g2);
        }
    }

    // ── Inner class Stain ─────────────────────────────────────────────────────

    private static class Stain {
        BufferedImage sprite;
        int     x, y, size;
        double  rotation;
        float   opacity;
        float   startOpacity;

        int holdFrames;   // frame menunggu sebelum fade
        int fadeFrames;   // durasi fade
        int timer = 0;

        boolean holding = true;

        Stain(BufferedImage sprite, int x, int y, int size, double rotation,
              float startOpacity, int holdFrames, int fadeFrames) {
            this.sprite       = sprite;
            this.x            = x;
            this.y            = y;
            this.size         = size;
            this.rotation     = rotation;
            this.startOpacity = startOpacity;
            this.opacity      = startOpacity;
            this.holdFrames   = holdFrames;
            this.fadeFrames   = fadeFrames;
        }

        void update() {
            timer++;
            if (holding) {
                if (timer >= holdFrames) {
                    holding = false;
                    timer   = 0;
                }
            } else {
                // Fade out smooth (ease-in: lambat di awal, cepat di akhir)
                float t = (float) timer / fadeFrames;
                float eased = t * t; // ease-in quadratic
                opacity = startOpacity * (1f - eased);
                if (opacity < 0) opacity = 0;
            }
        }

        void draw(Graphics2D g2) {
            if (sprite == null || opacity <= 0) return;

            Composite oldComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

            // Gambar dengan rotasi dari pusat sprite
            int cx = x + size / 2;
            int cy = y + size / 2;

            var oldTransform = g2.getTransform();
            g2.translate(cx, cy);
            g2.rotate(rotation);
            g2.drawImage(sprite, -size / 2, -size / 2, size, size, null);
            g2.setTransform(oldTransform);

            g2.setComposite(oldComposite);
        }

        boolean isDone() {
            return !holding && opacity <= 0;
        }
    }

    public void reset() {
        activeStains.clear();
    }
}