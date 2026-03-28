package game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import bot.*;
import player.Player;
import trivia.AnswerValidator;
import trivia.Question;
import trivia.QuestionBank;
import trivia.QuestionType;
import ui.ConsoleIO;
import ui.SoundManager;

public class Game {

    private final ConsoleIO io;
    private final QuestionBank questionBank;
    private final GameState gameState;
    private MapGrid map;
    private final SoundManager sound;
    private final Bonus bonus;

    private static final long TIME_LIMIT_MS = 15000;
    // New feature: if a player answers correctly in <= 3 seconds, base points are
    // doubled.
    private static final long LIGHTNING_BONUS_MS = 3_000;

    public Game(ConsoleIO io, QuestionBank questionBank) {
        this.io = io;
        this.questionBank = questionBank;
        this.gameState = new GameState();
        this.map = new MapGrid(3);
        this.sound = new SoundManager();
        this.bonus = new Bonus(io, questionBank);
    }

    /**
     * Handles a numeric estimation challenge where all players answer the same
     * question.
     * The player closest to the target value wins.
     * Integrated with the betting system exclusively for the final round.
     */
    private void handleNumericRound(Question question, boolean isLastRound) {
        // Stores responses (player, guess, and time) to compare them later
        List<NumericWinnerCalculator.EstimationResponse> roundData = new ArrayList<>();

        // Map to store the wager for each player to process later
        Map<Player, Integer> playerBets = new HashMap<>();

        for (Player p : gameState.getPlayers()) {
            // Hot seat handoff logic
            displayHotSeatHeader(p);

            // Wager logic: triggered only if it's the final round of the game
            int wager = 0;
            if (isLastRound) {
                wager = handleBetting(p, question);
            }
            playerBets.put(p, wager);

            // Display question details for the current player
            io.println("");
            io.println("  " + p.getName() + " - ESTIMATION CHALLENGE");
            io.println("  ----------------------------------------");
            io.println(
                    "  " + question.formatForConsole().replace("\n", "\n  "));

            if (p.getBonusTokens() > 0) {
                io.println("\n  [!] You have " + p.getBonusTokens() + " bonus token(s) available!");
                String use = io.readNonEmptyString("  Use a lifeline for this question? (yes/no): ").toLowerCase();
                if (use.startsWith("y")) {
                    p.setHasUsedBonus(false); // Reset to track if they actually picked a bonus
                    bonus.offerBonusIfAvailable(p, question);

                    if (p.hasUsedBonus()) {
                        p.useBonusToken();
                        io.println("  Token used! Remaining: " + p.getBonusTokens());
                    }
                }
            }

            // Capture start time to measure response speed
            long startTime = System.currentTimeMillis();

            // Read and parse the numeric guess
            io.println(
                    "  You have " + (TIME_LIMIT_MS / 1000) + " seconds to answer!");
            String input = readAnswerWithTimeout(question);
            double val;
            if (input.equals("__TIMEOUT__")) {
                val = Double.MAX_VALUE;
                int penalty = calculatePoints(question);
                p.subtractScore(penalty);
            } else {
                val = Double.parseDouble(input.replace(",", "."));
            }

            long endTime = System.currentTimeMillis();
            long elapsed = endTime - startTime;

            // Update player's total time and store round performance
            p.setTimer(p.getTimer() + elapsed);
            roundData.add(
                    new NumericWinnerCalculator.EstimationResponse(p, val, elapsed));
        }

        // Identify the winner based on proximity to target and response time
        double correctAnswer = question.getNumericAnswer();
        Player winner = NumericWinnerCalculator.calculateWinner(
                correctAnswer,
                roundData);

        // Display results summary table
        io.println("");
        io.println("  >>> ROUND SUMMARY <<<");
        io.println("  Correct Answer: " + (int) correctAnswer);
        io.println("");
        io.println(
                "  " +
                        padRight("PLAYER", 12) +
                        " | " +
                        padRight("GUESS", 8) +
                        " | " +
                        padRight("DIFF", 6) +
                        " | TIME");
        io.println("  --------------------------------------------------");

        for (NumericWinnerCalculator.EstimationResponse res : roundData) {
            int diff = (int) Math.abs(res.value - correctAnswer);
            double timeSec = res.timeTaken / 1000.0;

            io.println(
                    "  " +
                            padRight(res.player.getName(), 12) +
                            " | " +
                            padRight(String.valueOf((int) res.value), 8) +
                            " | " +
                            padRight(String.valueOf(diff), 6) +
                            " | " +
                            String.format("%.2fs", timeSec));
        }
        io.println("  --------------------------------------------------");

        // Finalize scores for all participants based on the winner and wagers
        if (winner != null) {
            io.println("  WINNER OF THE ESTIMATION: " + winner.getName());
        }

        for (NumericWinnerCalculator.EstimationResponse res : roundData) {
            Player p = res.player;
            boolean isWinner = (p == winner);
            int wager = playerBets.getOrDefault(p, 0);

            // Universal score processing (handles wagers, standard points, and streaks)
            processScore(p, question, isWinner, wager, res.timeTaken);
        }

        io.println("");
    }

    /**
     * Utility method to manage the "Hot Seat" device handoff between players.
     */
    private void displayHotSeatHeader(Player p) {
        io.println("\n\n  +----------------------------------------+");
        io.println(
                "  |     PASS THE DEVICE TO " +
                        padRight(p.getName().toUpperCase(), 15) +
                        " |");
        io.println("  |     Other player, look away!           |");
        io.println("  +----------------------------------------+");
        io.readLine("  Press ENTER when ready...");
        io.println("");
    }

    public void run() {
        // 1. Ask for sound preference at the very beginning
        String soundChoice = io
                .readNonEmptyString("  Enable sound effects and music? (yes/no): ")
                .toLowerCase();
        boolean isMuted = soundChoice.equals("no") || soundChoice.equals("n");
        sound.setMuted(isMuted);
        sound.startBackground();
        boolean playAgain = true;

        while (playAgain) {
            gameState.reset();
            printWelcomeMessage();

            int mapSize = chooseMapSize();
            this.map = new MapGrid(mapSize);
            setupPlayers();
            setStartingPlayer();
            int questionsPerPlayer = mapSize;

            io.println("");
            String mode = io.selectFromList(
                    "  Choose game mode:",
                    List.of(
                            "Choose category & difficulty",
                            "Random Round (computer decides)"));

            io.println("");
            io.println("  All players registered. Let the battle begin!");
            io.println("");
            sound.play(SoundManager.GAME_START);

            boolean randomRound = mode.equals(
                    "Random Round (computer decides)");

            Random random = new Random();
            List<Question> roundQuestions = new ArrayList<>();

            if (!randomRound) {
                // Randomly decide which player chooses category and which chooses difficulty
                boolean player1ChoosesCategory = random.nextBoolean();

                Player categoryChooser = player1ChoosesCategory
                        ? gameState.getPlayers().get(0)
                        : gameState.getPlayers().get(1);
                Player difficultyChooser = player1ChoosesCategory
                        ? gameState.getPlayers().get(1)
                        : gameState.getPlayers().get(0);

                // Category selection
                io.println(
                        "  " +
                                categoryChooser.getName() +
                                ", you will choose the CATEGORY for all questions!");
                io.println("");
                List<String> categories = new ArrayList<>(
                        questionBank.getCategories());
                String selectedCategory = io.selectFromList(
                        "  Choose a CATEGORY:",
                        categories);
                io.println("  Category selected: " + selectedCategory);
                io.println("");

                // Difficulty selection
                io.println(
                        "  " +
                                difficultyChooser.getName() +
                                ", you will choose the DIFFICULTY for all questions!");
                io.println("");
                List<String> difficulties = new ArrayList<>(
                        questionBank.getDifficulties(selectedCategory));
                String selectedDifficulty = io.selectFromList(
                        "  Choose a DIFFICULTY:",
                        difficulties);
                io.println("  Difficulty selected: " + selectedDifficulty);
                io.println("");

                // Get questions for the game based on selections
                for (int i = 0; i < questionsPerPlayer; i++) {
                    Question q = questionBank.getQuestion(
                            selectedCategory,
                            selectedDifficulty);
                    if (q != null) {
                        roundQuestions.add(q);
                    }
                }
            } else {
                io.println(
                        "  Random Round enabled: The computer will choose category and difficulty each question!");
                io.println("");

                List<String> categories = new ArrayList<>(
                        questionBank.getCategories());

                for (int i = 0; i < questionsPerPlayer; i++) {
                    String category = categories.get(
                            random.nextInt(categories.size()));

                    List<String> diffs = new ArrayList<>(
                            questionBank.getDifficulties(category));
                    if (diffs.isEmpty()) {
                        i--; // try again
                        continue;
                    }

                    String difficulty = diffs.get(random.nextInt(diffs.size()));

                    Question q = questionBank.getQuestion(category, difficulty);
                    if (q != null) {
                        roundQuestions.add(q);
                    } else {
                        i--; // try again
                        continue;
                    }
                }
            }

            if (roundQuestions.isEmpty()) {
                if (randomRound) {
                    io.println(
                            "  ERROR: No questions available for Random Round!");
                } else {
                    io.println(
                            "  ERROR: No questions available for this category/difficulty combination!");
                }
                io.println("  Game cannot start.");
                return;
            }

            if (roundQuestions.size() < questionsPerPlayer) {
                io.println(
                        "  WARNING: Only " +
                                roundQuestions.size() +
                                " question(s) available instead of " +
                                questionsPerPlayer +
                                ".");
                io.println(
                        "  Continuing with " +
                                roundQuestions.size() +
                                " question(s)...");
            }

            io.println(
                    "  Starting game with " + roundQuestions.size() + " questions!");
            io.println("");

            // Play multiple rounds with pre-selected questions
            for (int round = 0; round < roundQuestions.size(); round++) {
                String checkSettings = io.readNonEmptyString(
                        "\n  Press ENTER to start Round " + (round + 1) + " or type 's' for Bot Settings:");
                if (checkSettings.equalsIgnoreCase("s")) {
                    boolean botFound = false;
                    for (Player p : gameState.getPlayers()) {
                        if (p.isBot()) {
                            botFound = true;
                            io.println("\n SETTINGS: " + p.getName().toUpperCase() + " ");
                            String newDiff = io.selectFromList("  Choose new difficulty:",
                                    List.of("Easy", "Medium", "Hard", "Keep Current"));

                            switch (newDiff) {
                                case "Easy" -> changeBotDifficulty(p, new bot.EasyBot());
                                case "Medium" -> changeBotDifficulty(p, new bot.MediumBot());
                                case "Hard" -> changeBotDifficulty(p, new bot.HardBot());
                            }
                        }
                    }
                    if (!botFound) {
                        io.println("  [!] No Bots found in the current game. Settings ignored.");
                    }
                }

                io.println("");
                io.println("");
                io.println(
                        "   =========== ROUND " +
                                (round + 1) +
                                " of " +
                                roundQuestions.size() +
                                " ===========");

                Question currentQuestion = roundQuestions.get(round);
                if (randomRound) {
                    io.println(
                            "  Category: " +
                                    currentQuestion.getCategory() +
                                    " | Difficulty: " +
                                    currentQuestion.getDifficulty());
                }
                io.println("");

                boolean[] roundResults = new boolean[gameState
                        .getPlayers()
                        .size()];
                long[] roundTimes = new long[gameState.getPlayers().size()];
                boolean isLastRound = (round == roundQuestions.size() - 1);
                if (currentQuestion.getType() == QuestionType.NUMERIC) {
                    handleNumericRound(currentQuestion, isLastRound);
                } else {
                    // Hot seat: alternate between players each round
                    for (int p = 0; p < gameState.getPlayers().size(); p++) {
                        gameState.setCurrentPlayerIndex(p);
                        Player currentPlayer = gameState.getPlayers().get(p);
                        Question playerQuestion = currentQuestion.cloneQuestion();
                        displayHotSeatHeader(currentPlayer);
                        int wager = 0;
                        if (isLastRound) {
                            wager = handleBetting(
                                    currentPlayer,
                                    currentQuestion);
                        }
                        io.println("");
                        io.println(
                                "  " +
                                        currentPlayer.getName() +
                                        " - Question " +
                                        (round + 1) +
                                        " of " +
                                        roundQuestions.size());
                        io.println(
                                "  ----------------------------------------");
                        io.println(
                                "  " +
                                        playerQuestion
                                                .formatForConsole()
                                                .replace("\n", "\n  "));

                        if (currentPlayer.getBonusTokens() > 0) {
                            io.println("\n  [!] You have " + currentPlayer.getBonusTokens()
                                    + " bonus token(s) available!");
                            String use = io.readNonEmptyString("  Use a lifeline for this question? (yes/no): ")
                                    .toLowerCase();

                            if (use.startsWith("y")) {
                                // reset flag to ensure we only consume token if a bonus is actually selected
                                currentPlayer.setHasUsedBonus(false);

                                bonus.offerBonusIfAvailable(currentPlayer, playerQuestion);

                                if (currentPlayer.hasUsedBonus()) {
                                    currentPlayer.useBonusToken();
                                    io.println("  Token consumed! Remaining: " + currentPlayer.getBonusTokens());
                                }
                            }
                        }

                        io.println(
                                "  You have " +
                                        (TIME_LIMIT_MS / 1000) +
                                        " seconds to answer!");
                        long startTime = System.currentTimeMillis();
                        String response = readAnswerWithTimeout(playerQuestion);
                        long endTime = System.currentTimeMillis();
                        long elapsedTime = endTime - startTime;

                        roundTimes[p] = elapsedTime;
                        currentPlayer.setTimer(
                                currentPlayer.getTimer() + elapsedTime);

                        boolean isCorrect;
                        if (response.equals("__TIMEOUT__")) {
                            isCorrect = false;
                            int penalty = calculatePoints(currentQuestion);
                            currentPlayer.subtractScore(penalty);
                            io.println(
                                    "   >> TIME'S UP! -" +
                                            penalty +
                                            " points penalty.");
                        } else {
                            isCorrect = AnswerValidator.isCorrect(
                                    playerQuestion,
                                    response);
                        }

                        roundResults[p] = isCorrect;
                        processScore(
                                currentPlayer,
                                playerQuestion,
                                isCorrect,
                                wager,
                                elapsedTime);

                        if (!isCorrect) {
                            String correctAnswer;
                            if (playerQuestion.getType() == QuestionType.NUMERIC) {
                                correctAnswer = String.valueOf(
                                        currentQuestion.getNumericAnswer());
                            } else {
                                correctAnswer = playerQuestion.getAnswer();
                            }
                            io.println(
                                    "   >> WRONG or TIMEOUT! The correct answer was: " +
                                            correctAnswer);
                        }

                        io.println("  Score: " + currentPlayer.getScore());
                        io.println("");
                    }
                }

                applySpeedBonus(roundResults, roundTimes);

                handleTerritoryPhase(
                        roundResults[0],
                        roundTimes[0],
                        roundResults[1],
                        roundTimes[1]);

                if (map.isMapFull()) {
                    io.println("\n=== SHOP TIME ===");
                    for (Player p : gameState.getPlayers()) {
                        displayHotSeatHeader(p);
                        openShop(p);
                    }
                    handleAttackPhase();
                    break;
                }
            }

            // Final results
            sound.stopBackground();
            determineWinner();

            showPlayerStatistics();

            // option to play again
            String choice = io
                    .readNonEmptyString(
                            "  Would you like to play another game? (yes/no): ")
                    .toLowerCase();
            playAgain = choice.equals("yes") || choice.equals("y");

            if (playAgain) {
                io.println("\n  Refreshing the battlefield...");
            }
        }

        io.println("Thanks for playing!");
    }

    @SuppressWarnings("unused") // Kept for potential future use without timeout
    private String readValidAnswer(Question question) {
        while (true) {
            String response = io.readNonEmptyString("  Your answer:");
            if (AnswerValidator.isValidAnswer(question, response)) {
                return response;
            }
            io.println("  Invalid answer. Please enter a valid option.");
        }
    }

    /**
     * Reads a valid answer within the time limit.
     * Timer runs silently in the background and returns "__TIMEOUT__" if time
     * expires.
     */
    private String readAnswerWithTimeout(Question question) {
        final boolean[] done = { false };
        final String[] result = { null };

        // Start ticking sound while player is answering
        sound.startTimer();

        // Input reading thread
        Thread inputThread = new Thread(() -> {
            while (!done[0]) {
                try {
                    String response = io.readLineWithTimeoutAndCountdown(
                            "  Your answer:",
                            TIME_LIMIT_MS);
                    if (AnswerValidator.isValidAnswer(question, response)) {
                        result[0] = response;
                        done[0] = true;
                        break;
                    } else {
                        io.println(
                                "  Invalid answer. Please enter a valid option.");
                    }
                } catch (TimeoutException e) {
                    done[0] = true;
                    break;
                }
            }
        });

        inputThread.start();

        try {
            inputThread.join();
        } catch (InterruptedException e) {
            // ignored
        }

        // Stop ticking sound once answered or timed out
        sound.stopTimer();

        if (result[0] == null) {
            io.println("  !! TIME'S UP !!");
            return "__TIMEOUT__";
        }
        return result[0];
    }

    private void printWelcomeMessage() {
        io.println("");
        io.println("  +========================================+");
        io.println("  |                                        |");
        io.println("  |      M I N D W A R S  T R I V I A      |");
        io.println("  |       -  Where Brains Conquer  -       |");
        io.println("  |                                        |");
        io.println("  +========================================+");
        io.println("");

        io.println("  RULES:");
        io.println(
                "  1. BATTLE: Answer correctly and BE FAST! Speed is the tie-breaker.");
        io.println(
                "  2. REWARD: Round Winner claims 1 more cell from the map than the runner up. Claims scale with the map size.");
        io.println(
                "             - Once claimed, the cell will show your player symbol.");

        io.println("");
    }

    private void setupPlayers() {
        io.println("  The game requires 2 players.");
        io.println("");
        for (int i = 1; i <= 2; i++) {
            String name = io.readNonEmptyString(
                    "  Enter name for Player " + i + ":");
            Player newPlayer = new Player(name);
            char symbol = (i == 1) ? 'X' : 'O';
            newPlayer.setSymbol(symbol);
            map.initVisibilityForPlayer(symbol);
            gameState.addPlayer(newPlayer);
            io.println("");
        }
    }

    private void setStartingPlayer() {
        gameState.setCurrentPlayerIndex(0);

        String startName = gameState.getPlayers().get(0).getName();
        io.println("  " + startName + " will go first.");
    }

    private void determineWinner() {
        io.println("\n=== GAME OVER ===");
        io.println("\nFinal Scores:");

        io.println("");
        io.println("  +========================================+");
        io.println("  |                                        |");
        io.println("  |           FINAL SCOREBOARD             |");
        io.println("  |                                        |");
        io.println("  +========================================+");
        io.println("");

        // show all player scores and territory
        for (Player player : gameState.getPlayers()) {
            double timeInSeconds = player.getTimer() / 1000.0;
            int territory = map.countTerritory(player.getSymbol());

            io.println(
                    "    " +
                            padRight(player.getName(), 15) +
                            padRight(player.getScore() + " pts", 10) +
                            padRight(territory + " terr", 10) +
                            String.format("%.2fs", timeInSeconds));
        }

        io.println("");
        io.println("  ------------------------------------------");

        // calculate the winner (considers score first, then territory)
        Player winner = WinnerCalculator.getWinnerOrNull(
                gameState.getPlayers(),
                map);

        if (winner == null) {
            io.println("  It's a TIE! Same score and territory.");
        } else {
            sound.play(SoundManager.VICTORY);
            io.println("  WINNER: " + winner.getName() + "!");
        }

        io.println("  ------------------------------------------");
        io.println("");
        io.println("  Thanks for playing MindWars!");
        io.println("");
    }

    private String padRight(String text, int length) {
        if (text.length() >= length)
            return text;
        StringBuilder sb = new StringBuilder(text);
        while (sb.length() < length) {
            sb.append(' ');
        }

        return sb.toString();
    }

    private int calculatePoints(Question question) {
        String diff = question.getDifficulty().toUpperCase();
        // logic: easy=10 medium=20 hard=30
        return switch (diff) {
            case "HARD" -> 300;
            case "MEDIUM" -> 200;
            default -> 100; // covers "easy" or any unexpected strings
        };
    }

    private void askToClaim(int playerNum) {
        boolean done = false;

        Player currentPlayer = gameState.getPlayers().get(playerNum - 1);
        char symbol = currentPlayer.getSymbol();

        while (!done) {
            map.displayForPlayer(io, symbol);
            String input = io.readNonEmptyString(
                    "  " +
                            currentPlayer.getName() +
                            " (" +
                            symbol +
                            "), enter coordinates row,col:");

            if (!input.contains(",")) {
                io.println("  Invalid format! Please use: row,col");
                continue;
            }

            try {
                String[] parts = input.split(",");
                int r = Integer.parseInt(parts[0].trim());
                int c = Integer.parseInt(parts[1].trim());

                boolean cellHasBonus = map.hasBonus(r, c);

                if (map.claimCell(symbol, r, c)) {
                    sound.play(SoundManager.TERRITORY);
                    // reveal the zone
                    map.revealNeighbourForPlayer(symbol, r, c);
                    map.revealCellForPlayer(symbol, r, c);

                    io.println(
                            "  Success! Cell [" +
                                    r +
                                    "," +
                                    c +
                                    "] is now marked with '" +
                                    symbol +
                                    "'.");
                    io.println("");

                    if (cellHasBonus) {
                        io.println("\n BONUS TOKEN FOUND!");
                        io.println(
                                "  Congratulations " + currentPlayer.getName() + "!");

                        currentPlayer.addBonusToken();

                        io.println("  You found a power-up! You now have " +
                                currentPlayer.getBonusTokens() + " token(s) stored.");
                    }

                    done = true;
                    io.println("");
                } else {
                    io.println(
                            "  That cell is either outside the map or already taken! Try again.");
                }
            } catch (NumberFormatException e) {
                io.println(
                        "  Error: Please enter valid numbers for row and column.");
            }
        }
    }

    private void handleTerritoryPhase(
            boolean p1Correct,
            long p1Time,
            boolean p2Correct,
            long p2Time) {
        int winner;
        String reason;

        if (p1Correct && !p2Correct) {
            winner = 1;
            reason = "being the only one with the correct answer!";
        } else if (!p1Correct && p2Correct) {
            winner = 2;
            reason = "being the only one with the correct answer!";
        } else if (p1Correct && p2Correct) {
            if (p1Time <= p2Time) {
                winner = 1;
            } else {
                winner = 2;
            }
            reason = "answering correctly AND being faster!";
        } else {
            if (p1Time <= p2Time) {
                winner = 1;
            } else {
                winner = 2;
            }
            reason = "being faster (even though both missed the mark)!";
        }

        int loser;
        if (winner == 1) {
            loser = 2;
        } else {
            loser = 1;
        }

        String winnerName = gameState.getPlayers().get(winner - 1).getName();
        String loserName = gameState.getPlayers().get(loser - 1).getName();

        io.println("\n\n  >> ROUND CONQUEROR: " + winnerName);
        io.println("  >> Reason: " + reason);
        io.println("");
        io.println("");

        int mapSize = map.getSize();
        int winnerClaims = mapSize / 2 + 1;
        int loserClaims = mapSize / 2;

        for (int i = 1; i <= winnerClaims; i++) {
            io.println(
                    "  [" +
                            winnerName +
                            " Selection " +
                            i +
                            "/" +
                            winnerClaims +
                            "]");
            askToClaim(winner);
        }
        io.println("");

        for (int i = 1; i <= loserClaims; i++) {
            io.println(
                    "  [" + loserName + " Selection" + i + "/" + loserClaims + "]");
            askToClaim(loser);
        }
        io.println("");
    }

    private void applySpeedBonus(boolean[] results, long[] times) {
        if (results.length >= 2 && results[0] && results[1]) {
            int winnerIndex;

            if (times[0] <= times[1]) {
                winnerIndex = 0;
            } else {
                winnerIndex = 1;
            }

            Player speedWinner = gameState.getPlayers().get(winnerIndex);

            int speedBonus = 1;
            speedWinner.addScore(speedBonus);

            io.println(
                    "\n   SPEED BONUS: " +
                            speedWinner.getName() +
                            " was faster! +" +
                            speedBonus +
                            " pts");

            // actually add the bonus to the player's score
            io.println("   Updated Score: " + speedWinner.getScore());
        }
    }

    /**
     * Handles the wager input logic for a trailing player.
     *
     * @return The amount of points wagered, or 0 if declined.
     */
    private int handleBetting(Player player, Question q) {
        if (player.getScore() <= 0) {
            io.println(
                    "\n  " +
                            player.getName() +
                            ", you have 0 points. Skipping betting phase.");
            return 0;
        }
        io.println("\n  *** SPECIAL BETTING OPPORTUNITY ***");
        io.println(
                "  Category: " +
                        q.getCategory().toUpperCase() +
                        " | Difficulty: HARD");
        io.println("  Your current score: [" + player.getScore() + "]");

        String choice = io
                .readNonEmptyString("  Do you want to bet your points? (yes/no):")
                .toLowerCase();

        if (choice.equals("yes") || choice.equals("y")) {
            while (true) {
                try {
                    String input = io.readNonEmptyString(
                            "  Enter wager (1 - " + player.getScore() + "):");
                    int bet = Integer.parseInt(input);

                    if (bet > 0 && bet <= player.getScore()) {
                        return bet;
                    }
                    io.println(
                            "  Invalid amount! Max bet allowed is " +
                                    player.getScore());
                } catch (NumberFormatException e) {
                    io.println("  Please enter a valid number.");
                }
            }
        }
        return 0; // Player chose not to bet
    }

    /**
     * Processes the final score for a player based on their answer and wager.
     * If a wager is placed, it doubles the bet on success or subtracts it on
     * failure.
     * If no wager is placed, it applies standard points and manages the win streak.
     */
    /**
     * Processes the final score for a player based on their answer and wager.
     * Handles both standard questions and numeric estimation challenges.
     */
    private void processScore(
            Player player,
            Question q,
            boolean isCorrect,
            int wager,
            long elapsedTimeMs) {
        boolean playSounds = q.getType() != QuestionType.OPEN_ENDED &&
                q.getType() != QuestionType.NUMERIC;

        if (isCorrect) {
            player.addCorrectAnswer(elapsedTimeMs);
            if (playSounds)
                sound.play(SoundManager.CORRECT);
            if (wager > 0) {
                // Wager logic: Double the bet
                int winAmount = wager * 2;
                player.addScore(winAmount);
                io.println(
                        "\n  >> CORRECT! [" +
                                player.getName() +
                                "] won " +
                                winAmount +
                                " points from the bet!");
            } else {
                // Standard points logic: Use the difficulty-based scoring
                int points = calculatePoints(q); // Now correctly returns 10, 20, or 30

                // ⚡ Lightning bonus (<= 3 seconds)
                if (elapsedTimeMs <= LIGHTNING_BONUS_MS) {
                    points *= 2;
                    sound.play(SoundManager.LIGHTNING);
                    io.println(
                            "  >> ⚡ LIGHTNING! Answered in <= 3s: base points doubled.");
                }

                // setStreak adds the points and checks for the STREAK_BONUS (3 pts)
                player.setStreak(points);

                io.println(
                        "\n  >> CORRECT! [" +
                                player.getName() +
                                "] earned " +
                                points +
                                " points.");
                if (player.getStreak() >= 2) {
                    io.println("     Streak Bonus applied! +3 pts");
                }
            }
        } else {
            player.addWrongAnswer(elapsedTimeMs);
            if (playSounds)
                sound.play(SoundManager.INCORRECT);
            if (wager > 0) {
                // Failure logic: Subtract the bet
                player.subtractScore(wager);
                io.println(
                        "  >> WRONG! [" +
                                player.getName() +
                                "] lost the bet of " +
                                wager +
                                " points.");
            } else {
                // Failure logic: Reset streak, no points lost
                player.resetStreak();
            }
        }
    }

    private int chooseMapSize() {
        String mapSize = io.selectFromList(
                "  Choose a map size. ",
                List.of("Small 3x3", "Medium 5x5", "Large 7x7"));

        return switch (mapSize) {
            case "Medium 5x5" -> 5;
            case "Large 7x7" -> 7;
            default -> 3;
        };
    }

    private void handleAttackPhase() {
        io.println("  |        ALL TERRITORIES CLAIMED         |");
        io.println("  |        PHASE 2: THE INVASION           |");

        for (Player attacker : gameState.getPlayers()) {
            displayHotSeatHeader(attacker);
            char attackerSym = attacker.getSymbol();
            Player defender = (attackerSym == 'X')
                    ? gameState.getPlayers().get(1)
                    : gameState.getPlayers().get(0);
            char defenderSym = defender.getSymbol();

            io.println("  " + attacker.getName() + ", choose your move!");
            map.display(io);

            int attR = -1,
                    attC = -1,
                    defR = -1,
                    defC = -1;

            boolean sourceValid = false;
            while (!sourceValid) {
                try {
                    String input = io.readNonEmptyString(
                            "  Select YOUR territory to attack FROM (row,col) or 'd' to see map:");

                    if (input.equalsIgnoreCase("d")) {
                        map.display(io);
                        continue;
                    }

                    String[] p = input.split(",");
                    attR = Integer.parseInt(p[0].trim());
                    attC = Integer.parseInt(p[1].trim());

                    if (map.getOwner(attR, attC) == attackerSym) {
                        sourceValid = true;
                    } else {
                        io.println("  Error: You don't own that cell!");
                    }
                } catch (Exception e) {
                    io.println("  Invalid format. Use row,col or 'd'.");
                }
            }

            boolean targetValid = false;
            while (!targetValid) {
                try {
                    String input = io.readNonEmptyString(
                            "  Select ADJACENT enemy territory to ATTACK (row,col) or 'd' to see map:");

                    if (input.equalsIgnoreCase("d")) {
                        map.display(io);
                        continue;
                    }

                    String[] p = input.split(",");
                    defR = Integer.parseInt(p[0].trim());
                    defC = Integer.parseInt(p[1].trim());

                    if (map.getOwner(defR, defC) != defenderSym) {
                        io.println("  Error: That's not an enemy territory!");
                    } else if (!map.isAdjacent(attR, attC, defR, defC)) {
                        io.println("  Error: Cell is not adjacent!");
                    } else {
                        targetValid = true;
                    }
                } catch (Exception e) {
                    io.println("  Invalid format. Use row,col or 'd'.");
                }
            }

            int attempts = 0;
            boolean battleResolved = false;
            boolean attacked = false;
            WeaponType weapon_use = null;

            while (attempts < 2 && !battleResolved) {
                attempts++;
                attacked = false;
                weapon_use = null;
                Question q = questionBank.getAllQuestionsAsList().get(0);
                Question q_defender = q.cloneQuestion();
                String correctAnswer = (q.getType() == QuestionType.NUMERIC)
                        ? String.valueOf(q.getNumericAnswer())
                        : q.getAnswer();
                String correctAnswer_defender = (q_defender.getType() == QuestionType.NUMERIC)
                        ? String.valueOf(q_defender.getNumericAnswer())
                        : q_defender.getAnswer();

                if (attempts == 2) {
                    io.println(
                            "\n  !!! TIE-BREAKER QUESTION (Final attempt) !!!");
                } else {
                    io.println("\n BATTLE QUESTION ");
                }

                displayHotSeatHeader(attacker);
                io.println("  ATTACKER [" + attacker.getName() + "]:");
                io.println(q.formatForConsole());
                if (attacker.hasAttackWeapon()) {
                    io.println("Do you want to use a weapon?");
                    String choiceAtt = io.readNonEmptyString("1) Yes\n2) No");

                    if (choiceAtt.equals("1") ||
                            choiceAtt.equals("yes") ||
                            choiceAtt.equals("Yes")) {
                        attacked = true;
                        List<WeaponType> attackWeapons = attacker.getAttackWeapon();

                        if (!attackWeapons.isEmpty()) {
                            io.println("\nChoose a weapon to use:");
                            for (int i = 0; i < attackWeapons.size(); i++) {
                                io.println(
                                        (i + 1) + ") " + attackWeapons.get(i));
                            }

                            int weaponChoice = io.readInt(
                                    "\nEnter the number of the weapon: ",
                                    1,
                                    attackWeapons.size());
                            WeaponType selectedWeapon = attackWeapons.get(
                                    weaponChoice - 1);
                            weapon_use = selectedWeapon;

                            // Use the weapon
                            Weapon currentWeapon = new Weapon(
                                    selectedWeapon,
                                    io);

                            q_defender = currentWeapon.useWeapon(
                                    q_defender,
                                    selectedWeapon,
                                    questionBank,
                                    weapon_use,
                                    q);
                            correctAnswer_defender = (q_defender.getType() == QuestionType.NUMERIC)
                                    ? String.valueOf(q_defender.getNumericAnswer())
                                    : q_defender.getAnswer();
                            io.println("You used: " + selectedWeapon);
                            if (selectedWeapon != null)
                                attacker.useWeapon(
                                        selectedWeapon);
                            io.println(q.formatForConsole());
                        } else {
                            io.println("You have no attack weapons available.");
                        }
                    }
                }
                String attAns = readAnswerWithTimeout(q);
                boolean attCorrect = !attAns.equals("__TIMEOUT__") &&
                        AnswerValidator.isCorrect(q, attAns);

                if (attCorrect) {
                    io.println("   >> CORRECT!");
                } else {
                    io.println(
                            "   >> WRONG! The correct answer was: " + correctAnswer);
                }

                displayHotSeatHeader(defender);
                io.println("  DEFENDER [" + defender.getName() + "]:");
                if (attacked) {
                    if (defender.hasDefendWeapon()) {
                        io.println(
                                "You were attacked by do you want to use a weapon of defense?");
                        String choiceDef = io.readNonEmptyString(
                                "1) Yes\n2) No");

                        if (choiceDef.equals("1") ||
                                choiceDef.equals("yes") ||
                                choiceDef.equals("Yes")) {
                            List<WeaponType> defenseWeapons = defender.getDefendWeapon();

                            if (!defenseWeapons.isEmpty()) {
                                io.println("\nChoose a defense weapon to use:");
                                for (int i = 0; i < defenseWeapons.size(); i++) {
                                    io.println(
                                            (i + 1) + ") " + defenseWeapons.get(i));
                                }

                                int weaponChoice = io.readInt(
                                        "\nEnter the number of the weapon: ",
                                        1,
                                        defenseWeapons.size());
                                WeaponType selectedWeapon = defenseWeapons.get(
                                        weaponChoice - 1);

                                // Use the weapon
                                Weapon currentWeapon = new Weapon(
                                        selectedWeapon,
                                        io);

                                q_defender = currentWeapon.useWeapon(
                                        q_defender,
                                        selectedWeapon,
                                        questionBank,
                                        weapon_use,
                                        q);
                                correctAnswer_defender = (q_defender.getType() == QuestionType.NUMERIC)
                                        ? String.valueOf(
                                                q_defender.getNumericAnswer())
                                        : q_defender.getAnswer();
                                io.println("You used: " + selectedWeapon);
                                if (selectedWeapon != null)
                                    defender.useWeapon(
                                            selectedWeapon);
                            } else {
                                io.println(
                                        "You have no defense weapons available.");
                            }
                        }
                    } else {
                        io.println("You have no defense weapons.");
                    }
                }
                io.println(q_defender.formatForConsole());
                String defAns = readAnswerWithTimeout(q_defender);
                boolean defCorrect = !defAns.equals("__TIMEOUT__") &&
                        AnswerValidator.isCorrect(q_defender, defAns);

                if (defCorrect) {
                    io.println("   >> CORRECT!");
                } else {
                    io.println(
                            "   >> WRONG! The correct answer was: " +
                                    correctAnswer_defender);
                }

                if (attCorrect && !defCorrect) {
                    io.println(
                            "\n  >> SUCCESS! " +
                                    attacker.getName() +
                                    " conquered the territory!");
                    map.setOwner(defR, defC, attackerSym);
                    sound.play(SoundManager.TERRITORY);
                    battleResolved = true;
                } else if (!attCorrect && defCorrect) {
                    io.println(
                            "\n  >> REPELLED! " +
                                    defender.getName() +
                                    " defended successfully!");
                    battleResolved = true;
                } else {
                    if (attempts < 2) {
                        io.println(
                                "\n  >> TIE! Moving to the final tie-breaker question...");
                    } else {
                        io.println(
                                "\n  >> DOUBLE TIE! Attack failed. The territory remains with " +
                                        defender.getName() +
                                        ".");
                    }
                }
            }
        }
    }

    private void showPlayerStatistics() {
        io.println("\n=== PLAYER STATISTICS ===\n");
        for (Player player : gameState.getPlayers()) {
            io.println("Statistics for " + player.getName() + ":");
            io.println("  Correct Answers   : " + player.getCorrectAnswers());
            io.println("  Wrong Answers     : " + player.getWrongAnswers());
            io.println(
                    "  Average Response  : " +
                            String.format(
                                    "%.2f seconds",
                                    player.getAverageResponseTime()));
            io.println(
                    "  Fastest Response  : " +
                            String.format("%.2f seconds", player.getFastestResponse()));
            io.println("");
        }
    }

    private void openShop(Player player) {
        boolean shopping = true;

        while (shopping) {
            io.println("\n=== SHOP ===");
            io.println("Your current score: " + player.getScore());
            io.println("\n");
            for (WeaponType w : WeaponType.values()) {
                io.println(
                        (w.ordinal() + 1) + ". " + w + " (" + w.getCost() + ")");
            }
            io.println("0. Exit Shop\n");
            io.println("H. to see the help\n");

            String choice = io.readNonEmptyString("Select item (0 to exit):");

            if (choice.equals("0")) {
                shopping = false;
                continue;
            }
            if (choice.equals("H") ||
                    choice.equals("h") ||
                    choice.equals("help") ||
                    choice.equals("Help")) {
                io.println(
                        "// Offensive weapons //\n" +
                                "CANNON(30) -> Increases the difficulty of the opponent's question\n" +
                                "CROSSBOW(30) -> Allows you to choose the opponent's question category\n" +
                                "BURST(65) -> Can choose the category and difficulty max, works 50% of the time\n\n" +
                                "// Defensive weapons //\n" +
                                "SHIELD(60) -> Cancels an incoming attack of CANNON and CROSSBOW, 50% chance to cancel BURST\n"
                                +
                                "LASER_SIGHT(30) -> Allows you to choose your own question category\n" +
                                "HELMET(30) -> Lowers the difficulty of your question\n");
            }

            WeaponType selected = null;
            try {
                int index = Integer.parseInt(choice) - 1;
                if (index >= 0 && index < WeaponType.values().length) {
                    selected = WeaponType.values()[index];
                }
            } catch (NumberFormatException ignored) {
            }

            if (selected != null) {
                int cost = selected.getCost();
                if (player.getScore() >= cost) {
                    player.subtractScore(cost);
                    player.addWeapon(selected);
                    io.println(
                            "Item purchased: " +
                                    selected +
                                    " (remaining score: " +
                                    player.getScore() +
                                    ")");
                    io.println(
                            "Your current inventory: " + player.getInventory());
                } else {
                    io.println("\nNot enough points to buy this weapon!");
                }
            } else {
                io.println("Invalid selection. Please choose a valid item.");
            }

            // Optionally: exit automatically if player has no points for any weapon
            boolean affordable = false;
            for (WeaponType w : WeaponType.values()) {
                if (player.getScore() >= w.getCost()) {
                    affordable = true;
                    break;
                }
            }
            if (!affordable) {
                io.println(
                        "You don't have enough points to buy any more weapons. Exiting shop.");
                shopping = false;
            }
        }

        io.println(
                "Exiting shop. Your current inventory: " + player.getInventory());
    }

    public void changeBotDifficulty(Player player, BotStrategy newStrategy) {
        if (player.isBot()) {
            player.setStrategy(newStrategy);
            io.println("  [SETTINGS] Bot difficulty changed to: " + newStrategy.getDifficultyName());
        }
    }

}
