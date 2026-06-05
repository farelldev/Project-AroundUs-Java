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