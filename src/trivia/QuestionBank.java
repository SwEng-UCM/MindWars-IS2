package trivia;

import java.io.*;
import java.util.*;

public class QuestionBank {
    private List<Question> questions;

    public QuestionBank(String filePath) {
        this.questions = new ArrayList<>();
        loadQuestionsFromFile(filePath);
        Collections.shuffle(this.questions); // randomize the game
    }

    private void loadQuestionsFromFile(String filePath) {
        // parsing logic to turn raw text into Java objects
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;

                // format: TYPE|PROMPT|ANSWER|CHOICE1,CHOICE2...
                String[] parts = line.split("\\|"); // splits a single line into pieces using the | symbol as a separator
                QuestionType type = QuestionType.valueOf(parts[0]); // converts the first string into the actual Java enum type
                String prompt = parts[1];
                String answer = parts[2];
                List<String> choices = new ArrayList<>();

                if (parts.length > 3) {
                    choices = Arrays.asList(parts[3].split(",")); // if it's mcq it splits that specific section by commas to create a list of options
                }

                questions.add(new Question(type, prompt, choices, answer)); // builds a new Question object to store in the list
            }
        } catch (IOException e) {
            System.err.println("Error loading questions: " + e.getMessage());
        }
    }

    public Question getRandomQuestion() {
        if (questions.isEmpty()) return null;
        return questions.remove(0); // pulls the first question & the same question isn't asked twice in one session
    }
}