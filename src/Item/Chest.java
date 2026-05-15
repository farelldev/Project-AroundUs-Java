package Item;

import Entity.Player;
import java.util.Random;

public class Chest {
    private float x;
    private float y;
    private Item content;
    private boolean isOpened;

    public Chest (float x, float y){
        this.x = x;
        this.y = y;
        this.isOpened = isOpened;
        this.content = generateRandomItem();
    }

    private Item generateRandomItem(){
        Random random = new Random();
        int roll = random.nextInt(2); // angka 0 atau 1

        if (roll == 0){
            return new Bandage(20);
        }else{
            return new Ammo(10);
        }
    }
    public void open (Player player){
        if (!isOpened){
            content.use(player);
            isOpened = true;
            System.out.println("Chest dibuka! Kamu mendapat: " + content.getName());
        }else {
            System.out.println("Chest sudah dibuka");
        }
    }
    public void appear(){
        isOpened = false;
        content = generateRandomItem();
    }
    public void disappear(){
        isOpened = true;
    }
    public float getX() { return x; }
    public float getY() { return y; }
    public boolean isOpened() { return isOpened; }
}
