package ui;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

/**
 * MainMenuPanel — layar utama sebelum masuk ke game.
 *
 * State machine:
 *   LOADING      → animasi 3 frame loadingScreen sprite
 *   MENU_IDLE    → mainMenu_1 + tombol Start/Settings/Credits
 *   FLICKER_DEAD → mainMenu_deadlamp  (saat klik Start, lampu mati)
 *   FLICKER_ON   → mainMenu_after     (lampu nyala lagi → lanjut ke game)
 *
 * Durasi flicker:
 *   - 0 ms     : putar flicker_lamp + tampil mainMenu_deadlamp
 *   - 2000 ms  : stop flicker_lamp, putar flicker_lampDone, tampil mainMenu_after
 *   - selesai  : transisi ke game
 */
public class MainMenuPanel extends JPanel implements Runnable {

    // ── Screen size (sama persis dengan GamePanel) ──────────────────────────
    public static final int SCREEN_W = 768;   // 16 * 3 * 16
    public static final int SCREEN_H = 576;   // 12 * 3 * 16

    // ── State machine ────────────────────────────────────────────────────────
    public enum MenuState {
        LOADING,
        MENU_IDLE,
        FLICKER_DEAD,
        FLICKER_ON
    }
    private MenuState state = MenuState.LOADING;

    // ── Sprites ──────────────────────────────────────────────────────────────
    private BufferedImage[] loadingFrames = new BufferedImage[3];
    private BufferedImage imgMenuIdle;     // mainMenu_1
    private BufferedImage imgDeadlamp;     // mainMenu_deadlamp
    private BufferedImage imgAfter;        // mainMenu_after

    // Button sprites  (320x180 asli → kita scale saat draw)
    private BufferedImage btnStartIdle,   btnStartHover,   btnStartClicked;
    private BufferedImage btnSettingsIdle,btnSettingsHover,btnSettingsClicked;
    private BufferedImage btnCreditsIdle, btnCreditsHover, btnCreditsClicked;

    // ── Button state tracking ─────────────────────────────────────────────────
    private enum BtnId { START, SETTINGS, CREDITS, NONE }
    private BtnId hoveredBtn = BtnId.NONE;
    private BtnId clickedBtn = BtnId.NONE;

    // ── Button rects (ditentukan saat paint, atau di-set di setupButtons) ────
    private Rectangle rectStart    = new Rectangle();
    private Rectangle rectSettings = new Rectangle();
    private Rectangle rectCredits  = new Rectangle();

    // ── Loading animation ─────────────────────────────────────────────────────
    private int  loadingFrame   = 0;
    private long loadingTimer   = 0;
    private static final int LOADING_FRAME_DELAY  = 250;
    private static final int LOADING_TOTAL_CYCLES = 12;
    private int  loadingCycleCount = 0;

    // ── Flicker timing ────────────────────────────────────────────────────────
    private static final long FLICKER_LAMP_MS          = 2000;
    private static final long DONE_TO_GAME_FALLBACK_MS = 600;
    private static final long FLICKER_OFF_DELAY_MS     = 350;
    private static final int  FLICKER_PULSE_MS         = 120;
    private long flickerStartTime    = 0;
    private long flickerDoneStartTime = 0;

    // ── Game launch callback ──────────────────────────────────────────────────
    private Runnable onStartGame;

    // ── Game loop ─────────────────────────────────────────────────────────────
    private Thread  loopThread;
    private boolean running = false;

    // ── SFX ───────────────────────────────────────────────────────────────────
    private Clip sfxFlicker;
    private Clip sfxFlickerDone;

    // ── Timing ───────────────────────────────────────────────────────────────
    private long lastTime;

    // ── SoundManager (diteruskan ke SettingsPanel) ───────────────────────────
    private SoundManager soundManager;

    // ── Volume master yang disimpan lintas panel ──────────────────────────────
    private float masterVolume = 1.0f;

    // ─────────────────────────────────────────────────────────────────────────
    //  CONSTRUCTOR
    // ─────────────────────────────────────────────────────────────────────────
    public MainMenuPanel(Runnable onStartGame) {
        this(onStartGame, new SoundManager());
    }

    public MainMenuPanel(Runnable onStartGame, SoundManager soundManager) {
        this.onStartGame  = onStartGame;
        this.soundManager = soundManager != null ? soundManager : new SoundManager();

        this.setPreferredSize(new Dimension(SCREEN_W, SCREEN_H));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);

        loadAssets();
        setupMouseListeners();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  ASSET LOADING
    // ─────────────────────────────────────────────────────────────────────────
    private void loadAssets() {
        try {
            loadingFrames[0] = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/mainMenu/loadingScreen_1.png"));
            loadingFrames[1] = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/mainMenu/loadingScreen_2.png"));
            loadingFrames[2] = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/mainMenu/loadingScreen_3.png"));

            imgMenuIdle = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/mainMenu/mainMenu_1.png"));
            imgDeadlamp = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/mainMenu/mainMenu_deadlamp.png"));
            imgAfter    = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/mainMenu/mainMenu_after.png"));

            btnStartIdle     = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/button/start_idle.png"));
            btnStartHover    = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/button/start_hover.png"));
            btnStartClicked  = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/button/start_clicked.png"));

            btnSettingsIdle    = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/button/settings_idle.png"));
            btnSettingsHover   = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/button/settings_hover.png"));
            btnSettingsClicked = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/button/settings_clicked.png"));

            btnCreditsIdle    = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/button/credits_idle.png"));
            btnCreditsHover   = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/button/credits_hover.png"));
            btnCreditsClicked = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/button/credits_clicked.png"));

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[MainMenuPanel] Gagal load asset: " + e.getMessage());
        }

        sfxFlicker = loadClip(
                "flicker_lamp",
                "/flicker_lamp.wav",
                "/flickering_lamp.wav",
                "/soundEffects/flicker_lamp.wav"
        );
        sfxFlickerDone = loadClip(
                "flicker_lampDone",
                "/flicker_lampDone.wav",
                "/soundEffects/flicker_lampDone.wav"
        );
    }

    private Clip loadClip(String label, String... resourcePaths) {
        for (String resourcePath : resourcePaths) {
            try {
                InputStream is = getClass().getResourceAsStream(resourcePath);
                if (is == null) continue;
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(is);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                return clip;
            } catch (Exception e) {
                System.out.println("[MainMenuPanel] Gagal load " + label + " dari resource " + resourcePath + ": " + e.getMessage());
            }
        }

        File audioFile = new File("soundEffects", label + ".wav");
        if (audioFile.exists()) {
            try {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(audioFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                return clip;
            } catch (Exception e) {
                System.out.println("[MainMenuPanel] Gagal load " + label + " dari file " + audioFile.getPath() + ": " + e.getMessage());
            }
        }

        System.out.println("[MainMenuPanel] SFX " + label + " tidak ditemukan.");
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  MOUSE LISTENERS
    // ─────────────────────────────────────────────────────────────────────────
    private void setupMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (state != MenuState.MENU_IDLE) return;
                int mx = e.getX(), my = e.getY();
                if      (rectStart.contains(mx, my))    hoveredBtn = BtnId.START;
                else if (rectSettings.contains(mx, my)) hoveredBtn = BtnId.SETTINGS;
                else if (rectCredits.contains(mx, my))  hoveredBtn = BtnId.CREDITS;
                else                                    hoveredBtn = BtnId.NONE;
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (state != MenuState.MENU_IDLE) return;
                int mx = e.getX(), my = e.getY();
                if      (rectStart.contains(mx, my))    clickedBtn = BtnId.START;
                else if (rectSettings.contains(mx, my)) clickedBtn = BtnId.SETTINGS;
                else if (rectCredits.contains(mx, my))  clickedBtn = BtnId.CREDITS;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (state != MenuState.MENU_IDLE) return;
                int mx = e.getX(), my = e.getY();

                if (clickedBtn == BtnId.START && rectStart.contains(mx, my)) {
                    triggerStartFlicker();
                } else if (clickedBtn == BtnId.SETTINGS && rectSettings.contains(mx, my)) {
                    openSettings();
                } else if (clickedBtn == BtnId.CREDITS && rectCredits.contains(mx, my)) {
                    System.out.println("[MainMenu] Credits diklik (belum diimplementasi)");
                }
                clickedBtn = BtnId.NONE;
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SETTINGS
    // ─────────────────────────────────────────────────────────────────────────
    private void openSettings() {
        stopLoop();
        // Pastikan saat kembali dari Settings, menu langsung tampil (bukan loading ulang)
        state = MenuState.MENU_IDLE;
        hoveredBtn = BtnId.NONE;
        clickedBtn = BtnId.NONE;

        Container parent = getParent();
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (parent == null && frame == null) return;

        // Pakai array[1] sebagai wrapper agar objek settingsPanel bisa
        // direferensikan di dalam lambda (Java tidak izinkan local variable
        // non-effectively-final di dalam lambda).
        SettingsPanel[] ref = new SettingsPanel[1];

        ref[0] = new SettingsPanel(soundManager, () -> {
            masterVolume = ref[0].getMasterVolume();

            if (parent != null && parent.getLayout() instanceof CardLayout cardLayout) {
                cardLayout.show(parent, "MENU");
                parent.revalidate();
                parent.repaint();
            } else if (frame != null) {
                frame.setContentPane(MainMenuPanel.this);
                frame.revalidate();
                frame.repaint();
            }
            MainMenuPanel.this.requestFocusInWindow();
            startLoop();
        });

        ref[0].setMasterVolume(masterVolume);

        if (parent != null && parent.getLayout() instanceof CardLayout cardLayout) {
            parent.add(ref[0], "SETTINGS");
            cardLayout.show(parent, "SETTINGS");
            parent.revalidate();
            parent.repaint();
        } else {
            frame.setContentPane(ref[0]);
            frame.revalidate();
            frame.repaint();
        }
        ref[0].requestFocusInWindow();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  START GAME FLOW
    // ─────────────────────────────────────────────────────────────────────────
    private void triggerStartFlicker() {
        state = MenuState.FLICKER_DEAD;
        hoveredBtn = BtnId.NONE;
        clickedBtn = BtnId.NONE;
        flickerStartTime = System.currentTimeMillis();
        flickerDoneStartTime = 0;
        playClip(sfxFlicker, "flicker_lamp");
    }

    private void playClip(Clip clip, String label) {
        if (clip != null) {
            try {
                if (clip.isRunning()) clip.stop();
                clip.setFramePosition(0);
                applyVolumeToClip(clip, soundManager != null ? soundManager.getMasterVolume() : masterVolume);
                clip.start();
                System.out.println("[SFX] " + label + " diputar.");
            } catch (Exception e) {
                System.out.println("[SFX] Error saat putar " + label + ": " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("[SFX] " + label + " NULL - gagal load di loadAssets()");
        }
    }

    private void stopClip(Clip clip) {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    private void applyVolumeToClip(Clip clip, float volume) {
        if (clip == null || !clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) return;
        FloatControl fc = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float safeVolume = Math.max(0.0001f, Math.min(1.0f, volume));
        float db = volume <= 0.0f ? fc.getMinimum() : (float) (20.0 * Math.log10(safeVolume));
        fc.setValue(Math.max(fc.getMinimum(), Math.min(fc.getMaximum(), db)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  GAME LOOP
    // ─────────────────────────────────────────────────────────────────────────
    public void startLoop() {
        running = true;
        lastTime = System.currentTimeMillis();
        loopThread = new Thread(this, "MainMenuLoop");
        loopThread.start();
    }

    public void stopLoop() {
        running = false;
        stopClip(sfxFlicker);
        stopClip(sfxFlickerDone);
    }

    @Override
    public void run() {
        while (running) {
            long now = System.currentTimeMillis();
            long dt  = now - lastTime;
            lastTime = now;

            updateState(dt);
            repaint();

            try { Thread.sleep(16); } catch (InterruptedException ignored) {}
        }
    }

    private void updateState(long dt) {
        switch (state) {
            case LOADING -> updateLoading(dt);
            case FLICKER_DEAD, FLICKER_ON -> updateFlicker();
            default -> {}
        }
    }

    private void updateLoading(long dt) {
        loadingTimer += dt;
        if (loadingTimer >= LOADING_FRAME_DELAY) {
            loadingTimer = 0;
            loadingFrame = (loadingFrame + 1) % loadingFrames.length;
            loadingCycleCount++;
            if (loadingCycleCount >= LOADING_TOTAL_CYCLES) {
                state = MenuState.MENU_IDLE;
            }
        }
    }

    private void updateFlicker() {
        long now     = System.currentTimeMillis();
        long elapsed = now - flickerStartTime;

        if (state == MenuState.FLICKER_DEAD && elapsed >= FLICKER_LAMP_MS) {
            stopClip(sfxFlicker);
            state = MenuState.FLICKER_ON;
            flickerDoneStartTime = now;
            playClip(sfxFlickerDone, "flicker_lampDone");
        }

        if (state == MenuState.FLICKER_ON
                && now - flickerDoneStartTime >= getClipDurationMs(sfxFlickerDone, DONE_TO_GAME_FALLBACK_MS)) {
            stopLoop();
            SwingUtilities.invokeLater(onStartGame);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  RENDERING
    // ─────────────────────────────────────────────────────────────────────────
    private long getClipDurationMs(Clip clip, long fallbackMs) {
        if (clip == null) return fallbackMs;
        long durationMs = clip.getMicrosecondLength() / 1000;
        return durationMs > 0 ? durationMs : fallbackMs;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        switch (state) {
            case LOADING      -> drawLoading(g2);
            case MENU_IDLE    -> drawMenuIdle(g2);
            case FLICKER_DEAD -> drawFlickerStart(g2);
            case FLICKER_ON   -> drawImage(g2, imgAfter);
        }

        g2.dispose();
    }

    private void drawLoading(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, SCREEN_W, SCREEN_H);
        if (loadingFrames[loadingFrame] != null) {
            g2.drawImage(loadingFrames[loadingFrame], 0, 0, SCREEN_W, SCREEN_H, null);
        }
    }

    private void drawMenuIdle(Graphics2D g2) {
        drawImage(g2, imgMenuIdle);
        drawButtons(g2);
    }

    private void drawFlickerStart(Graphics2D g2) {
        long elapsed = System.currentTimeMillis() - flickerStartTime;

        if (elapsed < FLICKER_OFF_DELAY_MS) {
            drawImage(g2, imgDeadlamp);
            return;
        }

        int pulse = (int) ((elapsed - FLICKER_OFF_DELAY_MS) / FLICKER_PULSE_MS);
        float alpha = (pulse % 2 == 0) ? 0.35f : 1.0f;

        Composite oldComposite = g2.getComposite();
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, SCREEN_W, SCREEN_H);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        drawImage(g2, imgMenuIdle);
        g2.setComposite(oldComposite);
    }

    private void drawImage(Graphics2D g2, BufferedImage img) {
        if (img != null) {
            g2.drawImage(img, 0, 0, SCREEN_W, SCREEN_H, null);
        } else {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, SCREEN_W, SCREEN_H);
        }
    }

    private void drawButtons(Graphics2D g2) {
        int btnW = 192;
        int btnH = 108;
        int gap  = 6;

        int totalH = btnH * 3 + gap * 2;
        int startX = (SCREEN_W - btnW) / 2;
        int startY = (SCREEN_H - totalH) / 2 + 60;

        rectStart   .setBounds(startX, startY,                    btnW, btnH);
        rectSettings.setBounds(startX, startY + btnH + gap,       btnW, btnH);
        rectCredits .setBounds(startX, startY + (btnH + gap) * 2, btnW, btnH);

        drawBtn(g2, BtnId.START,    rectStart,    btnStartIdle,    btnStartHover,    btnStartClicked);
        drawBtn(g2, BtnId.SETTINGS, rectSettings, btnSettingsIdle, btnSettingsHover, btnSettingsClicked);
        drawBtn(g2, BtnId.CREDITS,  rectCredits,  btnCreditsIdle,  btnCreditsHover,  btnCreditsClicked);
    }

    private void drawBtn(Graphics2D g2, BtnId id, Rectangle rect,
                         BufferedImage idle, BufferedImage hover, BufferedImage clicked) {
        BufferedImage img;
        if      (clickedBtn == id) img = clicked != null ? clicked : idle;
        else if (hoveredBtn == id) img = hover   != null ? hover   : idle;
        else                       img = idle;

        if (img != null) {
            g2.drawImage(img, rect.x, rect.y, rect.width, rect.height, null);
        }
    }
}
