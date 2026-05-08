/*
 * @author Leopold Popper
 * AI-assisted: yes (Claude by Anthropic, via Claude Code)
 * @author Dimofte Raisa
 * @author Ioannis Stogiannaris
 * AI-assisted: no
 *
 * @author Radu Gabriel Stanescu
 * AI-assisted: yes (Codex)
 */
package trivia;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Manages a collection of quiz questions loaded from a JSON file
 * The class organizes questions into a nested map structure for efficient
 * retrieval by category and difficulty level.
 */
public class QuestionBank {
    // structure: category -> (difficulty -> questions)
    private Map<String, Map<String, List<Question>>> organizedQuestions = new HashMap<>();

    public QuestionBank(String jsonPath) {// load questions from the specified JSON file
        loadFromJson(jsonPath);
        System.out.println("DEBUG: QuestionBank loaded " + getAllQuestionsAsList().size() + " total questions.");
    }

    private void loadFromJson(String jsonPath) {// reads the JSON file and organizes questions into the nested map
        try (Reader reader = openReader(jsonPath)) {
            if (reader == null) {
                System.err.println("Error loading JSON: resource not found: " + jsonPath);
                return;
            }
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

    /**
     * Resolve {@code jsonPath} from (1) the classpath (so the file can live
     * inside the packaged JAR), (2) a sibling file on disk, in that order.
     */
    private Reader openReader(String jsonPath) throws java.io.IOException {
        String resourceName = jsonPath.startsWith("/") ? jsonPath : "/" + jsonPath;
        InputStream in = QuestionBank.class.getResourceAsStream(resourceName);
        if (in != null) {
            return new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        Path p = Paths.get(jsonPath);
        if (Files.exists(p)) {
            return new BufferedReader(new InputStreamReader(Files.newInputStream(p), StandardCharsets.UTF_8));
        }
        try {
            return new FileReader(jsonPath);
        } catch (java.io.FileNotFoundException e) {
            return null;
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