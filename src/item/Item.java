package item;

import Entity.Player;

public abstract class Item {
    String name;

    public abstract void use(Player player);
}
