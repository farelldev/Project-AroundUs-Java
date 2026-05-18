package combat;

import java.awt.Rectangle;

public class Bullet {
    private float x, y;
    private final float speed = 10f;
    private int dmg;
    private boolean isActive = true;

    private double velX, velY;

    public Bullet(float startX, float startY, float targetX, float targetY, int dmg) {
        this.x = startX;
        this.y = startY;
        this.dmg = dmg;

        double angle = Math.atan2(targetY - startY, targetX - startX);
        this.velX = speed * Math.cos(angle);
        this.velY = speed * Math.sin(angle);
    }

    public void update() {
        if (isActive) {
            // peluru bergerak
            x += velX;
            y += velY;

            // DRAFT: ukuran frame 800 x 600
            if (x < 0 || x > 800 || y < 0 || y > 600) {
                deactivate();
            }
        }
    }

    public void hit(Damageable target) {
        target.takeDmg(this.dmg);
        deactivate();
    }

    public void deactivate() {
        this.isActive = false;
    }

    public Rectangle getBounds() {
        // DRAFT: ukuran peluru 5 piksel
        return new Rectangle((int) x, (int) y, 5, 5);
    }

    // getter
    public float getX() { return x; }
    public float getY() { return y; }
    public boolean isActive() { return isActive; }
}