package entity;

import combat.Damageable;

public abstract class Character extends Entity implements Damageable {

    protected int hp;
    protected int baseDmg;

    public Character(int hp, int baseDmg) {
        this.hp      = hp;
        this.baseDmg = baseDmg;
    }

    @Override
    public void takeDmg(int dmg) {
        if (dmg < 0) return;
        this.hp = Math.max(0, this.hp - dmg);
        System.out.printf("[%s] -%d HP  →  HP: %d%n",
                getClass().getSimpleName(), dmg, this.hp);
    }

    public boolean isAlive() {
        return this.hp > 0;
    }

    public abstract void attack();

    public int  getHp()      { return hp; }
    public int  getBaseDmg() { return baseDmg; }

    public void setHp(int hp)           { this.hp      = Math.max(0, hp); }
    public void setBaseDmg(int baseDmg) { this.baseDmg = baseDmg; }
}