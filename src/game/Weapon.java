package game;

package game;

public class Weapon {

    private final WeaponType type;

    public Weapon(WeaponType type) {
        this.type = type;
    }

    public WeaponType getType() {
        return type;
    }

    public String getName() {
        return type.toString(); // utilise le nom défini dans WeaponType
    }

    public int getCost() {
        return type.getCost(); // utilise le coût défini dans WeaponType
    }

    @Override
    public String toString() {
        return getName() + " (" + getCost() + " pts)";
    }
}
