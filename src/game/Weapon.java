package game;

public class Weapon {

    private final WeaponType type;
    private final String name;
    private final int cost;

    public Weapon(WeaponType type, String name, int cost) {
        this.type = type;
        this.name = name;
        this.cost = cost;
    }

    public WeaponType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int getCost() {
        return cost;
    }
}
