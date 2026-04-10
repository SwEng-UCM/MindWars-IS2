package bot;

import trivia.Question;

import java.util.List;
import java.util.Random;

public class EasyBot implements BotStrategy {
    
    private final Random random = new Random();

    @Override
    public String getAnswer(Question question){

        List <String> choices = question.getChoices();

        if (choices != null && !choices.isEmpty()) {
            
            return choices.get(random.nextInt(choices.size()));
        }

        // Fallback for open-ended questions or missing choices
        return "I'm not sure";
    }
}