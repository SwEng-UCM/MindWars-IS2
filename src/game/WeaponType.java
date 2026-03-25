package game;

public enum WeaponType {
    // Offensive weapons
    CANNON(30), // Increases the difficulty of the opponent's question
    CROSSBOW(30), // Allows you to choose the opponent's question category
    BURST(65), // can choose the category ans difficulty max and shell work on 50% of case

    // Defensive weapons
    SHIELD(60), // Cancels an incoming attack
    LASER_SIGHT(30), // Allows you to choose your own question category
    HELMET(30); // Lowers the difficulty of your question

    private final int cost;

    WeaponType(int cost) {
        this.cost = cost;
    }

    public int getCost() {
        return cost;
    }

    @Override
    public String toString() {
        return switch (this) {
            case CANNON -> "Cannon";
            case CROSSBOW -> "Crossbow";
            case BURST -> "Burst";
            case SHIELD -> "Shield";
            case LASER_SIGHT -> "Laser Sight";
            case HELMET -> "Helmet";
        };
    }
}
