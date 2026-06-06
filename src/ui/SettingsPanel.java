package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;

/**
 * SettingsPanel — layar pengaturan game.
 *
 * Fitur:
 *   - Slider master volume (0% - 100%), langsung terdengar efeknya pada BGM
 *   - Tombol Back untuk kembali ke MainMenuPanel
 *   - Desain sesuai tema game (gelap, survival, pixel-art aesthetic)
 *   - Font NineByFiveNbp-MypB.ttf
 */
public class SettingsPanel extends JPanel {

    // ── Ukuran layar ─────────────────────────────────────────────────────────
    public static final int SCREEN_W = 768;
    public static final int SCREEN_H = 576;

    // ── Palette warna tema game ───────────────────────────────────────────────
    private static final Color BG_DARK      = new Color(8,  6,  3);
    private static final Color BORDER_COLOR = new Color(110, 75, 30);
    private static final Color ACCENT_GOLD  = new Color(190, 140, 50);
    private static final Color TEXT_PRIMARY = new Color(220, 195, 140);
    private static final Color TEXT_DIM     = new Color(130, 110, 70);
    private static final Color TRACK_BG     = new Color(35, 25, 12);
    private static final Color TRACK_FILL   = new Color(150, 105, 35);
    private static final Color THUMB_NORMAL = new Color(200, 150, 55);
    private static final Color THUMB_HOVER  = new Color(230, 185, 80);
    private static final Color THUMB_PRESS  = new Color(255, 210, 100);
    private static final Color BTN_NORMAL   = new Color(40, 28, 12);
    private static final Color BTN_HOVER    = new Color(60, 42, 18);
    private static final Color BTN_PRESS    = new Color(25, 18, 7);

    // ── Font ─────────────────────────────────────────────────────────────────
    private Font fontTitle;
    private Font fontLabel;
    private Font fontSmall;
    private Font fontBtn;

    // ── Volume (0.0 – 1.0) ───────────────────────────────────────────────────
    private float masterVolume = 1.0f;

    // ── Slider geometry ───────────────────────────────────────────────────────
    private static final int SLIDER_X     = 220;
    private static final int SLIDER_Y     = 270;
    private static final int SLIDER_W     = 328;
    private static final int SLIDER_H     = 8;
    private static final int THUMB_RADIUS = 10;

    private boolean thumbHovered  = false;
    private boolean thumbDragging = false;
    private int     dragOffsetX   = 0;

    // ── Tombol Back ──────────────────────────────────────────────────────────
    private static final int BTN_W = 160;
    private static final int BTN_H = 42;
    private static final int BTN_X = (SCREEN_W - BTN_W) / 2;
    private static final int BTN_Y = 420;
    private boolean btnHovered = false;
    private boolean btnPressed = false;

    // ── Referensi ─────────────────────────────────────────────────────────────
    private final SoundManager soundManager;
    private final Runnable     onBack;

    // ── Noise texture ────────────────────────────────────────────────────────
    private BufferedImage noiseTexture;

    // ─────────────────────────────────────────────────────────────────────────
    //  CONSTRUCTOR
    // ─────────────────────────────────────────────────────────────────────────
    public SettingsPanel(SoundManager soundManager, Runnable onBack) {
        this.soundManager = soundManager;
        this.onBack       = onBack;

        setPreferredSize(new Dimension(SCREEN_W, SCREEN_H));
        setBackground(BG_DARK);
        setDoubleBuffered(true);
        setFocusable(true);
        setLayout(null);

        loadFont();
        generateNoiseTexture();
        setupMouseListeners();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  FONT
    // ─────────────────────────────────────────────────────────────────────────
    private void loadFont() {
        Font base = null;
        String[] paths = { "/fonts/NineByFiveNbp-MypB.ttf", "/NineByFiveNbp-MypB.ttf" };
        for (String path : paths) {
            try {
                InputStream is = getClass().getResourceAsStream(path);
                if (is == null) continue;
                base = Font.createFont(Font.TRUETYPE_FONT, is);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(base);
                System.out.println("[SettingsPanel] Font dimuat dari " + path);
                break;
            } catch (Exception e) {
                System.out.println("[SettingsPanel] Gagal load font: " + e.getMessage());
            }
        }
        if (base == null) {
            System.out.println("[SettingsPanel] Fallback ke Monospaced.");
            base = new Font("Monospaced", Font.PLAIN, 12);
        }
        fontTitle = base.deriveFont(Font.PLAIN, 36f);
        fontLabel = base.deriveFont(Font.PLAIN, 18f);
        fontSmall = base.deriveFont(Font.PLAIN, 13f);
        fontBtn   = base.deriveFont(Font.PLAIN, 20f);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  NOISE TEXTURE
    // ─────────────────────────────────────────────────────────────────────────
    private void generateNoiseTexture() {
        noiseTexture = new BufferedImage(SCREEN_W, SCREEN_H, BufferedImage.TYPE_INT_ARGB);
        java.util.Random rnd = new java.util.Random(42);
        for (int y = 0; y < SCREEN_H; y++)
            for (int x = 0; x < SCREEN_W; x++)
                noiseTexture.setRGB(x, y, (rnd.nextInt(22) << 24) | 0xFFFFFF);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  MOUSE
    // ─────────────────────────────────────────────────────────────────────────
    private void setupMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int mx = e.getX(), my = e.getY();
                thumbHovered = isOverThumb(mx, my);
                btnHovered   = getBtnRect().contains(mx, my);
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (!thumbDragging) return;
                int rawX     = e.getX() - dragOffsetX;
                int clamped  = Math.max(SLIDER_X, Math.min(SLIDER_X + SLIDER_W, rawX));
                masterVolume = (float)(clamped - SLIDER_X) / SLIDER_W;
                applyVolume();
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int mx = e.getX(), my = e.getY();

                // Klik langsung pada track → geser thumb ke posisi klik
                if (isOverTrack(mx, my)) {
                    int clamped  = Math.max(SLIDER_X, Math.min(SLIDER_X + SLIDER_W, mx));
                    masterVolume = (float)(clamped - SLIDER_X) / SLIDER_W;
                    thumbDragging = true;
                    dragOffsetX   = 0;
                    applyVolume();
                    repaint();
                    return;
                }

                // Mulai drag thumb
                if (isOverThumb(mx, my)) {
                    thumbDragging = true;
                    dragOffsetX   = mx - thumbCenterX();
                    repaint();
                }

                // Tekan tombol Back
                if (getBtnRect().contains(mx, my)) {
                    btnPressed = true;
                    if (soundManager != null) soundManager.playSFX("uiClick");
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                boolean wasPressed = btnPressed;
                thumbDragging = false;
                btnPressed    = false;
                thumbHovered  = isOverThumb(e.getX(), e.getY());
                btnHovered    = getBtnRect().contains(e.getX(), e.getY());

                // Eksekusi Back hanya kalau release di atas tombol
                if (wasPressed && getBtnRect().contains(e.getX(), e.getY())) {
                    if (onBack != null) SwingUtilities.invokeLater(onBack);
                }
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                thumbHovered = false;
                btnHovered   = false;
                repaint();
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────────────────
    private int thumbCenterX() { return SLIDER_X + (int)(masterVolume * SLIDER_W); }
    private int thumbCenterY() { return SLIDER_Y + SLIDER_H / 2; }

    private boolean isOverThumb(int mx, int my) {
        int dx = mx - thumbCenterX(), dy = my - thumbCenterY();
        return dx*dx + dy*dy <= (THUMB_RADIUS+4)*(THUMB_RADIUS+4);
    }

    private boolean isOverTrack(int mx, int my) {
        return mx >= SLIDER_X - 4 && mx <= SLIDER_X + SLIDER_W + 4
                && my >= SLIDER_Y - 8  && my <= SLIDER_Y + SLIDER_H + 8;
    }

    private Rectangle getBtnRect() { return new Rectangle(BTN_X, BTN_Y, BTN_W, BTN_H); }

    /**
     * Kirim nilai volume ke SoundManager — langsung terdengar pada BGM.
     */
    private void applyVolume() {
        if (soundManager != null) {
            soundManager.setMasterVolume(masterVolume);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  GETTER / SETTER (dipakai MainMenuPanel untuk simpan & restore volume)
    // ─────────────────────────────────────────────────────────────────────────
    public float getMasterVolume() { return masterVolume; }

    public void setMasterVolume(float v) {
        masterVolume = Math.max(0f, Math.min(1f, v));
        repaint();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  RENDERING
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);

        drawBackground(g2);
        drawNoise(g2);
        drawPanel(g2);
        drawTitle(g2);
        drawVolumeSection(g2);
        drawBackButton(g2);
        drawCornerDecals(g2);

        g2.dispose();
    }

    private void drawBackground(Graphics2D g2) {
        g2.setColor(BG_DARK);
        g2.fillRect(0, 0, SCREEN_W, SCREEN_H);
        RadialGradientPaint vignette = new RadialGradientPaint(
                new Point(SCREEN_W/2, SCREEN_H/2), Math.max(SCREEN_W, SCREEN_H) * 0.75f,
                new float[]{ 0f, 1f },
                new Color[]{ new Color(30,20,8,0), new Color(0,0,0,200) }
        );
        g2.setPaint(vignette);
        g2.fillRect(0, 0, SCREEN_W, SCREEN_H);
    }

    private void drawNoise(Graphics2D g2) {
        if (noiseTexture != null) g2.drawImage(noiseTexture, 0, 0, null);
    }

    private void drawPanel(Graphics2D g2) {
        int pw=480, ph=340, px=(SCREEN_W-pw)/2, py=(SCREEN_H-ph)/2-10;

        // Bayangan
        g2.setColor(new Color(0,0,0,120));
        g2.fill(new RoundRectangle2D.Float(px+6, py+6, pw, ph, 8, 8));

        // Background
        g2.setPaint(new GradientPaint(px, py, new Color(22,16,8,235), px, py+ph, new Color(12,9,4,235)));
        g2.fill(new RoundRectangle2D.Float(px, py, pw, ph, 8, 8));

        // Border luar
        g2.setColor(BORDER_COLOR);
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new RoundRectangle2D.Float(px, py, pw, ph, 8, 8));

        // Border dalam
        g2.setColor(new Color(60,40,15,180));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(px+3, py+3, pw-6, ph-6, 6, 6));

        // Scratch lines
        g2.setColor(new Color(255,255,255,8));
        for (int i = py+20; i < py+ph-10; i+=14)
            g2.drawLine(px+6, i, px+pw-6, i);
    }

    private void drawTitle(Graphics2D g2) {
        String title = "SETTINGS";
        g2.setFont(fontTitle);
        FontMetrics fm = g2.getFontMetrics();
        int tx = (SCREEN_W - fm.stringWidth(title)) / 2;
        int ty = 145;

        g2.setColor(new Color(0,0,0,160));
        g2.drawString(title, tx+3, ty+3);

        g2.setPaint(new GradientPaint(tx, ty-fm.getAscent(), new Color(240,200,80), tx, ty, new Color(160,110,30)));
        g2.drawString(title, tx, ty);
    }



    private void drawVolumeSection(Graphics2D g2) {
        // Label
        g2.setFont(fontLabel);
        String labelText = "MASTER VOLUME";
        FontMetrics fm = g2.getFontMetrics();
        int lx = (SCREEN_W - fm.stringWidth(labelText)) / 2;
        g2.setColor(new Color(0,0,0,120));
        g2.drawString(labelText, lx+2, 232);
        g2.setColor(TEXT_PRIMARY);
        g2.drawString(labelText, lx, 230);

        // Ikon speaker
        drawSpeakerIcon(g2, SLIDER_X-28, thumbCenterY()-8, masterVolume);

        // Track background
        g2.setColor(TRACK_BG);
        g2.fillRoundRect(SLIDER_X, SLIDER_Y, SLIDER_W, SLIDER_H, SLIDER_H, SLIDER_H);
        g2.setColor(new Color(60,45,20));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(SLIDER_X, SLIDER_Y, SLIDER_W, SLIDER_H, SLIDER_H, SLIDER_H);

        // Track fill
        int fillW = (int)(masterVolume * SLIDER_W);
        if (fillW > 0) {
            g2.setPaint(new GradientPaint(SLIDER_X, SLIDER_Y, new Color(100,70,20),
                    SLIDER_X+fillW, SLIDER_Y, new Color(210,160,55)));
            g2.fillRoundRect(SLIDER_X, SLIDER_Y, fillW, SLIDER_H, SLIDER_H, SLIDER_H);
            // Highlight
            g2.setPaint(new GradientPaint(SLIDER_X, SLIDER_Y, new Color(255,220,100,60),
                    SLIDER_X, SLIDER_Y+SLIDER_H/2, new Color(255,220,100,0)));
            g2.fillRoundRect(SLIDER_X, SLIDER_Y, fillW, SLIDER_H/2, SLIDER_H/2, SLIDER_H/2);
        }

        // Thumb
        int tcx = thumbCenterX(), tcy = thumbCenterY();
        Color thumbColor = thumbDragging ? THUMB_PRESS : (thumbHovered ? THUMB_HOVER : THUMB_NORMAL);

        g2.setColor(new Color(0,0,0,100));
        g2.fillOval(tcx-THUMB_RADIUS+2, tcy-THUMB_RADIUS+2, THUMB_RADIUS*2, THUMB_RADIUS*2);

        g2.setPaint(new RadialGradientPaint(new Point(tcx-2, tcy-2), THUMB_RADIUS,
                new float[]{0f,1f}, new Color[]{thumbColor.brighter(), thumbColor.darker()}));
        g2.fillOval(tcx-THUMB_RADIUS, tcy-THUMB_RADIUS, THUMB_RADIUS*2, THUMB_RADIUS*2);

        g2.setColor(thumbColor.darker().darker());
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(tcx-THUMB_RADIUS, tcy-THUMB_RADIUS, THUMB_RADIUS*2, THUMB_RADIUS*2);

        g2.setColor(new Color(0,0,0,80));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(tcx, tcy-5, tcx, tcy+5);

        // Persentase
        g2.setFont(fontSmall);
        String volPct = (int)(masterVolume*100) + "%";
        FontMetrics fmS = g2.getFontMetrics();
        int vx = SLIDER_X+SLIDER_W+18, vy = tcy+fmS.getAscent()/2-1;
        g2.setColor(new Color(0,0,0,100));
        g2.drawString(volPct, vx+1, vy+1);
        g2.setColor(ACCENT_GOLD);
        g2.drawString(volPct, vx, vy);

        // Tick marks
        g2.setFont(fontSmall.deriveFont(10f));
        FontMetrics fmT = g2.getFontMetrics();
        String[] ticks = {"0","25","50","75","100"};
        for (int i=0; i<ticks.length; i++) {
            int tx2 = SLIDER_X + (int)(i/4.0 * SLIDER_W);
            g2.setColor(new Color(100,75,30,150));
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(tx2, SLIDER_Y+SLIDER_H+4, tx2, SLIDER_Y+SLIDER_H+10);
            g2.setColor(TEXT_DIM);
            g2.drawString(ticks[i], tx2-fmT.stringWidth(ticks[i])/2, SLIDER_Y+SLIDER_H+22);
        }
    }

    private void drawSpeakerIcon(Graphics2D g2, int x, int y, float vol) {
        g2.setColor(vol > 0 ? ACCENT_GOLD : TEXT_DIM);
        g2.setStroke(new BasicStroke(1.5f));
        g2.fillPolygon(new int[]{x, x+6, x+6, x},    new int[]{y+5, y+5, y+11, y+11}, 4);
        g2.fillPolygon(new int[]{x+6, x+12, x+12, x+6}, new int[]{y+5, y+2, y+14, y+11}, 4);
        if (vol > 0.01f) {
            g2.setColor(new Color(ACCENT_GOLD.getRed(), ACCENT_GOLD.getGreen(), ACCENT_GOLD.getBlue(), 180));
            g2.drawArc(x+13, y+5, 5, 6, -45, 90);
        }
        if (vol > 0.4f) g2.drawArc(x+15, y+3, 6, 10, -45, 90);
    }

    private void drawBackButton(Graphics2D g2) {
        Rectangle r = getBtnRect();
        Color bg     = btnPressed ? BTN_PRESS : (btnHovered ? BTN_HOVER : BTN_NORMAL);
        Color border = btnPressed ? ACCENT_GOLD.darker() : (btnHovered ? ACCENT_GOLD : BORDER_COLOR);

        g2.setColor(new Color(0,0,0,120));
        g2.fill(new RoundRectangle2D.Float(r.x+3, r.y+3, r.width, r.height, 6, 6));

        g2.setPaint(new GradientPaint(r.x, r.y, bg.brighter(), r.x, r.y+r.height, bg));
        g2.fill(new RoundRectangle2D.Float(r.x, r.y, r.width, r.height, 6, 6));

        g2.setColor(border);
        g2.setStroke(new BasicStroke(btnHovered ? 1.8f : 1.2f));
        g2.draw(new RoundRectangle2D.Float(r.x, r.y, r.width, r.height, 6, 6));

        g2.setFont(fontBtn);
        String label = "< BACK";
        FontMetrics fm = g2.getFontMetrics();
        int tx = r.x + (r.width - fm.stringWidth(label)) / 2;
        int ty = r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2;
        g2.setColor(new Color(0,0,0,150));
        g2.drawString(label, tx+2, ty+2);
        g2.setColor(btnHovered ? TEXT_PRIMARY : TEXT_DIM);
        g2.drawString(label, tx, ty);
    }

    private void drawCornerDecals(Graphics2D g2) {
        g2.setColor(new Color(100,70,25,100));
        g2.setStroke(new BasicStroke(1.5f));
        int m=16, s=18;
        g2.drawLine(m, m, m+s, m);         g2.drawLine(m, m, m, m+s);
        g2.drawLine(SCREEN_W-m, m, SCREEN_W-m-s, m); g2.drawLine(SCREEN_W-m, m, SCREEN_W-m, m+s);
        g2.drawLine(m, SCREEN_H-m, m+s, SCREEN_H-m); g2.drawLine(m, SCREEN_H-m, m, SCREEN_H-m-s);
        g2.drawLine(SCREEN_W-m, SCREEN_H-m, SCREEN_W-m-s, SCREEN_H-m);
        g2.drawLine(SCREEN_W-m, SCREEN_H-m, SCREEN_W-m, SCREEN_H-m-s);
    }
}