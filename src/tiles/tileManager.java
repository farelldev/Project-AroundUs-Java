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
        tile = new tile[10];

        // Mengambil ukuran dari GamePanel (16 kolom x 12 baris)
        mapTileNum = new int[gp.maxscreenCol][gp.maxscreenRow];

        getTileImage();

        // Membaca file map txt
        loadMap("/maps/map01.txt");
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