package tiles;

import main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class tileManager {

    GamePanel gp;
    public tile[] tile;
    public int[][] mapTileNum; // Tambahan 1: Array 2D untuk menyimpan angka map

    public tileManager(GamePanel gp) {
        this.gp = gp;
        tile = new tile[256];

        // Mengambil ukuran dari GamePanel (16 kolom x 12 baris)
        mapTileNum = new int[gp.maxscreenCol][gp.maxscreenRow];

        getTileImage();

        // Membaca file map txt
        loadMap("/maps/building01.txt");
    }

    public void getTileImage() {
        try {
            tile[0] = new tile();
            tile[0].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/basicGrass.png"));

            tile[1] = new tile();
            tile[1].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/basicWall.png"));
            tile[1].collision = true;

            tile[2] = new tile();
            tile[2].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/basicWater.png"));
            tile[2].collision = true;


            // Side Decor
            tile[14] = new tile();
            tile[14].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/side_decor_right.png"));

            tile[37] = new tile();
            tile[37].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/side_decor1_down.png"));

            tile[38] = new tile();
            tile[38].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/side_decor2_down.png"));

            tile[39] = new tile();
            tile[39].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/side_decor3_down.png"));

// Stairs
            tile[16] = new tile();
            tile[16].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/stair_2.png"));

            tile[57] = new tile();
            tile[57].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/stair_1.png"));

// Barricades (Collision = true)
            tile[17] = new tile();
            tile[17].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/barricade_1.png"));
            tile[17].collision = true;

            tile[18] = new tile();
            tile[18].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/barricade_2.png"));
            tile[18].collision = true;

// Doors
            tile[19] = new tile();
            tile[19].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/door_closed.png"));
            tile[19].collision = true;

            tile[20] = new tile();
            tile[20].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/door_opened.png"));

            tile[21] = new tile();
            tile[21].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/door_opened2.png"));

            tile[40] = new tile();
            tile[40].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/side_door1_left.png"));

            tile[41] = new tile();
            tile[41].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/side_door2_left.png"));

// Normal Paths
            tile[22] = new tile();
            tile[22].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/path_1.png"));

            tile[23] = new tile();
            tile[23].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/path_2.png"));

            tile[24] = new tile();
            tile[24].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/path_3.png"));

            tile[25] = new tile();
            tile[25].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/path_4.png"));

            tile[26] = new tile();
            tile[26].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/path_5.png"));

            tile[27] = new tile();
            tile[27].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/path_6.png"));

            tile[28] = new tile();
            tile[28].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/path_7.png"));

            tile[29] = new tile();
            tile[29].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/path_8.png"));

            tile[30] = new tile();
            tile[30].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/path_9.png"));

            tile[31] = new tile();
            tile[31].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/path_10.png"));

// Water Paths (Collision = true)
            tile[32] = new tile();
            tile[32].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/path_water1.png"));
            tile[32].collision = true;

            tile[33] = new tile();
            tile[33].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/path_water2.png"));
            tile[33].collision = true;

            tile[42] = new tile();
            tile[42].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/side_path_water_down.png"));
            tile[42].collision = true;

// Seng / Zinc Fences (Collision = true)
            tile[34] = new tile();
            tile[34].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/seng_1.png"));
            tile[34].collision = true;

            tile[35] = new tile();
            tile[35].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/seng_2.png"));
            tile[35].collision = true;

            tile[36] = new tile();
            tile[36].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/seng_3.png"));
            tile[36].collision = true;

// Side Paths
            tile[15] = new tile();
            tile[15].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/side_path4_right.png"));

            tile[43] = new tile();
            tile[43].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/side_path1_down.png"));

            tile[44] = new tile();
            tile[44].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/side_path1_left.png"));

            tile[45] = new tile();
            tile[45].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/side_path1_right.png"));

            tile[46] = new tile();
            tile[46].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/side_path2_down.png"));

            tile[47] = new tile();
            tile[47].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/side_path2_left.png"));

            tile[48] = new tile();
            tile[48].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/side_path2_right.png"));

            tile[49] = new tile();
            tile[49].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/side_path3_down.png"));

            tile[50] = new tile();
            tile[50].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/side_path3_right.png"));

            tile[51] = new tile();
            tile[51].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/side_path4_down.png"));

            tile[52] = new tile();
            tile[52].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/side_path5_down.png"));

            tile[53] = new tile();
            tile[53].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/side_path6_down.png"));

            tile[54] = new tile();
            tile[54].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/side_path7_down.png"));

// Walls (Collision = true)
            tile[55] = new tile();
            tile[55].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/side_wall1_left.png"));
            tile[55].collision = true;

            tile[58] = new tile();
            tile[58].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/wall_1.png"));
            tile[58].collision = true;

            tile[59] = new tile();
            tile[59].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/wall_2.png"));
            tile[59].collision = true;

            tile[60] = new tile();
            tile[60].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/wall_3.png"));
            tile[60].collision = true;

            tile[61] = new tile();
            tile[61].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/wall_4.png"));
            tile[61].collision = true;

            tile[62] = new tile();
            tile[62].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/wall_5.png"));
            tile[62].collision = true;

            tile[63] = new tile();
            tile[63].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/wall_6.png"));
            tile[63].collision = true;

            tile[64] = new tile();
            tile[64].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/wall_7.png"));
            tile[64].collision = true;

            tile[65] = new tile();
            tile[65].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/wall_8.png"));
            tile[65].collision = true;

            tile[66] = new tile();
            tile[66].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/wall_9.png"));
            tile[66].collision = true;

// Windows (Collision = true)
            tile[56] = new tile();
            tile[56].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/side_window1_left.png"));
            tile[56].collision = true;

            tile[67] = new tile();
            tile[67].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/window_1.png"));
            tile[67].collision = true;

            tile[68] = new tile();
            tile[68].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/window_2.png"));
            tile[68].collision = true;

            tile[69] = new tile();
            tile[69].image = ImageIO.read(getClass().getResourceAsStream("/basicTexture/buildingTexture/window_3.png"));
            tile[69].collision = true;

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // Tambahan 2: Fungsi untuk membaca angka dari file text (.txt)
    public void loadMap(String filePath) {
        try {
            InputStream is = getClass().getResourceAsStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            int col = 0;
            int row = 0;

            while (col < gp.maxscreenCol && row < gp.maxscreenRow) {
                String line = br.readLine(); // Membaca satu baris teks

                while (col < gp.maxscreenCol) {
                    String[] numbers = line.split(" "); // Memisahkan angka berdasarkan spasi
                    int num = Integer.parseInt(numbers[col]); // Mengubah teks jadi angka (integer)

                    mapTileNum[col][row] = num;
                    col++;
                }

                if (col == gp.maxscreenCol) {
                    col = 0;
                    row++;
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Tambahan 3: Fungsi untuk menggambar tile ke layar
    public void draw(Graphics2D g2) {
        int col = 0;
        int row = 0;
        int x = 0;
        int y = 0;

        while (col < gp.maxscreenCol && row < gp.maxscreenRow) {

            int tileNum = mapTileNum[col][row]; // ambil angka (ID tile) dari array

            g2.drawImage(tile[tileNum].image, x, y, gp.tileSize, gp.tileSize, null);

            col++;
            x += gp.tileSize;

            if (col == gp.maxscreenCol) {
                col = 0;
                x = 0;
                row++;
                y += gp.tileSize;
            }
        }
    }
}