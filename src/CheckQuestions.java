import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.*;

/**
 * CheckQuestions validates the questions.json file structure.
 * It checks if there are sufficient questions (minimum 3) for each
 * category-difficulty combination.
 */
public class CheckQuestions {

    private static class QuestionData {
        String category;
        String difficulty;
        // other fields are not needed for validation
    }

    public static void main(String[] args) {
        String jsonPath = "questions.json";

        try (FileReader reader = new FileReader(jsonPath)) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<QuestionData>>() {
            }.getType();
            List<QuestionData> questions = gson.fromJson(reader, listType);

            // organize questions by category and difficulty
            Map<String, Map<String, Integer>> categories = new HashMap<>();

            for (QuestionData q : questions) {
                categories.putIfAbsent(q.category, new HashMap<>());
                Map<String, Integer> difficultyMap = categories.get(q.category);
                difficultyMap.put(q.difficulty, difficultyMap.getOrDefault(q.difficulty, 0) + 1);
            }

            // display distinct categories
            System.out.println("\nDistinct categories:");
            List<String> sortedCategories = new ArrayList<>(categories.keySet());
            Collections.sort(sortedCategories);
            for (String cat : sortedCategories) {
                System.out.println("  - " + cat);
            }

            System.out.println("\nTotal categories: " + categories.size());
            System.out.println("\nComplete distribution (minimum required: 3):");
            System.out.printf("%-20s %-8s %-8s %-8s%n", "Category", "EASY", "MEDIUM", "HARD");
            System.out.println("-".repeat(50));

            List<String> insufficient = new ArrayList<>();

            for (String cat : sortedCategories) {
                Map<String, Integer> diffMap = categories.get(cat);
                int easy = diffMap.getOrDefault("EASY", 0);
                int medium = diffMap.getOrDefault("MEDIUM", 0);
                int hard = diffMap.getOrDefault("HARD", 0);

                System.out.printf("%-20s %-8d %-8d %-8d%n", cat, easy, medium, hard);

                if (easy < 3) {
                    insufficient.add(cat + " EASY (" + easy + "/3)");
                }
                if (medium < 3) {
                    insufficient.add(cat + " MEDIUM (" + medium + "/3)");
                }
                if (hard < 3) {
                    insufficient.add(cat + " HARD (" + hard + "/3)");
                }
            }

            System.out.println("\nTotal questions: " + questions.size());

            if (!insufficient.isEmpty()) {
                System.out.println("\n❌ Insufficient combinations (<3 questions):");
                for (String combo : insufficient) {
                    System.out.println("  - " + combo);
                }
                System.out.println("\nTotal insufficient combinations: " + insufficient.size());
            } else {
                System.out.println("\n✓ All combinations have at least 3 questions for each difficulty!");
            }

        } catch (Exception e) {
            System.err.println("Error loading JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
