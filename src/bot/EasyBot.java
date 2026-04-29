package bot;

import trivia.Question;

import java.util.List;
import java.util.Random;

public class EasyBot implements BotStrategy {
    
    private final Random random = new Random();

    @Override
    public String getAnswer(Question question){
        if (question.getType() == trivia.QuestionType.ORDERING) {
            return BotAnswerUtil.buildOrderingAnswer(question, random, false);
        }

        List <String> choices = question.getChoices();

        if (choices != null && !choices.isEmpty()) {
            
            return choices.get(random.nextInt(choices.size()));
        }

        return BotAnswerUtil.buildFallbackAnswer(question, random, false);
    }


    @Override
    public long getResponseTime(){
        return 2000;
    }


    @Override
    public String getDifficultyName(){
        return "Easy";
    }
}