package ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class GameOverScreen extends JPanel {

    // ── Ukuran layar ──────────────────────────────────────────────────────────
    private static final int W = 768;
    private static final int H = 576;

    // ── Ukuran elemen (dari user) ─────────────────────────────────────────────
    private static final int INFO_W  = 500;
    private static final int INFO_H  = 300;
    private static final int MENU_W  = 900;
    private static final int MENU_H  = 585;
    private static final int DIGIT_H = 52;
    private static final int BTN_W   = 250;
    private static final int BTN_H   = 100;
    private static final int GAP     = 5;

    // ── Koordinat — dihitung sekali ───────────────────────────────────────────
    private static final int MENU_X  = (W - MENU_W) / 2;          // -66
    private static final int MENU_Y  = (H - MENU_H) / 2 - 15;     // -20

    // PERBAIKAN: tombol di dalam menu (90% dari tinggi menu) bukan di bawahnya
    // MENU_Y + MENU_H + 16 = 581 > H(576) → keluar layar!
    private static final int BTN_Y   = MENU_Y + (int)(MENU_H * 0.75);

    private static final int TRY_X   = (W - (BTN_W * 2 + GAP)) / 2;
    private static final int BACK_X  = TRY_X + BTN_W + GAP;

    private static final Rectangle TRY_RECT  = new Rectangle(TRY_X,  BTN_Y, BTN_W, BTN_H);
    private static final Rectangle BACK_RECT = new Rectangle(BACK_X, BTN_Y, BTN_W, BTN_H);

    // ── State ─────────────────────────────────────────────────────────────────
    private boolean active         = false;
    private int     finalScore     = 0;
    private int     displayedScore = 0;
    private int     animTick       = 0;
    private float   overlayAlpha   = 0f;
    private boolean musicStarted   = false;
    private static final int   ANIM_DURATION_TICKS = 90;
    private static final float FADE_SPEED          = 0.03f;

    // ── Gambar ────────────────────────────────────────────────────────────────
    private BufferedImage gameOverInfoImg;
    private BufferedImage highScoreMenuImg;
    private final BufferedImage[] numberImgs = new BufferedImage[10];
    private BufferedImage btnTryIdle,    btnTryHover,    btnTryClicked;
    private BufferedImage btnBackIdle,   btnBackHover,   btnBackClicked;

    // ── State tombol ──────────────────────────────────────────────────────────
    private volatile boolean tryHover    = false;
    private volatile boolean tryPressed  = false;
    private volatile boolean backHover   = false;
    private volatile boolean backPressed = false;

    // ── Lainnya ───────────────────────────────────────────────────────────────
    private BufferedImage blurredBG = null;
    private Timer         animTimer = null;

    private final Runnable     onTryAgain;
    private final Runnable     onBackToMain;
    private final SoundManager soundManager;

    // ── Konstruktor ───────────────────────────────────────────────────────────
    public GameOverScreen(SoundManager soundManager, Runnable onTryAgain, Runnable onBackToMain) {
        this.soundManager  = soundManager;
        this.onTryAgain    = onTryAgain;
        this.onBackToMain  = onBackToMain;

        setPreferredSize(new Dimension(W, H));
        setOpaque(false);
        setVisible(false);

        loadImages();
        setupMouseListeners();
    }

    // ── Load aset ─────────────────────────────────────────────────────────────
    private void loadImages() {
        try {
            gameOverInfoImg  = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/gameOver/gameOverinfo.png"));
            highScoreMenuImg = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/gameOver/highScoreMenu.png"));
        } catch (Exception e) {
            System.out.println("[GameOverScreen] Gagal muat gameOver images: " + e.getMessage());
        }
        for (int i = 0; i <= 9; i++) {
            try {
                numberImgs[i] = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/numbers/number" + i + ".png"));
            } catch (Exception e) {
                System.out.println("[GameOverScreen] Gagal muat number" + i);
            }
        }
        try {
            btnTryIdle    = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/button/tryAgain_idle.png"));
            btnTryHover   = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/button/tryAgain_hover.png"));
            btnTryClicked = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/button/tryAgain_clicked.png"));
            btnBackIdle    = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/button/backToMain_idle.png"));
            btnBackHover   = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/button/backToMain_hover.png"));
            btnBackClicked = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/button/backToMain_clicked.png"));
        } catch (Exception e) {
            System.out.println("[GameOverScreen] Gagal muat button images: " + e.getMessage());
        }
    }

    // ── Mouse ─────────────────────────────────────────────────────────────────
    private void setupMouseListeners() {
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (!active) return;
                Point p = e.getPoint();
                tryHover  = TRY_RECT.contains(p);
                backHover = BACK_RECT.contains(p);
                repaint();
            }
            @Override
            public void mouseDragged(MouseEvent e) { mouseMoved(e); }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!active) return;
                Point p = e.getPoint();
                tryPressed  = TRY_RECT.contains(p);
                backPressed = BACK_RECT.contains(p);
                System.out.println("[GameOver] pressed try=" + tryPressed + " back=" + backPressed + " point=" + p);
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!active) return;
                Point p = e.getPoint();
                boolean wasTry  = tryPressed  && TRY_RECT.contains(p);
                boolean wasBack = backPressed && BACK_RECT.contains(p);
                System.out.println("[GameOver] released wasTry=" + wasTry + " wasBack=" + wasBack + " point=" + p);
                tryPressed = backPressed = false;
                repaint();

                if (wasTry) {
                    soundManager.playSFX("uiClick");
                    if (onTryAgain != null) onTryAgain.run();
                } else if (wasBack) {
                    soundManager.playSFX("uiClick");
                    if (onBackToMain != null) onBackToMain.run();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                tryHover = backHover = tryPressed = backPressed = false;
                repaint();
            }
        });
    }

    // ── Public API ────────────────────────────────────────────────────────────
    public void showScreen(int score, JComponent gameSrc) {
        this.finalScore     = score;
        this.displayedScore = 0;
        this.animTick       = 0;
        this.overlayAlpha   = 0f;
        this.musicStarted   = false;
        this.active         = true;
        tryHover = backHover = tryPressed = backPressed = false;

        if (gameSrc != null) {
            try {
                BufferedImage snap = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
                Graphics2D sg = snap.createGraphics();
                gameSrc.paintComponents(sg);
                sg.dispose();
                blurredBG = applyBoxBlur(snap, 8);
            } catch (Exception e) {
                blurredBG = null;
            }
        }

        setVisible(true);
        requestFocusInWindow();
        startAnimTimer();
    }

    public void hideScreen() {
        active = false;
        if (animTimer != null) animTimer.stop();
        setVisible(false);
    }

    public boolean isActive() { return active; }

    // ── Anim timer ────────────────────────────────────────────────────────────
    private void startAnimTimer() {
        if (animTimer != null) animTimer.stop();
        animTimer = new Timer(16, e -> {
            if (overlayAlpha < 1f)
                overlayAlpha = Math.min(1f, overlayAlpha + FADE_SPEED);

            if (!musicStarted && overlayAlpha >= 0.5f) {
                soundManager.stopBGM();
                soundManager.playBGM("gameOver");
                musicStarted = true;
            }

            if (animTick <= ANIM_DURATION_TICKS) {
                float t     = (float) animTick / ANIM_DURATION_TICKS;
                float eased = 1f - (1f - t) * (1f - t) * (1f - t);
                displayedScore = (int)(finalScore * eased);
                animTick++;
            } else {
                displayedScore = finalScore;
            }

            repaint();

            if (overlayAlpha >= 1f && animTick > ANIM_DURATION_TICKS + 60)
                animTimer.stop();
        });
        animTimer.start();
    }

    // ── Blur ──────────────────────────────────────────────────────────────────
    private BufferedImage applyBoxBlur(BufferedImage src, int passes) {
        int size = 7;
        float[] data = new float[size * size];
        float val = 1f / (size * size);
        for (int i = 0; i < data.length; i++) data[i] = val;
        Kernel kernel = new Kernel(size, size, data);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        BufferedImage result = src;
        for (int p = 0; p < passes; p++) {
            BufferedImage tmp = new BufferedImage(result.getWidth(), result.getHeight(), BufferedImage.TYPE_INT_RGB);
            result = op.filter(result, tmp);
        }
        return result;
    }

    // ── Paint ─────────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        if (!active) return;
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            // 1. Blur BG
            if (blurredBG != null) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, overlayAlpha * 1.5f)));
                g2.drawImage(blurredBG, 0, 0, W, H, null);
            }

            // 2. Gelap
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, overlayAlpha * 0.60f));
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, W, H);

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, overlayAlpha));

            // 3. gameOverInfo
            if (gameOverInfoImg != null)
                g2.drawImage(gameOverInfoImg, (W - INFO_W) / 2, 28, INFO_W, INFO_H, null);

            // 4. highScoreMenu
            if (highScoreMenuImg != null)
                g2.drawImage(highScoreMenuImg, MENU_X, MENU_Y, MENU_W, MENU_H, null);

            // 5. Digit score
            drawScoreDigits(g2);

            // 6. Tombol
            BufferedImage tryImg  = tryPressed  ? btnTryClicked  : (tryHover  ? btnTryHover  : btnTryIdle);
            BufferedImage backImg = backPressed ? btnBackClicked : (backHover ? btnBackHover : btnBackIdle);
            if (tryImg  != null) g2.drawImage(tryImg,  TRY_X,  BTN_Y, BTN_W, BTN_H, null);
            if (backImg != null) g2.drawImage(backImg, BACK_X, BTN_Y, BTN_W, BTN_H, null);

        } finally {
            g2.dispose();
        }
    }

    // ── Digit score ───────────────────────────────────────────────────────────
    private void drawScoreDigits(Graphics2D g2) {
        String scoreStr = String.valueOf(displayedScore);
        int spacing = -30;

        int[] dw = new int[scoreStr.length()];
        int totalW = 0;
        for (int i = 0; i < scoreStr.length(); i++) {
            int d = scoreStr.charAt(i) - '0';
            BufferedImage img = (d >= 0 && d <= 9) ? numberImgs[d] : null;
            dw[i] = (img != null) ? (int)((double) img.getWidth() / img.getHeight() * DIGIT_H) : 28;
            totalW += dw[i];
        }
        totalW += spacing * Math.max(0, scoreStr.length() - 1);

        int digitY = MENU_Y + (int)(MENU_H * 0.63);
        int curX   = MENU_X + (MENU_W - totalW) / 2;

        for (int i = 0; i < scoreStr.length(); i++) {
            int d = scoreStr.charAt(i) - '0';
            BufferedImage img = (d >= 0 && d <= 9) ? numberImgs[d] : null;
            if (img != null)
                g2.drawImage(img, curX, digitY - DIGIT_H, dw[i], DIGIT_H, null);
            curX += dw[i] + spacing;
        }
    }
}