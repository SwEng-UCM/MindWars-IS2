package trivia;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.*;

public class QuestionBank {
    // structure: category -> (dificulty -> questions)
    private Map<String, Map<String, List<Question>>> organizedQuestions = new HashMap<>();

    public QuestionBank(String jsonPath) {
        System.out.println("DEBUG: QuestionBank loaded " + getAllQuestionsAsList().size() + " total questions.");
        loadFromJson(jsonPath);
    }

    private void loadFromJson(String jsonPath) {
        try (FileReader reader = new FileReader(jsonPath)) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Question>>() {
            }.getType();
            List<Question> allQuestions = gson.fromJson(reader, listType);

            for (Question q : allQuestions) {
                organizedQuestions
                        .computeIfAbsent(q.getCategory(), k -> new HashMap<>())
                        .computeIfAbsent(q.getDifficulty(), k -> new ArrayList<>())
                        .add(q);
            }
            // shuffling the questions
            organizedQuestions.values().forEach(diffMap -> diffMap.values().forEach(Collections::shuffle));
        } catch (Exception e) {
            System.err.println("Error loading JSON: " + e.getMessage());
        }
    }

    public Set<String> getCategories() {
        return organizedQuestions.keySet();
    }

    public Set<String> getDifficulties(String category) {
        if (organizedQuestions.containsKey(category)) {
            return organizedQuestions.get(category).keySet();
        }
        return new HashSet<>();
    }

    public Question getQuestion(String cat, String diff) {
        if (organizedQuestions.containsKey(cat) && organizedQuestions.get(cat).containsKey(diff)) {
            List<Question> list = organizedQuestions.get(cat).get(diff);
            return list.isEmpty() ? null : list.remove(0);
        }
        return null;
    }

    public List<Question> getAllQuestionsAsList() {
        List<Question> all = new ArrayList<>();
        // iterate through the map and collect all elements into a single list
        for (Map<String, List<Question>> diffMap : organizedQuestions.values()) {
            for (List<Question> list : diffMap.values()) {
                all.addAll(list);
            }
        }
        Collections.shuffle(all);
        return all;
    }
}