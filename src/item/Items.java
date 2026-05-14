package item;

import Entity.Player;

public abstract class Items {
    String name;

    public abstract void use(Player player);

    public void setName(String name){
        this.name = name;
    }
}
