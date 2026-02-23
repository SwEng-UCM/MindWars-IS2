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

<<<<<<< HEAD
    private void loadQuestionsFromFile(String filePath) {
        // parsing logic to turn raw text into Java objects
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#"))
                    continue;

                // format: TYPE|PROMPT|ANSWER|CHOICE1,CHOICE2...
                String[] parts = line.split("\\|"); // splits a single line into pieces using the | symbol as a
                                                    // separator
                QuestionType type = QuestionType.valueOf(parts[0]); // converts the first string into the actual Java
                                                                    // enum type
                String prompt = parts[1];
                String answer = parts[2];
                List<String> choices = new ArrayList<>();

                if (parts.length > 3) {
                    choices = Arrays.asList(parts[3].split(",")); // if it's mcq it splits that specific section by
                                                                  // commas to create a list of options
                }

                questions.add(new Question(type, prompt, choices, answer)); // builds a new Question object to store in
                                                                            // the list
=======
    private void loadFromJson(String jsonPath) {
        try (FileReader reader = new FileReader(jsonPath)) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Question>>(){}.getType();
            List<Question> allQuestions = gson.fromJson(reader, listType);

            for (Question q : allQuestions) {
                organizedQuestions
                    .computeIfAbsent(q.getCategory(), k -> new HashMap<>())
                    .computeIfAbsent(q.getDifficulty(), k -> new ArrayList<>())
                    .add(q);
>>>>>>> main
            }
            // shuffling the questions
            organizedQuestions.values().forEach(diffMap -> 
                diffMap.values().forEach(Collections::shuffle));
        } catch (Exception e) {
            System.err.println("Error loading JSON: " + e.getMessage());
        }
    }

<<<<<<< HEAD
    public Question getRandomQuestion() {
        if (questions.isEmpty())
            return null;
        return questions.remove(0); // pulls the first question & the same question isn't asked twice in one session
=======
    public Set<String> getCategories() { return organizedQuestions.keySet(); }

    public Question getQuestion(String cat, String diff) {
        if (organizedQuestions.containsKey(cat) && organizedQuestions.get(cat).containsKey(diff)) {
            List<Question> list = organizedQuestions.get(cat).get(diff);
            return list.isEmpty() ? null : list.remove(0);
        }
        return null;
>>>>>>> main
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