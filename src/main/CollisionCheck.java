package main;

import entity.Entity;

public class CollisionCheck {

    GamePanel gp;

    public CollisionCheck(GamePanel gp) {
        this.gp = gp;
    }

    public void checkTile(Entity entity) {
        int entityLeftWorldX   = entity.x + entity.solidArea.x;
        int entityRightWorldX  = entity.x + entity.solidArea.x + entity.solidArea.width;
        int entityTopWorldY    = entity.y + entity.solidArea.y;
        int entityBottomWorldY = entity.y + entity.solidArea.y + entity.solidArea.height;

        int entityLeftCol   = entityLeftWorldX  / gp.tileSize;
        int entityRightCol  = entityRightWorldX / gp.tileSize;
        int entityTopRow    = entityTopWorldY   / gp.tileSize;
        int entityBottomRow = entityBottomWorldY / gp.tileSize;

        switch (entity.direction) {
            case "up": {
                int row = (entityTopWorldY - entity.speed) / gp.tileSize;
                // CEK TANGGA
                checkStair(entity, entityLeftCol, row);
                checkStair(entity, entityRightCol, row);
                if (gp.tileM.isCollision(entityLeftCol, row) ||
                        gp.tileM.isCollision(entityRightCol, row)) {
                    entity.collisionOn = true;
                }
                break;
            }
            case "down": {
                int row = (entityBottomWorldY + entity.speed) / gp.tileSize;
                checkStair(entity, entityLeftCol, row);
                checkStair(entity, entityRightCol, row);
                if (gp.tileM.isCollision(entityLeftCol, row) ||
                        gp.tileM.isCollision(entityRightCol, row)) {
                    entity.collisionOn = true;
                }
                break;
            }
            case "left": {
                int col = (entityLeftWorldX - entity.speed) / gp.tileSize;
                checkStair(entity, col, entityTopRow);
                checkStair(entity, col, entityBottomRow);
                if (gp.tileM.isCollision(col, entityTopRow) ||
                        gp.tileM.isCollision(col, entityBottomRow)) {
                    entity.collisionOn = true;
                }
                break;
            }
            case "right": {
                int col = (entityRightWorldX + entity.speed) / gp.tileSize;
                checkStair(entity, col, entityTopRow);
                checkStair(entity, col, entityBottomRow);
                if (gp.tileM.isCollision(col, entityTopRow) ||
                        gp.tileM.isCollision(col, entityBottomRow)) {
                    entity.collisionOn = true;
                }
                break;
            }
        }
    }

    /**
     * Paksa entity keluar dari tile collision jika sudah terlanjur masuk.
     * Dipanggil tiap frame untuk zombie agar tidak stuck di dalam tembok.
     */
    public void pushOutOfCollision(entity.Entity entity) {
        // Titik tengah hitbox entity
        int cx = entity.x + entity.solidArea.x + entity.solidArea.width  / 2;
        int cy = entity.y + entity.solidArea.y + entity.solidArea.height / 2;

        int col = cx / gp.tileSize;
        int row = cy / gp.tileSize;

        if (col < 0 || col >= gp.maxWorldCol || row < 0 || row >= gp.maxWorldRow) return;

        if (gp.tileM.isCollision(col, row)) {
            // Hitung jarak ke tepi terdekat tile dan dorong ke sana
            int tileLeft   = col * gp.tileSize;
            int tileRight  = tileLeft + gp.tileSize;
            int tileTop    = row * gp.tileSize;
            int tileBottom = tileTop + gp.tileSize;

            int distLeft   = cx - tileLeft;
            int distRight  = tileRight - cx;
            int distTop    = cy - tileTop;
            int distBottom = tileBottom - cy;

            int minDist = Math.min(Math.min(distLeft, distRight), Math.min(distTop, distBottom));

            if (minDist == distLeft)       entity.x -= distLeft   + entity.solidArea.width  / 2 + 1;
            else if (minDist == distRight) entity.x += distRight  + entity.solidArea.width  / 2 + 1;
            else if (minDist == distTop)   entity.y -= distTop    + entity.solidArea.height / 2 + 1;
            else                           entity.y += distBottom + entity.solidArea.height / 2 + 1;

            // Sinkronisasi preciseX/Y jika entity adalah Zombie
            if (entity instanceof entity.Zombie) {
                ((entity.Zombie) entity).syncPrecise();
            }
        }
    }

    private void checkStair(Entity entity, int col, int row) {
        if (!entity.equals(gp.getPlayer())) return;
        if (col < 0 || col >= gp.maxWorldCol || row < 0 || row >= gp.maxWorldRow) return;
        int num = gp.tileM.mapTileNum[col][row];
        if (num == 16 || num == 57) {
            gp.nearStair     = true;
            gp.nearStairCol  = col;
            gp.nearStairRow  = row;
        }
    }

    // Cek tangga setiap frame tanpa bergantung pada movement player
    public void checkNearStair() {
        Entity entity = gp.getPlayer();

        int leftX   = entity.x + entity.solidArea.x;
        int rightX  = entity.x + entity.solidArea.x + entity.solidArea.width;
        int topY    = entity.y + entity.solidArea.y;
        int bottomY = entity.y + entity.solidArea.y + entity.solidArea.height;

        int leftCol   = leftX  / gp.tileSize;
        int rightCol  = rightX / gp.tileSize;
        int topRow    = topY   / gp.tileSize;
        int bottomRow = bottomY / gp.tileSize;

        // Cek semua 4 sudut hitbox player
        checkStair(entity, leftCol,  topRow);
        checkStair(entity, rightCol, topRow);
        checkStair(entity, leftCol,  bottomRow);
        checkStair(entity, rightCol, bottomRow);
    }
}