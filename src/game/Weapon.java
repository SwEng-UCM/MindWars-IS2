package game;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import player.Player;
import trivia.Question;
import trivia.QuestionBank;
import trivia.QuestionType;
import ui.ConsoleIO;

public class Weapon {

    private final WeaponType type;
    private final ConsoleIO io;
    private final Random random = new Random();

    public Weapon(WeaponType type, ConsoleIO io) {
        this.type = type;
        this.io = io;
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

    public Question useWeapon(
        Question q,
        WeaponType w,
        QuestionBank qb,
        WeaponType attack_weapon,
        Question q_att
    ) {
        switch (w) {
            case CANNON:
                return cannon(q, qb);
            case CROSSBOW:
                return crossbow(q, qb);
            case BURST:
                return burst(q, qb);
            case SHIELD:
                return shield(q_att, q, attack_weapon);
            case LASER_SIGHT:
                return laserSight(q, qb);
            case HELMET:
                return helmet(q, qb);
            default:
                io.println("Invalid Weapon");
                return q;
        }
    }

    private Question cannon(Question q, QuestionBank qb) {
        Question newQ;
        switch (q.getDifficulty()) {
            case "EASY":
                newQ = qb.getQuestion(q.getCategory(), "MEDIUM");
                return newQ;
            case "MEDIUM":
                newQ = qb.getQuestion(q.getCategory(), "HARD");
                return newQ;
            case "HARD":
                newQ = qb.getQuestion(q.getCategory(), "HARD");
                return newQ;
            default:
                newQ = q;
                return newQ;
        }
    }

    private Question crossbow(Question q, QuestionBank qb) {
        List<String> categories = new ArrayList<>(qb.getCategories());
        String selectedCategory = io.selectFromList(
            "  Choose a CATEGORY:",
            categories
        );
        io.println("  Category selected: " + selectedCategory);
        io.println("");
        Question newQ = qb.getQuestion(selectedCategory, q.getDifficulty());
        return newQ;
    }

    private Question helmet(Question q, QuestionBank qb) {
        Question newQ;
        switch (q.getDifficulty()) {
            case "EASY":
                newQ = qb.getQuestion(q.getCategory(), "EASY");
                return newQ;
            case "MEDIUM":
                newQ = qb.getQuestion(q.getCategory(), "EASY");
                return newQ;
            case "HARD":
                newQ = qb.getQuestion(q.getCategory(), "MEDIUM");
                return newQ;
            default:
                newQ = q;
                return newQ;
        }
    }

    private Question laserSight(Question q, QuestionBank qb) {
        List<String> categories = new ArrayList<>(qb.getCategories());
        String selectedCategory = io.selectFromList(
            "  Choose a CATEGORY:",
            categories
        );
        io.println("  Category selected: " + selectedCategory);
        io.println("");
        Question newQ = qb.getQuestion(selectedCategory, q.getDifficulty());
        return newQ;
    }

    private Question shield(
        Question q_att,
        Question q_deff,
        WeaponType attack_weapon
    ) {
        if (attack_weapon == WeaponType.BURST && random.nextDouble() >= 0.5) {
            return q_att;
        }
        return q_deff;
    }

    private Question burst(Question q, QuestionBank qb) {
        List<String> categories = new ArrayList<>(qb.getCategories());
        String selectedCategory = io.selectFromList(
            "  Choose a CATEGORY:",
            categories
        );
        io.println("  Category selected: " + selectedCategory);
        io.println("");
        Question newQ = qb.getQuestion(selectedCategory, "HARD");
        return newQ;
    }
}
